#!/bin/bash

# Correction du problème de chargement des ROMs
set -e

echo "🔧 Correction du problème de chargement des ROMs..."
echo "=================================================="

# Créer un backup
cp app/src/main/cpp/libretro_wrapper.cpp app/src/main/cpp/libretro_wrapper.cpp.backup2

echo "✅ Backup créé"

# Créer une version améliorée du wrapper avec protection SIGSEGV plus robuste
cat > app/src/main/cpp/libretro_wrapper_improved.cpp << 'EOF'
#include "libretro_wrapper.h"
#include <dlfcn.h>
#include <android/log.h>
#include <unistd.h>
#include <chrono>
#include <signal.h>
#include <setjmp.h>
#include <sys/mman.h>

#define LOG_TAG "LibretroWrapper"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

// --- Variables globales ---
pthread_t coreThread;
pthread_t audioThread;
pthread_t renderThread;
std::atomic<bool> running(false);

JavaVM* g_vm = nullptr;
jobject g_surfaceView = nullptr;

// --- Gestion des signaux améliorée ---
static sigjmp_buf sigsegv_env;
static bool sigsegv_occurred = false;
static bool core_initialized = false;
static bool rom_loaded = false;

static void sigsegv_handler(int sig) {
    LOGE("Signal SIGSEGV capturé - sortie propre");
    sigsegv_occurred = true;
    siglongjmp(sigsegv_env, 1);
}

void setup_signal_handlers() {
    struct sigaction sa;
    sa.sa_handler = sigsegv_handler;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_RESTART;
    sigaction(SIGSEGV, &sa, nullptr);
    LOGI("Gestionnaire SIGSEGV installé");
}

// --- Libretro API dynamiques ---
static void* core_handle = nullptr;
static void (*retro_init)() = nullptr;
static void (*retro_deinit)() = nullptr;
static void (*retro_run)() = nullptr;
static bool (*retro_load_game)(const struct retro_game_info*) = nullptr;
static void (*retro_unload_game)() = nullptr;
static void (*retro_get_system_av_info)(struct retro_system_av_info*) = nullptr;
static void (*retro_set_environment)(bool (*)(unsigned, void*)) = nullptr;
static void (*retro_set_video_refresh)(void (*)(const void*, unsigned, unsigned, size_t)) = nullptr;
static void (*retro_set_audio_sample_batch)(size_t (*)(const int16_t*, size_t)) = nullptr;
static void (*retro_set_input_poll)(void (*)()) = nullptr;
static int16_t (*retro_set_input_state)(int16_t (*)(unsigned, unsigned, unsigned, unsigned)) = nullptr;

// --- Buffers et états ---
VideoBuffer videoBuffer;
AudioRingBuffer audioBuffer;
InputState inputState;

// --- Callbacks LibRetro ---
bool environment_callback(unsigned cmd, void* data) {
    LOGI("Environment callback: cmd=%u (0x%x) - DEBUT", cmd, cmd);
    
    switch (cmd) {
        case RETRO_ENVIRONMENT_SET_CORE_OPTIONS_INTL:
            LOGI("SET_CORE_OPTIONS_INTL - accepté");
            return true;
            
        case RETRO_ENVIRONMENT_SET_BLOCK_AUDIO_MIX:
            LOGI("SET_BLOCK_AUDIO_MIX - accepté");
            return true;
            
        case RETRO_ENVIRONMENT_SET_PIXEL_FORMAT:
            LOGI("SET_PIXEL_FORMAT - accepté");
            return true;
            
        case RETRO_ENVIRONMENT_GET_VFS_INTERFACE:
            LOGI("GET_VFS_INTERFACE - retourne NULL");
            return false;
            
        case RETRO_ENVIRONMENT_GET_SENSOR_INTERFACE:
            LOGI("GET_SENSOR_INTERFACE - retourne NULL");
            return false;
            
        case RETRO_ENVIRONMENT_GET_MEMORY_MAP:
            LOGI("GET_MEMORY_MAP - retourne NULL");
            return false;
            
        case RETRO_ENVIRONMENT_SET_MEMORY_MAPS:
            LOGI("SET_MEMORY_MAPS - accepté sans traitement (1/10)");
            return true;
            
        default:
            // Gestion des commandes non supportées
            if (cmd >= 0x10000) {
                LOGI("Environment callback: commande non supportée %u (0x%x) - on retourne TRUE pour debug", cmd, cmd);
                return handle_unsupported_command(cmd);
            } else {
                LOGI("Environment callback: commande non supportée %u (0x%x) - on retourne TRUE pour debug", cmd, cmd);
                return true; // Accepter par défaut pour éviter les crashes
            }
    }
}

