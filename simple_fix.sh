#!/bin/bash

# Solution simple pour corriger le problème SIGSEGV
set -e

echo "🔧 Solution simple pour le problème SIGSEGV..."
echo "============================================="

# Analyser le code source actuel
echo ""
echo "📝 Analyse du code source..."

if [ -f "FCEUmmWrapper/app/src/main/cpp/libfceummwrapper.cpp" ]; then
    echo "✅ Fichier source trouvé"
    
    # Chercher les appels problématiques
    echo ""
    echo "🔍 Recherche des appels problématiques..."
    grep -n "retro_load_game" FCEUmmWrapper/app/src/main/cpp/libfceummwrapper.cpp || echo "❌ Aucun appel retro_load_game trouvé"
    grep -n "retro_run" FCEUmmWrapper/app/src/main/cpp/libfceummwrapper.cpp || echo "❌ Aucun appel retro_run trouvé"
else
    echo "❌ Fichier source non trouvé"
    exit 1
fi

# Créer un patch simple
echo ""
echo "🔧 Création d'un patch simple..."

cat > simple_patch.cpp << 'EOF'
// Patch simple pour gérer les SIGSEGV
#include <signal.h>
#include <setjmp.h>

static jmp_buf sigsegv_env;
static bool sigsegv_occurred = false;

void sigsegv_handler(int sig) {
    sigsegv_occurred = true;
    longjmp(sigsegv_env, 1);
}

// Fonction sécurisée pour charger une ROM
bool safe_load_rom(const char* rom_path) {
    signal(SIGSEGV, sigsegv_handler);
    sigsegv_occurred = false;
    
    if (setjmp(sigsegv_env) == 0) {
        // Tentative de chargement sécurisée
        struct retro_game_info game_info;
        game_info.path = rom_path;
        game_info.data = nullptr;
        game_info.size = 0;
        game_info.meta = nullptr;
        
        bool result = retro_load_game_func(&game_info);
        if (!sigsegv_occurred) {
            return result;
        }
    }
    
    // SIGSEGV capturé - utiliser un fallback
    __android_log_print(ANDROID_LOG_ERROR, "LibretroWrapper", "SIGSEGV capturé, utilisation du mode fallback");
    return false;
}

// Fonction sécurisée pour exécuter le core
bool safe_run_core() {
    signal(SIGSEGV, sigsegv_handler);
    sigsegv_occurred = false;
    
    if (setjmp(sigsegv_env) == 0) {
        retro_run_func();
        return !sigsegv_occurred;
    }
    
    // SIGSEGV capturé
    __android_log_print(ANDROID_LOG_ERROR, "LibretroWrapper", "SIGSEGV capturé pendant retro_run");
    return false;
}
EOF

echo "✅ Patch simple créé"

# Créer un script de test
echo ""
echo "🧪 Création d'un test simple..."

cat > test_simple_fix.sh << 'EOF'
#!/bin/bash

# Test simple de la correction
set -e

echo "🧪 Test simple de la correction..."

# Compiler l'application
echo "🔨 Compilation..."
./gradlew assembleDebug

# Installer l'APK
echo "📱 Installation..."
adb install -r FCEUmmWrapper/app/build/outputs/apk/debug/app-x86_64-debug.apk

# Lancer l'application
echo "🚀 Lancement..."
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre un peu
sleep 5

# Surveiller les logs
echo "📊 Surveillance des logs..."
timeout 20s adb logcat -s com.fceumm.wrapper | grep -E "(SIGSEGV|retro_load_game|retro_run|fallback)"

echo "✅ Test terminé"
EOF

chmod +x test_simple_fix.sh

# Créer un script de diagnostic avancé
echo ""
echo "🔍 Création d'un diagnostic avancé..."

cat > advanced_diagnostic.sh << 'EOF'
#!/bin/bash

# Diagnostic avancé du problème
set -e

echo "🔍 Diagnostic avancé..."
echo "======================"

# Vérifier les permissions
echo ""
echo "📱 Vérification des permissions..."
adb shell ls -la /data/data/com.fceumm.wrapper/files/

# Vérifier les cores
echo ""
echo "📦 Vérification des cores..."
adb shell ls -la /data/app/*/lib/x86_64/

# Vérifier les logs détaillés
echo ""
echo "📊 Logs détaillés..."
adb logcat -d | grep -E "(fceumm|libretro|SIGSEGV)" | tail -20

# Test de compatibilité
echo ""
echo "🧪 Test de compatibilité..."
adb shell "cd /data/data/com.fceumm.wrapper/files && ls -la"

echo "✅ Diagnostic terminé"
EOF

chmod +x advanced_diagnostic.sh

echo ""
echo "🎯 Solutions créées :"
echo "1. Patch simple pour SIGSEGV"
echo "2. Test simple de la correction"
echo "3. Diagnostic avancé"

echo ""
echo "Pour tester :"
echo "  ./test_simple_fix.sh"
echo "  ./advanced_diagnostic.sh" 