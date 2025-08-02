# Script pour compiler un core avec validations correctes et logs détaillés
param(
    [string]$Architecture = "arm64-v8a"
)

Write-Host "🔨 Compilation d'un core avec validations correctes pour $Architecture..." -ForegroundColor Green

# Configuration
$NDK_PATH = "C:\Users\Quentin\AppData\Local\Android\Sdk\ndk\28.2.13676358"
$BUILD_DIR = "proper_core_build"
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
$AR = "$TOOLCHAIN\bin\llvm-ar.cmd"
$STRIP = "$TOOLCHAIN\bin\llvm-strip.cmd"

# Flags de compilation
$CFLAGS = "-fPIC -DANDROID -D__ANDROID_API__=$API_LEVEL -O2 -ffast-math -DHAVE_NEON=0 -DHAVE_SSE=0 -DHAVE_MMX=0"
$LDFLAGS = "-shared -Wl,--no-undefined -Wl,-z,relro,-z,now -lm"

Write-Host "📋 Configuration:" -ForegroundColor Yellow
Write-Host "  Architecture: $Architecture" -ForegroundColor Cyan
Write-Host "  Target: $TARGET" -ForegroundColor Cyan
Write-Host "  API Level: $API_LEVEL" -ForegroundColor Cyan

# Aller dans le répertoire de build
Set-Location $BUILD_DIR

# Créer un core avec validations correctes et logs détaillés
$PROPER_CORE = @"
#include <libretro.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <math.h>

// Variables globales libretro
retro_log_printf_t log_cb;
retro_video_refresh_t video_cb;
retro_audio_sample_batch_t audio_batch_cb;
retro_input_poll_t input_poll_cb;
retro_input_state_t input_state_cb;
retro_environment_t environ_cb;

// Variables d'état
static bool initialized = false;
static bool game_loaded = false;
static unsigned frame_width = 256;
static unsigned frame_height = 240;
static unsigned frame_pitch = 256;
static uint16_t frame_buffer[256 * 240];

// Buffer ROM plus grand pour supporter les ROMs NES (jusqu'à 1MB)
static uint8_t rom_data[1048576]; // 1MB pour supporter les ROMs plus grandes
static size_t rom_size = 0;

// Couleurs pour l'affichage
#define COLOR_BLACK     0x0000
#define COLOR_WHITE     0xFFFF
#define COLOR_RED       0xF800
#define COLOR_GREEN     0x07E0
#define COLOR_BLUE      0x001F
#define COLOR_YELLOW    0xFFE0
#define COLOR_CYAN      0x07FF
#define COLOR_MAGENTA   0xF81F

void retro_init(void) {
    initialized = true;
    if (log_cb) log_cb(RETRO_LOG_INFO, "Core avec validations correctes initialisé\n");
}

void retro_deinit(void) {
    initialized = false;
    game_loaded = false;
    rom_size = 0;
}

unsigned retro_api_version(void) {
    return RETRO_API_VERSION;
}

void retro_get_system_info(struct retro_system_info *info) {
    info->library_name = "FCEUmm Validé";
    info->library_version = "1.0.0";
    info->valid_extensions = "nes|NES";
    info->need_fullpath = false;
    info->block_extract = false;
}

void retro_get_system_av_info(struct retro_system_av_info *info) {
    info->geometry.base_width = frame_width;
    info->geometry.base_height = frame_height;
    info->geometry.max_width = frame_width;
    info->geometry.max_height = frame_height;
    info->geometry.aspect_ratio = 4.0f / 3.0f;
    
    info->timing.fps = 60.0;
    info->timing.sample_rate = 44100.0;
}

void retro_set_environment(retro_environment_t callback) {
    environ_cb = callback;
}

void retro_set_video_refresh(retro_video_refresh_t callback) {
    video_cb = callback;
}

void retro_set_audio_sample_batch(retro_audio_sample_batch_t callback) {
    audio_batch_cb = callback;
}

void retro_set_input_poll(retro_input_poll_t callback) {
    input_poll_cb = callback;
}

void retro_set_input_state(retro_input_state_t callback) {
    input_state_cb = callback;
}

void retro_set_controller_port_device(unsigned port, unsigned device) {
    // Pas d'implémentation pour le moment
}

void retro_reset(void) {
    // Reset minimal
}

