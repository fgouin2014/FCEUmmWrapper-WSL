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
