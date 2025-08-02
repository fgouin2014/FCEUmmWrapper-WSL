# Script PowerShell pour nettoyer le Makefile

param(
    [string]$Architecture = "x86_64"
)

Write-Host "🧹 Nettoyage du Makefile..." -ForegroundColor Green

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

# Supprimer toutes les références aux fichiers inexistants
$content = $content -replace "src/fds\.c\s+", ""
$content = $content -replace "src/boards/fds\.c\s+", ""
$content = $content -replace "src/boards/n106\.c\s+", ""
$content = $content -replace "src/boards/nsf\.c\s+", ""

# Nettoyer les lignes vides
$content = $content -replace "SOURCES \+= \s*`r?`n", ""
$content = $content -replace "SOURCES \+= `r?`n", ""

# Sauvegarder le Makefile nettoyé
Set-Content $MAKEFILE_PATH $content -Encoding UTF8

Write-Host "✅ Makefile nettoyé" -ForegroundColor Green

# Retourner au répertoire racine
Set-Location ..\..

Write-Host "🎉 Nettoyage terminé!" -ForegroundColor Green
Write-Host "💡 Relancez maintenant: .\build_real_fceumm_core.ps1 -Architecture $Architecture" -ForegroundColor Cyan 