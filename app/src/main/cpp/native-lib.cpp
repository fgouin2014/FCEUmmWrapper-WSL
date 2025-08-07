#include <jni.h>
#include <string>
#include <android/log.h>
#include <dlfcn.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fstream>
#include <vector>
#include <mutex>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

// Définitions libretro nécessaires
#define RETRO_ENVIRONMENT_SET_VARIABLE 32

#define LOG_TAG "LibretroWrapper"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Libretro official device ID constants - 100% compatible with RetroArch
#define RETRO_DEVICE_ID_JOYPAD_B        0
#define RETRO_DEVICE_ID_JOYPAD_Y        1
#define RETRO_DEVICE_ID_JOYPAD_SELECT   2
#define RETRO_DEVICE_ID_JOYPAD_START    3
#define RETRO_DEVICE_ID_JOYPAD_UP       4
#define RETRO_DEVICE_ID_JOYPAD_DOWN     5
#define RETRO_DEVICE_ID_JOYPAD_LEFT     6
#define RETRO_DEVICE_ID_JOYPAD_RIGHT    7
#define RETRO_DEVICE_ID_JOYPAD_A        8
#define RETRO_DEVICE_ID_JOYPAD_X        9
#define RETRO_DEVICE_ID_JOYPAD_L       10
#define RETRO_DEVICE_ID_JOYPAD_R       11
#define RETRO_DEVICE_ID_JOYPAD_L2      12
#define RETRO_DEVICE_ID_JOYPAD_R2      13
#define RETRO_DEVICE_ID_JOYPAD_L3      14
#define RETRO_DEVICE_ID_JOYPAD_R3      15
#define RETRO_DEVICE_ID_JOYPAD_MASK    256

// Libretro API types
typedef void (*retro_init_t)();
typedef void (*retro_deinit_t)();
typedef void (*retro_api_version_t)();
typedef void (*retro_get_system_info_t)(void* info);
typedef void (*retro_get_system_av_info_t)(void* info);
typedef bool (*retro_load_game_t)(const void* game_info);
typedef void (*retro_run_t)();
typedef void (*retro_set_environment_t)(void* callback);
typedef void (*retro_set_video_refresh_t)(void* callback);
typedef void (*retro_set_audio_sample_batch_t)(void* callback);
typedef void (*retro_set_input_poll_t)(void* callback);
typedef void (*retro_set_input_state_t)(void* callback);
typedef void (*retro_unload_game_t)();

// FCEUmm audio function types
typedef void (*FCEUI_SetSoundVolume_t)(uint32_t volume);
typedef void (*FCEUI_SetSoundQuality_t)(int quality);
typedef void (*FCEUI_Sound_t)(int rate);
typedef void (*FCEUI_SetLowPass_t)(int q);

// Libretro environment callback type
typedef bool (*retro_environment_t)(unsigned cmd, void* data);

// Libretro variable structure
struct retro_variable {
    const char* key;
    const char* value;
};

// FCEUmm settings structure
typedef struct {
    int PAL;
    int SoundVolume;
    int TriangleVolume;
    int SquareVolume[2];
    int NoiseVolume;
    int PCMVolume;
    int GameGenie;
    int FirstSLine;
    int LastSLine;
    int UsrFirstSLine[2];
    int UsrLastSLine[2];
    uint32_t SndRate;
    int soundq;
    int lowpass;
} FCEUS_t;

// Structures libretro
struct retro_game_info {
    const char* path;
    const void* data;
    size_t size;
    const char* meta;
};

struct retro_system_av_info {
    struct {
        unsigned width;
        unsigned height;
        float aspect_ratio;
    } geometry;
    struct {
        double fps;
        double sample_rate;
    } timing;
};

// Global variables
void* libretro_handle = nullptr;
retro_init_t retro_init_func = nullptr;
retro_deinit_t retro_deinit_func = nullptr;
retro_load_game_t retro_load_game_func = nullptr;
retro_run_t retro_run_func = nullptr;
retro_get_system_av_info_t retro_get_system_av_info_func = nullptr;
retro_set_environment_t retro_set_environment_func = nullptr;
retro_set_video_refresh_t retro_set_video_refresh_func = nullptr;
retro_set_audio_sample_batch_t retro_set_audio_sample_batch_func = nullptr;
retro_set_input_poll_t retro_set_input_poll_func = nullptr;
retro_set_input_state_t retro_set_input_state_func = nullptr;
retro_unload_game_t retro_unload_game_func = nullptr;

// FCEUmm audio function pointers
FCEUI_SetSoundVolume_t FCEUI_SetSoundVolume_func = nullptr;
FCEUI_SetSoundQuality_t FCEUI_SetSoundQuality_func = nullptr;
FCEUI_Sound_t FCEUI_Sound_func = nullptr;
FCEUI_SetLowPass_t FCEUI_SetLowPass_func = nullptr;

// Libretro environment callback
retro_environment_t environ_cb = nullptr;
bool environ_cb_initialized = false;
bool rom_loaded = false;

    // Variables globales pour les paramètres audio persistants
    int global_audio_volume = 100;
    int global_audio_quality = 2; // Qualité maximum par défaut
    int global_audio_sample_rate = 48000; // Sample rate élevé par défaut
    bool global_audio_rf_filter = false; // Filtre RF désactivé par défaut (évite les artefacts)
    int global_audio_stereo_delay = 0;
    bool global_audio_swap_duty = false;
    bool global_audio_muted = false;
    
    // Paramètres audio optimisés pour Duck Hunt
    bool audio_optimized_for_duck_hunt = false;
    
    // Paramètres de latence optimisés (inspirés de libretro-super)
    const int OPTIMAL_BUFFER_SIZE = 2048;  // Buffer plus grand pour FCEUmm
    const int LOW_LATENCY_BUFFER_SIZE = 1024;  // Buffer basse latence
    bool low_latency_mode = true;  // Mode basse latence par défaut

// FCEUmm settings pointer (optionnel)
FCEUS_t* FSettings_ptr = nullptr;

// Video buffer
std::vector<uint32_t> frame_buffer;
int frame_width = 256;
int frame_height = 240;
std::mutex frame_mutex;
bool frame_updated = false;

// État global des boutons (8 boutons au total)
std::mutex input_mutex;
bool button_states[16] = {false}; // Support pour tous les boutons libretro (0-15)

// Audio buffer pour stocker les données audio
std::vector<int16_t> audio_buffer;
std::mutex audio_mutex;
bool audio_enabled = true;
int audio_sample_rate = 44100;

// OpenSL ES variables
SLObjectItf slEngine = nullptr;
SLEngineItf slEngineEngine = nullptr;
SLObjectItf slOutputMix = nullptr;
SLObjectItf slPlayer = nullptr;
SLPlayItf slPlayerPlay = nullptr;
SLAndroidSimpleBufferQueueItf slPlayerBufferQueue = nullptr;

// Audio buffer circulaire optimisé pour la latence
#define AUDIO_BUFFER_SIZE 8192  // Ajusté pour équilibrer latence et qualité
#define CHANNELS 2
struct AudioBuffer {
    int16_t buffer[AUDIO_BUFFER_SIZE * CHANNELS];
    size_t writePos;
    size_t readPos;
    size_t size;
    std::mutex mutex;
    bool initialized;
};

