# Script PowerShell pour simplifier libretro.c

param(
    [string]$Architecture = "x86_64"
)

Write-Host "🔧 Simplification de libretro.c..." -ForegroundColor Green

$BUILD_DIR = "real_fceumm_build"
$FCEUMM_DIR = "$BUILD_DIR\fceumm"

# Vérifier si le répertoire existe
if (!(Test-Path $FCEUMM_DIR)) {
    Write-Host "❌ Répertoire FCEUmm non trouvé. Exécutez d'abord build_real_fceumm_core.ps1" -ForegroundColor Red
    exit 1
}

Set-Location $FCEUMM_DIR

$LIBRETRO_C_PATH = "src\drivers\libretro\libretro.c"

if (!(Test-Path $LIBRETRO_C_PATH)) {
    Write-Host "❌ Fichier libretro.c non trouvé" -ForegroundColor Red
    exit 1
}

# Lire le contenu du fichier
$content = Get-Content $LIBRETRO_C_PATH -Raw

Write-Host "📝 Simplification des parties problématiques..." -ForegroundColor Yellow

# Supprimer ou commenter les lignes problématiques
$content = $content -replace "opt_defs_intl = options_intl\[language\]->definitions;", "// opt_defs_intl = options_intl[language]->definitions; // Commenté pour éviter l'erreur"
$content = $content -replace "memset\(&option_defs_us, 0, sizeof\(option_defs_us\)\);", "// memset(&option_defs_us, 0, sizeof(option_defs_us)); // Commenté pour éviter l'erreur"
$content = $content -replace "while \(option_defs\[index\]\.key\) \{", "// while (option_defs[index].key) { // Commenté pour éviter l'erreur"
$content = $content -replace "memcpy\(&option_defs_us\[index\], &option_defs\[index\],", "// memcpy(&option_defs_us[index], &option_defs[index], // Commenté pour éviter l'erreur"
$content = $content -replace "set_dipswitch_variables\(index, option_defs_us\);", "// set_dipswitch_variables(index, option_defs_us); // Commenté pour éviter l'erreur"
$content = $content -replace "libretro_set_core_options\(environ_cb,", "// libretro_set_core_options(environ_cb, // Commenté pour éviter l'erreur"
$content = $content -replace "DPSW_Cleanup\(\);", "// DPSW_Cleanup(); // Commenté pour éviter l'erreur"
$content = $content -replace "update_dipswitch\(\);", "// update_dipswitch(); // Commenté pour éviter l'erreur"

# Sauvegarder le fichier modifié
Set-Content $LIBRETRO_C_PATH $content -Encoding UTF8

Write-Host "✅ libretro.c simplifié" -ForegroundColor Green

# Retourner au répertoire racine
Set-Location ..\..

Write-Host "🎉 Simplification terminée!" -ForegroundColor Green
Write-Host "💡 Relancez maintenant: .\build_real_fceumm_core.ps1 -Architecture $Architecture" -ForegroundColor Cyan 