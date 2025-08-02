#!/bin/bash

# Script pour améliorer le wrapper avec des fonctionnalités avancées
set -e

echo "🚀 Amélioration du wrapper FCEUmm..."

# Créer un script de mise à jour automatique des cores
echo "📥 Création du script de mise à jour automatique..."
cat > update_cores.sh << 'EOF'
#!/bin/bash

# Script de mise à jour automatique des cores officiels
set -e

echo "🔄 Mise à jour des cores officiels..."

# URLs des builds nightly
X86_64_URL="https://buildbot.libretro.com/nightly/android/latest/x86_64"
ARM64_URL="https://buildbot.libretro.com/nightly/android/latest/arm64-v8a"

# Date actuelle
DATE=$(date +%Y%m%d)

# Créer le répertoire de backup
mkdir -p core_backups/$DATE

# Sauvegarder les cores actuels
if [ -f "FCEUmmWrapper/app/src/main/jniLibs/x86_64/libfceumm.so" ]; then
    cp FCEUmmWrapper/app/src/main/jniLibs/x86_64/libfceumm.so core_backups/$DATE/
    echo "✅ Backup x86_64 créé"
fi

if [ -f "FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/libfceumm.so" ]; then
    cp FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/libfceumm.so core_backups/$DATE/
    echo "✅ Backup arm64-v8a créé"
fi

# Télécharger les nouveaux cores
echo "📥 Téléchargement des nouveaux cores..."
wget -O official_cores/x86_64/fceumm_libretro_android.so.zip "$X86_64_URL/fceumm_libretro_android.so.zip"
wget -O official_cores/arm64-v8a/fceumm_libretro_android.so.zip "$ARM64_URL/fceumm_libretro_android.so.zip"

# Extraire et installer
cd official_cores/x86_64 && unzip -o fceumm_libretro_android.so.zip && cd ../..
cd official_cores/arm64-v8a && unzip -o fceumm_libretro_android.so.zip && cd ../..

# Copier dans le projet
cp official_cores/x86_64/fceumm_libretro_android.so FCEUmmWrapper/app/src/main/jniLibs/x86_64/libfceumm.so
cp official_cores/arm64-v8a/fceumm_libretro_android.so FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/libfceumm.so

echo "✅ Cores mis à jour avec succès"
echo "📁 Backup disponible dans core_backups/$DATE/"
EOF

chmod +x update_cores.sh

# Créer un script de diagnostic avancé
echo "🔍 Création du script de diagnostic avancé..."
cat > diagnose.sh << 'EOF'
#!/bin/bash

# Script de diagnostic avancé pour le wrapper
set -e

echo "🔍 Diagnostic avancé du wrapper FCEUmm..."

# Vérifier l'environnement
echo ""
echo "📋 ENVIRONNEMENT:"
echo "================="
echo "Android SDK: $ANDROID_HOME"
echo "NDK: $ANDROID_NDK"
echo "Java: $(java -version 2>&1 | head -1)"
echo "Gradle: $(./gradlew --version | grep "Gradle")"

# Vérifier les cores
echo ""
echo "📱 CORES:"
echo "=========="
for arch in x86_64 arm64-v8a armeabi-v7a x86; do
    core_path="FCEUmmWrapper/app/src/main/jniLibs/$arch/libfceumm.so"
    if [ -f "$core_path" ]; then
        echo "✅ $arch: $(ls -lh "$core_path" | awk '{print $5}')"
        echo "   Architecture: $(file "$core_path" | cut -d: -f2)"
    else
        echo "❌ $arch: Core manquant"
    fi
done

# Vérifier les ROMs
echo ""
echo "🎮 ROMS:"
echo "========"
if [ -d "FCEUmmWrapper/app/src/main/assets/roms" ]; then
    ls -lh FCEUmmWrapper/app/src/main/assets/roms/
else
    echo "❌ Répertoire ROMs manquant"
fi

