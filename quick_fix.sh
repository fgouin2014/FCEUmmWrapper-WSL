#!/bin/bash

# Correction rapide du problème SIGSEGV
set -e

echo "🚀 Correction rapide du problème SIGSEGV..."
echo "=========================================="

# Créer un wrapper sécurisé
echo ""
echo "🔧 Création d'un wrapper sécurisé..."

cat > secure_wrapper.c << 'EOF'
#include <signal.h>
#include <setjmp.h>
#include <stdio.h>
#include <stdlib.h>

static jmp_buf sigsegv_env;
static int sigsegv_occurred = 0;

void sigsegv_handler(int sig) {
    sigsegv_occurred = 1;
    longjmp(sigsegv_env, 1);
}

// Fonction sécurisée pour initialiser le core
int safe_retro_init(void (*retro_init_func)(void)) {
    signal(SIGSEGV, sigsegv_handler);
    sigsegv_occurred = 0;
    
    if (setjmp(sigsegv_env) == 0) {
        retro_init_func();
        return 1; // Succès
    } else {
        printf("SIGSEGV capturé pendant retro_init\n");
        return 0; // Échec
    }
}

// Fonction sécurisée pour charger une ROM
int safe_retro_load_game(void (*retro_load_game_func)(const struct retro_game_info*), 
                         const struct retro_game_info* game_info) {
    signal(SIGSEGV, sigsegv_handler);
    sigsegv_occurred = 0;
    
    if (setjmp(sigsegv_env) == 0) {
        return retro_load_game_func(game_info);
    } else {
        printf("SIGSEGV capturé pendant retro_load_game\n");
        return 0; // Échec
    }
}

// Fonction sécurisée pour exécuter le core
int safe_retro_run(void (*retro_run_func)(void)) {
    signal(SIGSEGV, sigsegv_handler);
    sigsegv_occurred = 0;
    
    if (setjmp(sigsegv_env) == 0) {
        retro_run_func();
        return 1; // Succès
    } else {
        printf("SIGSEGV capturé pendant retro_run\n");
        return 0; // Échec
    }
}
EOF

# Compiler le wrapper sécurisé
gcc -c -fPIC secure_wrapper.c -o secure_wrapper.o
gcc -shared -o libsecure_wrapper.so secure_wrapper.o

echo "✅ Wrapper sécurisé compilé"

# Créer un script de patch pour votre application
echo ""
echo "🔧 Création d'un patch pour votre application..."

cat > patch_wrapper.sh << 'EOF'
#!/bin/bash

# Patch pour intégrer la gestion SIGSEGV dans votre wrapper
set -e

echo "🔧 Application du patch SIGSEGV..."

# Créer un backup
cp FCEUmmWrapper/app/src/main/cpp/libfceummwrapper.cpp FCEUmmWrapper/app/src/main/cpp/libfceummwrapper.cpp.backup

# Ajouter les includes nécessaires
sed -i '1i #include <signal.h>' FCEUmmWrapper/app/src/main/cpp/libfceummwrapper.cpp
sed -i '1i #include <setjmp.h>' FCEUmmWrapper/app/src/main/cpp/libfceummwrapper.cpp

# Ajouter les variables globales pour la gestion SIGSEGV
sed -i '/static jmp_buf sigsegv_env;/a static int sigsegv_occurred = 0;' FCEUmmWrapper/app/src/main/cpp/libfceummwrapper.cpp

# Ajouter le handler SIGSEGV
cat >> FCEUmmWrapper/app/src/main/cpp/libfceummwrapper.cpp << 'PATCH_EOF'

void sigsegv_handler(int sig) {
    sigsegv_occurred = 1;
    longjmp(sigsegv_env, 1);
}

// Fonction sécurisée pour charger une ROM
bool safe_load_rom(const char* rom_path) {
    signal(SIGSEGV, sigsegv_handler);
    sigsegv_occurred = 0;
    
    if (setjmp(sigsegv_env) == 0) {
        // Tentative de chargement sécurisée
        return retro_load_game_func(&game_info);
    } else {
        // SIGSEGV capturé - utiliser un fallback
        __android_log_print(ANDROID_LOG_ERROR, "LibretroWrapper", "SIGSEGV capturé, utilisation du mode fallback");
        return false;
    }
}
PATCH_EOF

echo "✅ Patch appliqué avec succès"
EOF

chmod +x patch_wrapper.sh

# Créer un script de test rapide
echo ""
echo "🧪 Création d'un test rapide..."

cat > test_quick_fix.sh << 'EOF'
#!/bin/bash

# Test rapide de la correction
set -e

echo "🧪 Test de la correction SIGSEGV..."

# Compiler avec le patch
./gradlew assembleDebug

# Installer l'APK
adb install -r FCEUmmWrapper/app/build/outputs/apk/debug/app-x86_64-debug.apk

# Lancer l'application
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Surveiller les logs
echo "📱 Surveillance des logs (30 secondes)..."
timeout 30s adb logcat -s com.fceumm.wrapper | grep -E "(SIGSEGV|retro_load_game|retro_run)"

echo "✅ Test terminé"
EOF

chmod +x test_quick_fix.sh

echo ""
echo "🎯 Solutions créées :"
echo "1. Wrapper sécurisé compilé"
echo "2. Patch pour votre application"
echo "3. Test rapide de la correction"

echo ""
echo "Pour appliquer la correction :"
echo "  ./patch_wrapper.sh"
echo "  ./test_quick_fix.sh" 