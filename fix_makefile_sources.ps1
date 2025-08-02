# Script PowerShell pour corriger les sources dans le Makefile

param(
    [string]$Architecture = "x86_64"
)

Write-Host "🔧 Correction des sources dans le Makefile..." -ForegroundColor Green

$BUILD_DIR = "real_fceumm_build"
$FCEUMM_DIR = "$BUILD_DIR\fceumm"

# Vérifier si le répertoire existe
if (!(Test-Path $FCEUMM_DIR)) {
    Write-Host "❌ Répertoire FCEUmm non trouvé. Exécutez d'abord build_real_fceumm_core.ps1" -ForegroundColor Red
    exit 1
}

Set-Location $FCEUMM_DIR

$MAKEFILE_PATH = "Makefile.simple"

if (!(Test-Path $MAKEFILE_PATH)) {
    Write-Host "❌ Makefile.simple non trouvé" -ForegroundColor Red
    exit 1
}

# Lire le contenu du Makefile
$content = Get-Content $MAKEFILE_PATH -Raw

Write-Host "📝 Suppression des sources inexistantes..." -ForegroundColor Yellow

# Supprimer les sources qui n'existent pas
$content = $content -replace "SOURCES \+= src/boards/fds\.c", "# SOURCES += src/boards/fds.c # Fichier inexistant"
$content = $content -replace "SOURCES \+= src/boards/n106\.c", "# SOURCES += src/boards/n106.c # Fichier inexistant"
$content = $content -replace "SOURCES \+= src/boards/nsf\.c", "# SOURCES += src/boards/nsf.c # Fichier inexistant"

# Sauvegarder le Makefile corrigé
Set-Content $MAKEFILE_PATH $content -Encoding UTF8

Write-Host "✅ Makefile corrigé" -ForegroundColor Green

# Retourner au répertoire racine
Set-Location ..\..

Write-Host "🎉 Correction terminée!" -ForegroundColor Green
Write-Host "💡 Relancez maintenant: .\build_real_fceumm_core.ps1 -Architecture $Architecture" -ForegroundColor Cyan 