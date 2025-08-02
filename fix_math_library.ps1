# Script PowerShell pour corriger le problème de liaison mathématique
# Ajoute la bibliothèque mathématique -lm au Makefile

param(
    [string]$Architecture = "x86_64"
)

Write-Host "🔧 Correction du problème de liaison mathématique..." -ForegroundColor Green

$BUILD_DIR = "real_core_build"
$FCEUMM_DIR = "$BUILD_DIR\fceumm"
$MAKEFILE = "$FCEUMM_DIR\Makefile.libretro"

# Vérifier si le répertoire existe
if (!(Test-Path $FCEUMM_DIR)) {
    Write-Host "❌ Répertoire FCEUmm non trouvé. Exécutez d'abord build_real_core.ps1" -ForegroundColor Red
    exit 1
}

if (!(Test-Path $MAKEFILE)) {
    Write-Host "❌ Makefile.libretro non trouvé" -ForegroundColor Red
    exit 1
}

Write-Host "📁 Makefile trouvé: $MAKEFILE" -ForegroundColor Yellow

# Lire le contenu du Makefile
$content = Get-Content $MAKEFILE -Raw

# Vérifier si -lm est déjà présent
if ($content -match "-lm") {
    Write-Host "✅ Bibliothèque mathématique -lm déjà présente" -ForegroundColor Green
} else {
    Write-Host "🔧 Ajout de la bibliothèque mathématique -lm..." -ForegroundColor Yellow
    
    # Chercher la ligne LDFLAGS et ajouter -lm
    if ($content -match "LDFLAGS\s*\+=") {
        # Ajouter -lm à LDFLAGS
        $content = $content -replace "(LDFLAGS\s*\+=\s*[^\r\n]*)", "`$1 -lm"
        Write-Host "📝 Ajouté -lm à LDFLAGS" -ForegroundColor Cyan
    } elseif ($content -match "LDFLAGS\s*=") {
        # Ajouter -lm à LDFLAGS
        $content = $content -replace "(LDFLAGS\s*=\s*[^\r\n]*)", "`$1 -lm"
        Write-Host "📝 Ajouté -lm à LDFLAGS" -ForegroundColor Cyan
    } else {
        # Ajouter une nouvelle ligne LDFLAGS
        $content = $content -replace "(\$\(CC\)[^\r\n]*)", "`$1`nLDFLAGS += -lm"
        Write-Host "📝 Ajouté nouvelle ligne LDFLAGS += -lm" -ForegroundColor Cyan
    }
    
    # Sauvegarder le fichier modifié
    Set-Content $MAKEFILE $content -Encoding UTF8
    Write-Host "✅ Makefile modifié: $MAKEFILE" -ForegroundColor Green
}

Write-Host "🎉 Correction terminée!" -ForegroundColor Green
Write-Host "💡 Relancez maintenant: .\build_real_core.ps1 -Architecture $Architecture" -ForegroundColor Cyan 