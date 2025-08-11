# Test simple des corrections d'overlay
Write-Host "Test des corrections d'overlay RetroArch" -ForegroundColor Green

# Lancer l'application
Write-Host "Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.EmulationActivity

# Attendre
Start-Sleep -Seconds 3

# Capturer les logs
Write-Host "Recuperation des logs..." -ForegroundColor Yellow
adb logcat -d | Select-String -Pattern "RetroArchOverlaySystem" | Select-Object -Last 10

Write-Host "Test termine !" -ForegroundColor Green


