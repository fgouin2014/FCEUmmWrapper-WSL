#include <jni.h>
#include <string>
#include <android/log.h>
#include <dlfcn.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fstream>
#include <vector>
#include <mutex>

#define LOG_TAG "LibretroWrapper"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

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

// Video buffer
std::vector<uint32_t> frame_buffer;
int frame_width = 256;
int frame_height = 240;
std::mutex frame_mutex;
bool frame_updated = false;

// Callback functions
bool environment_callback(unsigned cmd, void* data) {
    LOGI("Environment callback: cmd=%u", cmd);
    return false;
}

void video_refresh_callback(const void* data, unsigned width, unsigned height, size_t pitch) {
    if (!data) return;
    
    std::lock_guard<std::mutex> lock(frame_mutex);
    
    // Mettre à jour les dimensions si nécessaire
    if (width != frame_width || height != frame_height) {
        frame_width = width;
        frame_height = height;
        frame_buffer.resize(width * height);
        LOGI("Dimensions vidéo mises à jour: %dx%d, pitch: %zu", width, height, pitch);
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
    LOGI("Frame vidéo mise à jour: %dx%d (RGB565->RGBA8888)", width, height);
}

size_t audio_sample_batch_callback(const int16_t* data, size_t frames) {
    // Audio callback - sera implémenté plus tard
    return frames;
}

void input_poll_callback() {
    // Input poll callback - sera implémenté plus tard
}

int16_t input_state_callback(unsigned port, unsigned device, unsigned index, unsigned id) {
    // Input state callback - sera implémenté plus tard
    return 0;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_fceumm_wrapper_MainActivity_initLibretro(JNIEnv* env, jobject thiz) {
    LOGI("Initialisation du wrapper Libretro");
    
    // Charger le core libretro
    const char* core_path = "/data/data/com.fceumm.wrapper/files/fceumm_libretro_android.so";
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
    
    if (!retro_init_func || !retro_load_game_func || !retro_run_func) {
        LOGE("Fonctions libretro manquantes");
        return JNI_FALSE;
    }
    
    LOGI("Fonctions libretro récupérées avec succès");
    
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
    
    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_fceumm_wrapper_MainActivity_loadROM(JNIEnv* env, jobject thiz, jstring rom_path) {
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
        
        // Récupérer les informations vidéo
        if (retro_get_system_av_info_func) {
            retro_system_av_info av_info;
            retro_get_system_av_info_func(&av_info);
            frame_width = av_info.geometry.width;
            frame_height = av_info.geometry.height;
            frame_buffer.resize(frame_width * frame_height);
            LOGI("Informations vidéo: %dx%d", frame_width, frame_height);
        }
        
        return JNI_TRUE;
    } else {
        LOGE("Échec du chargement de la ROM");
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_MainActivity_runFrame(JNIEnv* env, jobject thiz) {
    if (retro_run_func) {
        retro_run_func();
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
    return frame_updated;
}

extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_MainActivity_cleanup(JNIEnv* env, jobject thiz) {
    LOGI("Nettoyage du wrapper Libretro");
    
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
    
    LOGI("Wrapper Libretro déinitialisé");
}
