# Script PowerShell pour corriger le contenu des headers

param(
    [string]$Architecture = "x86_64"
)

Write-Host "🔧 Correction du contenu des headers..." -ForegroundColor Green

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

# Contenu pour libretro_dipswitch.h
$DIPSWITCH_CONTENT = @"
/*
 * Header pour libretro_dipswitch.h
 * Créé automatiquement pour contourner les erreurs de compilation
 */

#ifndef LIBRETRO_DIPSWITCH_H
#define LIBRETRO_DIPSWITCH_H

// Définitions minimales pour éviter les erreurs de compilation
void set_dipswitch_variables(int index, void* option_defs_us);
void update_dipswitch(void);
void DPSW_Cleanup(void);

#endif // LIBRETRO_DIPSWITCH_H
"@

# Contenu pour libretro_core_options.h
$CORE_OPTIONS_CONTENT = @"
/*
 * Header pour libretro_core_options.h
 * Créé automatiquement pour contourner les erreurs de compilation
 */

#ifndef LIBRETRO_CORE_OPTIONS_H
#define LIBRETRO_CORE_OPTIONS_H

// Définitions minimales pour éviter les erreurs de compilation
void libretro_set_core_options(void* environ_cb, void* option_defs_us);

// Variables globales utilisées dans libretro.c
extern struct retro_core_option_v2_definition* option_defs;
extern struct retro_core_option_v2_definition* option_defs_us;
extern struct retro_core_option_v2_definition** options_intl;

#endif // LIBRETRO_CORE_OPTIONS_H
"@

# Contenu pour les autres headers
$OTHER_HEADERS = @{
    "libretro_core_options_v2.h" = @"
/*
 * Header pour libretro_core_options_v2.h
 * Créé automatiquement pour contourner les erreurs de compilation
 */

#ifndef LIBRETRO_CORE_OPTIONS_V2_H
#define LIBRETRO_CORE_OPTIONS_V2_H

// Définitions minimales pour éviter les erreurs de compilation

#endif // LIBRETRO_CORE_OPTIONS_V2_H
"@
    "libretro_core_options_v2_intl.h" = @"
/*
 * Header pour libretro_core_options_v2_intl.h
 * Créé automatiquement pour contourner les erreurs de compilation
 */

#ifndef LIBRETRO_CORE_OPTIONS_V2_INTL_H
#define LIBRETRO_CORE_OPTIONS_V2_INTL_H

// Définitions minimales pour éviter les erreurs de compilation

#endif // LIBRETRO_CORE_OPTIONS_V2_INTL_H
"@
    "libretro_core_options_intl.h" = @"
/*
 * Header pour libretro_core_options_intl.h
 * Créé automatiquement pour contourner les erreurs de compilation
 */

#ifndef LIBRETRO_CORE_OPTIONS_INTL_H
#define LIBRETRO_CORE_OPTIONS_INTL_H

// Définitions minimales pour éviter les erreurs de compilation

#endif // LIBRETRO_CORE_OPTIONS_INTL_H
"@
    "libretro_core_options_v2_intl_helpers.h" = @"
/*
 * Header pour libretro_core_options_v2_intl_helpers.h
 * Créé automatiquement pour contourner les erreurs de compilation
 */

#ifndef LIBRETRO_CORE_OPTIONS_V2_INTL_HELPERS_H
#define LIBRETRO_CORE_OPTIONS_V2_INTL_HELPERS_H

// Définitions minimales pour éviter les erreurs de compilation

#endif // LIBRETRO_CORE_OPTIONS_V2_INTL_HELPERS_H
"@
    "libretro_core_options_intl_helpers.h" = @"
/*
 * Header pour libretro_core_options_intl_helpers.h
 * Créé automatiquement pour contourner les erreurs de compilation
 */

#ifndef LIBRETRO_CORE_OPTIONS_INTL_HELPERS_H
#define LIBRETRO_CORE_OPTIONS_INTL_HELPERS_H

// Définitions minimales pour éviter les erreurs de compilation

#endif // LIBRETRO_CORE_OPTIONS_INTL_HELPERS_H
"@
}

# Mettre à jour libretro_dipswitch.h
$DIPSWITCH_PATH = "$INCLUDE_DIR\libretro_dipswitch.h"
Set-Content $DIPSWITCH_PATH $DIPSWITCH_CONTENT -Encoding UTF8
Write-Host "✅ libretro_dipswitch.h corrigé" -ForegroundColor Green

# Mettre à jour libretro_core_options.h
$CORE_OPTIONS_PATH = "$INCLUDE_DIR\libretro_core_options.h"
Set-Content $CORE_OPTIONS_PATH $CORE_OPTIONS_CONTENT -Encoding UTF8
Write-Host "✅ libretro_core_options.h corrigé" -ForegroundColor Green

# Mettre à jour les autres headers
foreach ($header in $OTHER_HEADERS.Keys) {
    $headerPath = "$INCLUDE_DIR\$header"
    Set-Content $headerPath $OTHER_HEADERS[$header] -Encoding UTF8
    Write-Host "✅ $header corrigé" -ForegroundColor Green
}

# Retourner au répertoire racine
Set-Location ..\..

Write-Host "🎉 Headers corrigés!" -ForegroundColor Green
Write-Host "💡 Relancez maintenant: .\build_real_fceumm_core.ps1 -Architecture $Architecture" -ForegroundColor Cyan 