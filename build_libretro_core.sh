#!/bin/bash

# Script pour compiler le core libretro FCEUmm pour Android
set -e

# Configuration
ANDROID_NDK="/home/quentin/Android/Sdk/ndk/28.2.13676358"
FCEUMM_DIR="libretro-super/libretro-fceumm"
BUILD_DIR="build_libretro"
TARGET_ARCH="arm64-v8a"  # ou armeabi-v7a, x86, x86_64

# Créer le répertoire de build
mkdir -p $BUILD_DIR
cd $BUILD_DIR

# Configuration selon l'architecture
case $TARGET_ARCH in
    "arm64-v8a")
        TOOLCHAIN="$ANDROID_NDK/toolchains/llvm/prebuilt/linux-x86_64"
        TARGET="aarch64-linux-android"
        API_LEVEL="21"
        ;;
    "armeabi-v7a")
        TOOLCHAIN="$ANDROID_NDK/toolchains/llvm/prebuilt/linux-x86_64"
        TARGET="armv7a-linux-androideabi"
        API_LEVEL="21"
        ;;
    "x86")
        TOOLCHAIN="$ANDROID_NDK/toolchains/llvm/prebuilt/linux-x86_64"
        TARGET="i686-linux-android"
        API_LEVEL="21"
        ;;
    "x86_64")
        TOOLCHAIN="$ANDROID_NDK/toolchains/llvm/prebuilt/linux-x86_64"
        TARGET="x86_64-linux-android"
        API_LEVEL="21"
        ;;
    *)
        echo "Architecture non supportée: $TARGET_ARCH"
        exit 1
        ;;
esac

# Variables d'environnement
export PATH="$TOOLCHAIN/bin:$PATH"
export CC="$TOOLCHAIN/bin/$TARGET$API_LEVEL-clang"
export CXX="$TOOLCHAIN/bin/$TARGET$API_LEVEL-clang++"
export AR="$TOOLCHAIN/bin/llvm-ar"
export STRIP="$TOOLCHAIN/bin/llvm-strip"
export RANLIB="$TOOLCHAIN/bin/llvm-ranlib"

# Flags de compilation
CFLAGS="-fPIC -DANDROID -D__ANDROID_API__=$API_LEVEL -O3 -ffast-math"
CXXFLAGS="$CFLAGS -std=c++17"
LDFLAGS="-shared -Wl,--no-undefined"

echo "Compilation du core libretro FCEUmm pour $TARGET_ARCH..."
echo "Toolchain: $TOOLCHAIN"
echo "Target: $TARGET"
echo "API Level: $API_LEVEL"

# Compiler le core
cd ../$FCEUMM_DIR

# Sources principales
SOURCES="
src/fceu.c
src/fceu-memory.c
src/fceu-endian.c
src/cart.c
src/cheat.c
src/crc32.c
src/debug.c
src/fds.c
src/fds_apu.c
src/file.c
src/filter.c
src/general.c
src/ines.c
src/input.c
src/md5.c
src/nsf.c
src/palette.c
src/ppu.c
src/sound.c
src/state.c
src/unif.c
src/video.c
src/vsuni.c
src/x6502.c
"

