# Standoff 2 — Internal (0.38.2)

Внутреннее меню (инжектируемая `.so`) под **Standoff 2 v0.38.2**.
Оставлено только самое важное: рабочее меню + плавающая **полоска «MENU»** для открытия.

## Что внутри
| Файл | Назначение |
|------|------------|
| `jni/Includes/Offsets.h` | **Оффсеты 0.38.2** (обновлено) |
| `jni/Includes/Il2Cpp.h`  | База `libil2cpp.so`, attach потока, чтение/запись полей |
| `jni/Main.cpp`           | Хук `eglSwapBuffers` (рендер) + `AInputQueue` (тач) |
| `jni/Menu/Menu.cpp`      | UI: полоска открытия + окно меню |
| `jni/Features.cpp`       | Читы через хуки геттеров (Dobby) |

## Меню
- Перетаскиваемая синяя полоска **MENU** — тап открывает/закрывает меню, удержание перетаскивает.
- Категории: **Оружие** (без отдачи / без разброса / быстрая стрельба / быстрая C4), **Режим (хост)**, **Игрок** (смена ника).

## Сборка
Собирается в CI (GitHub Actions → артефакт `libStandoffMenu-arm64`).
Локально (нужен Android NDK):
```bash
cmake -S Internal -B build \
  -DCMAKE_TOOLCHAIN_FILE=$NDK/build/cmake/android.toolchain.cmake \
  -DANDROID_ABI=arm64-v8a -DANDROID_PLATFORM=android-26
cmake --build build -j
# -> build/libStandoffMenu.so
```

## Заметки
- Read-only хуки геттеров (отдача / C4) работают локально и не требуют поиска синглтона.
- Тач-ввод повешен на `AInputQueue_getEvent` (libandroid). На некоторых сборках Unity
  может потребоваться адаптация источника ввода.
- Хост-тогглы (`GameModeModifiers`) применяются только если найден валидный объект режима.
