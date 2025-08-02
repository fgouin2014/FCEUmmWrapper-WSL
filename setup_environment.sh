#!/bin/bash
set -e

# 1. Mise à jour et installation des paquets de base
sudo apt update && sudo apt install -y \
  build-essential cmake ninja-build git unzip wget openjdk-17-jdk python3 python3-pip libncurses6 lib32z1 lib32stdc++6

# 2. Variables d'installation
ANDROID_SDK_ROOT="$HOME/Android/Sdk"
ANDROID_NDK_VERSION="r28b"
ANDROID_API_LEVEL=36
CMDLINE_TOOLS_VERSION="11076708_latest"
LIBRETRO_SUPER_DIR="$(pwd)/libretro-super"

# 3. Installation Android Command Line Tools
mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
cd "$ANDROID_SDK_ROOT"
wget -q https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}.zip -O cmdline-tools.zip
unzip -o cmdline-tools.zip -d cmdline-tools
mv cmdline-tools/cmdline-tools cmdline-tools/latest
rm cmdline-tools.zip

# 4. Installation SDK/NDK/API
export ANDROID_SDK_ROOT
export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"
yes | sdkmanager --sdk_root="$ANDROID_SDK_ROOT" --licenses
sdkmanager --sdk_root="$ANDROID_SDK_ROOT" "platform-tools" "platforms;android-${ANDROID_API_LEVEL}" "build-tools;34.0.0" "ndk;28.2.13676358"

# 5. Clonage/MAJ libretro-super
if [ ! -d "$LIBRETRO_SUPER_DIR" ]; then
  git clone https://github.com/libretro/libretro-super.git "$LIBRETRO_SUPER_DIR"
else
  cd "$LIBRETRO_SUPER_DIR" && git pull
fi

# 6. Permissions
chmod +x "$LIBRETRO_SUPER_DIR"/*.sh

# 7. Fin
cd "$(pwd)"
echo "\nEnvironnement prêt. Pensez à sourcer android_env.sh avant de builder." 