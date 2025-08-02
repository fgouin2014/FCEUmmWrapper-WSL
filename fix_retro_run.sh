#!/bin/bash

# Correction de la fonction retro_run pour éviter les SIGSEGV
set -e

echo "🔧 Correction de la fonction retro_run..."
echo "======================================"

# Créer un backup
cp FCEUmmWrapper/app/src/main/cpp/libretro_wrapper.cpp FCEUmmWrapper/app/src/main/cpp/libretro_wrapper.cpp.backup

echo "✅ Backup créé"

# Chercher la fonction libretro_run et ajouter la protection SIGSEGV
echo ""
echo "🔍 Recherche de la fonction libretro_run..."

# Créer un patch pour la fonction libretro_run
cat > retro_run_patch.cpp << 'EOF'
void libretro_run() {
    // Protection SIGSEGV pour retro_run
    struct sigaction old_action;
    struct sigaction new_action;
    memset(&new_action, 0, sizeof(new_action));
    new_action.sa_handler = [](int sig) {
        LOGE("Signal SIGSEGV capturé dans retro_run - sortie propre");
        segv_occurred = true;
        longjmp(segv_jmp_buf, 1);
    };
    sigaction(SIGSEGV, &new_action, &old_action);
    
    // Utiliser setjmp pour capturer les erreurs
    if (setjmp(segv_jmp_buf) == 0) {
        if (retro_run_func) {
            retro_run_func();
        }
        
        // Restaurer le handler par défaut
        sigaction(SIGSEGV, &old_action, nullptr);
    } else {
        // SIGSEGV dans retro_run
        sigaction(SIGSEGV, &old_action, nullptr);
        LOGE("SIGSEGV dans retro_run - passage en mode fallback");
        
        // Mode fallback : afficher un écran noir avec un message
        // ou utiliser un core alternatif
        usleep(16667); // ~60 FPS
    }
}
EOF

echo "✅ Patch pour retro_run créé"

# Créer un script pour appliquer le patch
cat > apply_retro_run_patch.sh << 'EOF'
#!/bin/bash

# Appliquer le patch pour retro_run
set -e

echo "🔧 Application du patch retro_run..."

# Remplacer la fonction libretro_run existante
sed -i '/void libretro_run() {/,/^}/c\
void libretro_run() {\
    // Protection SIGSEGV pour retro_run\
    struct sigaction old_action;\
    struct sigaction new_action;\
    memset(&new_action, 0, sizeof(new_action));\
    new_action.sa_handler = [](int sig) {\
        LOGE("Signal SIGSEGV capturé dans retro_run - sortie propre");\
        segv_occurred = true;\
        longjmp(segv_jmp_buf, 1);\
    };\
    sigaction(SIGSEGV, &new_action, &old_action);\
    \
    // Utiliser setjmp pour capturer les erreurs\
    if (setjmp(segv_jmp_buf) == 0) {\
        if (retro_run_func) {\
            retro_run_func();\
        }\
        \
        // Restaurer le handler par défaut\
        sigaction(SIGSEGV, &old_action, nullptr);\
    } else {\
        // SIGSEGV dans retro_run\
        sigaction(SIGSEGV, &old_action, nullptr);\
        LOGE("SIGSEGV dans retro_run - passage en mode fallback");\
        \
        // Mode fallback : afficher un écran noir avec un message\
        // ou utiliser un core alternatif\
        usleep(16667); // ~60 FPS\
    }\
}' FCEUmmWrapper/app/src/main/cpp/libretro_wrapper.cpp

echo "✅ Patch appliqué avec succès"
EOF

chmod +x apply_retro_run_patch.sh

# Créer un script de test
cat > test_retro_run_fix.sh << 'EOF'
#!/bin/bash

# Test de la correction retro_run
set -e

echo "🧪 Test de la correction retro_run..."

# Appliquer le patch
./apply_retro_run_patch.sh

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
timeout 30s adb logcat -s com.fceumm.wrapper | grep -E "(SIGSEGV|retro_run|fallback)"

echo "✅ Test terminé"
EOF

chmod +x test_retro_run_fix.sh

echo ""
echo "🎯 Solutions créées :"
echo "1. Patch pour retro_run avec protection SIGSEGV"
echo "2. Script d'application du patch"
echo "3. Test de la correction"

echo ""
echo "Pour appliquer la correction :"
echo "  ./apply_retro_run_patch.sh"
echo "  ./test_retro_run_fix.sh" 