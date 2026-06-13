// =====================================================================
//  Offsets.h  —  Standoff 2  v0.38.2  (IL2CPP)  —  ПОЛНЫЙ набор
//  Обновлено под 0.38.2. Собрано из outptCode + offsets0382 + api.h
//
//  RVA  = смещение функции внутри libil2cpp.so (база читается из maps)
//  OFS  = смещение поля внутри объекта (this + OFS)
// =====================================================================
#pragma once
#include <cstdint>

namespace OFS {

    // ================= IL2CPP API (libil2cpp.so) — 0.38.2 =================
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

    // ============================ Методы (RVA) ============================
    namespace Rva {
        // Экономика / смерть
        constexpr uintptr_t OnLocalPlayerDead       = 0x74D8540;
        constexpr uintptr_t OnPlayerWasKilled       = 0x7EFAE4C; // корутина смерти
        // Bomb (C4)
        constexpr uintptr_t get_DetonationDuration  = 0x5576474;
        constexpr uintptr_t set_DetonationDuration  = 0x5571148;
        // Настройки оружия
        constexpr uintptr_t get_WeaponSettings      = 0x5576EE8;
        constexpr uintptr_t get_RecoilAccelDuration = 0x76ADC40;
        constexpr uintptr_t get_RecoilAccelStep     = 0x76ADD20;
        // Кости / скелет
        constexpr uintptr_t GetBonesOfType          = 0x7B4C5CC;
        constexpr uintptr_t GetFirstBoneOfTypeAndSide = 0x7B59A1C;
        constexpr uintptr_t GetBone                 = 0x7B4BD08;
        // Прочее
        constexpr uintptr_t ToVector3               = 0x7B54E88;
        constexpr uintptr_t ClampAngle              = 0x7B486F8;
        // Ник игрока
        constexpr uintptr_t get_playerName_static   = 0x759B5D8;
        constexpr uintptr_t set_playerName_static   = 0x7589138;
        constexpr uintptr_t get_PlayerName          = 0x75C2698;
        constexpr uintptr_t set_PlayerName          = 0x75BE2D8;
        constexpr uintptr_t SendPlayerName          = 0x75B4C3C;
    }

    // ============================ Экономика / HUD =========================
    namespace Economy {
        constexpr int ctTeamMoneyText = 0x28;
        constexpr int trTeamMoneyText = 0x30;
        constexpr int LocalPlayerDeadEvent = 0x78;
    }

    namespace Hud {
        constexpr int FireZone    = 0x10;
        constexpr int Joystick    = 0x18;
        constexpr int AmmoView    = 0x20;
        constexpr int NewFpsView  = 0x38;
        constexpr int ChatButton  = 0x1A8;
        constexpr int GameChatHud = 0x1B0;
    }

    // ============================ Камера / чувствительность ===============
    namespace Camera {
        constexpr int rotationSensitivityFPS = 0x20;
        constexpr int rotationSensitivityPad = 0x50;
        constexpr int yMinLimit  = 0x54;
        constexpr int yMaxLimit  = 0x58;
        constexpr int current_x  = 0x2C;
        constexpr int current_y  = 0x30;
        constexpr int followSpeed = 0x38;
        constexpr int distance   = 0x3C;
    }

    // ============================ Кости / скелет ==========================
    namespace Bones {
        constexpr int typeSpine = 0x10;
        constexpr int typeHead  = 0x18;
        constexpr int typeArm   = 0x20;
        constexpr int typeLeg   = 0x28;
        constexpr int pelvis    = 0x78;
        constexpr int hand      = 0x80;
        constexpr int foot      = 0x88;
    }
    namespace Misc {
        constexpr int SpawnPoint = 0x10; // SpawnPoint.point
    }

    // ============================ PlayerManager ==========================
    namespace PlayerManager {
        constexpr int PlayersOnScene = 0x24;
        constexpr int PlayersList    = 0x28;  // Dictionary<int,PlayerController>
        constexpr int PlayersIdMap   = 0x30;
        constexpr int ActivePlayers  = 0x38;  // HashSet
        constexpr int Materials      = 0x40;  // Dictionary<string,Material>
        constexpr int Initialized    = 0x60;
        constexpr int LocalPlayer    = 0x68;  // твой персонаж
        constexpr int SelectedPlayer = 0x70;
        constexpr int SpawnPools     = 0x78;
    }

    // ============================ PlayerController =======================
    namespace Player {
        constexpr int MaxHealthCT     = 0x08; // static
        constexpr int MaxArmorCT      = 0x0C; // static
        constexpr int MaxHealthTR     = 0x10; // static
        constexpr int MaxArmorTR      = 0x14; // static
        constexpr int CameraHolder    = 0x28;
        constexpr int FpsCameraHolder = 0x30;
        constexpr int FpsDirective    = 0x38;
        constexpr int CharacterView1  = 0x48;
        constexpr int CharacterView2  = 0x50;
        constexpr int StateFlag       = 0x78; // bool
        constexpr int Side            = 0x79; // команда
        constexpr int AimController       = 0x80;
        constexpr int WeaponryController  = 0x88;
        constexpr int MecanimController   = 0x90;
        constexpr int MovementController  = 0x98;
        constexpr int ArmsAnimController  = 0xA0;
        constexpr int HitController       = 0xA8;
        constexpr int MaterialController  = 0xB0;
        constexpr int OcclusionController = 0xB8;
        constexpr int NetworkController   = 0xC0;
        constexpr int SoundController     = 0xE0;
        constexpr int MainCamera          = 0xE8;
        constexpr int FpsCamera           = 0xF0;
        constexpr int MarkerTrigger       = 0xF8;
        constexpr int Controllers         = 0x108; // Controller[]
        constexpr int CharacterController = 0x118; // Unity collider
        constexpr int SkinnedMeshLod      = 0x120;
        constexpr int CharacterLod        = 0x128;
        constexpr int PhotonView          = 0x150;
        constexpr int PhotonViewId1       = 0x158;
        constexpr int PhotonViewId2       = 0x15C;
        constexpr int PlayerName          = 0x160; // string
        constexpr int PhotonPlayer        = 0x160; // (см. PhotonView ниже)
    }

