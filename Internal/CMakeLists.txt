cmake_minimum_required(VERSION 3.18)
project(StandoffInternal CXX C)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
add_compile_options(-O2 -fvisibility=hidden -Wno-deprecated-declarations)

include(FetchContent)

# ---- Dear ImGui ----
FetchContent_Declare(imgui
    GIT_REPOSITORY https://github.com/ocornut/imgui.git
    GIT_TAG        v1.90.9)
FetchContent_MakeAvailable(imgui)

# ---- And64InlineHook (arm64 inline hook, чистый C++) ----
FetchContent_Declare(a64hook
    GIT_REPOSITORY https://github.com/Rprop/And64InlineHook.git
    GIT_TAG        master)
FetchContent_MakeAvailable(a64hook)

set(IMGUI_SRC
    ${imgui_SOURCE_DIR}/imgui.cpp
    ${imgui_SOURCE_DIR}/imgui_draw.cpp
    ${imgui_SOURCE_DIR}/imgui_tables.cpp
    ${imgui_SOURCE_DIR}/imgui_widgets.cpp
    ${imgui_SOURCE_DIR}/backends/imgui_impl_opengl3.cpp
    ${imgui_SOURCE_DIR}/backends/imgui_impl_android.cpp)

add_library(StandoffMenu SHARED
    jni/Main.cpp
    jni/Features.cpp
    jni/Menu/Menu.cpp
    ${a64hook_SOURCE_DIR}/And64InlineHook.cpp
    ${IMGUI_SRC})

target_include_directories(StandoffMenu PRIVATE
    jni
    ${imgui_SOURCE_DIR}
    ${a64hook_SOURCE_DIR})

target_compile_definitions(StandoffMenu PRIVATE IMGUI_IMPL_OPENGL_ES2)

target_link_libraries(StandoffMenu
    EGL GLESv2 android log dl)
