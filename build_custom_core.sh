#!/bin/bash

# Compilation d'un core personnalisé compatible
set -e

echo "🔨 Compilation d'un core personnalisé..."

# Créer le répertoire de build
mkdir -p custom_core_build
cd custom_core_build

# Cloner FCEUmm si nécessaire
if [ ! -d "fceumm" ]; then
    git clone https://github.com/libretro/libretro-fceumm.git fceumm
fi

cd fceumm

# Compiler avec des flags de compatibilité
make -f Makefile.libretro platform=android \
    CFLAGS="-O2 -fPIC -DANDROID -D__ANDROID__ -DHAVE_NEON=0 -DHAVE_SSE=0 -DHAVE_MMX=0" \
    LDFLAGS="-shared -Wl,--no-undefined" \
    HAVE_NEON=0 HAVE_SSE=0 HAVE_MMX=0

# Copier le core personnalisé
cp fceumm_libretro_android.so ../../custom_core.so

echo "✅ Core personnalisé compilé"
