# Test du redimensionnement du viewport RetroArch
Write-Host "🎮 Test du redimensionnement du viewport RetroArch" -ForegroundColor Green

# 1. Lancer l'application
Write-Host "1️⃣ Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainMenuActivity
Start-Sleep -Seconds 2

# 2. Lancer l'émulation
Write-Host "2️⃣ Lancement de l'émulation..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.EmulationActivity --es "selected_rom" "test.nes"
Start-Sleep -Seconds 3

# 3. Vérifier les logs de redimensionnement
Write-Host "3️⃣ Vérification des logs de redimensionnement..." -ForegroundColor Yellow
adb logcat -d | Select-String "Redimensionnement|Viewport|Émulation redimensionnée|Mode PORTRAIT|Mode LANDSCAPE"

# 4. Tester le changement d'orientation
Write-Host "4️⃣ Test du changement d'orientation..." -ForegroundColor Yellow
Write-Host "   - Rotation en landscape..."
adb shell content insert --uri content://settings/system --bind name:s:accelerometer_rotation --bind value:i:1
adb shell content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:1
Start-Sleep -Seconds 2

Write-Host "   - Rotation en portrait..."
adb shell content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:0
Start-Sleep -Seconds 2

# 5. Vérifier les logs après changement d'orientation
Write-Host "5️⃣ Vérification des logs après changement d'orientation..." -ForegroundColor Yellow
adb logcat -d | Select-String "Configuration changée|Redimensionnement viewport|Émulation redimensionnée"

Write-Host "✅ Test du redimensionnement du viewport RetroArch terminé" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Résultats attendus :" -ForegroundColor Cyan
Write-Host "   - En PORTRAIT : Écran de jeu réduit (70%), contrôles en bas (30%)" -ForegroundColor White
Write-Host "   - En LANDSCAPE : Écran de jeu plein, overlay superposé" -ForegroundColor White
Write-Host "   - L'émulation doit prendre toute la largeur disponible" -ForegroundColor White
