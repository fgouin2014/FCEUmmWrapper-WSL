# Script PowerShell pour créer des headers vides mais corrects

param(
    [string]$Architecture = "x86_64"
)

Write-Host "📝 Création de headers vides mais corrects..." -ForegroundColor Green

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

# Supprimer les fichiers 404
Remove-Item "$INCLUDE_DIR\libretro_dipswitch.h" -Force -ErrorAction SilentlyContinue
Remove-Item "$INCLUDE_DIR\libretro_core_options.h" -Force -ErrorAction SilentlyContinue

# Créer des headers vides mais corrects
$EMPTY_HEADERS = @{
    "libretro_dipswitch.h" = @"
/*
 * Header vide pour libretro_dipswitch.h
 * Créé automatiquement car le fichier n'existe pas dans libretro-common
 */

#ifndef LIBRETRO_DIPSWITCH_H
#define LIBRETRO_DIPSWITCH_H

// Définitions minimales pour éviter les erreurs de compilation
void set_dipswitch_variables(int index, void* option_defs_us);
void update_dipswitch(void);
void DPSW_Cleanup(void);

#endif // LIBRETRO_DIPSWITCH_H
"@
    "libretro_core_options.h" = @"
/*
 * Header vide pour libretro_core_options.h
 * Créé automatiquement car le fichier n'existe pas dans libretro-common
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
    "libretro_core_options_v2.h" = @"
/*
 * Header vide pour libretro_core_options_v2.h
 * Créé automatiquement car le fichier n'existe pas dans libretro-common
 */

#ifndef LIBRETRO_CORE_OPTIONS_V2_H
#define LIBRETRO_CORE_OPTIONS_V2_H

// Définitions minimales pour éviter les erreurs de compilation

#endif // LIBRETRO_CORE_OPTIONS_V2_H
"@
    "libretro_core_options_v2_intl.h" = @"
/*
 * Header vide pour libretro_core_options_v2_intl.h
 * Créé automatiquement car le fichier n'existe pas dans libretro-common
 */

#ifndef LIBRETRO_CORE_OPTIONS_V2_INTL_H
#define LIBRETRO_CORE_OPTIONS_V2_INTL_H

// Définitions minimales pour éviter les erreurs de compilation

#endif // LIBRETRO_CORE_OPTIONS_V2_INTL_H
"@
    "libretro_core_options_intl.h" = @"
/*
 * Header vide pour libretro_core_options_intl.h
 * Créé automatiquement car le fichier n'existe pas dans libretro-common
 */

#ifndef LIBRETRO_CORE_OPTIONS_INTL_H
#define LIBRETRO_CORE_OPTIONS_INTL_H

// Définitions minimales pour éviter les erreurs de compilation

#endif // LIBRETRO_CORE_OPTIONS_INTL_H
"@
    "libretro_core_options_v2_intl_helpers.h" = @"
/*
 * Header vide pour libretro_core_options_v2_intl_helpers.h
 * Créé automatiquement car le fichier n'existe pas dans libretro-common
 */

#ifndef LIBRETRO_CORE_OPTIONS_V2_INTL_HELPERS_H
#define LIBRETRO_CORE_OPTIONS_V2_INTL_HELPERS_H

// Définitions minimales pour éviter les erreurs de compilation

#endif // LIBRETRO_CORE_OPTIONS_V2_INTL_HELPERS_H
"@
    "libretro_core_options_intl_helpers.h" = @"
/*
 * Header vide pour libretro_core_options_intl_helpers.h
 * Créé automatiquement car le fichier n'existe pas dans libretro-common
 */

#ifndef LIBRETRO_CORE_OPTIONS_INTL_HELPERS_H
#define LIBRETRO_CORE_OPTIONS_INTL_HELPERS_H

// Définitions minimales pour éviter les erreurs de compilation

#endif // LIBRETRO_CORE_OPTIONS_INTL_HELPERS_H
"@
}

# Créer chaque header vide
foreach ($header in $EMPTY_HEADERS.Keys) {
    $headerPath = "$INCLUDE_DIR\$header"
    Set-Content $headerPath $EMPTY_HEADERS[$header] -Encoding UTF8
    Write-Host "✅ $header créé" -ForegroundColor Green
}

# Retourner au répertoire racine
Set-Location ..\..

Write-Host "🎉 Headers vides créés!" -ForegroundColor Green
Write-Host "💡 Relancez maintenant: .\build_real_fceumm_core.ps1 -Architecture $Architecture" -ForegroundColor Cyan 