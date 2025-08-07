# Test de tous les overlays disponibles
# Script pour vérifier que tous les overlays fonctionnent après la correction

Write-Host "=== TEST DE TOUS LES OVERLAYS ===" -ForegroundColor Green
Write-Host "Date: $(Get-Date)" -ForegroundColor Yellow
Write-Host ""

# Liste des overlays à tester
$overlays = @(
    "nes.cfg",
    "snes.cfg", 
    "gba.cfg",
    "genesis.cfg",
    "arcade.cfg",
    "nintendo64.cfg",
    "retropad.cfg",
    "psx.cfg",
    "dreamcast.cfg",
    "neogeo.cfg",
    "gameboy.cfg",
    "sms.cfg"
)

Write-Host "Overlays à tester: $($overlays.Count)" -ForegroundColor Cyan
Write-Host ""

# Vérifier que l'APK est installé
Write-Host "Vérification de l'installation..." -ForegroundColor Yellow
$packageName = "com.fceumm.wrapper"
$installed = adb shell pm list packages | Select-String $packageName

if ($installed) {
    Write-Host "✅ Package installé: $packageName" -ForegroundColor Green
} else {
    Write-Host "❌ Package non installé: $packageName" -ForegroundColor Red
    Write-Host "Installation de l'APK..." -ForegroundColor Yellow
    adb install app/build/outputs/apk/debug/app-debug.apk
}

Write-Host ""

# Fonction pour tester un overlay
function Test-Overlay {
    param($overlayName)
    
    Write-Host "Test de l'overlay: $overlayName" -ForegroundColor Cyan
    
    # Lancer l'activité avec l'overlay
    $activity = "com.fceumm.wrapper.OverlayIntegrationActivity"
    $intent = "am start -n $packageName/$activity --es overlay_cfg $overlayName"
    
    Write-Host "Lancement: $intent" -ForegroundColor Gray
    adb shell $intent
    
    # Attendre que l'activité se lance
    Start-Sleep -Seconds 3
    
    # Vérifier les logs pour voir si l'overlay s'est chargé
    Write-Host "Vérification des logs..." -ForegroundColor Gray
    $logs = adb logcat -d | Select-String "OverlayIntegration|RetroArchOverlaySystem" | Select-Object -Last 10
    
    $success = $false
    $hasError = $false
    
    foreach ($log in $logs) {
        if ($log -match "Overlay chargé et activé: $overlayName") {
            $success = $true
            Write-Host "✅ Overlay chargé avec succès" -ForegroundColor Green
        }
        if ($log -match "Erreur lors du chargement de l'overlay") {
            $hasError = $true
            Write-Host "❌ Erreur de chargement" -ForegroundColor Red
        }
        if ($log -match "✅ Overlay sélectionné") {
            Write-Host "✅ Overlay sélectionné correctement" -ForegroundColor Green
        }
        if ($log -match "❌ Aucun overlay approprié trouvé") {
            Write-Host "⚠️  Fallback utilisé" -ForegroundColor Yellow
        }
    }
    
    if (-not $success -and -not $hasError) {
        Write-Host "⚠️  Pas de confirmation de chargement" -ForegroundColor Yellow
    }
    
    # Fermer l'activité
    adb shell am force-stop $packageName
    Start-Sleep -Seconds 1
    
    Write-Host ""
    return $success
}

# Test de chaque overlay
$results = @{}
$successCount = 0

foreach ($overlay in $overlays) {
    $result = Test-Overlay $overlay
    $results[$overlay] = $result
    if ($result) {
        $successCount++
    }
}

# Résumé
Write-Host "=== RÉSUMÉ DES TESTS ===" -ForegroundColor Green
Write-Host ""

foreach ($overlay in $overlays) {
    $status = if ($results[$overlay]) { "✅" } else { "❌" }
    Write-Host "$status $overlay" -ForegroundColor $(if ($results[$overlay]) { "Green" } else { "Red" })
}

Write-Host ""
Write-Host "Résultats: $successCount/$($overlays.Count) overlays fonctionnels" -ForegroundColor $(if ($successCount -eq $overlays.Count) { "Green" } else { "Yellow" })

if ($successCount -eq $overlays.Count) {
    Write-Host "🎉 TOUS LES OVERLAYS FONCTIONNENT !" -ForegroundColor Green
} else {
    Write-Host "⚠️  Certains overlays ne fonctionnent pas encore" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== FIN DU TEST ===" -ForegroundColor Green 