bool retro_load_game(const struct retro_game_info *game) {
    if (log_cb) log_cb(RETRO_LOG_INFO, "=== DÉBUT retro_load_game ===\n");
    
    // Validation 1: Vérifier que game n'est pas NULL
    if (!game) {
        if (log_cb) log_cb(RETRO_LOG_ERROR, "❌ Game info est NULL\n");
        return false;
    }
    if (log_cb) log_cb(RETRO_LOG_INFO, "✅ Game info non NULL\n");
    
    // Validation 2: Vérifier que les données ROM existent
    if (!game->data) {
        if (log_cb) log_cb(RETRO_LOG_ERROR, "❌ Données ROM sont NULL\n");
        return false;
    }
    if (log_cb) log_cb(RETRO_LOG_INFO, "✅ Données ROM non NULL\n");
    
    // Validation 3: Vérifier que la taille n'est pas 0
    if (game->size == 0) {
        if (log_cb) log_cb(RETRO_LOG_ERROR, "❌ Taille ROM est 0\n");
        return false;
    }
    if (log_cb) log_cb(RETRO_LOG_INFO, "✅ Taille ROM: %zu bytes\n", game->size);
    
    // Validation 4: Vérifier que la ROM n'est pas trop grande
    if (game->size > sizeof(rom_data)) {
        if (log_cb) log_cb(RETRO_LOG_ERROR, "❌ ROM trop grande: %zu bytes (max: %zu)\n", game->size, sizeof(rom_data));
        return false;
    }
    if (log_cb) log_cb(RETRO_LOG_INFO, "✅ Taille ROM dans les limites\n");
    
    // Validation 5: Vérifier que c'est une ROM NES valide (header NES)
    const uint8_t* rom_header = (const uint8_t*)game->data;
    if (game->size < 16) {
        if (log_cb) log_cb(RETRO_LOG_ERROR, "❌ ROM trop petite pour avoir un header NES\n");
        return false;
    }
    
    // Vérifier le header NES (N E S 0x1A)
    if (rom_header[0] != 'N') {
        if (log_cb) log_cb(RETRO_LOG_ERROR, "❌ Header NES invalide: premier byte = 0x%02X (attendu: 'N')\n", rom_header[0]);
        return false;
    }
    if (rom_header[1] != 'E') {
        if (log_cb) log_cb(RETRO_LOG_ERROR, "❌ Header NES invalide: deuxième byte = 0x%02X (attendu: 'E')\n", rom_header[1]);
        return false;
    }
    if (rom_header[2] != 'S') {
        if (log_cb) log_cb(RETRO_LOG_ERROR, "❌ Header NES invalide: troisième byte = 0x%02X (attendu: 'S')\n", rom_header[2]);
        return false;
    }
    if (rom_header[3] != 0x1A) {
        if (log_cb) log_cb(RETRO_LOG_ERROR, "❌ Header NES invalide: quatrième byte = 0x%02X (attendu: 0x1A)\n", rom_header[3]);
        return false;
    }
    
    if (log_cb) log_cb(RETRO_LOG_INFO, "✅ Header NES valide détecté\n");
    
    // Afficher les informations du header NES
    uint8_t num_prg_banks = rom_header[4];
    uint8_t num_chr_banks = rom_header[5];
    uint8_t flags6 = rom_header[6];
    uint8_t flags7 = rom_header[7];
    
    if (log_cb) log_cb(RETRO_LOG_INFO, "📋 Header NES: PRG=%d, CHR=%d, Flags6=0x%02X, Flags7=0x%02X\n", 
                       num_prg_banks, num_chr_banks, flags6, flags7);
    
    // Tout est OK, copier les données
    memcpy(rom_data, game->data, game->size);
    rom_size = game->size;
    game_loaded = true;
    
    if (log_cb) log_cb(RETRO_LOG_INFO, "✅ ROM chargée avec succès: %zu bytes\n", rom_size);
    if (log_cb) log_cb(RETRO_LOG_INFO, "=== FIN retro_load_game (SUCCÈS) ===\n");
    return true;
}

void retro_unload_game(void) {
    game_loaded = false;
    rom_size = 0;
    memset(rom_data, 0, sizeof(rom_data));
}

unsigned retro_get_region(void) {
    return RETRO_REGION_NTSC;
}

void *retro_get_memory_data(unsigned id) {
    if (id == RETRO_MEMORY_SAVE_RAM) {
        return rom_data;
    }
    return NULL;
}

