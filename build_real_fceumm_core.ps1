# Script PowerShell final pour compiler un vrai core FCEUmm
# Utilise les techniques qui fonctionnent du script ultra-simple

param(
    [string]$Architecture = "x86_64"
)

Write-Host "🔨 Compilation d'un vrai core FCEUmm pour $Architecture..." -ForegroundColor Green

# Configuration
$NDK_PATH = "C:\Users\Quentin\AppData\Local\Android\Sdk\ndk\28.2.13676358"
$BUILD_DIR = "real_fceumm_build"
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
$CXX = "$TOOLCHAIN\bin\$TARGET$API_LEVEL-clang++.cmd"
$AR = "$TOOLCHAIN\bin\llvm-ar.cmd"
$STRIP = "$TOOLCHAIN\bin\llvm-strip.cmd"
$RANLIB = "$TOOLCHAIN\bin\llvm-ranlib.cmd"

# Flags de compilation
$CFLAGS = "-fPIC -DANDROID -D__ANDROID_API__=$API_LEVEL -O3 -ffast-math -DHAVE_NEON=0 -DHAVE_SSE=0 -DHAVE_MMX=0"
$CXXFLAGS = "$CFLAGS -std=c++17"
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

# Télécharger les headers libretro si nécessaire
$INCLUDE_DIR = "src\drivers\libretro\include"
if (!(Test-Path "$INCLUDE_DIR\libretro.h")) {
    Write-Host "📥 Téléchargement des headers libretro..." -ForegroundColor Yellow
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
        exit 1
    }
}

# Cloner libretro-common si nécessaire
$LIBRETRO_COMMON_DIR = "src\drivers\libretro\libretro-common"
if (!(Test-Path $LIBRETRO_COMMON_DIR)) {
    Write-Host "📥 Clonage de libretro-common..." -ForegroundColor Yellow
    try {
        git clone https://github.com/libretro/libretro-common.git $LIBRETRO_COMMON_DIR
        Write-Host "✅ libretro-common cloné" -ForegroundColor Green
    } catch {
        Write-Host "❌ Erreur lors du clonage de libretro-common" -ForegroundColor Red
        exit 1
    }
}

# Créer un Makefile simplifié mais complet
$SIMPLE_MAKEFILE = @"
# Makefile simplifié pour Android FCEUmm
TARGET := fceumm_libretro_android.so
CC := $CC
CXX := $CXX
AR := $AR
STRIP := $STRIP
RANLIB := $RANLIB

CFLAGS := $CFLAGS -Isrc/drivers/libretro/include -Isrc/drivers/libretro/libretro-common/include
CXXFLAGS := $CXXFLAGS -Isrc/drivers/libretro/include -Isrc/drivers/libretro/libretro-common/include
LDFLAGS := $LDFLAGS

# Sources principales
SOURCES := src/drivers/libretro/libretro.c src/drivers/libretro/libretro_dipswitch.c
SOURCES += src/cart.c src/cheat.c src/crc32.c src/fceu-endian.c src/fceu-memory.c
SOURCES += src/fceu.c src/fds.c src/fds_apu.c src/file.c src/filter.c src/general.c
SOURCES += src/input.c src/md5.c src/nsf.c src/palette.c src/ppu.c src/sound.c src/state.c

