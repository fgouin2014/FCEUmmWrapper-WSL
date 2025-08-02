# Script PowerShell pour télécharger tous les headers libretro manquants

param(
    [string]$Architecture = "x86_64"
)

Write-Host "📥 Téléchargement des headers libretro manquants..." -ForegroundColor Green

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

# Liste des headers à télécharger
$HEADERS = @(
    @{
        Name = "libretro.h"
        URL = "https://raw.githubusercontent.com/libretro/libretro-common/master/include/libretro.h"
    },
    @{
        Name = "libretro_dipswitch.h"
        URL = "https://raw.githubusercontent.com/libretro/libretro-common/master/include/libretro_dipswitch.h"
    },
    @{
        Name = "libretro_core_options.h"
        URL = "https://raw.githubusercontent.com/libretro/libretro-common/master/include/libretro_core_options.h"
    },
    @{
        Name = "libretro_core_options_v2.h"
        URL = "https://raw.githubusercontent.com/libretro/libretro-common/master/include/libretro_core_options_v2.h"
    },
    @{
        Name = "libretro_core_options_v2_intl.h"
        URL = "https://raw.githubusercontent.com/libretro/libretro-common/master/include/libretro_core_options_v2_intl.h"
    },
    @{
        Name = "libretro_core_options_intl.h"
        URL = "https://raw.githubusercontent.com/libretro/libretro-common/master/include/libretro_core_options_intl.h"
    },
    @{
        Name = "libretro_core_options_v2_intl_helpers.h"
        URL = "https://raw.githubusercontent.com/libretro/libretro-common/master/include/libretro_core_options_v2_intl_helpers.h"
    },
    @{
        Name = "libretro_core_options_intl_helpers.h"
        URL = "https://raw.githubusercontent.com/libretro/libretro-common/master/include/libretro_core_options_intl_helpers.h"
    }
)

# Télécharger chaque header
foreach ($header in $HEADERS) {
    $headerPath = "$INCLUDE_DIR\$($header.Name)"
    if (!(Test-Path $headerPath)) {
        Write-Host "📥 Téléchargement de $($header.Name)..." -ForegroundColor Yellow
        try {
            Invoke-WebRequest -Uri $header.URL -OutFile $headerPath
            Write-Host "✅ $($header.Name) téléchargé" -ForegroundColor Green
        } catch {
            Write-Host "❌ Erreur lors du téléchargement de $($header.Name)" -ForegroundColor Red
            Write-Host $_.Exception.Message -ForegroundColor Red
        }
    } else {
        Write-Host "✅ $($header.Name) existe déjà" -ForegroundColor Green
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
        Write-Host $_.Exception.Message -ForegroundColor Red
    }
}

# Retourner au répertoire racine
Set-Location ..\..

Write-Host "🎉 Headers libretro téléchargés!" -ForegroundColor Green
Write-Host "💡 Relancez maintenant: .\build_real_fceumm_core.ps1 -Architecture $Architecture" -ForegroundColor Cyan 