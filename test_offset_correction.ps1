# Test de la correction de décalage empirique
Write-Host "Test de la correction de decalage empirique..." -ForegroundColor Green

# Nettoyer les logs
Write-Host "Nettoyage des logs..." -ForegroundColor Yellow
adb logcat -c

# Lancer l'application
Write-Host "Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre un peu pour que l'app se lance
Start-Sleep -Seconds 3

Write-Host "Logs en temps reel (Ctrl+C pour arreter):" -ForegroundColor Yellow
Write-Host "Recherchez:" -ForegroundColor Cyan
Write-Host "  - 'Correction de decalage appliquee' avec offset" -ForegroundColor White
Write-Host "  - 'Coordonnees corrigees' avec nouvelles valeurs" -ForegroundColor White
Write-Host "  - 'Resultat pour [bouton]: true' (enfin !)" -ForegroundColor Green
Write-Host "  - Le personnage qui saute quand vous touchez le bouton B !" -ForegroundColor Green

# Filtrer les logs pertinents pour voir la correction
adb logcat | Select-String "RetroArchOverlaySystem.*correction|corrigees|Resultat.*true|Touch down"