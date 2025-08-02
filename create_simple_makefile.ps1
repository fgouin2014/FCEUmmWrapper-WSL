# Script PowerShell pour créer un Makefile simplifié

param(
    [string]$Architecture = "x86_64"
)

Write-Host "📝 Création d'un Makefile simplifié..." -ForegroundColor Green

$BUILD_DIR = "real_fceumm_build"
$FCEUMM_DIR = "$BUILD_DIR\fceumm"

# Vérifier si le répertoire existe
if (!(Test-Path $FCEUMM_DIR)) {
    Write-Host "❌ Répertoire FCEUmm non trouvé. Exécutez d'abord build_real_fceumm_core.ps1" -ForegroundColor Red
    exit 1
}

Set-Location $FCEUMM_DIR

# Configuration selon l'architecture
switch ($Architecture) {
    "x86_64" {
        $TOOLCHAIN = "C:\Users\Quentin\AppData\Local\Android\Sdk\ndk\28.2.13676358\toolchains\llvm\prebuilt\windows-x86_64"
        $TARGET = "x86_64-linux-android"
        $API_LEVEL = "21"
    }
    default {
        Write-Host "❌ Architecture non supportée: $Architecture" -ForegroundColor Red
        exit 1
    }
}

$CC = "$TOOLCHAIN\bin\$TARGET$API_LEVEL-clang.cmd"
$CXX = "$TOOLCHAIN\bin\$TARGET$API_LEVEL-clang++.cmd"
$AR = "$TOOLCHAIN\bin\llvm-ar.cmd"
$STRIP = "$TOOLCHAIN\bin\llvm-strip.cmd"
$RANLIB = "$TOOLCHAIN\bin\llvm-ranlib.cmd"

$CFLAGS = "-fPIC -DANDROID -D__ANDROID_API__=$API_LEVEL -O3 -ffast-math -DHAVE_NEON=0 -DHAVE_SSE=0 -DHAVE_MMX=0"
$CXXFLAGS = "$CFLAGS -std=c++17"
$LDFLAGS = "-shared -Wl,--no-undefined -Wl,-z,relro,-z,now -lm"

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

# Sources principales (sans les fichiers problématiques)
SOURCES := src/drivers/libretro/libretro.c src/drivers/libretro/libretro_dipswitch.c
SOURCES += src/cart.c src/cheat.c src/crc32.c src/fceu-endian.c src/fceu-memory.c
SOURCES += src/fceu.c src/fds_apu.c src/file.c src/filter.c src/general.c
SOURCES += src/input.c src/md5.c src/nsf.c src/palette.c src/ppu.c src/sound.c src/state.c

# Sources des boards (sélection des plus importants, sans fds.c et nsf.c)
SOURCES += src/boards/mmc1.c src/boards/mmc2and4.c src/boards/mmc3.c src/boards/mmc5.c
SOURCES += src/boards/vrc1.c src/boards/vrc2and4.c src/boards/vrc3.c src/boards/vrc6.c
SOURCES += src/boards/vrc7.c src/boards/vrc7p.c src/boards/vrcirq.c
SOURCES += src/boards/n106.c
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

Write-Host "✅ Makefile simplifié créé" -ForegroundColor Green

# Retourner au répertoire racine
Set-Location ..\..

Write-Host "🎉 Création terminée!" -ForegroundColor Green
Write-Host "💡 Relancez maintenant: .\build_real_fceumm_core.ps1 -Architecture $Architecture" -ForegroundColor Cyan 