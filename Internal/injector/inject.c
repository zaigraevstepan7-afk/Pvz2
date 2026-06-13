#include <stdlib.h>
#include <stdio.h>

// Инжект меню в Standoff 2 через AndKittyInjector.
// Собранную либу положи рядом как libStandoffMenu.so
int main() {
    system("monkey -p com.axlebolt.standoff2 -c android.intent.category.LAUNCHER 1");
    printf("ждём 6 сек, чтобы Standoff успел загрузиться...\n");
    system("sleep 6");

    int result = system(
        "./andkittyinjector -pkg 'com.axlebolt.standoff2' "
        "-lib './libStandoffMenu.so' "
        "-pid \"$(pidof com.axlebolt.standoff2)\" "
        "-dl_memfd -hide_maps -hide_solist");

    if (result == 0) printf("успешный инжект\n");
    else             printf("неудачный инжект\n");
    return 0;
}
