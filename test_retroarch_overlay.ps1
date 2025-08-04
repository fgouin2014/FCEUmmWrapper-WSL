# Script pour tester l'overlay RetroArch officiel
Write-Host "=== TEST DE L'OVERLAY RETROARCH OFFICIEL ===" -ForegroundColor Green

# Vérifier si l'application est installée
Write-Host "Vérification de l'installation..." -ForegroundColor Yellow
adb shell pm list packages | findstr fceumm

# Arrêter l'application si elle tourne
Write-Host "Arrêt de l'application..." -ForegroundColor Yellow
adb shell am force-stop com.fceumm.wrapper

# Nettoyer les logs
Write-Host "Nettoyage des logs..." -ForegroundColor Yellow
adb logcat -c

# Lancer l'application
Write-Host "Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre un peu
Start-Sleep -Seconds 5

# Capturer les logs
Write-Host "Capture des logs..." -ForegroundColor Yellow
adb logcat -s MainActivity:V RetroArchOverlayLoader:V SimpleFallbackOverlay:V -d

Write-Host "=== FIN DU TEST ===" -ForegroundColor Green 