// =====================================================================
//  Features.cpp — применение читов через хуки геттеров (Dobby).
//  Хуки возвращают наше значение и НЕ требуют поиска синглтона —
//  'this' приходит первым аргументом, поэтому работают сразу и безопасно.
// =====================================================================
#include "Includes/Il2Cpp.h"
#include "Menu/Menu.h"
#include "dobby.h"

using namespace OFS;

// ---- get_DetonationDuration -> мгновенная бомба ----
static float (*o_DetonationDuration)(void*);
static float h_DetonationDuration(void* self) {
    float v = o_DetonationDuration(self);
    return Menu::S.FastBomb ? 0.2f : v;
}

// ---- get_RecoilAccelStep -> 0 при NoRecoil ----
static float (*o_RecoilAccelStep)(void*);
static float h_RecoilAccelStep(void* self) {
    float v = o_RecoilAccelStep(self);
    return Menu::S.NoRecoil ? 0.0f : v;
}

// ---- get_RecoilAccelDuration -> 0 при NoRecoil ----
static float (*o_RecoilAccelDuration)(void*);
static float h_RecoilAccelDuration(void* self) {
    float v = o_RecoilAccelDuration(self);
    return Menu::S.NoRecoil ? 0.0f : v;
}

namespace Features {

    // Установить ник (статический сеттер + отправка на сервер)
    void ApplyNick() {
        if (!Menu::S.SpoofNick || !Menu::S.Nick[0]) return;
        auto set_name = il2cpp::Func<void(*)(void*)>(Rva::set_playerName_static);
        void* str = il2cpp::string_new ? il2cpp::string_new(Menu::S.Nick) : nullptr;
        if (str && set_name) set_name(str);
        Menu::S.SpoofNick = false; // применяем один раз
    }

    void InstallHooks() {
        DobbyHook(il2cpp::Func(Rva::get_DetonationDuration),
                  (void*) h_DetonationDuration, (void**) &o_DetonationDuration);
        DobbyHook(il2cpp::Func(Rva::get_RecoilAccelStep),
                  (void*) h_RecoilAccelStep, (void**) &o_RecoilAccelStep);
        DobbyHook(il2cpp::Func(Rva::get_RecoilAccelDuration),
                  (void*) h_RecoilAccelDuration, (void**) &o_RecoilAccelDuration);
    }

    // Вызывается каждый кадр из меню-цикла
    void Tick() {
        ApplyNick();
    }
}
