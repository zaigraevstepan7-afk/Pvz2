// =====================================================================
//  Menu.cpp — UI: плавающая полоска «открыть» + окно меню
// =====================================================================
#include "Menu.h"
#include "imgui.h"

namespace Menu {

    // Перетаскиваемая «полоска» для открытия меню
    static void DrawOpenBar() {
        ImGuiIO& io = ImGui::GetIO();
        static ImVec2 pos = ImVec2(20, io.DisplaySize.y * 0.4f);

        ImGui::SetNextWindowPos(pos, ImGuiCond_Always);
        ImGui::SetNextWindowSize(ImVec2(56, 56), ImGuiCond_Always);
        ImGui::PushStyleVar(ImGuiStyleVar_WindowRounding, 14.0f);
        ImGui::PushStyleColor(ImGuiCol_WindowBg, ImVec4(0.10f, 0.55f, 0.95f, 0.92f));
        ImGui::Begin("##openbar", nullptr,
            ImGuiWindowFlags_NoTitleBar | ImGuiWindowFlags_NoResize |
            ImGuiWindowFlags_NoScrollbar | ImGuiWindowFlags_NoCollapse);

        // Кнопка-полоска: тап = открыть/закрыть, удержание = перетаскивание
        ImVec2 p = ImGui::GetWindowPos();
        if (ImGui::InvisibleButton("##bar", ImVec2(56, 56))) {
            Open = !Open;
        }
        if (ImGui::IsItemActive() && ImGui::IsMouseDragging(0)) {
            pos.x += io.MouseDelta.x;
            pos.y += io.MouseDelta.y;
        }
        ImGui::SetCursorScreenPos(ImVec2(p.x + 14, p.y + 18));
        ImGui::Text("MENU");
        ImGui::End();
        ImGui::PopStyleColor();
        ImGui::PopStyleVar();
    }

    static void DrawWindow() {
        if (!Open) return;
        ImGui::SetNextWindowSize(ImVec2(320, 0), ImGuiCond_FirstUseEver);
        ImGui::SetNextWindowPos(ImVec2(90, 60), ImGuiCond_FirstUseEver);
        ImGui::Begin("Standoff 2  |  0.38.2", &Open, ImGuiWindowFlags_NoCollapse);

        if (ImGui::CollapsingHeader("Оружие", ImGuiTreeNodeFlags_DefaultOpen)) {
            ImGui::Checkbox("Без отдачи",   &S.NoRecoil);
            ImGui::Checkbox("Без разброса", &S.NoSpread);
            ImGui::Checkbox("Быстрая стрельба", &S.RapidFire);
            ImGui::Checkbox("Быстрая бомба C4", &S.FastBomb);
        }
        if (ImGui::CollapsingHeader("Режим (хост)", ImGuiTreeNodeFlags_DefaultOpen)) {
            ImGui::Checkbox("Высокий прыжок", &S.HighJump);
            ImGui::Checkbox("Беск. патроны",  &S.InfAmmo);
            ImGui::Checkbox("Беск. магазины",  &S.InfMags);
            ImGui::SliderFloat("Скорость бега", &S.SpeedMult, 1.0f, 3.0f, "%.1fx");
        }
        if (ImGui::CollapsingHeader("Игрок")) {
            ImGui::InputText("Ник", S.Nick, sizeof(S.Nick));
            ImGui::Checkbox("Сменить ник", &S.SpoofNick);
        }

        ImGui::Separator();
        ImGui::TextDisabled("by internal • build 0.38.2");
        ImGui::End();
    }

    void Draw() {
        DrawOpenBar();
        DrawWindow();
    }
}