void video_callback(const void* data, unsigned width, unsigned height, size_t pitch) {
    if (!data) return;
    
    pthread_mutex_lock(&videoBuffer.mutex);
    if (videoBuffer.frameBuffer && width <= videoBuffer.width && height <= videoBuffer.height) {
        const uint8_t* src = static_cast<const uint8_t*>(data);
        uint8_t* dst = videoBuffer.frameBuffer;
        
        for (unsigned y = 0; y < height; y++) {
            memcpy(dst + y * videoBuffer.width, src + y * pitch, width);
        }
        videoBuffer.hasNewFrame = true;
    }
    pthread_mutex_unlock(&videoBuffer.mutex);
}

size_t audio_callback(const int16_t* data, size_t frames) {
    if (!data || frames == 0) return 0;
    
    pthread_mutex_lock(&audioBuffer.mutex);
    size_t available = audioBuffer.size - audioBuffer.writePos;
    size_t to_write = std::min(frames, available);
    
    if (to_write > 0) {
        memcpy(audioBuffer.buffer + audioBuffer.writePos, data, to_write * sizeof(int16_t));
        audioBuffer.writePos = (audioBuffer.writePos + to_write) % audioBuffer.size;
    }
    pthread_mutex_unlock(&audioBuffer.mutex);
    
    return to_write;
}

void input_poll_callback() {
    // Polling des entrées - vide pour l'instant
}

int16_t input_state_callback(unsigned port, unsigned device, unsigned index, unsigned id) {
    if (port != 0 || device != RETRO_DEVICE_JOYPAD) return 0;
    
    pthread_mutex_lock(&inputState.mutex);
    int16_t state = inputState.gamepadState[id];
    pthread_mutex_unlock(&inputState.mutex);
    
    return state;
}

// --- Fonctions d'initialisation ---
void init_structures() {
    LOGI("Initialisation des structures");
    
    // Initialiser le buffer vidéo
    videoBuffer.width = 256;
    videoBuffer.height = 240;
    videoBuffer.frameBuffer = new uint8_t[videoBuffer.width * videoBuffer.height];
    videoBuffer.hasNewFrame = false;
    pthread_mutex_init(&videoBuffer.mutex, nullptr);
    
    // Initialiser le buffer audio
    audioBuffer.size = 8192;
    audioBuffer.buffer = new int16_t[audioBuffer.size];
    audioBuffer.readPos = 0;
    audioBuffer.writePos = 0;
    pthread_mutex_init(&audioBuffer.mutex, nullptr);
    
    // Initialiser l'état des entrées
    memset(inputState.gamepadState, 0, sizeof(inputState.gamepadState));
    pthread_mutex_init(&inputState.mutex, nullptr);
    
    LOGI("Structures initialisées");
}

void cleanup_on_error() {
    LOGI("Nettoyage en cas d'erreur");
    
    if (core_handle) {
        dlclose(core_handle);
        core_handle = nullptr;
    }
    
    if (videoBuffer.frameBuffer) {
        delete[] videoBuffer.frameBuffer;
        videoBuffer.frameBuffer = nullptr;
    }
    
    if (audioBuffer.buffer) {
        delete[] audioBuffer.buffer;
        audioBuffer.buffer = nullptr;
    }
    
    pthread_mutex_destroy(&videoBuffer.mutex);
    pthread_mutex_destroy(&audioBuffer.mutex);
    pthread_mutex_destroy(&inputState.mutex);
}

