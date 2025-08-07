# Script de test pour les corrections du systeme d'overlays
Write-Host "=== Test des corrections du systeme d'overlays ===" -ForegroundColor Green

# Verifier que les fichiers modifies existent
$files_to_check = @(
    "app/src/main/java/com/fceumm/wrapper/overlay/RetroArchOverlaySystem.java",
    "app/src/main/java/com/fceumm/wrapper/OverlayIntegrationActivity.java"
)

Write-Host "`nVerification des fichiers modifies:" -ForegroundColor Yellow
foreach ($file in $files_to_check) {
    if (Test-Path $file) {
        Write-Host "✓ $file" -ForegroundColor Green
    } else {
        Write-Host "✗ $file - MANQUANT" -ForegroundColor Red
    }
}

# Verifier les nouvelles methodes ajoutees
Write-Host "`nVerification des nouvelles methodes:" -ForegroundColor Yellow

# Verifier updateScreenDimensions dans RetroArchOverlaySystem
$retroarch_file = "app/src/main/java/com/fceumm/wrapper/overlay/RetroArchOverlaySystem.java"
if (Test-Path $retroarch_file) {
    $content = Get-Content $retroarch_file -Raw
    
    $methods_to_check = @(
        "updateScreenDimensions",
        "getScreenWidth",
        "getScreenHeight", 
        "isLandscape",
        "forceLayoutUpdate"
    )
    
    foreach ($method in $methods_to_check) {
        if ($content -match $method) {
            Write-Host "✓ Methode $method trouvee dans RetroArchOverlaySystem" -ForegroundColor Green
        } else {
            Write-Host "✗ Methode $method manquante dans RetroArchOverlaySystem" -ForegroundColor Red
        }
    }
}

# Verifier les modifications dans OverlayIntegrationActivity
$activity_file = "app/src/main/java/com/fceumm/wrapper/OverlayIntegrationActivity.java"
if (Test-Path $activity_file) {
    $content = Get-Content $activity_file -Raw
    
    $features_to_check = @(
        "updateScreenDimensions",
        "onConfigurationChanged",
        "postInvalidateDelayed",
        "forceLayoutUpdate"
    )
    
    foreach ($feature in $features_to_check) {
        if ($content -match $feature) {
            Write-Host "✓ Fonctionnalite $feature trouvee dans OverlayIntegrationActivity" -ForegroundColor Green
        } else {
            Write-Host "✗ Fonctionnalite $feature manquante dans OverlayIntegrationActivity" -ForegroundColor Red
        }
    }
}

# Verifier que les overlays sont toujours disponibles
Write-Host "`nVerification des overlays disponibles:" -ForegroundColor Yellow
$overlay_dir = "app/src/main/assets/overlays/gamepads/flat/"
if (Test-Path $overlay_dir) {
    $cfg_files = Get-ChildItem $overlay_dir -Filter "*.cfg"
    Write-Host "✓ $($cfg_files.Count) fichiers .cfg trouves" -ForegroundColor Green
    
    $img_dir = "$overlay_dir/img/"
    if (Test-Path $img_dir) {
        $img_files = Get-ChildItem $img_dir -Filter "*.png"
        Write-Host "✓ $($img_files.Count) images PNG trouvees" -ForegroundColor Green
    } else {
        Write-Host "✗ Dossier img/ manquant" -ForegroundColor Red
    }
} else {
    Write-Host "✗ Dossier overlays manquant" -ForegroundColor Red
}

# Resume des corrections apportees
Write-Host "`n=== Resume des corrections ===" -ForegroundColor Cyan
Write-Host "1. Ajout de la detection dynamique des dimensions d'ecran" -ForegroundColor White
Write-Host "2. Gestion des changements d'orientation avec onConfigurationChanged" -ForegroundColor White
Write-Host "3. Redessin automatique avec postInvalidateDelayed" -ForegroundColor White
Write-Host "4. Rechargement automatique des overlays lors des changements d'orientation" -ForegroundColor White
Write-Host "5. Methode forceLayoutUpdate pour forcer la mise a jour du layout" -ForegroundColor White

Write-Host "`n=== Instructions de test ===" -ForegroundColor Magenta
Write-Host "1. Compiler et installer l'application" -ForegroundColor White
Write-Host "2. Lancer OverlayIntegrationActivity" -ForegroundColor White
Write-Host "3. Verifier que les overlays s'affichent immediatement en portrait" -ForegroundColor White
Write-Host "4. Changer l'orientation en paysage" -ForegroundColor White
Write-Host "5. Verifier que le bon overlay s'affiche sans toucher l'ecran" -ForegroundColor White
Write-Host "6. Revenir en portrait" -ForegroundColor White
Write-Host "7. Verifier que le bon overlay s'affiche immediatement" -ForegroundColor White

Write-Host "`n=== Test de compilation ===" -ForegroundColor Yellow
try {
    $gradle_result = & ./gradlew assembleDebug 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Compilation reussie" -ForegroundColor Green
    } else {
        Write-Host "✗ Erreur de compilation:" -ForegroundColor Red
        Write-Host $gradle_result -ForegroundColor Red
    }
} catch {
    Write-Host "✗ Impossible d'executer gradlew" -ForegroundColor Red
}

Write-Host "`nTest termine!" -ForegroundColor Green 