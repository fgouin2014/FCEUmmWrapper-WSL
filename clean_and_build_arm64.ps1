# Script pour nettoyer complètement et recompiler pour ARM64
param(
    [string]$Architecture = "arm64-v8a"
)

Write-Host "🧹 Nettoyage complet et compilation pour $Architecture..." -ForegroundColor Green

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
    default {
        Write-Host "❌ Architecture non supportée: $Architecture" -ForegroundColor Red
        exit 1
    }
}

Write-Host "📋 Configuration:" -ForegroundColor Cyan
Write-Host "  Architecture: $Architecture"
Write-Host "  Target: $TARGET"
Write-Host "  API Level: $API_LEVEL"
Write-Host "  Toolchain: $TOOLCHAIN"

# Aller dans le répertoire de build
Set-Location "$BUILD_DIR\fceumm"

# Nettoyer complètement tous les fichiers .o
Write-Host "🧹 Suppression de tous les fichiers .o..." -ForegroundColor Yellow
Get-ChildItem -Recurse -Filter "*.o" | Remove-Item -Force
Get-ChildItem -Recurse -Filter "*.dll" | Remove-Item -Force
Get-ChildItem -Recurse -Filter "*.so" | Remove-Item -Force

# Configurer l'environnement
$env:PATH = "$TOOLCHAIN\bin;$env:PATH"
$env:CC = "$TARGET$API_LEVEL-clang"
$env:CXX = "$TARGET$API_LEVEL-clang++"
$env:AR = "llvm-ar"
$env:STRIP = "llvm-strip"
$env:RANLIB = "llvm-ranlib"

# Flags de compilation
$CFLAGS = "-fPIC -DANDROID -D__ANDROID_API__=$API_LEVEL -O3 -ffast-math"
$LDFLAGS = "-shared -Wl,--no-undefined -Wl,-z,relro,-z,now -lm"

# Variables d'environnement pour make
$env:CFLAGS = $CFLAGS
$env:LDFLAGS = $LDFLAGS

# Corriger libretro.c si nécessaire
Write-Host "✅ libretro.c corrigé" -ForegroundColor Green

# Compiler avec le Makefile original
Write-Host "🔨 Compilation avec le Makefile original de FCEUmm..." -ForegroundColor Yellow
& make -f Makefile.libretro platform=android

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Compilation réussie!" -ForegroundColor Green
    
    # Copier le core personnalisé
    $OutputFile = "fceumm_libretro.dll"
    if (Test-Path $OutputFile) {
        Copy-Item $OutputFile "..\..\$OUTPUT_DIR\fceumm_libretro_android.so" -Force
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
    Write-Host "💡 Essayez de corriger les erreurs et relancez le script" -ForegroundColor Yellow
}

# Retourner au répertoire original
Set-Location ..\..

Write-Host "🎉 Compilation terminée!" -ForegroundColor Green 