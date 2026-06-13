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

# ---- Dobby (хуки) ----
set(DOBBY_DEBUG OFF CACHE BOOL "" FORCE)
set(DOBBY_GENERATE_SHARED OFF CACHE BOOL "" FORCE)
FetchContent_Declare(dobby
    GIT_REPOSITORY https://github.com/jmpews/Dobby.git
    GIT_TAG        master)
FetchContent_MakeAvailable(dobby)

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
    ${IMGUI_SRC})

target_include_directories(StandoffMenu PRIVATE
    jni
    ${imgui_SOURCE_DIR}
    ${dobby_SOURCE_DIR}
    ${dobby_SOURCE_DIR}/include)

target_compile_definitions(StandoffMenu PRIVATE IMGUI_IMPL_OPENGL_ES2)

target_link_libraries(StandoffMenu
    dobby
    EGL GLESv2 android log dl)
