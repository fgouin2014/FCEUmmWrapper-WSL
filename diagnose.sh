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
