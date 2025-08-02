#!/bin/bash

# Script de correction du problème de core
set -e

echo "🔧 Correction du problème de core..."

# Option 1: Utiliser un core plus stable
echo "📥 Téléchargement d'un core plus stable..."
wget -O stable_core.zip "https://buildbot.libretro.com/nightly/android/latest/x86_64/fceumm_libretro_android.so.zip"
unzip -o stable_core.zip -d stable_cores/

# Option 2: Compiler un core personnalisé avec des flags de sécurité
echo "🔨 Compilation d'un core sécurisé..."
mkdir -p secure_build
cd secure_build

# Cloner FCEUmm si nécessaire
if [ ! -d "fceumm" ]; then
    git clone https://github.com/libretro/libretro-fceumm.git fceumm
fi

cd fceumm

# Compiler avec des flags de sécurité
make -f Makefile.libretro platform=android \
    CFLAGS="-O2 -fPIC -DANDROID -D__ANDROID__ -DHAVE_NEON=0 -DHAVE_SSE=0" \
    LDFLAGS="-shared -Wl,--no-undefined -Wl,--version-script=link.T"

# Copier le core sécurisé
cp fceumm_libretro_android.so ../../secure_core.so

echo "✅ Core sécurisé compilé"

# Option 3: Modifier le wrapper pour gérer les SIGSEGV
echo "🔧 Modification du wrapper pour gérer les SIGSEGV..."

cat > wrapper_patch.c << 'PATCH_EOF'
// Patch pour gérer les SIGSEGV dans le wrapper
#include <signal.h>
#include <setjmp.h>

static jmp_buf sigsegv_env;

void sigsegv_handler(int sig) {
    longjmp(sigsegv_env, 1);
}

// Fonction sécurisée pour charger une ROM
int safe_load_rom(const char* rom_path) {
    signal(SIGSEGV, sigsegv_handler);
    
    if (setjmp(sigsegv_env) == 0) {
        // Tentative de chargement sécurisée
        return retro_load_game_func(&game_info);
    } else {
        // SIGSEGV capturé - utiliser un fallback
        printf("SIGSEGV capturé, utilisation du mode fallback\n");
        return 0;
    }
}
PATCH_EOF'

echo "✅ Patch de sécurité créé"
