#!/bin/bash
set -e

rm -rf FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/*
rm -rf FCEUmmWrapper/app/src/main/jniLibs/armeabi-v7a/*
rm -rf FCEUmmWrapper/app/src/main/jniLibs/x86/*
rm -rf FCEUmmWrapper/app/src/main/jniLibs/x86_64/*
rm -rf FCEUmmWrapper/app/build
rm -rf FCEUmmWrapper/.gradle
rm -rf FCEUmmWrapper/build
rm -rf libretro-super/dist
find . -name '*.apk' -delete
find . -name '*.so' -delete 