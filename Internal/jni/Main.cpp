// =====================================================================
//  Main.cpp — точка входа .so
//  Рендер ImGui через хук eglSwapBuffers + ввод через AInputQueue.
// =====================================================================
#include <pthread.h>
#include <android/log.h>
#include <android/input.h>
#include <EGL/egl.h>
#include <GLES2/gl2.h>

#include "dobby.h"
#include "imgui.h"
#include "backends/imgui_impl_opengl3.h"
#include "backends/imgui_impl_android.h"

#include "Includes/Il2Cpp.h"
#include "Menu/Menu.h"

#define LOG(...) __android_log_print(ANDROID_LOG_INFO, "Internal", __VA_ARGS__)

namespace Features { void InstallHooks(); void Tick(); }

static bool g_imguiInit = false;

// ---------- Хук рендера ----------
static EGLBoolean (*o_eglSwapBuffers)(EGLDisplay, EGLSurface);
static EGLBoolean h_eglSwapBuffers(EGLDisplay dpy, EGLSurface surface) {
    EGLint w = 0, h = 0;
    eglQuerySurface(dpy, surface, EGL_WIDTH,  &w);
    eglQuerySurface(dpy, surface, EGL_HEIGHT, &h);

    if (!g_imguiInit) {
        ImGui::CreateContext();
        ImGuiIO& io = ImGui::GetIO();
        io.IniFilename = nullptr;
        ImGui::StyleColorsDark();
        ImGui_ImplOpenGL3_Init("#version 100");
        ImGui_ImplAndroid_Init(nullptr);
        g_imguiInit = true;
        LOG("ImGui инициализирован %dx%d", w, h);
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
    il2cpp::WaitForLib();          // ждём libil2cpp.so
    sleep(6);                      // даём игре прогрузиться
    il2cpp::Attach();              // привязываем поток к домену
    Features::InstallHooks();      // хуки читов

    // Рендер
    void* egl = dlopen("libEGL.so", RTLD_LAZY);
    void* sw  = egl ? dlsym(egl, "eglSwapBuffers") : nullptr;
    if (sw) DobbyHook(sw, (void*) h_eglSwapBuffers, (void**) &o_eglSwapBuffers);

    // Ввод
    void* la  = dlopen("libandroid.so", RTLD_LAZY);
    void* ge  = la ? dlsym(la, "AInputQueue_getEvent") : nullptr;
    if (ge) DobbyHook(ge, (void*) h_getEvent, (void**) &o_getEvent);

    LOG("Готово. Меню: тапни синюю полоску MENU.");
    return nullptr;
}

__attribute__((constructor))
static void entry() {
    pthread_t t;
    pthread_create(&t, nullptr, init_thread, nullptr);
}