size_t retro_get_memory_size(unsigned id) {
    if (id == RETRO_MEMORY_SAVE_RAM) {
        return rom_size;
    }
    return 0;
}

void retro_cheat_reset(void) {
    // Pas d'implémentation
}

void retro_cheat_set(unsigned index, bool enabled, const char *code) {
    // Pas d'implémentation
}

void retro_run(void) {
    if (!initialized) return;
    
    static int frame_count = 0;
    frame_count++;
    
    // Créer un motif de test avec information sur la ROM
    for (int y = 0; y < frame_height; y++) {
        for (int x = 0; x < frame_width; x++) {
            int index = y * frame_pitch + x;
            uint16_t color;
            
            // Créer un motif de damier animé
            if (((x / 16) + (y / 16) + frame_count) % 2 == 0) {
                color = COLOR_GREEN; // Vert pour indiquer que c'est le core validé
            } else {
                color = COLOR_BLUE;
            }
            
            // Ajouter une bordure blanche
            if (x < 8 || x >= frame_width - 8 || y < 8 || y >= frame_height - 8) {
                color = COLOR_WHITE;
            }
            
            // Afficher la taille de la ROM en jaune
            if (y >= 100 && y < 120 && x >= 80 && x < 176) {
                int char_x = (x - 80) / 8;
                int char_y = (y - 100) / 10;
                if (char_x < 12 && char_y < 2) {
                    color = COLOR_YELLOW;
                }
            }
            
            frame_buffer[index] = color;
        }
    }
    
    // Envoyer l'image
    if (video_cb) {
        video_cb(frame_buffer, frame_width, frame_height, frame_pitch * 2);
    }
    
    // Audio avec un son de test
    if (audio_batch_cb) {
        int16_t audio_buffer[735]; // 44100 Hz / 60 FPS
        
        // Générer un son de test simple
        for (int i = 0; i < 735; i++) {
            int sample = (int)(32767.0 * 0.1 * sin(2.0 * 3.14159 * 440.0 * (frame_count * 735 + i) / 44100.0));
            audio_buffer[i] = (int16_t)sample;
        }
        
        audio_batch_cb(audio_buffer, 735);
    }
}
"@

# Créer le fichier source
$SOURCE_FILE = "proper_core.c"
Set-Content $SOURCE_FILE $PROPER_CORE -Encoding UTF8

# Télécharger libretro.h
$LIBRETRO_H_URL = "https://raw.githubusercontent.com/libretro/libretro-common/master/include/libretro.h"
$LIBRETRO_H_PATH = "libretro.h"

try {
    Invoke-WebRequest -Uri $LIBRETRO_H_URL -OutFile $LIBRETRO_H_PATH
    Write-Host "✅ libretro.h téléchargé" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur lors du téléchargement de libretro.h" -ForegroundColor Red
    exit 1
}

# Compiler le core avec validations
Write-Host "🔨 Compilation du core avec validations..." -ForegroundColor Yellow

$COMPILE_CMD = "$CC $CFLAGS -I. -c $SOURCE_FILE -o proper_core.o"
Write-Host "Commande: $COMPILE_CMD" -ForegroundColor Cyan
& cmd /c $COMPILE_CMD

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Compilation réussie" -ForegroundColor Green
    
    # Lier le core
    $LINK_CMD = "$CC $LDFLAGS proper_core.o -o fceumm_libretro_android.so"
    Write-Host "Commande: $LINK_CMD" -ForegroundColor Cyan
    & cmd /c $LINK_CMD
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Liaison réussie" -ForegroundColor Green
        
        # Copier le core
        Copy-Item "fceumm_libretro_android.so" "..\$OUTPUT_DIR\" -Force
        Write-Host "📁 Core copié vers $OUTPUT_DIR" -ForegroundColor Green
        
        # Vérifier la taille
        $fileSize = (Get-Item "..\$OUTPUT_DIR\fceumm_libretro_android.so").Length
        Write-Host "📏 Taille du fichier: $fileSize bytes" -ForegroundColor Cyan
        
        Write-Host "🎉 Core avec validations compilé avec succès!" -ForegroundColor Green
    } else {
        Write-Host "❌ Échec de la liaison" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Échec de la compilation" -ForegroundColor Red
}

# Retourner au répertoire racine
Set-Location .. 