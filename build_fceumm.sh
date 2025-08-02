#!/bin/bash
set -e

ARCH=$1
if [ -z "$ARCH" ]; then
  echo "Usage: $0 <arch> (arm64-v8a|armeabi-v7a|x86|x86_64)"; exit 1;
fi

cd libretro-super
case $ARCH in
  arm64-v8a)
    export CFLAGS="-O2 -ffast-math -march=armv8-a -fno-strict-aliasing"
    ./libretro-build-android-arm64_v8a.sh fceumm
    cp dist/android-arm64_v8a/fceumm_libretro.so ../FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/libfceumm.so
    ;;
  armeabi-v7a)
    export CFLAGS="-O2 -mfpu=neon -march=armv7-a -fno-strict-aliasing"
    ./libretro-build-android-armeabi_v7a.sh fceumm
    cp dist/android-armeabi_v7a/fceumm_libretro.so ../FCEUmmWrapper/app/src/main/jniLibs/armeabi-v7a/libfceumm.so
    ;;
  x86)
    export CFLAGS="-O2 -m32 -fno-strict-aliasing"
    ./libretro-build-android-x86.sh fceumm
    cp dist/android-x86/fceumm_libretro.so ../FCEUmmWrapper/app/src/main/jniLibs/x86/libfceumm.so
    ;;
  x86_64)
    export CFLAGS="-O2 -m64 -fno-strict-aliasing"
    ./libretro-build-android-x86_64.sh fceumm
    cp dist/android-x86_64/fceumm_libretro.so ../FCEUmmWrapper/app/src/main/jniLibs/x86_64/libfceumm.so
    ;;
  *)
    echo "Arch inconnue: $ARCH"; exit 2;
    ;;
esac
cd ..
echo "Build FCEUmm $ARCH terminé." 