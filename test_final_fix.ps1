# Test de la correction finale qui reproduit la logique de RetroArchOverlayLoader
Write-Host "Test de la correction finale de normalisation..." -ForegroundColor Green

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
Write-Host "  - 'Coordonnees normalisees par OverlayRenderView' avec dimensions" -ForegroundColor White
Write-Host "  - 'Bouton directionnel/action/combine detecte' avec hitbox" -ForegroundColor White
Write-Host "  - 'Resultat pour [nom_bouton]: true' (au lieu de false)" -ForegroundColor Green
Write-Host "  - Le personnage qui saute dans le jeu !" -ForegroundColor Green

# Filtrer les logs pertinents
adb logcat | Select-String "RetroArchOverlaySystem|OverlayRenderView|normalisees|detecte|Resultat|Touch down"