// =====================================================================
//  Menu.h — состояние читов + отрисовка меню (ImGui)
// =====================================================================
#pragma once

namespace Menu {
    // Видимость меню (переключается полоской «открыть»)
    inline bool Open = false;

    // ---- Самое важное (тогглы) ----
    struct State {
        // Оружие (через хук геттеров — работают локально)
        bool NoRecoil   = false;   // убрать отдачу/разброс
        bool RapidFire  = false;   // быстрый темп стрельбы
        bool NoSpread   = false;   // нулевой разброс
        bool FastBomb   = false;   // мгновенный таймер C4

        // Режим (хост): пишем в GameModeModifiers
        bool HighJump   = false;
        bool InfAmmo    = false;
        bool InfMags    = false;
        float SpeedMult = 1.0f;

        // Ник
        char Nick[32]   = "";
        bool SpoofNick  = false;
    };
    inline State S;

    void Draw();          // рисует меню + полоску открытия
}
