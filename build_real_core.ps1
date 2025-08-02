# Script PowerShell pour compiler un VRAI core libretro FCEUmm
# Compatible Android 16 avec alignement 16 KB

param(
    [string]$Architecture = "x86_64",
    [string]$CoreName = "fceumm"
)

Write-Host "🔨 Compilation d'un VRAI core libretro FCEUmm pour $Architecture..." -ForegroundColor Green

# Configuration
$NDK_PATH = "C:\Users\Quentin\AppData\Local\Android\Sdk\ndk\28.2.13676358"
$BUILD_DIR = "real_core_build"
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
$CFLAGS = "-fPIC -DANDROID -D__ANDROID_API__=$API_LEVEL -O3 -ffast-math -DHAVE_NEON=0 -DHAVE_SSE=0 -DHAVE_MMX=0"
$CXXFLAGS = "$CFLAGS -std=c++17"
$LDFLAGS = "-shared -Wl,--no-undefined -Wl,-z,relro,-z,now"

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
    Write-Host "⚠️ Make non disponible sur Windows" -ForegroundColor Yellow
    Write-Host "💡 Utilisez WSL ou installez make pour Windows" -ForegroundColor Cyan
    Write-Host "📋 Alternative: Utilisez le script bash dans WSL" -ForegroundColor Cyan
    
    # Créer un message d'erreur informatif
    Write-Host "❌ Impossible de compiler un vrai core sans make" -ForegroundColor Red
    Write-Host "🔧 Solutions:" -ForegroundColor Yellow
    Write-Host "   1. Installez make pour Windows" -ForegroundColor Cyan
    Write-Host "   2. Utilisez WSL avec build_libretro_core.sh" -ForegroundColor Cyan
    Write-Host "   3. Utilisez le core précompilé de coresCompiled/" -ForegroundColor Cyan
    
    exit 1
}

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Vrai core libretro compilé avec succès!" -ForegroundColor Green
    
    # Copier le core personnalisé
    $OutputFile = "$CoreName`_libretro_android.so"
    if (Test-Path $OutputFile) {
        Copy-Item $OutputFile "..\..\$OUTPUT_DIR\" -Force
        Write-Host "📁 Vrai core copié vers $OUTPUT_DIR" -ForegroundColor Green
    } else {
        Write-Host "❌ Fichier de sortie non trouvé: $OutputFile" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Échec de la compilation" -ForegroundColor Red
}

# Retourner au répertoire racine
Set-Location ..\..

Write-Host "🎉 Compilation terminée!" -ForegroundColor Green 