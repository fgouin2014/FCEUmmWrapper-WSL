#!/bin/bash
set -e

# 1. Nettoyage
./clean_build.sh

# 2. Récupération du core FCEUmm (libretro-super)
cd libretro-super
./libretro-fetch.sh fceumm
cd ..

# 3. Compilation core FCEUmm pour toutes arch
./build_fceumm.sh arm64-v8a
./build_fceumm.sh armeabi-v7a
./build_fceumm.sh x86
./build_fceumm.sh x86_64

# 4. Build APK Android
./build_android.sh

# 5. Déploiement et tests
./deploy_test.sh

echo "\nBuild complet terminé avec succès !" 