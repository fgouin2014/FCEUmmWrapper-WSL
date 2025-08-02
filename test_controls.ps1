# Script de test des contrôles pour FCEUmmWrapper
# Teste l'affichage, la réactivité et le fonctionnement des contrôles

Write-Host "=== TEST DES CONTRÔLES FCEUmmWrapper ===" -ForegroundColor Green

# 1. Vérifier que l'émulateur est connecté
Write-Host "`n1. Vérification de la connexion ADB..." -ForegroundColor Yellow
adb devices

# 2. Démarrer l'application si elle n'est pas déjà en cours
Write-Host "`n2. Démarrage de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity

# 3. Attendre que l'application se charge
Write-Host "`n3. Attente du chargement de l'application..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# 4. Capturer une capture d'écran pour vérifier l'affichage des contrôles
Write-Host "`n4. Capture d'écran pour vérifier l'affichage..." -ForegroundColor Yellow
adb shell screencap /sdcard/controls_test.png
adb pull /sdcard/controls_test.png ./controls_test.png
Write-Host "Capture d'écran sauvegardée dans controls_test.png" -ForegroundColor Green

# 5. Tester les contrôles tactiles
Write-Host "`n5. Test des contrôles tactiles..." -ForegroundColor Yellow

# Test du D-Pad
Write-Host "Test du D-Pad UP..." -ForegroundColor Cyan
adb shell input tap 100 400
Start-Sleep -Milliseconds 500

Write-Host "Test du D-Pad DOWN..." -ForegroundColor Cyan
adb shell input tap 100 500
Start-Sleep -Milliseconds 500

Write-Host "Test du D-Pad LEFT..." -ForegroundColor Cyan
adb shell input tap 80 450
Start-Sleep -Milliseconds 500

Write-Host "Test du D-Pad RIGHT..." -ForegroundColor Cyan
adb shell input tap 120 450
Start-Sleep -Milliseconds 500

# Test des boutons d'action
Write-Host "Test du bouton A..." -ForegroundColor Cyan
adb shell input tap 600 400
Start-Sleep -Milliseconds 500

Write-Host "Test du bouton B..." -ForegroundColor Cyan
adb shell input tap 680 400
Start-Sleep -Milliseconds 500

# Test des boutons système
Write-Host "Test du bouton START..." -ForegroundColor Cyan
adb shell input tap 300 700
Start-Sleep -Milliseconds 500

Write-Host "Test du bouton SELECT..." -ForegroundColor Cyan
adb shell input tap 400 700
Start-Sleep -Milliseconds 500

# 6. Capturer une nouvelle capture d'écran après les tests
Write-Host "`n6. Capture d'écran après les tests..." -ForegroundColor Yellow
adb shell screencap /sdcard/controls_test_after.png
adb pull /sdcard/controls_test_after.png ./controls_test_after.png
Write-Host "Capture d'écran après tests sauvegardée dans controls_test_after.png" -ForegroundColor Green

# 7. Vérifier les logs pour les événements de contrôle
Write-Host "`n7. Vérification des logs de contrôle..." -ForegroundColor Yellow
Write-Host "Collecte des logs des 30 dernières secondes..." -ForegroundColor Cyan
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 20

# 8. Test de multi-touch (simulation)
Write-Host "`n8. Test de multi-touch..." -ForegroundColor Yellow
Write-Host "Simulation de pression simultanée sur A et B..." -ForegroundColor Cyan
adb shell input touchscreen swipe 600 400 680 400 1000

# 9. Test de réactivité
Write-Host "`n9. Test de réactivité..." -ForegroundColor Yellow
Write-Host "Test de pression rapide sur le D-Pad..." -ForegroundColor Cyan
for ($i = 0; $i -lt 5; $i++) {
    adb shell input tap 100 400
    Start-Sleep -Milliseconds 100
    adb shell input tap 100 500
    Start-Sleep -Milliseconds 100
}

# 10. Résumé des tests
Write-Host "`n=== RÉSUMÉ DES TESTS ===" -ForegroundColor Green
Write-Host "✓ Tests de contrôle tactiles effectués" -ForegroundColor Green
Write-Host "✓ Captures d'écran générées" -ForegroundColor Green
Write-Host "✓ Logs collectés" -ForegroundColor Green
Write-Host "✓ Tests de réactivité effectués" -ForegroundColor Green

Write-Host "`nVérifiez les fichiers suivants :" -ForegroundColor Yellow
Write-Host "- controls_test.png : Affichage initial des contrôles" -ForegroundColor White
Write-Host "- controls_test_after.png : Affichage après les tests" -ForegroundColor White

Write-Host "`nPour surveiller les logs en temps réel :" -ForegroundColor Yellow
Write-Host "adb logcat -s 'com.fceumm.wrapper' -v time" -ForegroundColor Cyan

Write-Host "`nTest terminé !" -ForegroundColor Green 