# Vérifier la configuration
echo ""
echo "⚙️ CONFIGURATION:"
echo "================="
if [ -f "FCEUmmWrapper/app/build.gradle" ]; then
    echo "✅ build.gradle trouvé"
    echo "   compileSdk: $(grep "compileSdk" FCEUmmWrapper/app/build.gradle | head -1)"
    echo "   minSdk: $(grep "minSdk" FCEUmmWrapper/app/build.gradle | head -1)"
else
    echo "❌ build.gradle manquant"
fi

# Test de compilation rapide
echo ""
echo "🔨 TEST DE COMPILATION:"
echo "======================="
if ./gradlew assembleDebug --dry-run > /dev/null 2>&1; then
    echo "✅ Configuration Gradle valide"
else
    echo "❌ Problème de configuration Gradle"
fi

echo ""
echo "🎯 RECOMMANDATIONS:"
echo "==================="
echo "1. Exécutez ./test_emulation.sh pour tester l'émulation"
echo "2. Exécutez ./update_cores.sh pour mettre à jour les cores"
echo "3. Vérifiez les logs avec: adb logcat -s com.fceumm.wrapper"
EOF

chmod +x diagnose.sh

# Créer un script de développement
echo "🛠️ Création du script de développement..."
cat > dev_workflow.sh << 'EOF'
#!/bin/bash

# Workflow de développement pour FCEUmmWrapper
set -e

echo "🛠️ Workflow de développement FCEUmmWrapper"
echo "=========================================="

# Fonction d'aide
show_help() {
    echo "Usage: $0 [COMMANDE]"
    echo ""
    echo "Commandes disponibles:"
    echo "  build     - Compiler le projet"
    echo "  install   - Installer sur l'émulateur"
    echo "  test      - Lancer les tests"
    echo "  debug     - Mode debug complet"
    echo "  clean     - Nettoyer le projet"
    echo "  update    - Mettre à jour les cores"
    echo "  diagnose  - Diagnostic complet"
    echo "  help      - Afficher cette aide"
}

# Compilation
build() {
    echo "🔨 Compilation..."
    ./gradlew assembleDebug
    echo "✅ Compilation terminée"
}

# Installation
install() {
    echo "📱 Installation..."
    adb install -r FCEUmmWrapper/app/build/outputs/apk/debug/app-x86_64-debug.apk
    echo "✅ Installation terminée"
}

# Test
test() {
    echo "🧪 Tests..."
    ./test_emulation.sh
}

# Debug complet
debug() {
    echo "🐛 Mode debug..."
    build
    install
    adb shell am start -n com.fceumm.wrapper/.MainActivity
    echo "📊 Surveillance des logs (Ctrl+C pour arrêter)..."
    adb logcat -s com.fceumm.wrapper
}

# Nettoyage
clean() {
    echo "🧹 Nettoyage..."
    ./gradlew clean
    echo "✅ Nettoyage terminé"
}

# Mise à jour
update() {
    echo "🔄 Mise à jour..."
    ./update_cores.sh
}

# Diagnostic
diagnose() {
    echo "🔍 Diagnostic..."
    ./diagnose.sh
}

# Gestion des commandes
case "${1:-help}" in
    build) build ;;
    install) install ;;
    test) test ;;
    debug) debug ;;
    clean) clean ;;
    update) update ;;
    diagnose) diagnose ;;
    help|*) show_help ;;
esac
EOF

chmod +x dev_workflow.sh

echo ""
echo "✅ Améliorations créées:"
echo "========================"
echo "📥 update_cores.sh - Mise à jour automatique des cores"
echo "🔍 diagnose.sh - Diagnostic avancé"
echo "🛠️ dev_workflow.sh - Workflow de développement"
echo ""
echo "🎯 Utilisation:"
echo "==============="
echo "./dev_workflow.sh build    # Compiler"
echo "./dev_workflow.sh debug    # Mode debug complet"
echo "./dev_workflow.sh test     # Tests automatiques"
echo "./update_cores.sh          # Mettre à jour les cores"
echo "./diagnose.sh              # Diagnostic complet" 