// --- Fonction de chargement de ROM sécurisée ---
bool safe_load_rom(const char* rom_path) {
    LOGI("Tentative de chargement sécurisé de ROM: %s", rom_path);
    
    if (!rom_path || !retro_load_game) {
        LOGE("Paramètres invalides pour le chargement de ROM");
        return false;
    }
    
    // Vérifier que le fichier existe
    FILE* rom_file = fopen(rom_path, "rb");
    if (!rom_file) {
        LOGE("Impossible d'ouvrir la ROM: %s", rom_path);
        return false;
    }
    
    // Obtenir la taille du fichier
    fseek(rom_file, 0, SEEK_END);
    long rom_size = ftell(rom_file);
    fseek(rom_file, 0, SEEK_SET);
    
    if (rom_size <= 0) {
        LOGE("Taille de ROM invalide: %ld", rom_size);
        fclose(rom_file);
        return false;
    }
    
    LOGI("Taille de la ROM: %ld bytes", rom_size);
    
    // Vérifier l'en-tête NES
    unsigned char header[16];
    if (fread(header, 1, 16, rom_file) != 16) {
        LOGE("Impossible de lire l'en-tête NES");
        fclose(rom_file);
        return false;
    }
    
    // Vérifier la signature NES
    if (header[0] != 0x4E || header[1] != 0x45 || header[2] != 0x53 || header[3] != 0x1A) {
        LOGE("Signature NES invalide: %02X %02X %02X %02X", header[0], header[1], header[2], header[3]);
        fclose(rom_file);
        return false;
    }
    
    LOGI("ROM NES valide détectée");
    fclose(rom_file);
    
    // Préparer les informations de jeu
    struct retro_game_info game_info;
    game_info.path = rom_path;
    game_info.data = nullptr;
    game_info.size = 0;
    game_info.meta = nullptr;
    
    LOGI("Tentative de chargement ROM avec protection SIGSEGV...");
    
    // Protection SIGSEGV pour le chargement
    struct sigaction old_action;
    struct sigaction new_action;
    memset(&new_action, 0, sizeof(new_action));
    new_action.sa_handler = sigsegv_handler;
    sigaction(SIGSEGV, &new_action, &old_action);
    
    bool success = false;
    
    // Première tentative : chargement par chemin
    if (sigsetjmp(sigsegv_env, 1) == 0) {
        success = retro_load_game(&game_info);
        sigaction(SIGSEGV, &old_action, nullptr);
        
        if (success) {
            LOGI("ROM chargée avec succès par chemin");
            rom_loaded = true;
            return true;
        } else {
            LOGE("Échec chargement ROM par chemin");
        }
    } else {
        sigaction(SIGSEGV, &old_action, nullptr);
        LOGE("SIGSEGV pendant le chargement par chemin");
    }
    
    // Deuxième tentative : chargement en mémoire
    LOGI("Tentative de chargement en mémoire...");
    
    rom_file = fopen(rom_path, "rb");
    if (rom_file) {
        unsigned char* rom_data = new unsigned char[rom_size];
        if (fread(rom_data, 1, rom_size, rom_file) == rom_size) {
            fclose(rom_file);
            
            LOGI("Chargement avec données en mémoire (%ld bytes)", rom_size);
            
            struct retro_game_info game_info_mem;
            game_info_mem.path = nullptr;
            game_info_mem.data = rom_data;
            game_info_mem.size = rom_size;
            game_info_mem.meta = nullptr;
            
            // Protection SIGSEGV pour le chargement en mémoire
            sigaction(SIGSEGV, &new_action, &old_action);
            
            if (sigsetjmp(sigsegv_env, 1) == 0) {
                success = retro_load_game(&game_info_mem);
                sigaction(SIGSEGV, &old_action, nullptr);
                
                if (success) {
                    LOGI("ROM chargée avec succès en mémoire");
                    rom_loaded = true;
                    delete[] rom_data;
                    return true;
                } else {
                    LOGE("Échec chargement ROM en mémoire");
                }
            } else {
                sigaction(SIGSEGV, &old_action, nullptr);
                LOGE("SIGSEGV pendant le chargement en mémoire");
            }
            
            delete[] rom_data;
        } else {
            fclose(rom_file);
        }
    }
    
    LOGE("Toutes les tentatives de chargement ont échoué");
    return false;
}

// --- Thread principal du core ---
void* core_thread_func(void*) {
    LOGI("Core thread démarré");
    
    while (running) {
        if (sigsetjmp(sigsegv_env, 1) == 0) {
            if (retro_run && core_initialized) {
                auto start = std::chrono::high_resolution_clock::now();
                retro_run();
                auto end = std::chrono::high_resolution_clock::now();
                double ms = std::chrono::duration<double, std::milli>(end - start).count();
                LOGI("Frame exécutée en %.2f ms", ms);
            }
        } else {
            LOGE("SIGSEGV dans core thread - arrêt propre");
            break;
        }
        usleep(16667); // ~60 FPS
    }
    
    return nullptr;
}

