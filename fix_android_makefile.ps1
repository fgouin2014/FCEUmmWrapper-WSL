# Script PowerShell pour ajouter la configuration Android au Makefile
# Ajoute la section Android manquante

param(
    [string]$Architecture = "x86_64"
)

Write-Host "🔧 Ajout de la configuration Android au Makefile..." -ForegroundColor Green

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

# Vérifier si la configuration Android existe déjà
if ($content -match "platform.*android") {
    Write-Host "✅ Configuration Android déjà présente" -ForegroundColor Green
} else {
    Write-Host "🔧 Ajout de la configuration Android..." -ForegroundColor Yellow
    
    # Configuration Android à ajouter
    $androidConfig = @"

# Android
else ifeq (`$(platform), android)
	TARGET := `$(TARGET_NAME)_libretro_android.so
	fpic := -fPIC
	SHARED := -shared -Wl,--no-undefined
	LDFLAGS += -lm
	CFLAGS += -DANDROID
	HAVE_NEON = 0
	HAVE_SSE = 0
	HAVE_MMX = 0

"@
    
    # Chercher où insérer (après la section Unix)
    if ($content -match "else ifeq \(\$\(platform\), unix\)") {
        # Insérer après la section Unix
        $content = $content -replace "(else ifeq \(\$\(platform\), unix\)[^}]*})", "`$1`n$androidConfig"
        Write-Host "📝 Ajouté après la section Unix" -ForegroundColor Cyan
    } else {
        # Insérer après la première section platform
        $content = $content -replace "(else ifeq \(\$\(platform\), [^)]*\)[^}]*})", "`$1`n$androidConfig"
        Write-Host "📝 Ajouté après la première section platform" -ForegroundColor Cyan
    }
    
    # Sauvegarder le fichier modifié
    Set-Content $MAKEFILE $content -Encoding UTF8
    Write-Host "✅ Makefile modifié: $MAKEFILE" -ForegroundColor Green
}

Write-Host "🎉 Configuration Android ajoutée!" -ForegroundColor Green
Write-Host "💡 Relancez maintenant: .\build_real_core.ps1 -Architecture $Architecture" -ForegroundColor Cyan 