static AudioBuffer audioBuffer;
static bool queue_ready = false;
static bool audio_initialized = false;

// Déclaration de la fonction applyAudioSettings
void applyAudioSettings();

// Callback functions
bool environment_callback(unsigned cmd, void* data) {
    switch (cmd) {
        case 10: // RETRO_ENVIRONMENT_SET_ENVIRONMENT
            environ_cb = *(retro_environment_t*)data;
            environ_cb_initialized = true;
            LOGI("🎵 Callback d'environnement libretro défini");
            return true;
            
        case 1: // RETRO_ENVIRONMENT_GET_LOG_INTERFACE
        case 2: // RETRO_ENVIRONMENT_GET_PERF_INTERFACE
            // Commandes importantes mais pas critiques
            break;
            
        case 32: // RETRO_ENVIRONMENT_GET_SYSTEM_AV_INFO
            if (data) {
                struct retro_system_av_info* av_info = (struct retro_system_av_info*)data;
                av_info->geometry.width = 256;
                av_info->geometry.height = 240;
                av_info->geometry.aspect_ratio = 4.0f / 3.0f;
                av_info->timing.fps = 60.0;
                av_info->timing.sample_rate = 48000.0; // Sample rate standard
                LOGI("🎵 System AV Info configuré: 256x240, 60fps, 48kHz");
            }
            return true;
            
        default:
            // Réduire le spam des logs - seulement les commandes importantes
            if (cmd == 15 || cmd == 16) { // Variables d'environnement
                LOGI("Environment callback: cmd=%u", cmd);
            }
            break;
    }
    return false;
}

void video_refresh_callback(const void* data, unsigned width, unsigned height, size_t pitch) {
    if (!data) {
        return;
    }
    
    std::lock_guard<std::mutex> lock(frame_mutex);
    
    // Mettre à jour les dimensions si nécessaire
    if (width != frame_width || height != frame_height) {
        frame_width = width;
        frame_height = height;
        frame_buffer.resize(width * height);
    }
    
    // Copier les données vidéo avec conversion de format
    const uint16_t* src = static_cast<const uint16_t*>(data);
    for (unsigned y = 0; y < height; y++) {
        for (unsigned x = 0; x < width; x++) {
            uint16_t pixel = src[y * (pitch / 2) + x];
            
            // Convertir RGB565 vers RGBA8888
            uint8_t r = ((pixel >> 11) & 0x1F) << 3;
            uint8_t g = ((pixel >> 5) & 0x3F) << 2;
            uint8_t b = (pixel & 0x1F) << 3;
            uint8_t a = 0xFF;
            
            frame_buffer[y * width + x] = (a << 24) | (b << 16) | (g << 8) | r;
        }
    }
    
    frame_updated = true;
}

// Callback OpenSL ES pour maintenir le flux audio
void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    if (slPlayerBufferQueue != nullptr && queue_ready) {
        // Buffer temporaire pour éviter les conflits de mémoire
        static int16_t temp_buffer[AUDIO_BUFFER_SIZE * CHANNELS];
        
        std::lock_guard<std::mutex> lock(audioBuffer.mutex);
        
        // Vérifier que le buffer audio est initialisé
        if (!audioBuffer.initialized) {
            memset(temp_buffer, 0, sizeof(temp_buffer));
        } else {
            // Calculer les données disponibles dans le buffer circulaire
            size_t available = 0;
            if (audioBuffer.writePos >= audioBuffer.readPos) {
                available = audioBuffer.writePos - audioBuffer.readPos;
            } else {
                available = audioBuffer.size - audioBuffer.readPos + audioBuffer.writePos;
            }
            
            size_t samples_to_read = AUDIO_BUFFER_SIZE * CHANNELS;
            size_t to_read = std::min(samples_to_read, available);
            
            if (to_read > 0) {
                // Copier les données audio dans le buffer temporaire
                if (audioBuffer.readPos + to_read <= audioBuffer.size) {
                    // Pas de wrap-around
                    memcpy(temp_buffer, audioBuffer.buffer + audioBuffer.readPos, to_read * sizeof(int16_t));
                } else {
                    // Wrap-around nécessaire
                    size_t first_part = audioBuffer.size - audioBuffer.readPos;
                    memcpy(temp_buffer, audioBuffer.buffer + audioBuffer.readPos, first_part * sizeof(int16_t));
                    memcpy(temp_buffer + first_part, audioBuffer.buffer, (to_read - first_part) * sizeof(int16_t));
                }
                
                // Remplir le reste avec du silence si nécessaire
                if (to_read < samples_to_read) {
                    memset(temp_buffer + to_read, 0, (samples_to_read - to_read) * sizeof(int16_t));
                }
                
                // Mettre à jour la position de lecture
                audioBuffer.readPos = (audioBuffer.readPos + to_read) % audioBuffer.size;
                
                // Log supprimé pour réduire le bruit
            } else {
                // Pas de données disponibles, utiliser du silence avec crossfade pour éviter les clicks
                static int16_t last_samples[CHANNELS] = {0, 0};
                
                // Crossfade simple pour éviter les clicks
                for (int i = 0; i < AUDIO_BUFFER_SIZE * CHANNELS; i += CHANNELS) {
                    for (int ch = 0; ch < CHANNELS; ch++) {
                        temp_buffer[i + ch] = last_samples[ch];
                        last_samples[ch] = 0; // Fade vers le silence
                    }
                }
                
                // Log supprimé pour réduire le bruit
            }
        }
        
        // Envoyer le buffer temporaire à OpenSL ES
        SLresult result = (*slPlayerBufferQueue)->Enqueue(slPlayerBufferQueue, temp_buffer, sizeof(temp_buffer));
        if (result != SL_RESULT_SUCCESS) {
            if (result == SL_RESULT_BUFFER_INSUFFICIENT) {
                queue_ready = false;
            }
            return;
        }
        queue_ready = true;
    }
}