# Sources des mappers (ajouter selon les besoins)
MAPPER_SOURCES="
src/boards/01-222.c
src/boards/02-234.c
src/boards/03-243.c
src/boards/04-244.c
src/boards/05-245.c
src/boards/06-246.c
src/boards/07-250.c
src/boards/08-252.c
src/boards/09-253.c
src/boards/10-255.c
src/boards/11-256.c
src/boards/12-257.c
src/boards/13-258.c
src/boards/14-262.c
src/boards/15-263.c
src/boards/16-264.c
src/boards/17-265.c
src/boards/18-266.c
src/boards/19-267.c
src/boards/20-268.c
src/boards/21-269.c
src/boards/22-270.c
src/boards/23-271.c
src/boards/24-272.c
src/boards/25-273.c
src/boards/26-274.c
src/boards/27-275.c
src/boards/28-276.c
src/boards/29-277.c
src/boards/30-278.c
src/boards/31-279.c
src/boards/32-280.c
src/boards/33-281.c
src/boards/34-282.c
src/boards/35-283.c
src/boards/36-284.c
src/boards/37-285.c
src/boards/38-286.c
src/boards/39-287.c
src/boards/40-288.c
src/boards/41-289.c
src/boards/42-290.c
src/boards/43-291.c
src/boards/44-292.c
src/boards/45-293.c
src/boards/46-294.c
src/boards/47-295.c
src/boards/48-296.c
src/boards/49-297.c
src/boards/50-298.c
src/boards/51-299.c
src/boards/52-300.c
src/boards/53-301.c
src/boards/54-302.c
src/boards/55-303.c
src/boards/56-304.c
src/boards/57-305.c
src/boards/58-306.c
src/boards/59-307.c
src/boards/60-308.c
src/boards/61-309.c
src/boards/62-310.c
src/boards/63-311.c
src/boards/64-312.c
src/boards/65-313.c
src/boards/66-314.c
src/boards/67-315.c
src/boards/68-316.c
src/boards/69-317.c
src/boards/70-318.c
src/boards/71-319.c
src/boards/72-320.c
src/boards/73-321.c
src/boards/74-322.c
src/boards/75-323.c
src/boards/76-324.c
src/boards/77-325.c
src/boards/78-326.c
src/boards/79-327.c
src/boards/80-328.c
src/boards/81-329.c
src/boards/82-330.c
src/boards/83-331.c
src/boards/84-332.c
src/boards/85-333.c
src/boards/86-334.c
src/boards/87-335.c
src/boards/88-336.c
src/boards/89-337.c
src/boards/90-338.c
src/boards/91-339.c
src/boards/92-340.c
src/boards/93-341.c
src/boards/94-342.c
src/boards/95-343.c
src/boards/96-344.c
src/boards/97-345.c
src/boards/98-346.c
src/boards/99-347.c
src/boards/100-348.c
src/boards/101-349.c
src/boards/102-350.c
src/boards/103-351.c
src/boards/104-352.c
src/boards/105-353.c
src/boards/106-354.c
src/boards/107-355.c
src/boards/108-356.c
src/boards/109-357.c
src/boards/110-358.c
src/boards/111-359.c
src/boards/112-360.c
src/boards/113-361.c
src/boards/114-362.c
src/boards/115-363.c
src/boards/116-364.c
src/boards/117-365.c
src/boards/118-366.c
src/boards/119-367.c
src/boards/120-368.c
src/boards/121-369.c
src/boards/122-370.c
src/boards/123-371.c
src/boards/124-372.c
src/boards/125-373.c
src/boards/126-374.c
src/boards/127-375.c
src/boards/128-376.c
src/boards/129-377.c
src/boards/130-378.c
src/boards/131-379.c
src/boards/132-380.c
src/boards/133-381.c
src/boards/134-382.c
src/boards/135-383.c
src/boards/136-384.c
src/boards/137-385.c
src/boards/138-386.c
src/boards/139-387.c
src/boards/140-388.c
src/boards/141-389.c
src/boards/142-390.c
src/boards/143-391.c
src/boards/144-392.c
src/boards/145-393.c
src/boards/146-394.c
src/boards/147-395.c
src/boards/148-396.c
src/boards/149-397.c
src/boards/150-398.c
src/boards/151-399.c
src/boards/152-400.c
src/boards/153-401.c
src/boards/154-402.c
src/boards/155-403.c
src/boards/156-404.c
src/boards/157-405.c
src/boards/158-406.c
src/boards/159-407.c
src/boards/160-408.c
src/boards/161-409.c
src/boards/162-410.c
src/boards/163-411.c
src/boards/164-412.c
src/boards/165-413.c
src/boards/166-414.c
src/boards/167-415.c
src/boards/168-416.c
src/boards/169-417.c
src/boards/170-418.c
src/boards/171-419.c
src/boards/172-420.c
src/boards/173-421.c
src/boards/174-422.c
src/boards/175-423.c
src/boards/176-424.c
src/boards/177-425.c
src/boards/178-426.c
src/boards/179-427.c
src/boards/180-428.c
src/boards/181-429.c
src/boards/182-430.c
src/boards/183-431.c
src/boards/184-432.c
src/boards/185-433.c
src/boards/186-434.c
src/boards/187-435.c
src/boards/188-436.c
src/boards/189-437.c
src/boards/190-438.c
src/boards/191-439.c
src/boards/192-440.c
src/boards/193-441.c
src/boards/194-442.c
src/boards/195-443.c
src/boards/196-444.c
src/boards/197-445.c
src/boards/198-446.c
src/boards/199-447.c
src/boards/200-448.c
src/boards/201-449.c
src/boards/202-450.c
src/boards/203-451.c
src/boards/204-452.c
src/boards/205-453.c
src/boards/206-454.c
src/boards/207-455.c
src/boards/208-456.c
src/boards/209-457.c
src/boards/210-458.c
src/boards/211-459.c
src/boards/212-460.c
src/boards/213-461.c
src/boards/214-462.c
src/boards/215-463.c
src/boards/216-464.c
src/boards/217-465.c
src/boards/218-466.c
src/boards/219-467.c
src/boards/220-468.c
src/boards/221-469.c
src/boards/222-470.c
src/boards/223-471.c
src/boards/224-472.c
src/boards/225-473.c
src/boards/226-474.c
src/boards/227-475.c
src/boards/228-476.c
src/boards/229-477.c
src/boards/230-478.c
src/boards/231-479.c
src/boards/232-480.c
src/boards/233-481.c
src/boards/234-482.c
src/boards/235-483.c
src/boards/236-484.c
src/boards/237-485.c
src/boards/238-486.c
src/boards/239-487.c
src/boards/240-488.c
src/boards/241-489.c
src/boards/242-490.c
src/boards/243-491.c
src/boards/244-492.c
src/boards/245-493.c
src/boards/246-494.c
src/boards/247-495.c
src/boards/248-496.c
src/boards/249-497.c
src/boards/250-498.c
src/boards/251-499.c
src/boards/252-500.c
src/boards/253-501.c
src/boards/254-502.c
src/boards/255-503.c
"

