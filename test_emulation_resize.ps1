# Test du redimensionnement de l'émulation
Write-Host "🎮 Test du redimensionnement de l'émulation" -ForegroundColor Green

# 1. Lancer l'émulation
Write-Host "1️⃣ Lancement de l'émulation..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.EmulationActivity --es "selected_rom" "test.nes"
Start-Sleep -Seconds 3

# 2. Vérifier les logs de redimensionnement
Write-Host "2️⃣ Vérification des logs de redimensionnement..." -ForegroundColor Yellow
adb logcat -d | Select-String "Émulation configurée|Surface redimensionnée|Émulation forcée|Redimensionnement viewport"

# 3. Vérifier les dimensions de l'écran
Write-Host "3️⃣ Vérification des dimensions de l'écran..." -ForegroundColor Yellow
adb shell wm size

Write-Host "✅ Test du redimensionnement de l'émulation terminé" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Résultats attendus :" -ForegroundColor Cyan
Write-Host "   - L'émulation doit remplir tout le viewport disponible" -ForegroundColor White
Write-Host "   - En portrait : Écran de jeu réduit mais rempli" -ForegroundColor White
Write-Host "   - En landscape : Écran de jeu plein" -ForegroundColor White


