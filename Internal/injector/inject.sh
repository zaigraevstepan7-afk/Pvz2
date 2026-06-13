#!/system/bin/sh
# Инжект меню в Standoff 2 через AndKittyInjector (нужен root).
# Положи libStandoffMenu.so рядом с этим скриптом и запусти от root:
#   su -c sh inject.sh
PKG="com.axlebolt.standoff2"
LIB="./libStandoffMenu.so"

# Запускаем игру
monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
echo "Ждём 6 сек, пока Standoff загрузится..."
sleep 6

chmod +x ./andkittyinjector
./andkittyinjector -pkg "$PKG" -lib "$LIB" \
    -pid "$(pidof $PKG)" -dl_memfd -hide_maps -hide_solist

if [ $? -eq 0 ]; then echo "✅ успешный инжект"; else echo "❌ неудачный инжект"; fi