    // ============================ AimController ==========================
    namespace Aim {
        constexpr int spineOverride = 0x53; // bool
        constexpr int sensitivityX  = 0x58;
        constexpr int sensitivityY  = 0x5C;
        constexpr int minimumX      = 0x60;
        constexpr int maximumX      = 0x64;
        constexpr int headDamping   = 0xA8;
        constexpr int isAiming      = 0x1E0; // ADS bool
        constexpr int toolPivotTuning = 0x220;
        constexpr int spineRotation   = 0x221;
    }

    // ============================ MovementController =====================
    namespace Move {
        constexpr int neverIdle = 0x68; // никогда не стоять
        constexpr int float1    = 0x80;
        constexpr int float2    = 0x84;
    }

    // ============================ WeaponryController =====================
    namespace Weaponry {
        constexpr int WeaponsDict   = 0x58; // Dictionary<slot,WeaponController>
        constexpr int WeaponList    = 0x60;
        constexpr int SlotList      = 0x68;
        constexpr int CurrentSlot   = 0x88; // byte
        constexpr int PickupCtrl    = 0x90;
        constexpr int KitController = 0x98; // аптечки/гранаты
        constexpr int CurrentWeapon = 0xA0; // активное оружие
    }

    // ============================ WeaponSettings =========================
    namespace Weapon {
        constexpr int ctWeaponSettings   = 0x10;
        constexpr int trWeaponSettings   = 0x18;
        constexpr int AccuracyAngle      = 0x10;
        constexpr int RecoilAngle        = 0x14;
        constexpr int sightType          = 0x110;
        constexpr int scopeAimSensMult   = 0x12C;
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
        constexpr int gunSightViewParams = 0x198;
        constexpr int armorPenetration   = 0x1A0;
        constexpr int penetrationPower   = 0x1A4;
        constexpr int reloadDuration     = 0x1A8;
        constexpr int tacticalReload     = 0x1AC;
        constexpr int takeDuration       = 0x1C4;
        constexpr int impulse            = 0x1CC;
        constexpr int fireRateSafe       = 0x1D4;
        // Safe-значения
        constexpr int recoilAimMultSafe  = 0x210;
        constexpr int accuracyAimMultSafe = 0x21C;
        constexpr int recoilControlSafe  = 0x240;
        constexpr int weaponSettingsRef  = 0x330; // _weaponSettings
    }

    // ============================ GameModeModifiers ======================
    namespace Mode {
        constexpr int speedMultiplier   = 0x14;
        constexpr int highJump          = 0x18; // bool
        constexpr int weaponBuyTimeInf  = 0x19; // bool
        constexpr int buyEverywhere     = 0x1A; // bool
        constexpr int infinityAmmoAbs   = 0x1B; // bool
        constexpr int infinityMagazines = 0x1C; // bool
        constexpr int infinityGrenades  = 0x1D; // bool
        constexpr int friendlyFire      = 0x20; // bool
        constexpr int headshotOnly      = 0x21; // bool
        constexpr int startMoney        = 0x28;
        constexpr int maxMoney          = 0x2C;
        constexpr int health            = 0x30; // int
        constexpr int armor             = 0x34; // int
        // Тайминги FFA / Arcade
        constexpr int startingDuration  = 0x2E0;
        constexpr int deathMatchDuration = 0x2E4;
        constexpr int weaponBuyDuration = 0x300;
        constexpr int untouchableDuration = 0x304;
    }

    // ============================ Damage / Hit ===========================
    namespace Hit {
        constexpr int takenDamage = 0x30;
        constexpr int givenDamage = 0x38;
        constexpr int dmg1 = 0xA8, dmg2 = 0xAC, dmg3 = 0xB0; // PlayerHitController
    }

    // ============================ PlayerMainCamera =======================
    namespace MainCam {
        constexpr int Camera       = 0x20; // Unity Camera (static)
        constexpr int ScopeZoomer  = 0x28;
        constexpr int AnimController = 0x30;
        constexpr int Transform    = 0x38;
        constexpr int MainCameraGO = 0x40;
    }

    // ============================ PlayerMaterialController ================
    namespace Material {
        constexpr int BaseMaterial      = 0x58;
        constexpr int HighlightMaterial = 0x60; // подсветка/видимость (chams/visible)
        constexpr int PlayerCtrl        = 0x68;
        constexpr int NetworkCtrl       = 0x70;
        constexpr int ExtraMaterial     = 0x78;
    }

    // ============================ NetworkController ======================
    namespace Network {
        constexpr int PlayerCtrl = 0x90;
        constexpr int PhotonView = 0x98;
        constexpr int snapshotTime = 0xB0; // double
    }
}
