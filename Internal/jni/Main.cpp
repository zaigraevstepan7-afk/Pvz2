// =====================================================================
//  Main.cpp — точка входа .so
//  Рендер ImGui через хук eglSwapBuffers + ввод через AInputQueue.
//  Порядок: СНАЧАЛА рендер/ввод (не зависят от оффсетов), ПОТОМ il2cpp.
// =====================================================================
#include <pthread.h>
#include <android/log.h>
#include <android/input.h>
#include <EGL/egl.h>
#include <GLES2/gl2.h>

#include "And64InlineHook.hpp"
#include "imgui.h"
#include "backends/imgui_impl_opengl3.h"
#include "backends/imgui_impl_android.h"

#include "Includes/Il2Cpp.h"
#include "Menu/Menu.h"

#define LOG(...) __android_log_print(ANDROID_LOG_INFO, "Internal", __VA_ARGS__)

namespace Features { void InstallHooks(); void Tick(); }

static bool g_imguiInit = false;

// ---------- Рендер кадра ----------
static void RenderFrame(EGLDisplay dpy, EGLSurface surface) {
    EGLint w = 0, h = 0;
    eglQuerySurface(dpy, surface, EGL_WIDTH,  &w);
    eglQuerySurface(dpy, surface, EGL_HEIGHT, &h);
    if (w <= 0 || h <= 0) return;

    if (!g_imguiInit) {
        ImGui::CreateContext();
        ImGuiIO& io = ImGui::GetIO();
        io.IniFilename = nullptr;
        ImGui::StyleColorsDark();
        ImGui_ImplOpenGL3_Init("#version 100");
        ImGui_ImplAndroid_Init(nullptr);
        g_imguiInit = true;
        LOG(">>> ImGui ИНИЦИАЛИЗИРОВАН %dx%d — меню должно быть видно", w, h);
    }

    ImGuiIO& io = ImGui::GetIO();
    io.DisplaySize = ImVec2((float) w, (float) h);

    ImGui_ImplOpenGL3_NewFrame();
    ImGui_ImplAndroid_NewFrame();
    ImGui::NewFrame();

    Menu::Draw();
    Features::Tick();

    ImGui::Render();
    ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
}

// ---------- Хук рендера ----------
static EGLBoolean (*o_eglSwapBuffers)(EGLDisplay, EGLSurface);
static EGLBoolean h_eglSwapBuffers(EGLDisplay dpy, EGLSurface surface) {
    static bool first = true;
    if (first) { first = false; LOG(">>> ПЕРВЫЙ eglSwapBuffers пойман — рендер GLES активен"); }
    RenderFrame(dpy, surface);
    return o_eglSwapBuffers(dpy, surface);
}

// ---------- Хук ввода (тач) ----------
static int32_t (*o_getEvent)(AInputQueue*, AInputEvent**);
static int32_t h_getEvent(AInputQueue* q, AInputEvent** ev) {
    int32_t r = o_getEvent(q, ev);
    if (r >= 0 && ev && *ev && g_imguiInit)
        ImGui_ImplAndroid_HandleInputEvent(*ev);
    return r;
}

// ---------- Поток инициализации ----------
static void* init_thread(void*) {
    LOG(">>> init_thread старт");
    sleep(8); // даём игре и EGL прогрузиться

    // 1) РЕНДЕР (не зависит от игровых оффсетов!)
    void* sw = dlsym(RTLD_DEFAULT, "eglSwapBuffers");
    if (!sw) {
        void* egl = dlopen("libEGL.so", RTLD_NOW);
        sw = egl ? dlsym(egl, "eglSwapBuffers") : nullptr;
    }
    LOG(">>> eglSwapBuffers = %p", sw);
    if (sw) {
        A64HookFunction(sw, (void*) h_eglSwapBuffers, (void**) &o_eglSwapBuffers);
        LOG(">>> хук рендера установлен");
    } else {
        LOG("!!! eglSwapBuffers НЕ найден — игра на Vulkan?");
    }

    // 2) ВВОД
    void* ge = dlsym(RTLD_DEFAULT, "AInputQueue_getEvent");
    if (!ge) {
        void* la = dlopen("libandroid.so", RTLD_NOW);
        ge = la ? dlsym(la, "AInputQueue_getEvent") : nullptr;
    }
    if (ge) A64HookFunction(ge, (void*) h_getEvent, (void**) &o_getEvent);
    LOG(">>> AInputQueue_getEvent = %p", ge);

    // 3) IL2CPP — ОТКЛЮЧЕНО (v3): inline-хук коротких геттеров затирал
    //    соседние функции → краш при заходе в бой. Сначала стабильное меню.
    il2cpp::g_base = il2cpp::GetLibBase();
    LOG(">>> libil2cpp base = %p", (void*) il2cpp::g_base);
    // il2cpp::Attach();          // ВЫКЛ в v3
    // Features::InstallHooks();  // ВЫКЛ в v3 — вернём через безопасную запись в память

    LOG(">>> Готово (v3, чистое меню). Ищи '>>> ПЕРВЫЙ eglSwapBuffers'.");
    return nullptr;
}

__attribute__((constructor))
static void entry() {
    LOG(">>> .so загружена (constructor)");
    pthread_t t;
    pthread_create(&t, nullptr, init_thread, nullptr);
}
