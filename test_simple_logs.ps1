# Test simple pour voir les logs actuels
Write-Host "Test simple des logs..." -ForegroundColor Green

# Nettoyer les logs
adb logcat -c

# Lancer l'application
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre un peu
Start-Sleep -Seconds 2

Write-Host "Logs touch en temps reel:" -ForegroundColor Yellow
# Filtrer seulement les logs touch
adb logcat | Select-String "touch|Touch|onTouch|RetroArch"