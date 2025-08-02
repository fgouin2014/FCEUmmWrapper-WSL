# Script PowerShell ultra-simple pour tester la compilation

param(
    [string]$Architecture = "x86_64"
)

Write-Host "🧪 Test ultra-simple de compilation..." -ForegroundColor Green

# Configuration
$NDK_PATH = "C:\Users\Quentin\AppData\Local\Android\Sdk\ndk\28.2.13676358"
$OUTPUT_DIR = "app\src\main\assets\coreCustom\$Architecture"

# Créer le répertoire de sortie s'il n'existe pas
if (!(Test-Path $OUTPUT_DIR)) {
    New-Item -ItemType Directory -Path $OUTPUT_DIR -Force
}

# Configuration selon l'architecture
switch ($Architecture) {
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
$CFLAGS = "-fPIC -DANDROID -D__ANDROID_API__=$API_LEVEL -O3"
$LDFLAGS = "-shared -Wl,--no-undefined -lm"

Write-Host "📋 Configuration:" -ForegroundColor Yellow
Write-Host "  Architecture: $Architecture" -ForegroundColor Cyan
Write-Host "  Target: $TARGET" -ForegroundColor Cyan
Write-Host "  API Level: $API_LEVEL" -ForegroundColor Cyan
Write-Host "  Compiler: $CC" -ForegroundColor Cyan

# Créer un fichier de test minimal
$TEST_SOURCE = @"
#include <stdio.h>
#include <stdlib.h>

// Fonctions minimales pour un core libretro
void retro_init(void) { printf("Core initialized\n"); }
void retro_deinit(void) { printf("Core deinitialized\n"); }
unsigned retro_api_version(void) { return 1; }

void retro_get_system_info(void *info) {
    printf("System info requested\n");
}

void retro_get_system_av_info(void *info) {
    printf("AV info requested\n");
}

void retro_set_environment(void *callback) {
    printf("Environment set\n");
}

void retro_set_video_refresh(void *callback) {
    printf("Video refresh set\n");
}

void retro_set_audio_sample(void *callback) {
    printf("Audio sample set\n");
}

void retro_set_audio_sample_batch(void *callback) {
    printf("Audio batch set\n");
}

void retro_set_input_poll(void *callback) {
    printf("Input poll set\n");
}

void retro_set_input_state(void *callback) {
    printf("Input state set\n");
}

void retro_set_controller_port_device(unsigned port, unsigned device) {
    printf("Controller device set\n");
}

void retro_reset(void) {
    printf("Reset called\n");
}

void retro_run(void) {
    printf("Run called\n");
}

size_t retro_serialize_size(void) { return 0; }
int retro_serialize(void *data, size_t size) { return 0; }
int retro_unserialize(const void *data, size_t size) { return 0; }
void retro_cheat_reset(void) {}
void retro_cheat_set(unsigned index, int enabled, const char *code) {}
int retro_load_game(const void *game) { return 1; }
int retro_load_game_special(unsigned game_type, const void *info, size_t num_info) { return 0; }
void retro_unload_game(void) {}
unsigned retro_get_region(void) { return 0; }
void *retro_get_memory_data(unsigned id) { return NULL; }
size_t retro_get_memory_size(unsigned id) { return 0; }
"@

# Écrire le fichier de test
Set-Content "test_minimal_core.c" $TEST_SOURCE -Encoding UTF8

Write-Host "🔨 Compilation du core minimal..." -ForegroundColor Yellow

# Compiler le core minimal avec une commande simple
$COMPILE_COMMAND = "$CC $CFLAGS test_minimal_core.c $LDFLAGS -o fceumm_libretro_android.so"

Write-Host "Commande: $COMPILE_COMMAND" -ForegroundColor Cyan

try {
    # Exécuter la commande de compilation
    & cmd /c $COMPILE_COMMAND
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Core minimal compilé avec succès!" -ForegroundColor Green
        
        # Copier le core
        if (Test-Path "fceumm_libretro_android.so") {
            Copy-Item "fceumm_libretro_android.so" "$OUTPUT_DIR\" -Force
            Write-Host "📁 Core copié vers $OUTPUT_DIR" -ForegroundColor Green
            
            # Vérifier la taille du fichier
            $fileSize = (Get-Item "$OUTPUT_DIR\fceumm_libretro_android.so").Length
            Write-Host "📏 Taille du fichier: $fileSize bytes" -ForegroundColor Cyan
        } else {
            Write-Host "❌ Fichier de sortie non trouvé" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ Échec de la compilation" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Erreur lors de la compilation: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "🎉 Test terminé!" -ForegroundColor Green 