# Script PowerShell pour corriger le problème de compilation FCEUmm
# Ajoute la définition manquante de FCEU_VERSION_NUMERIC

param(
    [string]$Architecture = "x86_64"
)

Write-Host "🔧 Correction du problème de compilation FCEUmm..." -ForegroundColor Green

$BUILD_DIR = "real_fceumm_build"
$FCEUMM_DIR = "$BUILD_DIR\fceumm"

# Vérifier si le répertoire existe
if (!(Test-Path $FCEUMM_DIR)) {
    Write-Host "❌ Répertoire FCEUmm non trouvé. Exécutez d'abord build_real_fceumm_core.ps1" -ForegroundColor Red
    exit 1
}

# Chercher le fichier contenant FCEU_VERSION_NUMERIC
$FILES_TO_CHECK = @(
    "src\fceu.h",
    "src\fceu.c", 
    "src\general.c",
    "src\general.h"
)

$FOUND_FILE = $null
foreach ($file in $FILES_TO_CHECK) {
    $fullPath = "$FCEUMM_DIR\$file"
    if (Test-Path $fullPath) {
        $content = Get-Content $fullPath -Raw
        if ($content -match "FCEU_VERSION_NUMERIC") {
            Write-Host "📁 Fichier trouvé: $BUILD_DIR\fceumm\$file" -ForegroundColor Yellow
            $FOUND_FILE = $fullPath
            break
        }
    }
}

if (!$FOUND_FILE) {
    Write-Host "❌ Aucun fichier avec FCEU_VERSION_NUMERIC trouvé" -ForegroundColor Red
    exit 1
}

# Lire le contenu du fichier
$content = Get-Content $FOUND_FILE -Raw

# Vérifier si FCEU_VERSION_NUMERIC existe déjà
if ($content -match "FCEU_VERSION_NUMERIC") {
    Write-Host "✅ FCEU_VERSION_NUMERIC existe déjà" -ForegroundColor Green
} else {
    Write-Host "🔧 Ajout de la définition FCEU_VERSION_NUMERIC..." -ForegroundColor Yellow
    
    if ($content -match "#define FCEU_VERSION") {
        $content = $content -replace "(#define FCEU_VERSION[^\r\n]*)", "`$1`n#define FCEU_VERSION_NUMERIC 0x000000"
    } elseif ($content -match "#define.*VERSION") {
        $content = $content -replace "(#define.*VERSION[^\r\n]*)", "`$1`n#define FCEU_VERSION_NUMERIC 0x000000"
    } else {
        $content = $content -replace "(#include[^\r\n]*)", "`$1`n#define FCEU_VERSION_NUMERIC 0x000000"
    }
    
    Set-Content $FOUND_FILE $content -Encoding UTF8
    Write-Host "✅ Fichier modifié: $FOUND_FILE" -ForegroundColor Green
}

Write-Host "🎉 Correction terminée!" -ForegroundColor Green
Write-Host "💡 Relancez maintenant: .\build_real_fceumm_core.ps1 -Architecture $Architecture" -ForegroundColor Cyan 