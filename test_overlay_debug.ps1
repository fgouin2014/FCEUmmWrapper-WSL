# Script de test pour les corrections du systeme d'overlays avec debug
Write-Host "=== Test des corrections du systeme d'overlays avec debug ===" -ForegroundColor Green

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

# Verifier les nouvelles corrections ajoutees
Write-Host "`nVerification des nouvelles corrections:" -ForegroundColor Yellow

# Verifier les modifications dans RetroArchOverlaySystem
$retroarch_file = "app/src/main/java/com/fceumm/wrapper/overlay/RetroArchOverlaySystem.java"
if (Test-Path $retroarch_file) {
    $content = Get-Content $retroarch_file -Raw
    
    $debug_features = @(
        "Render ignore",
        "Rendu de.*boutons d'overlay",
        "ActiveOverlay.*Yes.*No"
    )
    
    foreach ($feature in $debug_features) {
        if ($content -match $feature) {
            Write-Host "✓ Debug feature '$feature' trouvee dans RetroArchOverlaySystem" -ForegroundColor Green
        } else {
            Write-Host "✗ Debug feature '$feature' manquante dans RetroArchOverlaySystem" -ForegroundColor Red
        }
    }
}

# Verifier les modifications dans OverlayIntegrationActivity
$activity_file = "app/src/main/java/com/fceumm/wrapper/OverlayIntegrationActivity.java"
if (Test-Path $activity_file) {
    $content = Get-Content $activity_file -Raw
    
    $corrections = @(
        "renderView\.post",
        "setOverlayEnabled\(true\)",
        "System Enabled",
        "Active Overlay",
        "postInvalidateDelayed\(500\)"
    )
    
    foreach ($correction in $corrections) {
        if ($content -match $correction) {
            Write-Host "✓ Correction '$correction' trouvee dans OverlayIntegrationActivity" -ForegroundColor Green
        } else {
            Write-Host "✗ Correction '$correction' manquante dans OverlayIntegrationActivity" -ForegroundColor Red
        }
    }
}

# Resume des nouvelles corrections apportees
Write-Host "`n=== Resume des nouvelles corrections ===" -ForegroundColor Cyan
Write-Host "1. Correction du timing d'initialisation avec renderView.post()" -ForegroundColor White
Write-Host "2. Activation forcee de l'overlay apres chargement" -ForegroundColor White
Write-Host "3. Activation forcee de l'overlay apres mise a jour des dimensions" -ForegroundColor White
Write-Host "4. Debug ameliore dans la methode render()" -ForegroundColor White
Write-Host "5. Debug ameliore dans drawDebugInfo()" -ForegroundColor White
Write-Host "6. Redessin optimise (500ms au lieu de 100ms)" -ForegroundColor White

Write-Host "`n=== Instructions de test ameliorees ===" -ForegroundColor Magenta
Write-Host "1. Compiler et installer l'application" -ForegroundColor White
Write-Host "2. Lancer OverlayIntegrationActivity" -ForegroundColor White
Write-Host "3. Verifier les logs pour voir les messages de debug" -ForegroundColor White
Write-Host "4. Verifier que les overlays s'affichent immediatement en portrait" -ForegroundColor White
Write-Host "5. Changer l'orientation en paysage" -ForegroundColor White
Write-Host "6. Verifier que le bon overlay s'affiche sans toucher l'ecran" -ForegroundColor White
Write-Host "7. Revenir en portrait" -ForegroundColor White
Write-Host "8. Verifier que le bon overlay s'affiche immediatement" -ForegroundColor White

Write-Host "`n=== Commandes de debug ===" -ForegroundColor Yellow
Write-Host "Pour voir les logs en temps reel:" -ForegroundColor White
Write-Host "adb logcat -s OverlayIntegration:V RetroArchOverlaySystem:V" -ForegroundColor Cyan

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