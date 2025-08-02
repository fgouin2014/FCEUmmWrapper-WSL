# Script de test des combinaisons de boutons
# Teste les diagonales et combinaisons A+B

Write-Host "=== TEST DES COMBINAISONS DE BOUTONS FCEUmmWrapper ===" -ForegroundColor Red

# 1. Vérifier que l'application est installée
Write-Host "`n1. Vérification de l'installation..." -ForegroundColor Yellow
adb shell pm list packages | findstr fceumm

# 2. Démarrer l'application
Write-Host "`n2. Démarrage de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity
Start-Sleep -Seconds 3

# 3. Capturer une capture d'écran initiale
Write-Host "`n3. Capture d'écran initiale..." -ForegroundColor Yellow
adb shell screencap /sdcard/test_combinaisons_initial.png
adb pull /sdcard/test_combinaisons_initial.png ./test_combinaisons_initial.png

# 4. Test des combinaisons de boutons
Write-Host "`n4. Test des combinaisons de boutons..." -ForegroundColor Yellow

# Test diagonale UP+RIGHT
Write-Host "Test diagonale UP+RIGHT..." -ForegroundColor Cyan
adb shell input touchscreen swipe 100 300 200 200 100
Start-Sleep -Milliseconds 500

# Test diagonale UP+LEFT
Write-Host "Test diagonale UP+LEFT..." -ForegroundColor Cyan
adb shell input touchscreen swipe 200 300 100 200 100
Start-Sleep -Milliseconds 500

# Test diagonale DOWN+RIGHT
Write-Host "Test diagonale DOWN+RIGHT..." -ForegroundColor Cyan
adb shell input touchscreen swipe 100 500 200 600 100
Start-Sleep -Milliseconds 500

# Test diagonale DOWN+LEFT
Write-Host "Test diagonale DOWN+LEFT..." -ForegroundColor Cyan
adb shell input touchscreen swipe 200 500 100 600 100
Start-Sleep -Milliseconds 500

# 5. Test combinaison A+B
Write-Host "`n5. Test combinaison A+B..." -ForegroundColor Yellow

# Presser A et B simultanément
Write-Host "Test A+B simultané..." -ForegroundColor Cyan
adb shell input touchscreen tap 600 400  # A
Start-Sleep -Milliseconds 100
adb shell input touchscreen tap 500 400  # B
Start-Sleep -Milliseconds 500

# 6. Test multi-touch avec deux doigts
Write-Host "`n6. Test multi-touch avec deux doigts..." -ForegroundColor Yellow

# Simuler deux doigts sur A et B
Write-Host "Test multi-touch A+B..." -ForegroundColor Cyan
adb shell input touchscreen tap 600 400  # Premier doigt sur A
Start-Sleep -Milliseconds 50
adb shell input touchscreen tap 500 400  # Deuxième doigt sur B
Start-Sleep -Milliseconds 1000

# 7. Test combinaison D-Pad + A
Write-Host "`n7. Test combinaison D-Pad + A..." -ForegroundColor Yellow

# Presser UP et A simultanément
Write-Host "Test UP+A..." -ForegroundColor Cyan
adb shell input touchscreen tap 100 300  # UP
Start-Sleep -Milliseconds 50
adb shell input touchscreen tap 600 400  # A
Start-Sleep -Milliseconds 1000

# Presser RIGHT et B simultanément
Write-Host "Test RIGHT+B..." -ForegroundColor Cyan
adb shell input touchscreen tap 200 400  # RIGHT
Start-Sleep -Milliseconds 50
adb shell input touchscreen tap 500 400  # B
Start-Sleep -Milliseconds 1000

# 8. Capturer une capture d'écran finale
Write-Host "`n8. Capture d'écran finale..." -ForegroundColor Yellow
adb shell screencap /sdcard/test_combinaisons_final.png
adb pull /sdcard/test_combinaisons_final.png ./test_combinaisons_final.png

# 9. Vérifier les logs
Write-Host "`n9. Vérification des logs..." -ForegroundColor Yellow
adb logcat -d | findstr -i "bouton\|button\|input\|touch" | Select-Object -Last 20

Write-Host "`n=== TEST TERMINÉ ===" -ForegroundColor Green
Write-Host "Vérifiez les captures d'écran et les logs pour analyser les combinaisons" -ForegroundColor White 