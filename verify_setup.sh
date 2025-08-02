#!/bin/bash
set -e

source ./android_env.sh

# Vérification outils
for tool in git cmake ninja ndk-build javac; do
  if ! command -v $tool &>/dev/null; then
    echo "Outil manquant: $tool"; exit 1
  fi
done

# Vérification variables d'environnement
for var in ANDROID_SDK_ROOT ANDROID_NDK_ROOT JAVA_HOME; do
  if [ -z "${!var}" ]; then
    echo "Variable d'environnement manquante: $var"; exit 1
  fi
done

# Test build FCEUmm pour chaque arch
cd libretro-super
RESULT=0
for ABI in armeabi-v7a arm64-v8a x86 x86_64; do
  echo "Test build FCEUmm ($ABI) ..."
  ./libretro-build-android-${ABI//-/_}.sh fceumm || RESULT=1
done
cd ..

if [ $RESULT -eq 0 ]; then
  echo "\nVérification complète: tous les builds FCEUmm OK."
else
  echo "\nAttention: au moins un build FCEUmm a échoué."
fi 