// Wrapper de compatibilité pour les cores officiels
#include <dlfcn.h>
#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "CompatWrapper", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "CompatWrapper", __VA_ARGS__)

// Fonction de compatibilité pour retro_load_game
bool compatible_retro_load_game(void* core_handle, const struct retro_game_info* game_info) {
    // Charger la fonction depuis le core
    bool (*original_retro_load_game)(const struct retro_game_info*) = 
        (bool(*)(const struct retro_game_info*))dlsym(core_handle, "retro_load_game");
    
    if (!original_retro_load_game) {
        LOGE("retro_load_game non trouvé dans le core");
        return false;
    }
    
    // Tentative avec protection
    try {
        return original_retro_load_game(game_info);
    } catch (...) {
        LOGE("Exception dans retro_load_game - utilisation du mode fallback");
        return false;
    }
}

// Fonction de compatibilité pour retro_run
void compatible_retro_run(void* core_handle) {
    void (*original_retro_run)(void) = 
        (void(*)(void))dlsym(core_handle, "retro_run");
    
    if (!original_retro_run) {
        LOGE("retro_run non trouvé dans le core");
        return;
    }
    
    // Tentative avec protection
    try {
        original_retro_run();
    } catch (...) {
        LOGE("Exception dans retro_run - mode fallback");
        // Mode fallback : afficher un écran noir
    }
}
