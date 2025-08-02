# Script PowerShell pour corriger le type de structure dans libretro.c

param(
    [string]$Architecture = "x86_64"
)

Write-Host "🔧 Correction du type de structure dans libretro.c..." -ForegroundColor Green

$BUILD_DIR = "real_fceumm_build"
$FCEUMM_DIR = "$BUILD_DIR\fceumm"

# Vérifier si le répertoire existe
if (!(Test-Path $FCEUMM_DIR)) {
    Write-Host "❌ Répertoire FCEUmm non trouvé. Exécutez d'abord build_real_fceumm_core.ps1" -ForegroundColor Red
    exit 1
}

Set-Location $FCEUMM_DIR

$LIBRETRO_C_PATH = "src\drivers\libretro\libretro.c"

# Lire le contenu du fichier
$content = Get-Content $LIBRETRO_C_PATH -Raw

Write-Host "📝 Correction du type de structure..." -ForegroundColor Yellow

# Corriger le nom de la structure
$content = $content -replace "struct retro_system_audio_video_info", "struct retro_system_av_info"

# Sauvegarder le fichier corrigé
Set-Content $LIBRETRO_C_PATH $content -Encoding UTF8

Write-Host "✅ Type de structure corrigé" -ForegroundColor Green

# Retourner au répertoire racine
Set-Location ..\..

Write-Host "🎉 Correction terminée!" -ForegroundColor Green
Write-Host "💡 Relancez maintenant: .\build_real_fceumm_core.ps1 -Architecture $Architecture" -ForegroundColor Cyan 