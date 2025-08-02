# Script de test du mapping des boutons
# Teste chaque bouton et vérifie les logs

Write-Host "=== TEST DU MAPPING DES BOUTONS ===" -ForegroundColor Green

# 1. Démarrer l'application
Write-Host "`n1. Démarrage de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity
Start-Sleep -Seconds 3

# 2. Nettoyer les logs
Write-Host "`n2. Nettoyage des logs..." -ForegroundColor Yellow
adb logcat -c

# 3. Test de chaque bouton avec pause pour voir les logs
Write-Host "`n3. Test de chaque bouton..." -ForegroundColor Yellow

# Test D-Pad UP
Write-Host "Test D-Pad UP..." -ForegroundColor Cyan
adb shell input tap 100 400
Start-Sleep -Seconds 1
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 2

# Test D-Pad DOWN
Write-Host "Test D-Pad DOWN..." -ForegroundColor Cyan
adb shell input tap 100 500
Start-Sleep -Seconds 1
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 2

# Test D-Pad LEFT
Write-Host "Test D-Pad LEFT..." -ForegroundColor Cyan
adb shell input tap 80 450
Start-Sleep -Seconds 1
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 2

# Test D-Pad RIGHT
Write-Host "Test D-Pad RIGHT..." -ForegroundColor Cyan
adb shell input tap 120 450
Start-Sleep -Seconds 1
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 2

# Test Bouton A
Write-Host "Test Bouton A..." -ForegroundColor Cyan
adb shell input tap 600 400
Start-Sleep -Seconds 1
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 2

# Test Bouton B
Write-Host "Test Bouton B..." -ForegroundColor Cyan
adb shell input tap 680 400
Start-Sleep -Seconds 1
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 2

# Test START
Write-Host "Test START..." -ForegroundColor Cyan
adb shell input tap 300 700
Start-Sleep -Seconds 1
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 2

# Test SELECT
Write-Host "Test SELECT..." -ForegroundColor Cyan
adb shell input tap 400 700
Start-Sleep -Seconds 1
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 2

# 4. Résumé des logs
Write-Host "`n4. Résumé de tous les logs..." -ForegroundColor Yellow
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 10

Write-Host "`nTest terminé !" -ForegroundColor Green 