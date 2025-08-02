#!/bin/bash

# Script d'intégration du wrapper LibRetro amélioré
# Remplace le wrapper existant par la version corrigée

set -e

echo "=== Intégration du wrapper LibRetro amélioré ==="

# Vérifier que nous sommes dans le bon répertoire
if [ ! -f "app/build.gradle" ]; then
    echo "❌ Répertoire de projet Android non trouvé"
    exit 1
fi

# Sauvegarder l'ancien wrapper
if [ -f "app/src/main/cpp/native-lib.cpp" ]; then
    echo "📦 Sauvegarde de l'ancien wrapper..."
    cp app/src/main/cpp/native-lib.cpp app/src/main/cpp/native-lib.cpp.backup
    echo "✅ Sauvegarde créée: native-lib.cpp.backup"
fi

# Intégrer le nouveau wrapper dans native-lib.cpp
echo "🔧 Intégration du wrapper amélioré..."

# Créer un nouveau native-lib.cpp qui utilise notre wrapper
cat > app/src/main/cpp/native-lib.cpp << 'EOF'
#include <jni.h>
#include <string>
#include <android/log.h>
#include <cstdio>
#include <stdint.h>
#include <cstring>
#include <cstdlib>
#include <unistd.h>
#include <sys/stat.h>
#include <cstdarg>
#include <dlfcn.h>

// Inclure notre wrapper amélioré
#include "libretro_wrapper.h"

#define LOG_TAG "FCEUmmWrapper"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

// Variables globales
static bool core_loaded = false;
static bool test_mode = true;
static uint32_t* frame_buffer = nullptr;
static int frame_width = 256;
static int frame_height = 240;
static int frame_counter = 0;

// Structures libretro
static struct retro_system_av_info av_info;

// Variables globales pour stocker les informations de jeu
static struct retro_game_info_ext* current_game_info_ext = nullptr;
static char current_game_path[2048] = {0};
static const void* current_game_data = nullptr;
static size_t current_game_size = 0;

// Fonction pour vérifier le format NES
static bool check_nes_format(const char* filename) {
    FILE* file = fopen(filename, "rb");
    if (!file) {
        LOGE("Impossible d'ouvrir le fichier: %s", filename);
        return false;
    }
    
    // Lire l'en-tête NES (16 bytes)
    unsigned char header[16];
    if (fread(header, 1, 16, file) != 16) {
        fclose(file);
        LOGE("Impossible de lire l'en-tête NES");
        return false;
    }
    
    // Vérifier la signature NES
    if (header[0] != 0x4E || header[1] != 0x45 || header[2] != 0x53 || header[3] != 0x1A) {
        fclose(file);
        LOGE("Signature NES invalide: %02X %02X %02X %02X", header[0], header[1], header[2], header[3]);
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
Java_com_fceumm_wrapper_EmulatorRenderer_nativeInit(JNIEnv* env, jobject thiz) {
    LOGI("Initialisation du renderer");
    
    // Initialiser le frame buffer
    if (frame_buffer) {
        delete[] frame_buffer;
    }
    frame_buffer = new uint32_t[frame_width * frame_height];
    
    // Créer un pattern de test
    create_test_pattern(frame_buffer, frame_width, frame_height);
    
    LOGI("Renderer initialisé: %dx%d", frame_width, frame_height);
}

extern "C" JNIEXPORT void JNICALL
Java_com_fceumm_wrapper_EmulatorRenderer_nativeRender(JNIEnv* env, jobject thiz) {
    // Le rendu est géré par le wrapper LibRetro
    frame_counter++;
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

echo "✅ Wrapper intégré dans native-lib.cpp"

# Mettre à jour CMakeLists.txt pour inclure notre wrapper
echo "🔧 Mise à jour de CMakeLists.txt..."

# Vérifier si CMakeLists.txt existe
if [ -f "app/src/main/cpp/CMakeLists.txt" ]; then
    # Ajouter notre wrapper aux sources
    sed -i 's/add_library(native-lib SHARED/& libretro_wrapper.cpp/' app/src/main/cpp/CMakeLists.txt
    echo "✅ CMakeLists.txt mis à jour"
else
    echo "⚠️  CMakeLists.txt non trouvé - mise à jour manuelle nécessaire"
fi

# Créer un script de build pour tester
echo "🔧 Création du script de build..."

cat > build_with_wrapper.sh << 'EOF'
#!/bin/bash

echo "=== Build avec wrapper amélioré ==="

# Nettoyer le build précédent
./gradlew clean

# Build avec le nouveau wrapper
./gradlew assembleDebug

echo "✅ Build terminé"
echo ""
echo "Pour installer sur l'appareil :"
echo "adb install app/build/outputs/apk/debug/app-debug.apk"
EOF

chmod +x build_with_wrapper.sh

echo "✅ Script de build créé: build_with_wrapper.sh"

# Créer un script de test
echo "🔧 Création du script de test..."

cat > test_app.sh << 'EOF'
#!/bin/bash

echo "=== Test de l'application avec wrapper amélioré ==="

# Vérifier que l'APK existe
if [ ! -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "❌ APK non trouvé. Exécutez d'abord: ./build_with_wrapper.sh"
    exit 1
fi

# Installer l'APK
echo "📱 Installation de l'APK..."
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Lancer l'application
echo "🚀 Lancement de l'application..."
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre un peu
sleep 5

# Vérifier les logs
echo "📋 Vérification des logs..."
adb logcat -d | grep -E "(FCEUmmWrapper|LibretroWrapper)" | tail -20

echo ""
echo "✅ Test terminé"
echo ""
echo "Si vous voyez des messages d'erreur SIGSEGV, le wrapper"
echo "devrait maintenant les gérer proprement sans crash."
EOF

chmod +x test_app.sh

echo "✅ Script de test créé: test_app.sh"

echo ""
echo "🎉 Intégration terminée !"
echo ""
echo "Prochaines étapes :"
echo "1. Exécuter : ./build_with_wrapper.sh"
echo "2. Tester : ./test_app.sh"
echo "3. Vérifier les logs pour voir si les SIGSEGV sont gérés"
echo ""
echo "Le wrapper amélioré devrait maintenant :"
echo "✅ Gérer les SIGSEGV sans crash"
echo "✅ Supporter les commandes LibRetro non standard"
echo "✅ Fournir un logging détaillé"
echo "✅ Nettoyer proprement les ressources" 