# Script PowerShell pour télécharger les headers libretro manquants

param(
    [string]$Architecture = "x86_64"
)

Write-Host "📥 Téléchargement des headers libretro..." -ForegroundColor Green

$BUILD_DIR = "simple_core_build"
$FCEUMM_DIR = "$BUILD_DIR\fceumm"

# Vérifier si le répertoire existe
if (!(Test-Path $FCEUMM_DIR)) {
    Write-Host "❌ Répertoire FCEUmm non trouvé. Exécutez d'abord build_simple_core.ps1" -ForegroundColor Red
    exit 1
}

Set-Location $FCEUMM_DIR

# Créer le répertoire pour les headers
$INCLUDE_DIR = "src\drivers\libretro\include"
if (!(Test-Path $INCLUDE_DIR)) {
    New-Item -ItemType Directory -Path $INCLUDE_DIR -Force
}

# Télécharger libretro.h
$LIBRETRO_H_URL = "https://raw.githubusercontent.com/libretro/libretro-common/master/include/libretro.h"
$LIBRETRO_H_PATH = "$INCLUDE_DIR\libretro.h"

Write-Host "📥 Téléchargement de libretro.h..." -ForegroundColor Yellow
try {
    Invoke-WebRequest -Uri $LIBRETRO_H_URL -OutFile $LIBRETRO_H_PATH
    Write-Host "✅ libretro.h téléchargé" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur lors du téléchargement de libretro.h" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

# Télécharger libretro-common si nécessaire
$LIBRETRO_COMMON_DIR = "src\drivers\libretro\libretro-common"
if (!(Test-Path $LIBRETRO_COMMON_DIR)) {
    Write-Host "📥 Clonage de libretro-common..." -ForegroundColor Yellow
    try {
        git clone https://github.com/libretro/libretro-common.git $LIBRETRO_COMMON_DIR
        Write-Host "✅ libretro-common cloné" -ForegroundColor Green
    } catch {
        Write-Host "❌ Erreur lors du clonage de libretro-common" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
    }
}

# Modifier le Makefile pour inclure les bons chemins
$MAKEFILE_PATH = "Makefile.simple"
if (Test-Path $MAKEFILE_PATH) {
    Write-Host "🔧 Mise à jour du Makefile avec les chemins d'inclusion..." -ForegroundColor Yellow
    
    $content = Get-Content $MAKEFILE_PATH -Raw
    
    # Ajouter les chemins d'inclusion
    $content = $content -replace "CFLAGS := ([^\r\n]*)", "CFLAGS := `$1 -Isrc/drivers/libretro/include -Isrc/drivers/libretro/libretro-common/include"
    
    Set-Content $MAKEFILE_PATH $content -Encoding UTF8
    Write-Host "✅ Makefile mis à jour" -ForegroundColor Green
}

# Retourner au répertoire racine
Set-Location ..\..

Write-Host "🎉 Headers libretro téléchargés!" -ForegroundColor Green
Write-Host "💡 Relancez maintenant: .\build_simple_core.ps1 -Architecture $Architecture" -ForegroundColor Cyan 