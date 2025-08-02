#!/bin/bash

# Script pour télécharger les cores officiels depuis les builds nightly
set -e

# URLs des builds nightly
X86_64_URL="https://buildbot.libretro.com/nightly/android/latest/x86_64"
ARM64_URL="https://buildbot.libretro.com/nightly/android/latest/arm64-v8a"

# Répertoires de destination
mkdir -p official_cores/x86_64
mkdir -p official_cores/arm64-v8a

echo "📥 Téléchargement des cores officiels..."

# Télécharger FCEUmm pour x86_64
echo "Téléchargement FCEUmm x86_64..."
wget -O official_cores/x86_64/fceumm_libretro_android.so.zip \
  "$X86_64_URL/fceumm_libretro_android.so.zip"

# Télécharger FCEUmm pour arm64-v8a  
echo "Téléchargement FCEUmm arm64-v8a..."
wget -O official_cores/arm64-v8a/fceumm_libretro_android.so.zip \
  "$ARM64_URL/fceumm_libretro_android.so.zip"

# Extraire les cores
echo "📦 Extraction des cores..."
cd official_cores/x86_64
unzip -o fceumm_libretro_android.so.zip
cd ../arm64-v8a
unzip -o fceumm_libretro_android.so.zip
cd ../..

echo "✅ Cores officiels téléchargés et extraits dans official_cores/"
echo "📁 Structure créée :"
echo "  official_cores/"
echo "  ├── x86_64/fceumm_libretro_android.so"
echo "  └── arm64-v8a/fceumm_libretro_android.so"

# Comparer avec vos builds
echo ""
echo "🔍 Comparaison avec vos builds :"
if [ -f "FCEUmmWrapper/app/src/main/jniLibs/x86_64/libfceumm.so" ]; then
    echo "Votre build x86_64: $(ls -lh FCEUmmWrapper/app/src/main/jniLibs/x86_64/libfceumm.so)"
    echo "Build officiel x86_64: $(ls -lh official_cores/x86_64/fceumm_libretro_android.so)"
fi

if [ -f "FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/libfceumm.so" ]; then
    echo "Votre build arm64-v8a: $(ls -lh FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/libfceumm.so)"
    echo "Build officiel arm64-v8a: $(ls -lh official_cores/arm64-v8a/fceumm_libretro_android.so)"
fi 