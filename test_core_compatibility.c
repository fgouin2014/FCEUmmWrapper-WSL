#include <stdio.h>
#include <dlfcn.h>
#include <signal.h>

void signal_handler(int sig) {
    printf("SIGSEGV capturé - Core incompatible\n");
    exit(1);
}

int main() {
    signal(SIGSEGV, signal_handler);
    
    void *handle = dlopen("./test_core.so", RTLD_LAZY);
    if (!handle) {
        printf("❌ Impossible de charger le core: %s\n", dlerror());
        return 1;
    }
    
    printf("✅ Core chargé avec succès\n");
    
    // Test des fonctions critiques
    void (*retro_init)(void) = dlsym(handle, "retro_init");
    if (!retro_init) {
        printf("❌ retro_init non trouvé\n");
        return 1;
    }
    
    printf("✅ retro_init trouvé\n");
    
    // Test d'initialisation
    retro_init();
    printf("✅ retro_init exécuté sans crash\n");
    
    dlclose(handle);
    printf("✅ Test terminé avec succès\n");
    return 0;
}
