# Script de test pour vérifier l'overlay des boutons
Write-Host "=== TEST DE L'OVERLAY DES BOUTONS ===" -ForegroundColor Green

# Vérifier si l'application est installée
Write-Host "Vérification de l'installation..." -ForegroundColor Yellow
adb shell pm list packages | findstr fceumm

# Lancer l'application et capturer les logs
Write-Host "Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre un peu
Start-Sleep -Seconds 3

# Capturer les logs pour voir ce qui se passe
Write-Host "Capture des logs..." -ForegroundColor Yellow
adb logcat -s MainActivity:V SimpleFallbackOverlay:V RetroArchOverlayView:V -d

Write-Host "=== FIN DU TEST ===" -ForegroundColor Green 