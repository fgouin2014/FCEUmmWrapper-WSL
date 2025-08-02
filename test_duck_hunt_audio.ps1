
# Script pour tester l'optimisation audio de Duck Hunt
Write-Host "🎯 Test d'optimisation audio pour Duck Hunt" -ForegroundColor Green

# Installer l'APK
Write-Host "📱 Installation de l'APK..." -ForegroundColor Yellow
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Lancer l'application
Write-Host "🚀 Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre le chargement
Start-Sleep -Seconds 3

# Ouvrir les paramètres audio
Write-Host "🎵 Ouverture des paramètres audio..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.AudioSettingsActivity

# Attendre
Start-Sleep -Seconds 2

# Capturer les logs
Write-Host "📋 Capture des logs audio..." -ForegroundColor Yellow
adb logcat -c
adb logcat | Select-String "🎵" | Out-File -FilePath "duck_hunt_audio_test.txt"

Write-Host "✅ Test terminé! Vérifiez le fichier duck_hunt_audio_test.txt" -ForegroundColor Green
Write-Host "🎯 Instructions:" -ForegroundColor Cyan
Write-Host "1. Chargez Duck Hunt dans l'émulateur" -ForegroundColor White
Write-Host "2. Allez dans les paramètres audio" -ForegroundColor White
Write-Host "3. Testez les différents paramètres" -ForegroundColor White
Write-Host "4. Vérifiez que le son des canards est amélioré" -ForegroundColor White 