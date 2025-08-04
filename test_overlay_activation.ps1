# Script de test pour vérifier l'activation des overlays
Write-Host "=== TEST D'ACTIVATION DES OVERLAYS ===" -ForegroundColor Green

Write-Host "`n--- Vérification des modifications ---" -ForegroundColor Cyan

# Vérifier que MainActivity a été modifié
$mainActivityFile = "app/src/main/java/com/fceumm/wrapper/MainActivity.java"
if (Test-Path $mainActivityFile) {
    $content = Get-Content $mainActivityFile -Raw
    
    $checks = @(
        @{ Name = "Forçage de l'activation"; Pattern = "overlayPreferences\.setOverlayEnabled\(true\)" },
        @{ Name = "Logs d'initialisation"; Pattern = "=== INITIALISATION DES OVERLAYS RETROARCH ===" },
        @{ Name = "Forçage de la visibilité"; Pattern = "retroArchOverlayView\.setVisibility" },
        @{ Name = "Forçage du rendu"; Pattern = "retroArchOverlayView\.forceRender\(\)" }
    )
    
    foreach ($check in $checks) {
        if ($content -match $check.Pattern) {
            Write-Host "✅ $($check.Name)" -ForegroundColor Green
        } else {
            Write-Host "❌ $($check.Name)" -ForegroundColor Red
        }
    }
} else {
    Write-Host "❌ MainActivity.java non trouvé" -ForegroundColor Red
}

# Vérifier la compilation
Write-Host "`n--- Vérification de la compilation ---" -ForegroundColor Cyan
try {
    Write-Host "Compilation en cours..." -ForegroundColor Yellow
    & "./gradlew" "assembleDebug" | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Compilation réussie" -ForegroundColor Green
    } else {
        Write-Host "❌ Erreur de compilation" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Erreur lors de la compilation : $_" -ForegroundColor Red
}

# Vérifier les APKs
Write-Host "`n--- Vérification des APKs ---" -ForegroundColor Cyan
$apkDir = "app/build/outputs/apk/debug"
if (Test-Path $apkDir) {
    $apks = Get-ChildItem $apkDir -Filter "*.apk"
    if ($apks.Count -gt 0) {
        Write-Host "✅ APKs générés ($($apks.Count) fichiers)" -ForegroundColor Green
        foreach ($apk in $apks) {
            $size = [math]::Round($apk.Length / 1MB, 2)
            Write-Host "   - $($apk.Name) ($size MB)" -ForegroundColor Gray
        }
    } else {
        Write-Host "❌ Aucun APK trouvé" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Dossier APK non trouvé" -ForegroundColor Red
}

Write-Host "`n--- INSTRUCTIONS DE TEST ---" -ForegroundColor Cyan
Write-Host "📱 Pour tester les overlays :" -ForegroundColor White
Write-Host "1. Installez l'APK sur votre appareil" -ForegroundColor Gray
Write-Host "2. Lancez l'application" -ForegroundColor Gray
Write-Host "3. Vérifiez les logs avec : adb logcat | grep -E '(MainActivity|RetroArchOverlay)'" -ForegroundColor Gray
Write-Host "4. Vous devriez voir les messages d'initialisation des overlays" -ForegroundColor Gray
Write-Host "5. Les boutons tactiles devraient maintenant être visibles" -ForegroundColor Gray

Write-Host "`n--- RÉSUMÉ ---" -ForegroundColor Cyan
Write-Host "🔧 Modifications apportées :" -ForegroundColor White
Write-Host "   - Forçage de l'activation des overlays" -ForegroundColor Green
Write-Host "   - Ajout de logs détaillés" -ForegroundColor Green
Write-Host "   - Forçage de la visibilité de la vue" -ForegroundColor Green
Write-Host "   - Forçage du rendu OpenGL" -ForegroundColor Green

Write-Host "`n🎯 Résultat attendu :" -ForegroundColor White
Write-Host "   - Les overlays devraient maintenant être visibles" -ForegroundColor Yellow
Write-Host "   - Les boutons tactiles devraient apparaître" -ForegroundColor Yellow
Write-Host "   - Le jeu devrait rester visible" -ForegroundColor Yellow

Write-Host "`nTest terminé." -ForegroundColor Green 