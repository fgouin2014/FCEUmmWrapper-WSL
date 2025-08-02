# Script de test de l'interface graphique des contrôles
# Teste l'affichage, la réactivité et le mapping des contrôles

Write-Host "=== TEST DE L'INTERFACE GRAPHIQUE DES CONTRÔLES ===" -ForegroundColor Green

# 1. Vérifier la connexion ADB
Write-Host "`n1. Vérification de la connexion ADB..." -ForegroundColor Yellow
$devices = adb devices
Write-Host "Appareils connectés :" -ForegroundColor Cyan
Write-Host $devices

# 2. Nettoyer les logs
Write-Host "`n2. Nettoyage des logs..." -ForegroundColor Yellow
adb logcat -c

# 3. Démarrer l'application
Write-Host "`n3. Démarrage de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity
Start-Sleep -Seconds 3

# 4. Capturer l'écran initial
Write-Host "`n4. Capture d'écran initiale..." -ForegroundColor Yellow
adb shell screencap /sdcard/gui_controls_initial.png
adb pull /sdcard/gui_controls_initial.png ./gui_controls_initial.png
Write-Host "Capture initiale sauvegardée dans gui_controls_initial.png" -ForegroundColor Green

# 5. Test des zones de contrôle avec feedback visuel
Write-Host "`n5. Test des zones de contrôle..." -ForegroundColor Yellow

# Test D-Pad avec coordonnées précises
Write-Host "Test D-Pad UP..." -ForegroundColor Cyan
adb shell input tap 90 420
Start-Sleep -Milliseconds 500
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 2

Write-Host "Test D-Pad DOWN..." -ForegroundColor Cyan
adb shell input tap 90 480
Start-Sleep -Milliseconds 500
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 2

Write-Host "Test D-Pad LEFT..." -ForegroundColor Cyan
adb shell input tap 70 450
Start-Sleep -Milliseconds 500
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 2

Write-Host "Test D-Pad RIGHT..." -ForegroundColor Cyan
adb shell input tap 110 450
Start-Sleep -Milliseconds 500
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 2

# Test boutons A/B
Write-Host "Test bouton A..." -ForegroundColor Cyan
adb shell input tap 550 420
Start-Sleep -Milliseconds 500
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 2

Write-Host "Test bouton B..." -ForegroundColor Cyan
adb shell input tap 630 420
Start-Sleep -Milliseconds 500
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 2

# Test START/SELECT
Write-Host "Test START..." -ForegroundColor Cyan
adb shell input tap 340 720
Start-Sleep -Milliseconds 500
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 2

Write-Host "Test SELECT..." -ForegroundColor Cyan
adb shell input tap 440 720
Start-Sleep -Milliseconds 500
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 2

# 6. Test multi-touch
Write-Host "`n6. Test multi-touch..." -ForegroundColor Yellow
Write-Host "Test simultané D-Pad UP + Bouton A..." -ForegroundColor Cyan
adb shell input swipe 90 420 90 420 100
adb shell input swipe 550 420 550 420 100
Start-Sleep -Milliseconds 500
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 3

# 7. Test de réactivité rapide
Write-Host "`n7. Test de réactivité rapide..." -ForegroundColor Yellow
Write-Host "Test de pressions rapides..." -ForegroundColor Cyan
for ($i = 0; $i -lt 5; $i++) {
    adb shell input tap 90 420  # UP
    Start-Sleep -Milliseconds 100
    adb shell input tap 550 420  # A
    Start-Sleep -Milliseconds 100
}
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 5

# 8. Test d'orientation (si supporté)
Write-Host "`n8. Test d'orientation..." -ForegroundColor Yellow
Write-Host "Rotation en mode paysage..." -ForegroundColor Cyan
adb shell content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:1
Start-Sleep -Seconds 2

Write-Host "Test des contrôles en mode paysage..." -ForegroundColor Cyan
adb shell input tap 90 420  # D-Pad
adb shell input tap 550 420  # Bouton A
Start-Sleep -Milliseconds 500

Write-Host "Retour en mode portrait..." -ForegroundColor Cyan
adb shell content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:0
Start-Sleep -Seconds 2

# 9. Capture d'écran finale
Write-Host "`n9. Capture d'écran finale..." -ForegroundColor Yellow
adb shell screencap /sdcard/gui_controls_final.png
adb pull /sdcard/gui_controls_final.png ./gui_controls_final.png
Write-Host "Capture finale sauvegardée dans gui_controls_final.png" -ForegroundColor Green

# 10. Résumé des logs
Write-Host "`n10. Résumé des logs..." -ForegroundColor Yellow
Write-Host "Logs des événements de contrôle :" -ForegroundColor Cyan
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 15

# 11. Analyse des résultats
Write-Host "`n=== ANALYSE DES RÉSULTATS ===" -ForegroundColor Red

Write-Host "`nFichiers générés :" -ForegroundColor Yellow
Write-Host "- gui_controls_initial.png : Interface initiale" -ForegroundColor White
Write-Host "- gui_controls_final.png : Interface après tests" -ForegroundColor White

Write-Host "`nTests effectués :" -ForegroundColor Yellow
Write-Host "✓ Test des zones D-Pad (UP, DOWN, LEFT, RIGHT)" -ForegroundColor Green
Write-Host "✓ Test des boutons d'action (A, B)" -ForegroundColor Green
Write-Host "✓ Test des boutons système (START, SELECT)" -ForegroundColor Green
Write-Host "✓ Test multi-touch" -ForegroundColor Green
Write-Host "✓ Test de réactivité rapide" -ForegroundColor Green
Write-Host "✓ Test d'orientation" -ForegroundColor Green

Write-Host "`nVérifications à effectuer :" -ForegroundColor Yellow
Write-Host "1. Les contrôles sont-ils visibles sur les captures d'écran ?" -ForegroundColor White
Write-Host "2. Les logs montrent-ils les événements de pression ?" -ForegroundColor White
Write-Host "3. L'interface s'adapte-t-elle à l'orientation ?" -ForegroundColor White
Write-Host "4. Les animations de pression sont-elles visibles ?" -ForegroundColor White

Write-Host "`nTest terminé !" -ForegroundColor Green 