# Sources des entrées
INPUT_SOURCES="
src/input/arkanoid.c
src/input/mouse.c
src/input/zapper.c
src/input/powerpad.c
src/input/shadow.c
src/input/oekakids.c
src/input/fkb.c
src/input/suborkb.c
src/input/pec586kb.c
src/input/hypershot.c
src/input/mahjong.c
src/input/quiz.c
src/input/ftrainer.c
src/input/bworld.c
src/input/toprider.c
"

# Sources libretro
LIBRETRO_SOURCES="
src/drivers/libretro/libretro.c
src/drivers/libretro/libretro_dipswitch.c
"

# Compiler tous les fichiers source
echo "Compilation des fichiers source..."

# Compiler les sources principales
for src in $SOURCES; do
    if [ -f "$src" ]; then
        echo "Compiling $src..."
        $CC $CFLAGS -c "$src" -o "${src%.c}.o"
    fi
done

# Compiler les mappers
for src in $MAPPER_SOURCES; do
    if [ -f "$src" ]; then
        echo "Compiling $src..."
        $CC $CFLAGS -c "$src" -o "${src%.c}.o"
    fi
done

# Compiler les entrées
for src in $INPUT_SOURCES; do
    if [ -f "$src" ]; then
        echo "Compiling $src..."
        $CC $CFLAGS -c "$src" -o "${src%.c}.o"
    fi
done

# Compiler les sources libretro
for src in $LIBRETRO_SOURCES; do
    if [ -f "$src" ]; then
        echo "Compiling $src..."
        $CC $CFLAGS -c "$src" -o "${src%.c}.o"
    fi
done

# Lier la bibliothèque
echo "Liaison de la bibliothèque..."
$CC $LDFLAGS -o libfceumm.so *.o -lm

# Nettoyer les fichiers objets
rm -f *.o

echo "Core libretro FCEUmm compilé avec succès: libfceumm.so"

# Copier dans le projet Android
cp libfceumm.so ../../app/src/main/jniLibs/$TARGET_ARCH/

echo "Core copié vers app/src/main/jniLibs/$TARGET_ARCH/" 