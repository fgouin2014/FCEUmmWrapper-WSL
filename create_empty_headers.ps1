# Script PowerShell pour créer des headers vides

param(
    [string]$Architecture = "x86_64"
)

Write-Host "📝 Création de headers vides..." -ForegroundColor Green

$BUILD_DIR = "real_fceumm_build"
$FCEUMM_DIR = "$BUILD_DIR\fceumm"

# Vérifier si le répertoire existe
if (!(Test-Path $FCEUMM_DIR)) {
    Write-Host "❌ Répertoire FCEUmm non trouvé. Exécutez d'abord build_real_fceumm_core.ps1" -ForegroundColor Red
    exit 1
}

Set-Location $FCEUMM_DIR

# Créer le répertoire pour les headers
$INCLUDE_DIR = "src\drivers\libretro\include"
if (!(Test-Path $INCLUDE_DIR)) {
    New-Item -ItemType Directory -Path $INCLUDE_DIR -Force
}

# Liste des headers vides à créer
$EMPTY_HEADERS = @(
    "libretro_dipswitch.h",
    "libretro_core_options.h",
    "libretro_core_options_v2.h",
    "libretro_core_options_v2_intl.h",
    "libretro_core_options_intl.h",
    "libretro_core_options_v2_intl_helpers.h",
    "libretro_core_options_intl_helpers.h"
)

# Créer chaque header vide
foreach ($header in $EMPTY_HEADERS) {
    $headerPath = "$INCLUDE_DIR\$header"
    if (!(Test-Path $headerPath)) {
        Write-Host "📝 Création de $header..." -ForegroundColor Yellow
        
        $headerContent = @"
/*
 * Header vide pour $header
 * Créé automatiquement pour contourner les erreurs de compilation
 */

#ifndef `$(header.ToUpper().Replace('.', '_'))
#define `$(header.ToUpper().Replace('.', '_'))

// Définitions minimales pour éviter les erreurs de compilation

#endif // `$(header.ToUpper().Replace('.', '_'))
"@
        
        Set-Content $headerPath $headerContent -Encoding UTF8
        Write-Host "✅ $header créé" -ForegroundColor Green
    } else {
        Write-Host "✅ $header existe déjà" -ForegroundColor Green
    }
}

# Retourner au répertoire racine
Set-Location ..\..

Write-Host "🎉 Headers vides créés!" -ForegroundColor Green
Write-Host "💡 Relancez maintenant: .\build_real_fceumm_core.ps1 -Architecture $Architecture" -ForegroundColor Cyan 