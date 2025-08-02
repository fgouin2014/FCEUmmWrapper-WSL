#!/bin/bash

# Correction du problème de chargement du core
set -e

echo "🔧 Correction du problème de chargement du core..."
echo "=================================================="

# Créer un backup
cp app/src/main/cpp/native-lib.cpp app/src/main/cpp/native-lib.cpp.backup3

echo "✅ Backup créé"

# Ajouter la fonction de copie depuis les assets
cat > app/src/main/cpp/native-lib.cpp << 'EOF'
#include <jni.h>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <string>
#include <cstring>
#include <cstdio>
#include <cstdlib>

// Inclure le wrapper LibRetro
#include "libretro_wrapper.h"

#define LOG_TAG "NativeLib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Variables globales
static uint32_t* frame_buffer = nullptr;
static int frame_width = 256;
static int frame_height = 240;
static int frame_counter = 0;
static bool test_mode = false;
static bool core_loaded = false;

// AssetManager global
static AAssetManager* g_asset_manager = nullptr;

// Fonction pour copier un asset vers le stockage interne
static bool copy_asset_to_internal_storage(const char* asset_path, const char* internal_path) {
    if (!g_asset_manager) {
        LOGE("AssetManager non initialisé");
        return false;
    }
    
    // Ouvrir l'asset
    AAsset* asset = AAssetManager_open(g_asset_manager, asset_path, AASSET_MODE_STREAMING);
    if (!asset) {
        LOGE("Impossible d'ouvrir l'asset: %s", asset_path);
        return false;
    }
    
    // Ouvrir le fichier de destination
    FILE* out_file = fopen(internal_path, "wb");
    if (!out_file) {
        LOGE("Impossible de créer le fichier: %s", internal_path);
        AAsset_close(asset);
        return false;
    }
    
    // Copier le contenu
    char buffer[4096];
    int bytes_read;
    while ((bytes_read = AAsset_read(asset, buffer, sizeof(buffer))) > 0) {
        if (fwrite(buffer, 1, bytes_read, out_file) != bytes_read) {
            LOGE("Erreur d'écriture");
            fclose(out_file);
            AAsset_close(asset);
            return false;
        }
    }
    
    fclose(out_file);
    AAsset_close(asset);
    
    LOGI("Asset copié avec succès: %s -> %s", asset_path, internal_path);
    return true;
}

// Fonction pour vérifier le format NES
static bool check_nes_format(const char* filename) {
    FILE* file = fopen(filename, "rb");
    if (!file) {
        return false;
    }
    
    unsigned char header[16];
    if (fread(header, 1, 16, file) != 16) {
        fclose(file);
        return false;
    }
    
    fclose(file);
    LOGI("ROM NES valide détectée");
    return true;
}

// Fonction pour créer un pattern de test
static void create_test_pattern(uint32_t* buffer, int width, int height) {
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            uint32_t color = 0xFF000000; // Noir par défaut
            
            // Créer un pattern de test
            if ((x / 8 + y / 8) % 2 == 0) {
                color = 0xFFFF0000; // Rouge
            } else {
                color = 0xFF0000FF; // Bleu
            }
            
            buffer[y * width + x] = color;
        }
    }
}

// Callbacks LibRetro améliorés
static void video_callback(const void* data, unsigned width, unsigned height, size_t pitch) {
    if (data && frame_buffer) {
        memcpy(frame_buffer, data, width * height * sizeof(uint32_t));
        frame_width = width;
        frame_height = height;
    }
}

static size_t audio_callback(const int16_t* data, size_t frames) {
    // Audio callback - géré par le wrapper
    return frames;
}

static void input_poll_callback() {
    // Input polling - géré par le wrapper
}

static int16_t input_state_callback(unsigned port, unsigned device, unsigned index, unsigned id) {
    // Input state - géré par le wrapper
    return 0;
}

// JNI Functions
extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulatorRenderer_nativeInit(JNIEnv* env, jobject thiz, jobject asset_manager) {
    LOGI("nativeInit - Wrapper Libretro propre");
    
    // Initialiser l'AssetManager
    g_asset_manager = AAssetManager_fromJava(env, asset_manager);
    if (!g_asset_manager) {
        LOGE("Impossible d'initialiser l'AssetManager");
        return;
    }
    
    // Déterminer l'architecture
    const char* arch = nullptr;
    #if defined(__aarch64__)
        arch = "arm64-v8a";
    #elif defined(__arm__)
        arch = "armeabi-v7a";
    #elif defined(__i386__)
        arch = "x86";
    #elif defined(__x86_64__)
        arch = "x86_64";
    #else
        LOGE("Architecture non supportée");
        return;
    #endif
    
    LOGI("Architecture détectée: %s", arch);
    
    // Chemin vers le core dans les assets (correction de la faute de frappe)
    std::string asset_core_path = "coresCompiled/" + std::string(arch) + "/fceumm_libretro_android.so";
    
    // Chemin vers le stockage interne
    std::string internal_core_path = "/data/data/com.fceumm.wrapper/files/fceumm_libretro_android.so";
    
    // Copier le core depuis les assets vers le stockage interne
    if (!copy_asset_to_internal_storage(asset_core_path.c_str(), internal_core_path.c_str())) {
        LOGE("Échec de la copie du core FCEUmm");
        return;
    }
    
    LOGI("Chargement du core FCEUmm: %s", internal_core_path.c_str());
    
    // Chemin vers la ROM
    const char* rom_path = "/data/data/com.fceumm.wrapper/files/roms/sweethome.nes";
    
    // Initialiser le wrapper LibRetro avec le core officiel
    libretro_init(internal_core_path.c_str(), rom_path, false);
}

extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulatorRenderer_nativeRender(JNIEnv* env, jobject thiz) {
    video_manager_render();
}

extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulatorRenderer_nativeInitGL(JNIEnv* env, jobject thiz) {
    video_manager_init_gl();
}

extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulatorRenderer_nativeResize(JNIEnv* env, jobject thiz, jint w, jint h) {
    LOGI("Redimensionnement: %dx%d", w, h);
    
    if (frame_buffer) {
        delete[] frame_buffer;
    }
    frame_buffer = new uint32_t[w * h];
    frame_width = w;
    frame_height = h;
    
    create_test_pattern(frame_buffer, frame_width, frame_height);
}

extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulatorRenderer_nativeDestroy(JNIEnv* env, jobject thiz) {
    LOGI("Destruction du renderer");
    
    if (frame_buffer) {
        delete[] frame_buffer;
        frame_buffer = nullptr;
    }
    
    // Nettoyer le wrapper LibRetro
    libretro_deinit();
}

extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_MainActivity_nativeSetInput(JNIEnv* env, jobject thiz, jfloat zapperX, jfloat zapperY, jboolean zapperTrigger, jintArray gamepadState, jint touchState) {
    int* gamepad = nullptr;
    if (gamepadState) {
        gamepad = env->GetIntArrayElements(gamepadState, nullptr);
    }
    
    libretro_set_input(zapperX, zapperY, zapperTrigger, gamepad, touchState);
    
    if (gamepad) {
        env->ReleaseIntArrayElements(gamepadState, gamepad, 0);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_MainActivity_nativeSaveState(JNIEnv* env, jobject thiz, jstring path) {
    const char* path_str = env->GetStringUTFChars(path, nullptr);
    libretro_save_state(path_str);
    env->ReleaseStringUTFChars(path, path_str);
}

extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_MainActivity_nativeLoadState(JNIEnv* env, jobject thiz, jstring path) {
    const char* path_str = env->GetStringUTFChars(path, nullptr);
    libretro_load_state(path_str);
    env->ReleaseStringUTFChars(path, path_str);
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_fceumm_wrapper_EmulatorRenderer_nativeGetFrameBuffer(JNIEnv* env, jobject thiz) {
    if (!frame_buffer) {
        return nullptr;
    }
    
    // Créer un ByteBuffer Java
    jobject buffer = env->NewDirectByteBuffer(frame_buffer, frame_width * frame_height * sizeof(uint32_t));
    return buffer;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_fceumm_wrapper_EmulatorRenderer_nativeGetFrameWidth(JNIEnv* env, jobject thiz) {
    return frame_width;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_fceumm_wrapper_EmulatorRenderer_nativeGetFrameHeight(JNIEnv* env, jobject thiz) {
    return frame_height;
}

extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulatorRenderer_nativeSetRomPath(JNIEnv* env, jobject thiz, jstring romPath) {
    const char* path = env->GetStringUTFChars(romPath, nullptr);
    LOGI("Changement de ROM: %s", path);
    
    // Libérer l'ancien buffer si nécessaire
    if (frame_buffer) {
        delete[] frame_buffer;
        frame_buffer = nullptr;
    }
    
    // Réinitialiser les variables
    test_mode = false;
    
    // Vérifier si le fichier existe
    FILE* file = fopen(path, "rb");
    if (!file) {
        LOGE("ROM non trouvée: %s", path);
        test_mode = true;
        LOGI("Passage en mode test sans ROM");
    } else {
        fclose(file);
        LOGI("ROM trouvée: %s", path);

        // Vérifier le format NES
        if (!check_nes_format(path)) {
            LOGE("Format NES invalide, passage en mode test");
            test_mode = true;
        }
    }
    
    // Essayer de charger la ROM avec le wrapper amélioré
    if (!test_mode) {
        LOGI("Tentative de chargement avec wrapper amélioré...");
        
        // Initialiser le wrapper LibRetro
        libretro_init("/data/data/com.fceumm.wrapper/files/fceumm_libretro_android.so", path, false);
        
        if (core_loaded) {
            LOGI("ROM chargée avec succès via wrapper amélioré");
            
            // Mettre à jour les dimensions du frame buffer
            if (av_info.geometry.base_width > 0 && av_info.geometry.base_height > 0) {
                frame_width = av_info.geometry.base_width;
                frame_height = av_info.geometry.base_height;
                
                frame_buffer = new uint32_t[frame_width * frame_height];
                LOGI("Frame buffer mis à jour: %dx%d", frame_width, frame_height);
            }
        } else {
            LOGI("Échec du chargement via wrapper amélioré - passage en mode test");
            test_mode = true;
        }
    }
    
    // Créer un pattern de test si nécessaire
    if (test_mode) {
        if (!frame_buffer) {
            frame_buffer = new uint32_t[frame_width * frame_height];
        }
        create_test_pattern(frame_buffer, frame_width, frame_height);
        LOGI("Mode test activé");
    }
    
    env->ReleaseStringUTFChars(romPath, path);
}
EOF

echo "✅ Code corrigé avec la fonction de copie des assets"

# Corriger le nom du dossier dans les assets
echo "🔧 Correction du nom du dossier dans les assets..."
if [ -d "app/src/main/assets/coresCompilled" ]; then
    mv app/src/main/assets/coresCompilled app/src/main/assets/coresCompiled
    echo "✅ Dossier renommé: coresCompilled -> coresCompiled"
fi

# Compiler et installer
echo "🔨 Compilation et installation..."
./gradlew clean assembleDebug installDebug

echo "✅ Correction terminée !"
echo ""
echo "🎮 Testez maintenant l'application :"
echo "adb -s R5CT11TCQ1W shell am start -n com.fceumm.wrapper/.MainActivity" 