// --- Fonction principale d'initialisation ---
bool libretro_init(const char* core_path, const char* rom_path) {
    LOGI("Initialisation wrapper Libretro propre");
    
    if (!core_path) {
        LOGE("Chemin du core non fourni");
        return false;
    }
    
    init_structures();
    setup_signal_handlers();
    
    // Charger le core
    LOGI("Chargement du core: %s", core_path);
    core_handle = dlopen(core_path, RTLD_LAZY);
    if (!core_handle) {
        LOGE("Impossible de charger le core: %s", dlerror());
        cleanup_on_error();
        return false;
    }
    
    // Récupérer les fonctions LibRetro
    LOGI("Vérification des fonctions LibRetro:");
    
    retro_init = (void(*)())dlsym(core_handle, "retro_init");
    LOGI("  retro_init: %s", retro_init ? "OK" : "NULL");
    
    retro_deinit = (void(*)())dlsym(core_handle, "retro_deinit");
    LOGI("  retro_deinit: %s", retro_deinit ? "OK" : "NULL");
    
    retro_run = (void(*)())dlsym(core_handle, "retro_run");
    LOGI("  retro_run: %s", retro_run ? "OK" : "NULL");
    
    retro_load_game = (bool(*)(const struct retro_game_info*))dlsym(core_handle, "retro_load_game");
    LOGI("  retro_load_game: %s", retro_load_game ? "OK" : "NULL");
    
    retro_unload_game = (void(*)())dlsym(core_handle, "retro_unload_game");
    LOGI("  retro_unload_game: %s", retro_unload_game ? "OK" : "NULL");
    
    retro_get_system_av_info = (void(*)(struct retro_system_av_info*))dlsym(core_handle, "retro_get_system_av_info");
    LOGI("  retro_get_system_av_info: %s", retro_get_system_av_info ? "OK" : "NULL");
    
    retro_set_environment = (void(*)(bool(*)(unsigned, void*)))dlsym(core_handle, "retro_set_environment");
    LOGI("  retro_set_environment: %s", retro_set_environment ? "OK" : "NULL");
    
    retro_set_video_refresh = (void(*)(void(*)(const void*, unsigned, unsigned, size_t)))dlsym(core_handle, "retro_set_video_refresh");
    LOGI("  retro_set_video_refresh: %s", retro_set_video_refresh ? "OK" : "NULL");
    
    retro_set_audio_sample_batch = (void(*)(size_t(*)(const int16_t*, size_t)))dlsym(core_handle, "retro_set_audio_sample_batch");
    LOGI("  retro_set_audio_sample_batch: %s", retro_set_audio_sample_batch ? "OK" : "NULL");
    
    retro_set_input_poll = (void(*)(void(*)()))dlsym(core_handle, "retro_set_input_poll");
    LOGI("  retro_set_input_poll: %s", retro_set_input_poll ? "OK" : "NULL");
    
    retro_set_input_state = (void(*)(int16_t(*)(unsigned, unsigned, unsigned, unsigned)))dlsym(core_handle, "retro_set_input_state");
    LOGI("  retro_set_input_state: %s", retro_set_input_state ? "OK" : "NULL");
    
    if (!retro_init || !retro_run || !retro_load_game) {
        LOGE("Fonctions LibRetro essentielles manquantes");
        cleanup_on_error();
        return false;
    }
    
    LOGI("Fonctions Libretro récupérées avec succès");
    LOGI("Core chargé avec succès");
    
    // Configurer les callbacks
    LOGI("Configuration callback environment");
    if (retro_set_environment) {
        retro_set_environment(environment_callback);
        LOGI("Callback environment configuré");
    }
    
    LOGI("Configuration callback video");
    if (retro_set_video_refresh) {
        retro_set_video_refresh(video_callback);
        LOGI("Callback video configuré");
    }
    
    LOGI("Configuration callback audio");
    if (retro_set_audio_sample_batch) {
        retro_set_audio_sample_batch(audio_callback);
        LOGI("Callback audio configuré");
    }
    
    LOGI("Configuration callback input poll");
    if (retro_set_input_poll) {
        retro_set_input_poll(input_poll_callback);
        LOGI("Callback input poll configuré");
    }
    
    LOGI("Configuration callback input state");
    if (retro_set_input_state) {
        retro_set_input_state(input_state_callback);
        LOGI("Callback input state configuré");
    }
    
    // Initialiser le core
    LOGI("Appel retro_init_func");
    if (sigsetjmp(sigsegv_env, 1) == 0) {
        retro_init();
        core_initialized = true;
        LOGI("retro_init_func terminé avec succès");
    } else {
        LOGE("SIGSEGV pendant l'initialisation - sortie propre");
        cleanup_on_error();
        return false;
    }
    
    // Charger la ROM si fournie
    if (rom_path) {
        if (!safe_load_rom(rom_path)) {
            LOGE("Échec du chargement de ROM - passage en mode test");
            // Continuer sans ROM pour tester l'interface
        }
    }
    
    // Obtenir les informations système
    if (retro_get_system_av_info && rom_loaded) {
        LOGI("Appel retro_get_system_av_info_func");
        struct retro_system_av_info av_info;
        memset(&av_info, 0, sizeof(av_info));
        
        if (sigsetjmp(sigsegv_env, 1) == 0) {
            retro_get_system_av_info(&av_info);
            LOGI("Informations AV: %ux%u, FPS: %.2f",
                 av_info.geometry.base_width, av_info.geometry.base_height, av_info.timing.fps);
        } else {
            LOGE("SIGSEGV dans retro_get_system_av_info - utilisation des valeurs par défaut");
            // Utiliser des valeurs par défaut
            videoBuffer.width = 256;
            videoBuffer.height = 240;
        }
    }
    
    running = true;
    pthread_create(&coreThread, nullptr, core_thread_func, nullptr);
    LOGI("Thread core démarré");
    
    return true;
}

