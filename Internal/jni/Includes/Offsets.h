// =====================================================================
//  Offsets.h  —  Standoff 2  v0.38.2  (IL2CPP)
//  Только самое важное для рабочего меню. Обновлено под 0.38.2.
//
//  RVA  = смещение функции внутри libil2cpp.so (база читается из maps)
//  OFS  = смещение поля внутри объекта (this + OFS)
// =====================================================================
#pragma once
#include <cstdint>

namespace OFS {

    // ---- IL2CPP API (libil2cpp.so) — версия 0.38.2 ----
    namespace Api {
        constexpr uintptr_t thread_current        = 0x87AC288;
        constexpr uintptr_t domain_get            = 0x87AC1B4;
        constexpr uintptr_t thread_attach         = 0x87AF4E8;
        constexpr uintptr_t thread_detach         = 0x87AF564;
        constexpr uintptr_t string_new            = 0x500CC28;
        constexpr uintptr_t resolve_icall         = 0x50157A8;
        constexpr uintptr_t assembly_get_image    = 0x500EB20;
        constexpr uintptr_t class_from_name       = 0x4FF4088;
        constexpr uintptr_t domain_assembly_open  = 0x4FF4AA8;
        constexpr uintptr_t object_new            = 0x4FF434C;
        constexpr uintptr_t class_get_fields      = 0x877D240;
        constexpr uintptr_t class_get_field_name  = 0x877D308;
        constexpr uintptr_t class_get_interfaces  = 0x877D3B8;
        constexpr uintptr_t class_get_methods     = 0x877D480;
        constexpr uintptr_t class_from_type       = 0x877CBCC;
    }

    // ---- Методы (RVA) — самое важное ----
    namespace Rva {
        // Деньги / смерть
        constexpr uintptr_t OnLocalPlayerDead     = 0x74D8540;
        // Bomb (C4)
        constexpr uintptr_t get_DetonationDuration = 0x5576474;
        constexpr uintptr_t set_DetonationDuration = 0x5571148;
        // Настройки оружия
        constexpr uintptr_t get_WeaponSettings    = 0x5576EE8;
        constexpr uintptr_t get_RecoilAccelDuration = 0x76ADC40;
        constexpr uintptr_t get_RecoilAccelStep   = 0x76ADD20;
        // Ник игрока
        constexpr uintptr_t get_playerName_static = 0x759B5D8;
        constexpr uintptr_t set_playerName_static = 0x7589138;
        constexpr uintptr_t get_PlayerName        = 0x75C2698;
        constexpr uintptr_t set_PlayerName        = 0x75BE2D8;
        constexpr uintptr_t SendPlayerName        = 0x75B4C3C;
        // Урон/смерть
        constexpr uintptr_t OnPlayerWasKilled     = 0x7EFAE4C;
    }

    // ---- PlayerManager ----
    namespace PlayerManager {
        constexpr int PlayersOnScene = 0x24;
        constexpr int PlayersList    = 0x28;   // Dictionary<int,PlayerController>
        constexpr int LocalPlayer    = 0x68;   // твой персонаж
        constexpr int SelectedPlayer = 0x70;
    }

    // ---- PlayerController ----
    namespace Player {
        constexpr int AimController       = 0x80;
        constexpr int WeaponryController  = 0x88;
        constexpr int MovementController  = 0x98;
        constexpr int HitController       = 0xA8;
        constexpr int MaterialController  = 0xB0;
        constexpr int Side               = 0x79;   // команда
        constexpr int PlayerName         = 0x160;  // string
        constexpr int PhotonView         = 0x150;
    }

    // ---- AimController (чувствительность / отдача-разброс) ----
    namespace Aim {
        constexpr int sensitivityX = 0x58;
        constexpr int sensitivityY = 0x5C;
        constexpr int minimumX     = 0x60;
        constexpr int maximumX     = 0x64;
        constexpr int isAiming     = 0x1E0;
    }

    // ---- MovementController ----
    namespace Move {
        constexpr int neverIdle = 0x68;
    }

    // ---- WeaponryController ----
    namespace Weaponry {
        constexpr int CurrentWeapon = 0xA0;   // активное оружие
        constexpr int CurrentSlot   = 0x88;   // byte
    }

    // ---- WeaponSettings (характеристики оружия) ----
    namespace Weapon {
        constexpr int AccuracyAngle      = 0x10;
        constexpr int RecoilAngle        = 0x14;
        constexpr int doubledAmmo        = 0x138;
        constexpr int damage             = 0x140;
        constexpr int hitRange           = 0x148;
        constexpr int fireRate           = 0x14C;
        constexpr int recoilControl      = 0x150;
        constexpr int recoilParameters   = 0x158;
        constexpr int accuracyMultJump   = 0x168;
        constexpr int recoilMultCrouch   = 0x178;
        constexpr int accuracyMultCrouch = 0x17C;
        constexpr int recoilAimMult      = 0x180;
        constexpr int armorPenetration   = 0x1A0;
        constexpr int penetrationPower   = 0x1A4;
        constexpr int reloadDuration     = 0x1A8;
        constexpr int impulse            = 0x1CC;
        constexpr int fireRateSafe       = 0x1D4;
    }

    // ---- GameModeModifiers (настройки режима — только хост) ----
    namespace Mode {
        constexpr int speedMultiplier   = 0x14;
        constexpr int highJump          = 0x18;  // bool
        constexpr int infinityAmmo      = 0x1B;  // bool
        constexpr int infinityMagazines = 0x1C;  // bool
        constexpr int infinityGrenades  = 0x1D;  // bool
        constexpr int friendlyFire      = 0x20;  // bool
        constexpr int headshotOnly      = 0x21;  // bool
        constexpr int health            = 0x30;  // int
        constexpr int armor             = 0x34;  // int
        constexpr int startMoney        = 0x28;
        constexpr int maxMoney          = 0x2C;
    }
}
