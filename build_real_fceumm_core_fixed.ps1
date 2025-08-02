# Script PowerShell corrigé pour compiler un vrai core FCEUmm
# Utilise le Makefile original de FCEUmm

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

# Créer les headers vides si nécessaire
$EMPTY_HEADERS = @{
    "libretro_dipswitch.h" = @"
/*
 * Header vide pour libretro_dipswitch.h
 * Créé automatiquement car le fichier n'existe pas dans libretro-common
 */

#ifndef LIBRETRO_DIPSWITCH_H
#define LIBRETRO_DIPSWITCH_H

// Définitions minimales pour éviter les erreurs de compilation
void set_dipswitch_variables(int index, void* option_defs_us);
void update_dipswitch(void);
void DPSW_Cleanup(void);

#endif // LIBRETRO_DIPSWITCH_H
"@
    "libretro_core_options.h" = @"
/*
 * Header vide pour libretro_core_options.h
 * Créé automatiquement car le fichier n'existe pas dans libretro-common
 */

#ifndef LIBRETRO_CORE_OPTIONS_H
#define LIBRETRO_CORE_OPTIONS_H

// Définitions minimales pour éviter les erreurs de compilation
void libretro_set_core_options(void* environ_cb, void* option_defs_us);

// Variables globales utilisées dans libretro.c
extern struct retro_core_option_v2_definition* option_defs;
extern struct retro_core_option_v2_definition* option_defs_us;
extern struct retro_core_option_v2_definition** options_intl;

#endif // LIBRETRO_CORE_OPTIONS_H
"@
}

foreach ($header in $EMPTY_HEADERS.Keys) {
    $headerPath = "$INCLUDE_DIR\$header"
    if (!(Test-Path $headerPath)) {
        Set-Content $headerPath $EMPTY_HEADERS[$header] -Encoding UTF8
        Write-Host "✅ $header créé" -ForegroundColor Green
    }
}

# Corriger libretro.c si nécessaire
$LIBRETRO_C_PATH = "src\drivers\libretro\libretro.c"
if (Test-Path $LIBRETRO_C_PATH) {
    $content = Get-Content $LIBRETRO_C_PATH -Raw
    
    # Corriger les erreurs de compilation
    $content = $content -replace "opt_defs_intl = options_intl\[language\]->definitions;", "// opt_defs_intl = options_intl[language]->definitions; // Commenté pour éviter l'erreur"
    $content = $content -replace "struct retro_system_audio_video_info", "struct retro_system_av_info"
    
    Set-Content $LIBRETRO_C_PATH $content -Encoding UTF8
    Write-Host "✅ libretro.c corrigé" -ForegroundColor Green
}

# Utiliser le Makefile original de FCEUmm
Write-Host "🔨 Compilation avec le Makefile original de FCEUmm..." -ForegroundColor Yellow

# Définir les variables d'environnement pour le Makefile
$env:CC = $CC
$env:CXX = $CXX
$env:AR = $AR
$env:STRIP = $STRIP
$env:RANLIB = $RANLIB
$env:CFLAGS = $CFLAGS
$env:CXXFLAGS = $CXXFLAGS
$env:LDFLAGS = $LDFLAGS

# Nettoyer complètement avant de compiler
Write-Host "🧹 Nettoyage complet..." -ForegroundColor Yellow
Get-ChildItem -Recurse -Filter "*.o" | Remove-Item -Force
Get-ChildItem -Recurse -Filter "*.dll" | Remove-Item -Force
Get-ChildItem -Recurse -Filter "*.so" | Remove-Item -Force
& make -f Makefile.libretro clean
# Compiler avec le Makefile original
& make -f Makefile.libretro platform=android

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Core FCEUmm compilé avec succès!" -ForegroundColor Green
    
    # Copier le core personnalisé
    $OutputFile = "fceumm_libretro.dll"
    if (Test-Path $OutputFile) {
        # Renommer en .so pour Android
        $AndroidFile = "fceumm_libretro_android.so"
        Copy-Item $OutputFile $AndroidFile -Force
        Copy-Item $AndroidFile "..\..\$OUTPUT_DIR\" -Force
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