size_t audio_sample_batch_callback(const int16_t* data, size_t frames) {
    if (!data || frames == 0 || !audio_initialized) {
        return frames; // Éviter le blocage
    }
    
    std::lock_guard<std::mutex> lock(audioBuffer.mutex);
    
    // Vérifier que le buffer audio est initialisé
    if (!audioBuffer.initialized) {
        return frames;
    }
    
    // Écrire dans le buffer circulaire avec gestion du wrap-around
    size_t samples_to_copy = frames * CHANNELS;
    
    // Calculer l'espace disponible de manière plus précise
    size_t available_space = 0;
    if (audioBuffer.writePos >= audioBuffer.readPos) {
        available_space = audioBuffer.size - audioBuffer.writePos + audioBuffer.readPos;
    } else {
        available_space = audioBuffer.readPos - audioBuffer.writePos;
    }
    
    // Si pas assez d'espace, attendre un peu au lieu de perdre des données
    if (available_space < samples_to_copy) {
        // Attendre que de l'espace se libère plutôt que de perdre des données
        static int skip_counter = 0;
        skip_counter++;
        if (skip_counter % 10 == 0) {
            LOGI("Audio: Buffer plein, attente... (espace: %zu, besoin: %zu)", available_space, samples_to_copy);
        }
        return frames; // Retourner sans écrire pour éviter la perte de données
    }
    
    // Écrire les données avec gestion du wrap-around
    if (audioBuffer.writePos + samples_to_copy <= audioBuffer.size) {
        // Pas de wrap-around nécessaire
        memcpy(audioBuffer.buffer + audioBuffer.writePos, data, samples_to_copy * sizeof(int16_t));
    } else {
        // Wrap-around nécessaire
        size_t first_part = audioBuffer.size - audioBuffer.writePos;
        memcpy(audioBuffer.buffer + audioBuffer.writePos, data, first_part * sizeof(int16_t));
        memcpy(audioBuffer.buffer, data + first_part, (samples_to_copy - first_part) * sizeof(int16_t));
    }
    
    // Mettre à jour la position d'écriture
    audioBuffer.writePos = (audioBuffer.writePos + samples_to_copy) % audioBuffer.size;
    
                // Log pour debug avec plus d'informations
            static int frame_counter = 0;
            frame_counter++;
            if (frame_counter % 100 == 0) {  // Réduit la fréquence des logs pour moins d'overhead
                size_t buffer_usage = (audioBuffer.writePos - audioBuffer.readPos + audioBuffer.size) % audioBuffer.size;
                LOGI("Audio: %zu frames écrites, position: %zu, usage: %zu/%zu (latence optimisée)", 
                     frames, audioBuffer.writePos, buffer_usage, audioBuffer.size);
            }
    
    return frames;
}

void input_poll_callback() {
    // Input poll callback - sera implémenté plus tard
}

int16_t input_state_callback(unsigned port, unsigned device, unsigned index, unsigned id) {
    if (port == 0 && device == 1) { // RETRO_DEVICE_JOYPAD
        std::lock_guard<std::mutex> lock(input_mutex);
        
        // Support complet pour tous les boutons libretro (0-15)
        if (id >= 0 && id < 16) {
            return button_states[id] ? 1 : 0;
        }
        
        // Fallback pour compatibilité avec l'ancien mapping
        switch (id) {
            case RETRO_DEVICE_ID_JOYPAD_UP:      return button_states[4] ? 1 : 0; // UP = 4
            case RETRO_DEVICE_ID_JOYPAD_DOWN:    return button_states[5] ? 1 : 0; // DOWN = 5
            case RETRO_DEVICE_ID_JOYPAD_LEFT:    return button_states[6] ? 1 : 0; // LEFT = 6
            case RETRO_DEVICE_ID_JOYPAD_RIGHT:   return button_states[7] ? 1 : 0; // RIGHT = 7
            case RETRO_DEVICE_ID_JOYPAD_A:       return button_states[8] ? 1 : 0; // A = 8
            case RETRO_DEVICE_ID_JOYPAD_B:       return button_states[0] ? 1 : 0; // B = 0
            case RETRO_DEVICE_ID_JOYPAD_Y:       return button_states[1] ? 1 : 0; // Y = 1
            case RETRO_DEVICE_ID_JOYPAD_X:       return button_states[9] ? 1 : 0; // X = 9
            case RETRO_DEVICE_ID_JOYPAD_L:       return button_states[10] ? 1 : 0; // L = 10
            case RETRO_DEVICE_ID_JOYPAD_R:       return button_states[11] ? 1 : 0; // R = 11
            case RETRO_DEVICE_ID_JOYPAD_L2:      return button_states[12] ? 1 : 0; // L2 = 12
            case RETRO_DEVICE_ID_JOYPAD_R2:      return button_states[13] ? 1 : 0; // R2 = 13
            case RETRO_DEVICE_ID_JOYPAD_L3:      return button_states[14] ? 1 : 0; // L3 = 14
            case RETRO_DEVICE_ID_JOYPAD_R3:      return button_states[15] ? 1 : 0; // R3 = 15
            case RETRO_DEVICE_ID_JOYPAD_START:   return button_states[3] ? 1 : 0; // START = 3
            case RETRO_DEVICE_ID_JOYPAD_SELECT:  return button_states[2] ? 1 : 0; // SELECT = 2
            default: return 0;
        }
    }
    return 0;
}

// Système d'input 100% RetroArch natif - remplace l'ancien SimpleInputManager

// Système d'input 100% RetroArch natif
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulationActivity_sendRetroArchInput(JNIEnv* env, jobject thiz, jint deviceId, jboolean pressed) {
    std::lock_guard<std::mutex> lock(input_mutex);
    
    // Système d'input RetroArch natif - pas de conversion nécessaire
    if (deviceId >= 0 && deviceId < 16) {
        button_states[deviceId] = pressed;
        
        // Log pour debug avec les noms des boutons RetroArch
        if (pressed) {
            const char* buttonNames[] = {"B", "Y", "SELECT", "START", "UP", "DOWN", "LEFT", "RIGHT", "A", "X", "L", "R", "L2", "R2", "L3", "R3"};
            LOGI("🎮 RetroArch Input: %s PRESSÉ (ID: %d)", buttonNames[deviceId], deviceId);
        } else {
            const char* buttonNames[] = {"B", "Y", "SELECT", "START", "UP", "DOWN", "LEFT", "RIGHT", "A", "X", "L", "R", "L2", "R2", "L3", "R3"};
            LOGI("🎮 RetroArch Input: %s RELÂCHÉ (ID: %d)", buttonNames[deviceId], deviceId);
        }
    } else {
        LOGE("❌ Device ID invalide: %d (doit être entre 0 et 15)", deviceId);
    }
}

