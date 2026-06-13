// =====================================================================
//  Il2Cpp.h — минимальный резолвер базы libil2cpp.so + хелперы
// =====================================================================
#pragma once
#include <cstdint>
#include <cstdio>
#include <cstring>
#include <unistd.h>
#include <dlfcn.h>
#include "Offsets.h"

namespace il2cpp {

    // База libil2cpp.so в памяти процесса
    inline uintptr_t GetLibBase(const char* name = "libil2cpp.so") {
        uintptr_t base = 0;
        FILE* fp = fopen("/proc/self/maps", "r");
        if (!fp) return 0;
        char line[512];
        while (fgets(line, sizeof(line), fp)) {
            if (strstr(line, name)) {
                base = (uintptr_t) strtoul(line, nullptr, 16);
                break;
            }
        }
        fclose(fp);
        return base;
    }

    inline uintptr_t g_base = 0;

    // Дождаться загрузки libil2cpp.so
    inline uintptr_t WaitForLib(const char* name = "libil2cpp.so") {
        while (!(g_base = GetLibBase(name))) usleep(200000);
        return g_base;
    }

    // RVA -> абсолютный адрес
    template <typename T = void*>
    inline T Func(uintptr_t rva) { return reinterpret_cast<T>(g_base + rva); }

    // ---- Привязка нашего потока к домену IL2CPP ----
    inline void* (*domain_get)();
    inline void* (*thread_attach)(void*);
    inline void* (*string_new)(const char*);

    inline void Attach() {
        domain_get    = Func<void* (*)()>(OFS::Api::domain_get);
        thread_attach = Func<void* (*)(void*)>(OFS::Api::thread_attach);
        string_new    = Func<void* (*)(const char*)>(OFS::Api::string_new);
        if (domain_get && thread_attach) thread_attach(domain_get());
    }

    // Чтение/запись полей
    template <typename T>
    inline T Read(uintptr_t obj, int off) {
        if (!obj) return T{};
        return *reinterpret_cast<T*>(obj + off);
    }
    template <typename T>
    inline void Write(uintptr_t obj, int off, T val) {
        if (!obj) return;
        *reinterpret_cast<T*>(obj + off) = val;
    }
}