# Sources des boards (sélection des plus importants)
SOURCES += src/boards/mmc1.c src/boards/mmc2and4.c src/boards/mmc3.c src/boards/mmc5.c
SOURCES += src/boards/vrc1.c src/boards/vrc2and4.c src/boards/vrc3.c src/boards/vrc6.c
SOURCES += src/boards/vrc7.c src/boards/vrc7p.c src/boards/vrcirq.c
SOURCES += src/boards/fds.c src/boards/n106.c src/boards/nsf.c
SOURCES += src/boards/24c01.c src/boards/24c02.c src/boards/24c21.c src/boards/24c22.c
SOURCES += src/boards/24c32.c src/boards/24c33.c src/boards/24c66.c src/boards/24c67.c
SOURCES += src/boards/24c88.c src/boards/24c93.c src/boards/24cxx.c
SOURCES += src/boards/77.c src/boards/8157.c src/boards/8237.c src/boards/8255a.c
SOURCES += src/boards/8255d.c src/boards/88-2mb.c src/boards/90-24c01.c src/boards/90-24c02.c
SOURCES += src/boards/95.c src/boards/a9746.c src/boards/ac08.c src/boards/afterburner.c
SOURCES += src/boards/alien.c src/boards/bandai.c src/boards/bb.c src/boards/bmc13in1j110.c
SOURCES += src/boards/bmc42in1r.c src/boards/bmc64in1nr.c src/boards/bmc70in1.c
SOURCES += src/boards/bmc72in1.c src/boards/bmc8157.c src/boards/bmc_11160.c
SOURCES += src/boards/bmc_12in1.c src/boards/bmc_150in1.c src/boards/bmc_190in1.c
SOURCES += src/boards/bmc_20in1.c src/boards/bmc_21in1.c src/boards/bmc_22in1.c
SOURCES += src/boards/bmc_23in1.c src/boards/bmc_31in1.c src/boards/bmc_35in1.c
SOURCES += src/boards/bmc_36in1.c src/boards/bmc_64in1.c src/boards/bmc_8157.c
SOURCES += src/boards/bmc_hik300.c src/boards/bmc_hik4.c src/boards/bmc_s2.c
SOURCES += src/boards/bmc_s4.c src/boards/bmc_s8.c src/boards/bmc_sa.c src/boards/bmc_sb.c
SOURCES += src/boards/bmc_tf1201.c src/boards/bmc_ws.c src/boards/bonza.c src/boards/bs-5.c
SOURCES += src/boards/cityfighter.c src/boards/coolboy.c src/boards/coolgirl.c src/boards/d103.c
SOURCES += src/boards/dance2000.c src/boards/datalatch.c src/boards/dreamtech01.c
SOURCES += src/boards/edu2000.c src/boards/emu2413.c src/boards/famicombox.c
SOURCES += src/boards/faridunrom.c src/boards/fk23c.c src/boards/flashrom.c
SOURCES += src/boards/gn26.c src/boards/h2288.c src/boards/hp10xx_hp20xx.c
SOURCES += src/boards/hp898f.c src/boards/inx007t.c src/boards/jyasic.c
SOURCES += src/boards/karaoke.c src/boards/kof97.c src/boards/latch.c src/boards/le05.c
SOURCES += src/boards/lh32.c src/boards/lh51.c src/boards/lh53.c src/boards/malee.c
SOURCES += src/boards/mihunche.c src/boards/n625092.c src/boards/novel.c
SOURCES += src/boards/onebus.c src/boards/pec-586.c src/boards/pic16c5x.c
SOURCES += src/boards/resetnromxin1.c src/boards/resettxrom.c src/boards/rt-01.c
SOURCES += src/boards/sachen.c src/boards/sheroes.c src/boards/sl1632.c
SOURCES += src/boards/subor.c src/boards/super40in1.c src/boards/supervision.c
SOURCES += src/boards/t-227-1.c src/boards/t-262.c src/boards/tengen.c
SOURCES += src/boards/transformer.c src/boards/txcchip.c src/boards/unrom512.c
SOURCES += src/boards/yoko.c

# Sources des inputs
SOURCES += src/input/arkanoid.c src/input/bbox.c src/input/bworld.c src/input/cursor.c
SOURCES += src/input/fkb.c src/input/ftrainer.c src/input/hypershot.c src/input/mahjong.c
SOURCES += src/input/mouse.c src/input/oekakids.c src/input/pec586kb.c src/input/powerpad.c
SOURCES += src/input/quiz.c src/input/shadow.c src/input/suborkb.c src/input/toprider.c
SOURCES += src/input/zapper.c

# Sources NTSC
SOURCES += src/ntsc/nes_ntsc.c

# Objets
OBJECTS := `$(SOURCES:.c=.o)

# Règles
all: `$(TARGET)

`$(TARGET): `$(OBJECTS)
	`$(CC) `$(OBJECTS) `$(LDFLAGS) -o `$(TARGET)

%.o: %.c
	`$(CC) `$(CFLAGS) -c `$< -o `$@

clean:
	rm -f `$(OBJECTS) `$(TARGET)

.PHONY: all clean
"@

# Écrire le Makefile simplifié
Set-Content "Makefile.simple" $SIMPLE_MAKEFILE -Encoding UTF8

Write-Host "🔨 Compilation avec Makefile simplifié..." -ForegroundColor Yellow

# Compiler avec le Makefile simplifié
& make -f Makefile.simple

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Core FCEUmm compilé avec succès!" -ForegroundColor Green
    
    # Copier le core personnalisé
    $OutputFile = "fceumm_libretro_android.so"
    if (Test-Path $OutputFile) {
        Copy-Item $OutputFile "..\..\$OUTPUT_DIR\" -Force
        Write-Host "📁 Core copié vers $OUTPUT_DIR" -ForegroundColor Green
        
        # Vérifier la taille du fichier
        $fileSize = (Get-Item "..\..\$OUTPUT_DIR\fceumm_libretro_android.so").Length
        Write-Host "📏 Taille du fichier: $fileSize bytes" -ForegroundColor Cyan
        
        if ($fileSize -gt 100000) {
            Write-Host "🎉 Core FCEUmm complet compilé avec succès!" -ForegroundColor Green
        } else {
            Write-Host "⚠️  Core semble trop petit, possible problème de compilation" -ForegroundColor Yellow
        }
    } else {
        Write-Host "❌ Fichier de sortie non trouvé: $OutputFile" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Échec de la compilation" -ForegroundColor Red
    Write-Host "💡 Essayez de corriger les erreurs et relancez le script" -ForegroundColor Cyan
}

# Retourner au répertoire racine
Set-Location ..\..

Write-Host "🎉 Compilation terminée!" -ForegroundColor Green 