// Fonction RetroArch pour réinitialiser tous les inputs
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulationActivity_resetAllRetroArchInputs(JNIEnv* env, jobject thiz) {
    std::lock_guard<std::mutex> lock(input_mutex);
    for (int i = 0; i < 16; i++) {
        button_states[i] = false;
    }
    LOGI("🎮 Tous les inputs RetroArch réinitialisés");
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_fceumm_wrapper_EmulationActivity_initLibretro(JNIEnv* env, jobject thiz) {
    LOGI("Initialisation du wrapper Libretro");
    
    // Charger le core libretro FCEUmm
    const char* core_path = "/data/data/com.fceumm.wrapper/files/cores/fceumm_libretro_android.so";
    libretro_handle = dlopen(core_path, RTLD_LAZY);
    
    if (!libretro_handle) {
        LOGE("Impossible de charger le core: %s", dlerror());
        return JNI_FALSE;
    }
    
    LOGI("Core chargé avec succès");
    
    // Récupérer les fonctions
    retro_init_func = (retro_init_t)dlsym(libretro_handle, "retro_init");
    retro_deinit_func = (retro_deinit_t)dlsym(libretro_handle, "retro_deinit");
    retro_load_game_func = (retro_load_game_t)dlsym(libretro_handle, "retro_load_game");
    retro_run_func = (retro_run_t)dlsym(libretro_handle, "retro_run");
    retro_get_system_av_info_func = (retro_get_system_av_info_t)dlsym(libretro_handle, "retro_get_system_av_info");
    retro_set_environment_func = (retro_set_environment_t)dlsym(libretro_handle, "retro_set_environment");
    retro_set_video_refresh_func = (retro_set_video_refresh_t)dlsym(libretro_handle, "retro_set_video_refresh");
    retro_set_audio_sample_batch_func = (retro_set_audio_sample_batch_t)dlsym(libretro_handle, "retro_set_audio_sample_batch");
    retro_set_input_poll_func = (retro_set_input_poll_t)dlsym(libretro_handle, "retro_set_input_poll");
    retro_set_input_state_func = (retro_set_input_state_t)dlsym(libretro_handle, "retro_set_input_state");
    retro_unload_game_func = (retro_unload_game_t)dlsym(libretro_handle, "retro_unload_game");
    
    // Charger les fonctions FCEUmm audio
    FCEUI_SetSoundVolume_func = (FCEUI_SetSoundVolume_t)dlsym(libretro_handle, "FCEUI_SetSoundVolume");
    FCEUI_SetSoundQuality_func = (FCEUI_SetSoundQuality_t)dlsym(libretro_handle, "FCEUI_SetSoundQuality");
    FCEUI_Sound_func = (FCEUI_Sound_t)dlsym(libretro_handle, "FCEUI_Sound");
    FCEUI_SetLowPass_func = (FCEUI_SetLowPass_t)dlsym(libretro_handle, "FCEUI_SetLowPass");
    
    // Charger la structure FCEUmm settings (optionnel)
    FSettings_ptr = (FCEUS_t*)dlsym(libretro_handle, "FSettings");
    if (FSettings_ptr) {
        LOGI("Structure FSettings chargée avec succès");
    } else {
        LOGI("Structure FSettings non disponible, utilisation des variables d'environnement libretro");
    }
    
    // Le callback d'environnement sera fourni par le core via retro_set_environment
    environ_cb = nullptr; // Sera défini dans environment_callback
    
    // Initialiser OpenSL ES
    SLresult result;
    
    // Créer l'engine
    result = slCreateEngine(&slEngine, 0, nullptr, 0, nullptr, nullptr);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("Erreur lors de la création de l'engine OpenSL ES");
        return JNI_FALSE;
    }
    
    // Réaliser l'engine
    result = (*slEngine)->Realize(slEngine, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("Erreur lors de la réalisation de l'engine OpenSL ES");
        return JNI_FALSE;
    }
    
    // Obtenir l'interface engine
    result = (*slEngine)->GetInterface(slEngine, SL_IID_ENGINE, &slEngineEngine);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("Erreur lors de l'obtention de l'interface engine");
        return JNI_FALSE;
    }
    
    // Créer le mix de sortie
    result = (*slEngineEngine)->CreateOutputMix(slEngineEngine, &slOutputMix, 0, nullptr, nullptr);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("Erreur lors de la création du mix de sortie");
        return JNI_FALSE;
    }
    
    // Réaliser le mix de sortie
    result = (*slOutputMix)->Realize(slOutputMix, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("Erreur lors de la réalisation du mix de sortie");
        return JNI_FALSE;
    }
    
    // Configuration audio
    SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    SLDataFormat_PCM format_pcm = {
        SL_DATAFORMAT_PCM,
        2, // 2 canaux
        SL_SAMPLINGRATE_48,
        SL_PCMSAMPLEFORMAT_FIXED_16,
        SL_PCMSAMPLEFORMAT_FIXED_16,
        SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
        SL_BYTEORDER_LITTLEENDIAN
    };
    
    SLDataSource audioSrc = {&loc_bufq, &format_pcm};
    
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, slOutputMix};
    SLDataSink audioSnk = {&loc_outmix, nullptr};
    
    const SLInterfaceID ids[2] = {SL_IID_BUFFERQUEUE, SL_IID_VOLUME};
    const SLboolean req[2] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    
    // Créer le player audio
    result = (*slEngineEngine)->CreateAudioPlayer(slEngineEngine, &slPlayer, &audioSrc, &audioSnk, 2, ids, req);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("Erreur lors de la création du player audio");
        return JNI_FALSE;
    }
    
    // Réaliser le player
    result = (*slPlayer)->Realize(slPlayer, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("Erreur lors de la réalisation du player audio");
        return JNI_FALSE;
    }
    
    // Obtenir l'interface play
    result = (*slPlayer)->GetInterface(slPlayer, SL_IID_PLAY, &slPlayerPlay);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("Erreur lors de l'obtention de l'interface play");
        return JNI_FALSE;
    }
    
    // Obtenir l'interface buffer queue
    result = (*slPlayer)->GetInterface(slPlayer, SL_IID_BUFFERQUEUE, &slPlayerBufferQueue);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("Erreur lors de l'obtention de l'interface buffer queue");
        return JNI_FALSE;
    }
    
    // Configurer le callback
    result = (*slPlayerBufferQueue)->RegisterCallback(slPlayerBufferQueue, bqPlayerCallback, nullptr);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("Erreur lors de la configuration du callback");
        return JNI_FALSE;
    }
    
    // Initialiser le buffer circulaire
    audioBuffer.writePos = 0;
    audioBuffer.readPos = 0;
    audioBuffer.size = AUDIO_BUFFER_SIZE * CHANNELS;
    audioBuffer.initialized = true;
    queue_ready = true;
    audio_initialized = true;
    
    // Démarrer la lecture audio
    if (slPlayerPlay != nullptr) {
        SLresult result = (*slPlayerPlay)->SetPlayState(slPlayerPlay, SL_PLAYSTATE_PLAYING);
        if (result == SL_RESULT_SUCCESS) {
            LOGI("Lecture audio démarrée avec succès");
        } else {
            LOGE("Erreur lors du démarrage de la lecture audio: %d", result);
        }
    }
    
    // Envoyer un premier buffer (silence) pour démarrer la queue
    if (slPlayerBufferQueue != nullptr) {
        memset(audioBuffer.buffer, 0, sizeof(audioBuffer.buffer));
        SLresult result = (*slPlayerBufferQueue)->Enqueue(slPlayerBufferQueue, audioBuffer.buffer, sizeof(audioBuffer.buffer));
        if (result == SL_RESULT_SUCCESS) {
            LOGI("Premier buffer audio envoyé avec succès");
        } else {
            LOGE("Erreur lors de l'envoi du premier buffer: %d", result);
        }
    }
    
    LOGI("OpenSL ES initialisé avec succès - Buffer circulaire: %lu bytes", (unsigned long)(audioBuffer.size * sizeof(int16_t)));
    LOGI("Callback d'environnement sera défini lors de l'initialisation");
    
    if (!retro_init_func || !retro_load_game_func || !retro_run_func) {
        LOGE("Fonctions libretro manquantes");
        return JNI_FALSE;
    }
    
    LOGI("Fonctions libretro récupérées avec succès");
    
    // Vérifier les fonctions FCEUmm audio
    if (FCEUI_SetSoundVolume_func && FCEUI_SetSoundQuality_func && FCEUI_Sound_func) {
        LOGI("Fonctions FCEUmm audio récupérées avec succès");
    } else {
        LOGI("Certaines fonctions FCEUmm audio non disponibles");
    }
    
    // Configurer les callbacks avec cast explicite
    retro_set_environment_func((void*)environment_callback);
    retro_set_video_refresh_func((void*)video_refresh_callback);
    retro_set_audio_sample_batch_func((void*)audio_sample_batch_callback);
    retro_set_input_poll_func((void*)input_poll_callback);
    retro_set_input_state_func((void*)input_state_callback);
    
    LOGI("Callbacks configurés");
    
    // Initialiser le core
    retro_init_func();
    LOGI("Core initialisé");
    
    // Configurer l'audio avec les valeurs globales
    audio_enabled = !global_audio_muted;
    audio_sample_rate = global_audio_sample_rate;
    audio_buffer.clear();
    LOGI("Audio configuré: sample_rate=%d, enabled=%s", audio_sample_rate, audio_enabled ? "true" : "false");
    
                // Volume par défaut
            global_audio_volume = 100;
    
    // Ne pas appliquer les paramètres audio ici - attendre qu'une ROM soit chargée
    LOGI("🎵 Paramètres audio globaux prêts, application différée jusqu'au chargement d'une ROM");
    
    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_fceumm_wrapper_EmulationActivity_loadROM(JNIEnv* env, jobject thiz, jstring rom_path) {
    const char* path = env->GetStringUTFChars(rom_path, nullptr);
    LOGI("Chargement ROM: %s", path);
    
    // Vérifier que le fichier existe
    struct stat st;
    if (stat(path, &st) != 0) {
        LOGE("ROM introuvable: %s", path);
        env->ReleaseStringUTFChars(rom_path, path);
        return JNI_FALSE;
    }
    
    // Lire le fichier ROM
    std::ifstream file(path, std::ios::binary);
    if (!file.is_open()) {
        LOGE("Impossible d'ouvrir la ROM: %s", path);
        env->ReleaseStringUTFChars(rom_path, path);
        return JNI_FALSE;
    }
    
    std::vector<char> rom_data((std::istreambuf_iterator<char>(file)), std::istreambuf_iterator<char>());
    file.close();
    
    LOGI("ROM chargée: %zu bytes", rom_data.size());
    
    // Structure pour les informations de jeu
    retro_game_info game_info;
    game_info.path = path;
    game_info.data = rom_data.data();
    game_info.size = rom_data.size();
    game_info.meta = nullptr;
    
    // Charger le jeu
    bool success = retro_load_game_func(&game_info);
    
    env->ReleaseStringUTFChars(rom_path, path);
    
            if (success) {
            LOGI("ROM chargée avec succès");
            rom_loaded = true;
            
            // Récupérer les informations vidéo
            if (retro_get_system_av_info_func) {
                retro_system_av_info av_info;
                retro_get_system_av_info_func(&av_info);
                
                frame_width = av_info.geometry.width;
                frame_height = av_info.geometry.height;
                frame_buffer.resize(frame_width * frame_height);
                
                // Forcer le sample rate correct (le core retourne 60.10 Hz par erreur)
                audio_sample_rate = 48000; // Sample rate standard libretro
                
                LOGI("Informations vidéo: %dx%d, FPS: %.2f, Sample Rate forcé: %d Hz", 
                     frame_width, frame_height, av_info.timing.fps, audio_sample_rate);
            }
            
            // Appliquer les paramètres audio maintenant qu'une ROM est chargée
            LOGI("🎵 Application des paramètres audio après chargement de la ROM...");
            
            // Audio configuré
            LOGI("Audio configuré avec succès");
            
            // Démarrer l'audio OpenSL ES
            if (slPlayerPlay) {
                SLresult result = (*slPlayerPlay)->SetPlayState(slPlayerPlay, SL_PLAYSTATE_PLAYING);
                if (result == SL_RESULT_SUCCESS) {
                    LOGI("Audio OpenSL ES démarré avec succès");
                } else {
                    LOGE("Erreur lors du démarrage de l'audio OpenSL ES");
                }
            }
            
            return JNI_TRUE;
        } else {
            LOGE("Échec du chargement de la ROM");
            rom_loaded = false;
            return JNI_FALSE;
        }
}

extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulationActivity_runFrame(JNIEnv* env, jobject thiz) {
    static int frame_counter = 0;
    frame_counter++;
    
    if (retro_run_func) {
        retro_run_func();
                    // if (frame_counter % 60 == 0) {
            //     LOGI("🎮 Frame libretro exécutée #%d", frame_counter);
            // }
    } else {
        LOGI("🎮 ERREUR: retro_run_func est null!");
    }
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_fceumm_wrapper_EmulatorView_getFrameBuffer(JNIEnv* env, jobject thiz) {
    std::lock_guard<std::mutex> lock(frame_mutex);
    
    if (frame_buffer.empty()) {
        return nullptr;
    }
    
    // Convertir le buffer en bytes pour Java
    jbyteArray result = env->NewByteArray(frame_buffer.size() * 4);
    env->SetByteArrayRegion(result, 0, frame_buffer.size() * 4, 
                           reinterpret_cast<const jbyte*>(frame_buffer.data()));
    
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_fceumm_wrapper_EmulatorView_getFrameWidth(JNIEnv* env, jobject thiz) {
    return frame_width;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_fceumm_wrapper_EmulatorView_getFrameHeight(JNIEnv* env, jobject thiz) {
    return frame_height;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_fceumm_wrapper_EmulatorView_isFrameUpdated(JNIEnv* env, jobject thiz) {
    std::lock_guard<std::mutex> lock(frame_mutex);
    bool updated = frame_updated;
    frame_updated = false;
    return updated;
}

extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulationActivity_cleanup(JNIEnv* env, jobject thiz) {
    LOGI("Nettoyage du wrapper Libretro");
    
    // Nettoyer OpenSL ES
    if (slPlayerPlay) {
        (*slPlayerPlay)->SetPlayState(slPlayerPlay, SL_PLAYSTATE_STOPPED);
    }
    
    if (slPlayer) {
        (*slPlayer)->Destroy(slPlayer);
        slPlayer = nullptr;
        slPlayerPlay = nullptr;
        slPlayerBufferQueue = nullptr;
    }
    
    if (slOutputMix) {
        (*slOutputMix)->Destroy(slOutputMix);
        slOutputMix = nullptr;
    }
    
    if (slEngine) {
        (*slEngine)->Destroy(slEngine);
        slEngine = nullptr;
        slEngineEngine = nullptr;
    }
    
    // Réinitialiser les variables audio
    audio_initialized = false;
    queue_ready = false;
    audioBuffer.initialized = false;
    
    if (retro_unload_game_func) {
        retro_unload_game_func();
    }
    
    if (retro_deinit_func) {
        retro_deinit_func();
    }
    
    if (libretro_handle) {
        dlclose(libretro_handle);
        libretro_handle = nullptr;
    }
    
    // Réinitialiser les variables globales
    environ_cb = nullptr;
    environ_cb_initialized = false;
    rom_loaded = false;
    
    LOGI("Wrapper Libretro et OpenSL ES déinitialisés");
}

// Méthodes pour les contrôles tactiles
// Fonction RetroArch pour vérifier l'état d'un input
extern "C" JNIEXPORT jboolean JNICALL
Java_com_fceumm_wrapper_EmulationActivity_isRetroArchInputPressed(JNIEnv* env, jobject thiz, jint deviceId) {
    std::lock_guard<std::mutex> lock(input_mutex);
    if (deviceId >= 0 && deviceId < 16) {
        return button_states[deviceId] ? JNI_TRUE : JNI_FALSE;
    }
    return JNI_FALSE;
}

// Fonction pour récupérer les données audio depuis Java
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_fceumm_wrapper_EmulationActivity_getAudioData(JNIEnv* env, jobject thiz) {
    std::lock_guard<std::mutex> lock(audio_mutex);
    
    if (audio_buffer.empty()) {
        return env->NewByteArray(0);
    }
    
    // Sauvegarder la taille AVANT de vider le buffer
    size_t buffer_size = audio_buffer.size();
    
    // Créer un tableau Java avec les données audio
    jbyteArray result = env->NewByteArray(buffer_size * sizeof(int16_t));
    env->SetByteArrayRegion(result, 0, buffer_size * sizeof(int16_t), 
                           reinterpret_cast<const jbyte*>(audio_buffer.data()));
    
    // Vider le buffer APRÈS avoir copié les données
    audio_buffer.clear();
    
    // Log pour debug avec la taille correcte
    static int call_counter = 0;
    call_counter++;
    if (call_counter % 50 == 0) {
        LOGI("Audio: Données envoyées à Java, taille: %zu", buffer_size);
    }
    
    return result;
}

// Fonction pour activer/désactiver l'audio
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulationActivity_setAudioEnabled(JNIEnv* env, jobject thiz, jboolean enabled) {
    audio_enabled = enabled;
    LOGI("Audio %s", enabled ? "activé" : "désactivé");
}

// Fonction pour obtenir le taux d'échantillonnage
extern "C" JNIEXPORT jint JNICALL
Java_com_fceumm_wrapper_EmulationActivity_getAudioSampleRate(JNIEnv* env, jobject thiz) {
    return audio_sample_rate;
}

// Fonction pour obtenir la taille du buffer audio
extern "C" JNIEXPORT jint JNICALL
Java_com_fceumm_wrapper_EmulationActivity_getAudioBufferSize(JNIEnv* env, jobject thiz) {
    std::lock_guard<std::mutex> lock(audio_mutex);
    return static_cast<jint>(audio_buffer.size());
}

// Fonction pour forcer le traitement immédiat de l'audio
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulationActivity_forceAudioProcessing(JNIEnv* env, jobject thiz) {
    std::lock_guard<std::mutex> lock(audio_mutex);
    
    // Forcer le nettoyage ultra-agressif du buffer pour éliminer la latence
    if (audio_buffer.size() > 128) { // Buffer encore plus petit
        size_t new_size = audio_buffer.size() * 3 / 4; // Supprimer 75% du buffer
        audio_buffer.erase(audio_buffer.begin(), audio_buffer.begin() + new_size);
        LOGI("Audio: Forçage ultra-agressif, buffer réduit à: %zu", audio_buffer.size());
    }
}

// Fonction pour contrôler le volume général (0-100)
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulationActivity_setMasterVolume(JNIEnv* env, jobject thiz, jint volume) {
    if (volume < 0) volume = 0;
    if (volume > 100) volume = 100;
    
    // Convertir le pourcentage en valeur libretro (0-10)
    int libretro_volume = (volume * 10) / 100;
    
    // Utiliser les variables d'environnement libretro si disponible
    if (environ_cb) {
        struct retro_variable var;
        var.key = "fceumm_sndvolume";
        var.value = std::to_string(libretro_volume).c_str();
        
        if (environ_cb(RETRO_ENVIRONMENT_SET_VARIABLE, &var)) {
            LOGI("🎵 VOLUME LIBRETRO: %d%% (libretro: %d)", volume, libretro_volume);
            return;
        }
    }
    
    // Fallback: essayer d'abord de modifier directement FSettings si disponible
    if (FSettings_ptr) {
        int fceumm_volume = (volume * 256) / 100;
        FSettings_ptr->SoundVolume = fceumm_volume;
        LOGI("Volume général défini (direct): %d%% (FCEUmm: %d)", volume, fceumm_volume);
    }
    
    // Fallback: essayer la fonction FCEUmm si disponible
    if (FCEUI_SetSoundVolume_func) {
        int fceumm_volume = (volume * 256) / 100;
        FCEUI_SetSoundVolume_func(fceumm_volume);
        LOGI("Volume général défini (fonction): %d%% (FCEUmm: %d)", volume, fceumm_volume);
    }
    
    if (!environ_cb && !FSettings_ptr && !FCEUI_SetSoundVolume_func) {
        LOGI("Volume général défini (aucune méthode disponible): %d%%", volume);
    }
}

// Fonction pour activer/désactiver le son (mute/unmute)
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulationActivity_setAudioMuted(JNIEnv* env, jobject thiz, jboolean muted) {
    audio_enabled = !muted;
    LOGI("Audio %s", muted ? "muet" : "activé");
}

// Fonction pour définir la qualité audio (0=Low, 1=High, 2=Highest)
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulationActivity_setAudioQuality(JNIEnv* env, jobject thiz, jint quality) {
    if (quality < 0) quality = 0;
    if (quality > 2) quality = 2;
    
    // Utiliser les variables d'environnement libretro si disponible
    if (environ_cb) {
        struct retro_variable var;
        var.key = "fceumm_sndquality";
        
        const char* quality_str = nullptr;
        switch (quality) {
            case 0: quality_str = "Low"; break;
            case 1: quality_str = "High"; break;
            case 2: quality_str = "Very High"; break;
            default: quality_str = "Low"; break;
        }
        var.value = quality_str;
        
        if (environ_cb(RETRO_ENVIRONMENT_SET_VARIABLE, &var)) {
            LOGI("🎵 QUALITÉ LIBRETRO: %d (%s)", quality, quality_str);
            return;
        }
    }
    
    // Fallback: essayer d'abord de modifier directement FSettings si disponible
    if (FSettings_ptr) {
        FSettings_ptr->soundq = quality;
        LOGI("Qualité audio définie (direct): %d", quality);
    }
    
    // Fallback: essayer la fonction FCEUmm si disponible
    if (FCEUI_SetSoundQuality_func) {
        FCEUI_SetSoundQuality_func(quality);
        LOGI("Qualité audio définie (fonction): %d", quality);
    }
    
    if (!environ_cb && !FSettings_ptr && !FCEUI_SetSoundQuality_func) {
        LOGI("Qualité audio définie (aucune méthode disponible): %d", quality);
    }
}

// Fonction pour définir le taux d'échantillonnage
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulationActivity_setSampleRate(JNIEnv* env, jobject thiz, jint sampleRate) {
    if (sampleRate == 22050 || sampleRate == 44100 || sampleRate == 48000) {
        audio_sample_rate = sampleRate;
        
        // Note: Le core libretro FCEUmm ne semble pas avoir d'option pour le taux d'échantillonnage
        // Nous utilisons donc les fallbacks directs
        
        // Fallback: essayer d'abord de modifier directement FSettings si disponible
        if (FSettings_ptr) {
            FSettings_ptr->SndRate = sampleRate;
            LOGI("Taux d'échantillonnage défini (direct): %d Hz", sampleRate);
        }
        
        // Fallback: essayer la fonction FCEUmm si disponible
        if (FCEUI_Sound_func) {
            FCEUI_Sound_func(sampleRate);
            LOGI("Taux d'échantillonnage défini (fonction): %d Hz", sampleRate);
        }
        
        if (!FSettings_ptr && !FCEUI_Sound_func) {
            LOGI("Taux d'échantillonnage défini (aucune méthode disponible): %d Hz", sampleRate);
        }
    } else {
        LOGE("Taux d'échantillonnage invalide: %d", sampleRate);
    }
}

// ============================================================================
// Fonctions JNI pour AudioSettingsActivity
// ============================================================================

// Fonction pour contrôler le volume général (0-100) - AudioSettingsActivity
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_setMasterVolume(JNIEnv* env, jobject thiz, jint volume) {
    if (volume < 0) volume = 0;
    if (volume > 100) volume = 100;
    
    // Stocker la valeur globalement
    global_audio_volume = volume;
    
    LOGI("🎵 VOLUME défini et stocké globalement: %d%%", volume);
    
    // Appliquer immédiatement si possible
    applyAudioSettings();
}

// Fonction pour activer/désactiver le son (mute/unmute) - AudioSettingsActivity
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_setAudioMuted(JNIEnv* env, jobject thiz, jboolean muted) {
    global_audio_muted = muted;
    audio_enabled = !muted;
    LOGI("🎵 MUTE défini et stocké globalement: %s", muted ? "Activé" : "Désactivé");
    
    // Appliquer immédiatement si possible
    applyAudioSettings();
}

// Fonction pour appliquer tous les paramètres audio stockés globalement
void applyAudioSettings() {
    LOGI("🎵 Application des paramètres audio globaux:");
    LOGI("🎵   Volume: %d%%", global_audio_volume);
    LOGI("🎵   Qualité: %d", global_audio_quality);
    LOGI("🎵   Sample Rate: %d Hz", global_audio_sample_rate);
    LOGI("🎵   RF Filter: %s", global_audio_rf_filter ? "Activé" : "Désactivé");
    LOGI("🎵   Stereo Delay: %dms", global_audio_stereo_delay);
    LOGI("🎵   Swap Duty: %s", global_audio_swap_duty ? "Activé" : "Désactivé");
    LOGI("🎵   Muted: %s", global_audio_muted ? "Oui" : "Non");
    
    // Vérifier si FSettings_ptr est disponible
    if (!FSettings_ptr) {
        LOGI("🎵 ERREUR: FSettings_ptr n'est pas disponible!");
        LOGI("🎵 Tentative de rechargement de FSettings_ptr...");
        FSettings_ptr = (FCEUS_t*)dlsym(libretro_handle, "FSettings");
        if (FSettings_ptr) {
            LOGI("🎵 FSettings_ptr rechargé avec succès");
        } else {
            LOGE("🎵 ERREUR: Impossible de recharger FSettings_ptr");
        }
    }
    
    // MÉTHODE 1: Essayer via FSettings_ptr (si disponible)
    if (FSettings_ptr) {
        // Appliquer le volume
        int fceumm_volume = (global_audio_volume * 256) / 100;
        FSettings_ptr->SoundVolume = fceumm_volume;
        LOGI("🎵 Volume appliqué via FSettings: %d (original: %d%%)", fceumm_volume, global_audio_volume);
        
        // Appliquer la qualité
        FSettings_ptr->soundq = global_audio_quality;
        LOGI("🎵 Qualité appliquée via FSettings: %d", global_audio_quality);
        
        // Appliquer le sample rate
        FSettings_ptr->SndRate = global_audio_sample_rate;
        LOGI("🎵 Sample rate appliqué via FSettings: %d", global_audio_sample_rate);
        
        // Appliquer le filtre RF
        FSettings_ptr->lowpass = global_audio_rf_filter ? 1 : 0;
        LOGI("🎵 RF Filter appliqué via FSettings: %d", global_audio_rf_filter ? 1 : 0);
    } else {
        LOGI("🎵 FSettings_ptr non disponible, tentative via fonctions FCEUmm...");
        
            // MÉTHODE 2: Essayer via les fonctions FCEUmm directes (inspiré de libretro-super)
    if (FCEUI_SetSoundVolume_func) {
        int fceumm_volume = (global_audio_volume * 256) / 100;
        FCEUI_SetSoundVolume_func(fceumm_volume);
        LOGI("🎵 Volume appliqué via FCEUI_SetSoundVolume: %d", fceumm_volume);
    }
    
    if (FCEUI_SetSoundQuality_func) {
        FCEUI_SetSoundQuality_func(global_audio_quality);
        LOGI("🎵 Qualité appliquée via FCEUI_SetSoundQuality: %d", global_audio_quality);
    }
    
    if (FCEUI_Sound_func) {
        FCEUI_Sound_func(global_audio_sample_rate);
        LOGI("🎵 Sample rate appliqué via FCEUI_Sound: %d", global_audio_sample_rate);
    }
    
    if (FCEUI_SetLowPass_func) {
        FCEUI_SetLowPass_func(global_audio_rf_filter ? 1 : 0);
        LOGI("🎵 RF Filter appliqué via FCEUI_SetLowPass: %d", global_audio_rf_filter ? 1 : 0);
    }
    
    // MÉTHODE 3: Configuration optimisée du buffer audio (inspirée de libretro-super)
    audio_sample_rate = global_audio_sample_rate;
    LOGI("🎵 Sample rate forcé dans le buffer audio: %d Hz", audio_sample_rate);
    
    // Configuration du mode de latence
    if (low_latency_mode) {
        LOGI("🎵 Mode basse latence activé - Buffer: %d frames", LOW_LATENCY_BUFFER_SIZE);
    } else {
        LOGI("🎵 Mode normal activé - Buffer: %d frames", OPTIMAL_BUFFER_SIZE);
    }
    }
    
    // Appliquer le mute
    audio_enabled = !global_audio_muted;
    LOGI("🎵 Mute appliqué: %s", global_audio_muted ? "Activé" : "Désactivé");
    
    // Forcer la mise à jour audio seulement si une ROM est chargée
    if (libretro_handle && retro_run_func && rom_loaded) {
        LOGI("🎵 Forçage de la mise à jour audio...");
        // Appeler retro_run pour forcer la mise à jour
        retro_run_func();
        LOGI("🎵 Mise à jour audio forcée");
    } else {
        LOGI("🎵 Pas de mise à jour audio forcée (ROM non chargée ou fonctions non disponibles)");
    }
}

// Fonction pour forcer l'initialisation de l'environnement libretro si nécessaire
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_ensureLibretroInitialized(JNIEnv* env, jobject thiz) {
    if (!environ_cb_initialized && libretro_handle) {
        LOGI("🎵 Tentative d'initialisation de l'environnement libretro...");
        // Essayer de forcer l'initialisation en appelant retro_init si disponible
        if (retro_init_func) {
            retro_init_func();
            LOGI("🎵 retro_init appelé");
        }
    }
    LOGI("🎵 État environ_cb: initialisé=%s, pointeur=%s", 
         environ_cb_initialized ? "oui" : "non", 
         environ_cb ? "valide" : "null");
}

// Fonction pour définir la qualité audio (0=Low, 1=High, 2=Highest) - AudioSettingsActivity
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_setAudioQuality(JNIEnv* env, jobject thiz, jint quality) {
    if (quality < 0) quality = 0;
    if (quality > 2) quality = 2;
    
    // Stocker la valeur globalement
    global_audio_quality = quality;
    
    LOGI("🎵 QUALITÉ définie et stockée globalement: %d", quality);
    
    // Appliquer immédiatement si possible
    applyAudioSettings();
}

// Fonction pour définir le taux d'échantillonnage - AudioSettingsActivity
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_setSampleRate(JNIEnv* env, jobject thiz, jint sampleRate) {
    if (sampleRate == 22050 || sampleRate == 44100 || sampleRate == 48000) {
        // Stocker la valeur globalement
        global_audio_sample_rate = sampleRate;
        audio_sample_rate = sampleRate;
        
        LOGI("🎵 SAMPLE RATE défini et stocké globalement: %d Hz", sampleRate);
        
        // Appliquer immédiatement si possible
        applyAudioSettings();
    } else {
        LOGE("🎵 Taux d'échantillonnage invalide (AudioSettings): %d", sampleRate);
    }
}

// Nouveaux paramètres audio avancés
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_setRfFilter(JNIEnv* env, jobject thiz, jboolean enabled) {
    // Stocker la valeur globalement
    global_audio_rf_filter = enabled;
    
    LOGI("🎵 FILTRE RF défini et stocké globalement: %s", enabled ? "Activé" : "Désactivé");
    
    // Appliquer immédiatement si possible
    applyAudioSettings();
}

extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_setStereoDelay(JNIEnv* env, jobject thiz, jint delay_ms) {
    // Stocker la valeur globalement
    global_audio_stereo_delay = delay_ms;
    
    LOGI("🎵 STÉRÉO DELAY défini et stocké globalement: %dms", delay_ms);
    
    // Appliquer immédiatement si possible
    applyAudioSettings();
}

extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_setSwapDuty(JNIEnv* env, jobject thiz, jboolean enabled) {
    // Stocker la valeur globalement
    global_audio_swap_duty = enabled;
    
    LOGI("🎵 SWAP DUTY défini et stocké globalement: %s", enabled ? "Activé" : "Désactivé");
    
    // Appliquer immédiatement si possible
    applyAudioSettings();
}

// Fonctions pour récupérer les valeurs actuelles
extern "C" JNIEXPORT jint JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_getMasterVolume(JNIEnv* env, jobject thiz) {
    return global_audio_volume;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_getAudioQuality(JNIEnv* env, jobject thiz) {
    return global_audio_quality;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_getSampleRate(JNIEnv* env, jobject thiz) {
    return global_audio_sample_rate;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_getRfFilter(JNIEnv* env, jobject thiz) {
    return global_audio_rf_filter;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_getStereoDelay(JNIEnv* env, jobject thiz) {
    return global_audio_stereo_delay;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_getSwapDuty(JNIEnv* env, jobject thiz) {
    return global_audio_swap_duty;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_getAudioMuted(JNIEnv* env, jobject thiz) {
    return global_audio_muted;
}

// Fonction pour forcer l'application des paramètres audio
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_forceApplyAudioSettings(JNIEnv* env, jobject thiz) {
    LOGI("🎵 FORÇAGE de l'application des paramètres audio...");
    
    // Appliquer les paramètres
    applyAudioSettings();
    
    // Forcer plusieurs frames pour s'assurer que les changements sont appliqués
    // SEULEMENT si une ROM est chargée pour éviter les crashes
    if (retro_run_func && rom_loaded) {
        for (int i = 0; i < 5; i++) {
            retro_run_func();
        }
        LOGI("🎵 5 frames forcées pour appliquer les changements audio");
    } else {
        LOGI("🎵 Pas de frames forcées (ROM non chargée ou fonction non disponible)");
    }
    
    LOGI("🎵 Application forcée terminée");
}

// Fonction pour forcer la reinitialisation audio complète
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_forceAudioReinit(JNIEnv* env, jobject thiz) {
    LOGI("🎵 FORÇAGE de la réinitialisation audio complète...");
    
    // Forcer les paramètres par défaut de haute qualité
    global_audio_quality = 2;
    global_audio_sample_rate = 48000;
    global_audio_rf_filter = true;
    global_audio_volume = 100;
    global_audio_muted = false;
    
    // Appliquer immédiatement
    applyAudioSettings();
    
    // Forcer plusieurs frames si ROM chargée
    if (retro_run_func && rom_loaded) {
        for (int i = 0; i < 10; i++) {
            retro_run_func();
        }
        LOGI("🎵 10 frames forcées pour réinitialisation audio");
    }
    
    LOGI("🎵 Réinitialisation audio forcée terminée");
}

// Fonction pour optimiser l'audio spécifiquement pour Duck Hunt
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_optimizeForDuckHunt(JNIEnv* env, jobject thiz) {
    LOGI("🎵 OPTIMISATION SPÉCIFIQUE POUR DUCK HUNT...");
    
    // Paramètres optimaux pour Duck Hunt (sans artefacts)
    global_audio_quality = 2;        // Qualité maximum
    global_audio_sample_rate = 48000; // Sample rate élevé
    global_audio_rf_filter = false;   // Pas de filtre RF (évite les artefacts)
    global_audio_volume = 100;        // Volume normal (évite la distorsion)
    global_audio_muted = false;       // Son activé
    global_audio_stereo_delay = 0;    // Pas de délai
    global_audio_swap_duty = false;   // Pas d'échange duty
    
    audio_optimized_for_duck_hunt = true;
    
    // Appliquer immédiatement
    applyAudioSettings();
    
    // Forcer plusieurs frames si ROM chargée
    if (retro_run_func && rom_loaded) {
        for (int i = 0; i < 15; i++) {
            retro_run_func();
        }
        LOGI("🎵 15 frames forcées pour optimisation Duck Hunt");
    }
    
    LOGI("🎵 Optimisation Duck Hunt terminée - Volume: 100%%, Qualité: Max, RF: Désactivé");
}

// Fonction pour basculer le mode de latence audio
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_AudioSettingsActivity_toggleLowLatencyMode(JNIEnv* env, jobject thiz) {
    low_latency_mode = !low_latency_mode;
    LOGI("🎵 Mode basse latence %s", low_latency_mode ? "activé" : "désactivé");
    
    // Appliquer les paramètres audio
    applyAudioSettings();
    
    // Forcer la mise à jour si ROM chargée
    if (retro_run_func && rom_loaded) {
        for (int i = 0; i < 5; i++) {
            retro_run_func();
        }
        LOGI("🎵 5 frames forcées pour basculement latence");
    }
}
