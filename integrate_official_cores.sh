#!/bin/bash

# Script pour intégrer les cores officiels dans le projet Android
set -e

echo "🔧 Intégration des cores officiels dans le projet..."

# Vérifier si les cores officiels existent
if [ ! -f "official_cores/x86_64/fceumm_libretro_android.so" ]; then
    echo "❌ Cores officiels non trouvés. Exécutez d'abord download_official_cores.sh"
    exit 1
fi

# Créer les répertoires de destination
mkdir -p FCEUmmWrapper/app/src/main/jniLibs/x86_64
mkdir -p FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a

# Sauvegarder les builds actuels
echo "💾 Sauvegarde des builds actuels..."
if [ -f "FCEUmmWrapper/app/src/main/jniLibs/x86_64/libfceumm.so" ]; then
    cp FCEUmmWrapper/app/src/main/jniLibs/x86_64/libfceumm.so FCEUmmWrapper/app/src/main/jniLibs/x86_64/libfceumm.so.backup
    echo "  ✅ Backup x86_64 créé"
fi

if [ -f "FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/libfceumm.so" ]; then
    cp FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/libfceumm.so FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/libfceumm.so.backup
    echo "  ✅ Backup arm64-v8a créé"
fi

# Copier les cores officiels
echo "📋 Copie des cores officiels..."

# x86_64
cp official_cores/x86_64/fceumm_libretro_android.so FCEUmmWrapper/app/src/main/jniLibs/x86_64/libfceumm.so
echo "  ✅ Core officiel x86_64 copié"

# arm64-v8a
cp official_cores/arm64-v8a/fceumm_libretro_android.so FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/libfceumm.so
echo "  ✅ Core officiel arm64-v8a copié"

# Vérifier les permissions
chmod 644 FCEUmmWrapper/app/src/main/jniLibs/x86_64/libfceumm.so
chmod 644 FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/libfceumm.so

echo ""
echo "📊 Résumé de l'intégration:"
echo "  📁 x86_64: $(ls -lh FCEUmmWrapper/app/src/main/jniLibs/x86_64/libfceumm.so)"
echo "  📁 arm64-v8a: $(ls -lh FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/libfceumm.so)"

echo ""
echo "🚀 Vous pouvez maintenant:"
echo "1. Compiler votre app: ./gradlew assembleDebug"
echo "2. Tester avec les cores officiels"
echo "3. Comparer les résultats avec vos builds"

echo ""
echo "🔄 Pour restaurer vos builds:"
echo "  cp FCEUmmWrapper/app/src/main/jniLibs/x86_64/libfceumm.so.backup FCEUmmWrapper/app/src/main/jniLibs/x86_64/libfceumm.so"
echo "  cp FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/libfceumm.so.backup FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/libfceumm.so" 