#!/bin/bash
# À sourcer: source android_env.sh
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
export ANDROID_NDK_ROOT="$ANDROID_SDK_ROOT/ndk/28.2.13676358"
export ANDROID_HOME="$ANDROID_SDK_ROOT"
export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64/bin:$PATH"
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
export TARGET_ABIS="armeabi-v7a arm64-v8a x86 x86_64"
export ANDROID_API_LEVEL=36 