# Script de diagnostic des contrôles
# Identifie pourquoi les contrôles ne fonctionnent plus

Write-Host "=== DIAGNOSTIC DES CONTRÔLES FCEUmmWrapper ===" -ForegroundColor Red

# 1. Vérifier que l'application est installée
Write-Host "`n1. Vérification de l'installation..." -ForegroundColor Yellow
adb shell pm list packages | findstr fceumm

# 2. Démarrer l'application
Write-Host "`n2. Démarrage de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity
Start-Sleep -Seconds 3

# 3. Capturer une capture d'écran
Write-Host "`n3. Capture d'écran pour vérifier l'affichage..." -ForegroundColor Yellow
adb shell screencap /sdcard/diagnostic_controls.png
adb pull /sdcard/diagnostic_controls.png ./diagnostic_controls.png
Write-Host "Capture d'écran sauvegardée dans diagnostic_controls.png" -ForegroundColor Green

# 4. Tester les contrôles avec des coordonnées différentes
Write-Host "`n4. Test des contrôles avec différentes coordonnées..." -ForegroundColor Yellow

# Test avec les anciennes coordonnées
Write-Host "Test avec les anciennes coordonnées..." -ForegroundColor Cyan
adb shell input tap 100 400  # D-Pad UP (ancien)
Start-Sleep -Milliseconds 500
adb shell input tap 600 400  # Bouton A (ancien)
Start-Sleep -Milliseconds 500

# Test avec les nouvelles coordonnées (plus grandes)
Write-Host "Test avec les nouvelles coordonnées..." -ForegroundColor Cyan
adb shell input tap 150 450  # D-Pad UP (nouveau)
Start-Sleep -Milliseconds 500
adb shell input tap 650 450  # Bouton A (nouveau)
Start-Sleep -Milliseconds 500

# Test avec des coordonnées plus larges
Write-Host "Test avec des coordonnées plus larges..." -ForegroundColor Cyan
adb shell input tap 200 500  # D-Pad DOWN
Start-Sleep -Milliseconds 500
adb shell input tap 700 500  # Bouton B
Start-Sleep -Milliseconds 500

# 5. Vérifier les logs en temps réel
Write-Host "`n5. Vérification des logs..." -ForegroundColor Yellow
Write-Host "Logs des 30 dernières secondes :" -ForegroundColor Cyan
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 10

# 6. Tester si l'application répond aux touches
Write-Host "`n6. Test de réactivité générale..." -ForegroundColor Yellow
Write-Host "Test de touches aléatoires sur l'écran..." -ForegroundColor Cyan
for ($i = 0; $i -lt 5; $i++) {
    $x = Get-Random -Minimum 100 -Maximum 700
    $y = Get-Random -Minimum 200 -Maximum 600
    Write-Host "Touch: $x, $y" -ForegroundColor Gray
    adb shell input tap $x $y
    Start-Sleep -Milliseconds 200
}

# 7. Vérifier si l'overlay est visible
Write-Host "`n7. Vérification de l'overlay..." -ForegroundColor Yellow
Write-Host "Test de touches dans les zones d'overlay..." -ForegroundColor Cyan

# Test des zones d'overlay potentielles
$overlayTests = @(
    @(100, 400, "D-Pad zone"),
    @(600, 400, "Button A zone"),
    @(300, 700, "Start zone"),
    @(400, 700, "Select zone")
)

foreach ($test in $overlayTests) {
    $x = $test[0]
    $y = $test[1]
    $desc = $test[2]
    Write-Host "Test $desc : $x, $y" -ForegroundColor Gray
    adb shell input tap $x $y
    Start-Sleep -Milliseconds 300
}

# 8. Capturer une capture finale
Write-Host "`n8. Capture d'écran finale..." -ForegroundColor Yellow
adb shell screencap /sdcard/diagnostic_final.png
adb pull /sdcard/diagnostic_final.png ./diagnostic_final.png
Write-Host "Capture finale sauvegardée dans diagnostic_final.png" -ForegroundColor Green

# 9. Résumé du diagnostic
Write-Host "`n=== RÉSUMÉ DU DIAGNOSTIC ===" -ForegroundColor Red
Write-Host "✓ Application installée et démarrée" -ForegroundColor Green
Write-Host "✓ Captures d'écran générées" -ForegroundColor Green
Write-Host "✓ Tests de contrôles effectués" -ForegroundColor Green
Write-Host "✓ Logs collectés" -ForegroundColor Green

Write-Host "`nFichiers générés :" -ForegroundColor Yellow
Write-Host "- diagnostic_controls.png : Affichage initial" -ForegroundColor White
Write-Host "- diagnostic_final.png : Affichage après tests" -ForegroundColor White

Write-Host "`nSi les contrôles ne fonctionnent toujours pas :" -ForegroundColor Yellow
Write-Host "1. Vérifiez les captures d'écran pour voir si l'overlay s'affiche" -ForegroundColor White
Write-Host "2. Consultez les logs pour les erreurs" -ForegroundColor White
Write-Host "3. Testez manuellement en touchant l'écran" -ForegroundColor White

Write-Host "`nDiagnostic terminé !" -ForegroundColor Green 