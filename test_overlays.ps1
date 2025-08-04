# Script pour tester les différents overlays RetroArch
Write-Host "=== Test des Overlays RetroArch ===" -ForegroundColor Green

# Installer l'APK
Write-Host "Installation de l'APK..." -ForegroundColor Yellow
adb -s R5CT11TCQ1W install -r app/build/outputs/apk/debug/app-arm64-v8a-debug.apk

# Démarrer l'application
Write-Host "Démarrage de l'application..." -ForegroundColor Yellow
adb -s R5CT11TCQ1W shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre que l'app démarre
Start-Sleep -Seconds 3

# Capturer les logs des overlays
Write-Host "Capture des logs des overlays..." -ForegroundColor Yellow
Write-Host "Appuyez sur Ctrl+C pour arrêter la capture" -ForegroundColor Red
Write-Host ""
Write-Host "=== Logs des Overlays ===" -ForegroundColor Cyan
adb -s R5CT11TCQ1W logcat -s RetroArchOverlayLoader:V MainActivity:V -d 