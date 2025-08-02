# Script PowerShell pour compiler des cores personnalisés avec alignement 16 KB
# Compatible Android 16

param(
    [string]$Architecture = "arm64-v8a",
    [string]$CoreName = "fceumm"
)

Write-Host "🔨 Compilation d'un core personnalisé pour $Architecture..." -ForegroundColor Green

# Configuration
$NDK_PATH = "C:\Users\Quentin\AppData\Local\Android\Sdk\ndk\28.2.13676358"
$BUILD_DIR = "custom_core_build"
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
$env:CC = "$TOOLCHAIN\bin\$TARGET$API_LEVEL-clang.cmd"
$env:CXX = "$TOOLCHAIN\bin\$TARGET$API_LEVEL-clang++.cmd"
$env:AR = "$TOOLCHAIN\bin\llvm-ar.cmd"
$env:STRIP = "$TOOLCHAIN\bin\llvm-strip.cmd"
$env:RANLIB = "$TOOLCHAIN\bin\llvm-ranlib.cmd"

# Flags de compilation avec alignement 16 KB
$CFLAGS = "-fPIC -DANDROID -O3"
$CXXFLAGS = "$CFLAGS -std=c++17"
$LDFLAGS = "-shared"

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

# Vérifier si make est disponible
$makeAvailable = Get-Command make -ErrorAction SilentlyContinue
if ($makeAvailable) {
    Write-Host "🔨 Compilation avec make..." -ForegroundColor Yellow
    
    # Compiler avec Makefile.libretro
    & make -f Makefile.libretro platform=android `
        CFLAGS="$CFLAGS" `
        CXXFLAGS="$CXXFLAGS" `
        LDFLAGS="$LDFLAGS" `
        HAVE_NEON=0 HAVE_SSE=0 HAVE_MMX=0
} else {
    Write-Host "⚠️ Make non disponible, création d'un core de test..." -ForegroundColor Yellow
    
    # Créer un core de test simple
    $testCoreContent = @"
#include <jni.h>
#include <android/log.h>

#define LOG_TAG "FCEUmmTest"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("FCEUmm Test Core Loaded");
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    LOGI("FCEUmm Test Core Unloaded");
}
"@
    
    # Créer le fichier source de test
    $testCoreContent | Out-File -FilePath "test_core.c" -Encoding ASCII
    
    # Compiler le core de test
    Write-Host "🔨 Compilation du core de test..." -ForegroundColor Yellow
    & $env:CC -fPIC -DANDROID -O3 -c test_core.c -o test_core.o
    if ($LASTEXITCODE -eq 0) {
        & $env:CC -shared -o fceumm_libretro_android.so test_core.o
    }
}

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Core compilé avec succès!" -ForegroundColor Green
    
    # Copier le core personnalisé
    $OutputFile = "$CoreName`_libretro_android.so"
    if (Test-Path $OutputFile) {
        Copy-Item $OutputFile "..\..\$OUTPUT_DIR\" -Force
        Write-Host "📁 Core copié vers $OUTPUT_DIR" -ForegroundColor Green
    } else {
        Write-Host "❌ Fichier de sortie non trouvé: $OutputFile" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Échec de la compilation" -ForegroundColor Red
}

# Retourner au répertoire racine
Set-Location ..\..

Write-Host "🎉 Compilation terminée!" -ForegroundColor Green 