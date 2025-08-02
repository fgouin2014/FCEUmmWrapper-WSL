# Test des combinaisons A+B
Write-Host "🎮 Test des combinaisons A+B" -ForegroundColor Green

# Attendre que l'app soit lancée
Start-Sleep -Seconds 2

# Test 1: Presser A seul
Write-Host "Test 1: Presser A seul" -ForegroundColor Yellow
adb shell input tap 800 1200
Start-Sleep -Seconds 1
adb shell input tap 800 1200

# Test 2: Presser B seul  
Write-Host "Test 2: Presser B seul" -ForegroundColor Yellow
adb shell input tap 900 1200
Start-Sleep -Seconds 1
adb shell input tap 900 1200

# Test 3: COMBINAISON A+B (le test principal)
Write-Host "Test 3: COMBINAISON A+B (test principal)" -ForegroundColor Red
adb shell input tap 800 1200  # Presser A
Start-Sleep -Milliseconds 500
adb shell input tap 900 1200  # Presser B (toujours en tenant A)
Start-Sleep -Seconds 2
adb shell input tap 900 1200  # Relâcher B
Start-Sleep -Milliseconds 500
adb shell input tap 800 1200  # Relâcher A

Write-Host "✅ Tests terminés ! Vérifiez les logs pour voir les combinaisons" -ForegroundColor Green 