void libretro_deinit() {
    LOGI("Déinitialisation wrapper Libretro");
    
    running = false;
    if (coreThread) {
        pthread_join(coreThread, nullptr);
    }
    
    if (retro_unload_game) retro_unload_game();
    if (retro_deinit) retro_deinit();
    if (core_handle) {
        dlclose(core_handle);
        core_handle = nullptr;
    }
    
    cleanup_on_error();
    LOGI("Wrapper Libretro déinitialisé");
}

void libretro_set_input(int button, bool pressed) {
    if (button >= 0 && button < 16) {
        pthread_mutex_lock(&inputState.mutex);
        inputState.gamepadState[button] = pressed ? 1 : 0;
        pthread_mutex_unlock(&inputState.mutex);
    }
}

// --- Fonctions de gestion vidéo ---
void video_manager_init(int width, int height) {
    LOGI("Initialisation vidéo: %dx%d", width, height);
    
    pthread_mutex_lock(&videoBuffer.mutex);
    if (videoBuffer.frameBuffer) {
        delete[] videoBuffer.frameBuffer;
    }
    
    videoBuffer.width = width;
    videoBuffer.height = height;
    videoBuffer.frameBuffer = new uint8_t[width * height];
    videoBuffer.hasNewFrame = false;
    pthread_mutex_unlock(&videoBuffer.mutex);
    
    LOGI("Video manager initialisé (OpenGL en attente)");
}

bool video_manager_get_frame(uint8_t* buffer, int* width, int* height) {
    pthread_mutex_lock(&videoBuffer.mutex);
    if (videoBuffer.hasNewFrame && videoBuffer.frameBuffer) {
        memcpy(buffer, videoBuffer.frameBuffer, videoBuffer.width * videoBuffer.height);
        *width = videoBuffer.width;
        *height = videoBuffer.height;
        videoBuffer.hasNewFrame = false;
        pthread_mutex_unlock(&videoBuffer.mutex);
        return true;
    }
    pthread_mutex_unlock(&videoBuffer.mutex);
    return false;
}

// --- Fonctions utilitaires ---
bool handle_unsupported_command(unsigned cmd) {
    // Commandes non critiques que l'on peut ignorer
    if (cmd == 0x1002d || cmd == 0x1002a || cmd == 0x10033) {
        LOGI("Commande non critique ignorée: 0x%x", cmd);
        return true;
    }
    
    // Commandes critiques qui peuvent causer des crashes
    if (cmd == 0x27) {
        LOGE("Commande critique non supportée - retourne false pour éviter le crash");
        return false;
    }
    
    return true;
}
EOF

echo "✅ Wrapper amélioré créé"

# Remplacer le fichier original
mv app/src/main/cpp/libretro_wrapper_improved.cpp app/src/main/cpp/libretro_wrapper.cpp

echo "✅ Wrapper remplacé avec la version améliorée"

# Compiler et tester
echo ""
echo "🔨 Compilation de la version améliorée..."
./gradlew clean assembleDebug

echo ""
echo "📱 Installation de la version améliorée..."
./gradlew installDebug

echo ""
echo "✅ Correction appliquée !"
echo ""
echo "🎯 Améliorations apportées :"
echo "1. Protection SIGSEGV plus robuste"
echo "2. Gestion des erreurs améliorée"
echo "3. Mode fallback sans ROM"
echo "4. Logs détaillés pour le debugging"
echo ""
echo "Testez maintenant l'application pour voir si les ROMs se chargent !" 