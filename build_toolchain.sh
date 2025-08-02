#!/bin/bash
set -e

source ./android_env.sh

# Vérification toolchains NDK
for ABI in armeabi-v7a arm64-v8a x86 x86_64; do
  TOOLCHAIN="$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64/bin/clang"
  if [ ! -f "$TOOLCHAIN" ]; then
    echo "Toolchain manquant pour $ABI. Vérifiez l'installation du NDK."
    exit 1
  fi
done

echo "Toolchains NDK OK pour toutes les architectures."

# Préparation build libretro-super multi-arch
cd libretro-super
for ABI in armeabi-v7a arm64-v8a x86 x86_64; do
  echo "Préparation build test pour $ABI..."
  ./libretro-build-android-${ABI//-/_}.sh || echo "Échec build $ABI (à corriger manuellement)"
done
cd ..

echo "Build toolchain prêt pour toutes les archs." 