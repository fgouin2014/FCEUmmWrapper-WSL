# Script PowerShell pour corriger le problème de compilation FCEUmm
# Ajoute la définition manquante de FCEU_VERSION_NUMERIC

param(
    [string]$Architecture = "x86_64"
)

Write-Host "🔧 Correction du problème de compilation FCEUmm..." -ForegroundColor Green

$BUILD_DIR = "real_core_build"
$FCEUMM_DIR = "$BUILD_DIR\fceumm"

# Vérifier si le répertoire existe
if (!(Test-Path $FCEUMM_DIR)) {
    Write-Host "❌ Répertoire FCEUmm non trouvé. Exécutez d'abord build_real_core.ps1" -ForegroundColor Red
    exit 1
}

# Chercher le fichier qui contient les définitions de version
$VERSION_FILES = @(
    "src\fceu.h",
    "src\fceu.c", 
    "src\general.c",
    "src\general.h"
)

$FOUND_FILE = $null
foreach ($file in $VERSION_FILES) {
    $fullPath = "$FCEUMM_DIR\$file"
    if (Test-Path $fullPath) {
        $FOUND_FILE = $fullPath
        break
    }
}

if (!$FOUND_FILE) {
    Write-Host "❌ Aucun fichier de version trouvé" -ForegroundColor Red
    exit 1
}

Write-Host "📁 Fichier trouvé: $FOUND_FILE" -ForegroundColor Yellow

# Lire le contenu du fichier
$content = Get-Content $FOUND_FILE -Raw

# Vérifier si FCEU_VERSION_NUMERIC existe déjà
if ($content -match "FCEU_VERSION_NUMERIC") {
    Write-Host "✅ FCEU_VERSION_NUMERIC existe déjà" -ForegroundColor Green
} else {
    Write-Host "🔧 Ajout de la définition FCEU_VERSION_NUMERIC..." -ForegroundColor Yellow
    
    # Chercher où ajouter la définition (après les autres définitions de version)
    if ($content -match "#define FCEU_VERSION") {
        # Ajouter après la ligne de version existante
        $content = $content -replace "(#define FCEU_VERSION[^\r\n]*)", "`$1`n#define FCEU_VERSION_NUMERIC 0x000000"
        Write-Host "📝 Ajouté après FCEU_VERSION" -ForegroundColor Cyan
    } elseif ($content -match "#define.*VERSION") {
        # Ajouter après une définition de version
        $content = $content -replace "(#define.*VERSION[^\r\n]*)", "`$1`n#define FCEU_VERSION_NUMERIC 0x000000"
        Write-Host "📝 Ajouté après une définition de version" -ForegroundColor Cyan
    } else {
        # Ajouter au début du fichier après les includes
        $content = $content -replace "(#include[^\r\n]*)", "`$1`n#define FCEU_VERSION_NUMERIC 0x000000"
        Write-Host "📝 Ajouté après les includes" -ForegroundColor Cyan
    }
    
    # Sauvegarder le fichier modifié
    Set-Content $FOUND_FILE $content -Encoding UTF8
    Write-Host "✅ Fichier modifié: $FOUND_FILE" -ForegroundColor Green
}

Write-Host "🎉 Correction terminée!" -ForegroundColor Green
Write-Host "💡 Relancez maintenant: .\build_real_core.ps1 -Architecture $Architecture" -ForegroundColor Cyan 