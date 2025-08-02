# Script PowerShell pour compiler directement le core avec clang
# Évite les problèmes de Makefile sur Windows

param(
    [string]$Architecture = "x86_64",
    [string]$CoreName = "fceumm"
)

Write-Host "🔨 Compilation directe d'un core libretro FCEUmm pour $Architecture..." -ForegroundColor Green

# Configuration
$NDK_PATH = "C:\Users\Quentin\AppData\Local\Android\Sdk\ndk\28.2.13676358"
$BUILD_DIR = "direct_core_build"
$OUTPUT_DIR = "app\src\main\assets\coreCustom\$Architecture"

# Créer les répertoires
if (!(Test-Path $BUILD_DIR)) {
    New-Item -ItemType Directory -Path $BUILD_DIR -Force
}
if (!(Test-Path $OUTPUT_DIR)) {
    New-Item -ItemType Directory -Path $OUTPUT_DIR -Force
}

# Configuration selon l'architecture
switch ($Architecture) {
    "arm64-v8a" {
        $TOOLCHAIN = "$NDK_PATH\toolchains\llvm\prebuilt\windows-x86_64"
        $TARGET = "aarch64-linux-android"
        $API_LEVEL = "21"
    }
    "armeabi-v7a" {
        $TOOLCHAIN = "$NDK_PATH\toolchains\llvm\prebuilt\windows-x86_64"
        $TARGET = "armv7a-linux-androideabi"
        $API_LEVEL = "21"
    }
    "x86" {
        $TOOLCHAIN = "$NDK_PATH\toolchains\llvm\prebuilt\windows-x86_64"
        $TARGET = "i686-linux-android"
        $API_LEVEL = "21"
    }
    "x86_64" {
        $TOOLCHAIN = "$NDK_PATH\toolchains\llvm\prebuilt\windows-x86_64"
        $TARGET = "x86_64-linux-android"
        $API_LEVEL = "21"
    }
    default {
        Write-Host "❌ Architecture non supportée: $Architecture" -ForegroundColor Red
        exit 1
    }
}

# Variables d'environnement
$env:PATH = "$TOOLCHAIN\bin;$env:PATH"
$CC = "$TOOLCHAIN\bin\$TARGET$API_LEVEL-clang.cmd"

# Flags de compilation
$CFLAGS = "-fPIC -DANDROID -D__ANDROID_API__=$API_LEVEL -O3 -ffast-math -DHAVE_NEON=0 -DHAVE_SSE=0 -DHAVE_MMX=0"
$INCLUDES = "-Isrc/drivers/libretro/include -Isrc/drivers/libretro/libretro-common/include"
$LDFLAGS = "-shared -Wl,--no-undefined -Wl,-z,relro,-z,now -lm"

Write-Host "📋 Configuration:" -ForegroundColor Yellow
Write-Host "  Architecture: $Architecture" -ForegroundColor Cyan
Write-Host "  Target: $TARGET" -ForegroundColor Cyan
Write-Host "  API Level: $API_LEVEL" -ForegroundColor Cyan
Write-Host "  Toolchain: $TOOLCHAIN" -ForegroundColor Cyan

# Aller dans le répertoire de build
Set-Location $BUILD_DIR

# Cloner FCEUmm si nécessaire
if (!(Test-Path "fceumm")) {
    Write-Host "📥 Clonage de FCEUmm..." -ForegroundColor Yellow
    git clone https://github.com/libretro/libretro-fceumm.git fceumm
}

Set-Location fceumm

# Télécharger les headers si nécessaire
$INCLUDE_DIR = "src\drivers\libretro\include"
if (!(Test-Path "$INCLUDE_DIR\libretro.h")) {
    Write-Host "📥 Téléchargement des headers..." -ForegroundColor Yellow
    if (!(Test-Path $INCLUDE_DIR)) {
        New-Item -ItemType Directory -Path $INCLUDE_DIR -Force
    }
    
    $LIBRETRO_H_URL = "https://raw.githubusercontent.com/libretro/libretro-common/master/include/libretro.h"
    $LIBRETRO_H_PATH = "$INCLUDE_DIR\libretro.h"
    
    try {
        Invoke-WebRequest -Uri $LIBRETRO_H_URL -OutFile $LIBRETRO_H_PATH
        Write-Host "✅ libretro.h téléchargé" -ForegroundColor Green
    } catch {
        Write-Host "❌ Erreur lors du téléchargement de libretro.h" -ForegroundColor Red
    }
}

# Créer un fichier de test simple pour vérifier la compilation
$TEST_SOURCE = @"
#include <libretro.h>
#include <stdio.h>

// Fonctions libretro minimales pour test
void retro_init(void) {}
void retro_deinit(void) {}
unsigned retro_api_version(void) { return RETRO_API_VERSION; }
void retro_get_system_info(struct retro_system_info *info) {
    info->library_name = "FCEUmm Test";
    info->library_version = "1.0.0";
    info->valid_extensions = "nes";
    info->need_fullpath = false;
    info->block_extract = false;
}
void retro_get_system_av_info(struct retro_system_audio_video_info *info) {}
void retro_set_environment(retro_environment_t callback) {}
void retro_set_video_refresh(retro_video_refresh_t callback) {}
void retro_set_audio_sample(retro_audio_sample_t callback) {}
void retro_set_audio_sample_batch(retro_audio_sample_batch_t callback) {}
void retro_set_input_poll(retro_input_poll_t callback) {}
void retro_set_input_state(retro_input_state_t callback) {}
void retro_set_controller_port_device(unsigned port, unsigned device) {}
void retro_reset(void) {}
void retro_run(void) {}
size_t retro_serialize_size(void) { return 0; }
bool retro_serialize(void *data, size_t size) { return false; }
bool retro_unserialize(const void *data, size_t size) { return false; }
void retro_cheat_reset(void) {}
void retro_cheat_set(unsigned index, bool enabled, const char *code) {}
bool retro_load_game(const struct retro_game_info *game) { return true; }
bool retro_load_game_special(unsigned game_type, const struct retro_game_info *info, size_t num_info) { return false; }
void retro_unload_game(void) {}
unsigned retro_get_region(void) { return RETRO_REGION_NTSC; }
void *retro_get_memory_data(unsigned id) { return NULL; }
size_t retro_get_memory_size(unsigned id) { return 0; }
"@

# Écrire le fichier de test
Set-Content "test_core.c" $TEST_SOURCE -Encoding UTF8

Write-Host "🔨 Compilation du test core..." -ForegroundColor Yellow

# Compiler le test core
$TEST_COMMAND = "& `"$CC`" $CFLAGS $INCLUDES test_core.c $LDFLAGS -o ${CoreName}_libretro_android.so"

Write-Host "Commande: $TEST_COMMAND" -ForegroundColor Cyan

try {
    Invoke-Expression $TEST_COMMAND
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Test core compilé avec succès!" -ForegroundColor Green
        
        # Copier le core personnalisé
        $OutputFile = "$CoreName`_libretro_android.so"
        if (Test-Path $OutputFile) {
            Copy-Item $OutputFile "..\..\$OUTPUT_DIR\" -Force
            Write-Host "📁 Test core copié vers $OUTPUT_DIR" -ForegroundColor Green
        } else {
            Write-Host "❌ Fichier de sortie non trouvé: $OutputFile" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ Échec de la compilation du test core" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Erreur lors de la compilation: $($_.Exception.Message)" -ForegroundColor Red
}

# Retourner au répertoire racine
Set-Location ..\..

Write-Host "🎉 Compilation terminée!" -ForegroundColor Green 