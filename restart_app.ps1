# Script pour forcer le redémarrage complet de l'application
Write-Host "=== REDÉMARRAGE COMPLET DE L'APPLICATION ===" -ForegroundColor Green

# Arrêter complètement l'application
Write-Host "Arrêt de l'application..." -ForegroundColor Yellow
adb shell am force-stop com.fceumm.wrapper

# Attendre un peu
Start-Sleep -Seconds 2

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
adb logcat -s MainActivity:V SimpleFallbackOverlay:V -d

Write-Host "=== FIN DU REDÉMARRAGE ===" -ForegroundColor Green 