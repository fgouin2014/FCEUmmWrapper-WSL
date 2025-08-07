# Test de la correction de la section grise
Write-Host "Test de la section grise corrigee (70% + 30%)..." -ForegroundColor Green

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
Write-Host "  - 'Rendu de l'overlay - largeur: [largeur], hauteur: [hauteur]'" -ForegroundColor White
Write-Host "  - 'Coordonnees utilisees directement' (plus de decalage empirique)" -ForegroundColor White
Write-Host "  - 'Resultat pour b: true' quand vous touchez le bouton B !" -ForegroundColor Green
Write-Host "  - Verifiez que la section grise fait maintenant 30% de l'ecran !" -ForegroundColor Green

# Filtrer les logs pertinents pour voir la correction
adb logcat | Select-String "OverlayRenderView.*largeur|hauteur|utilisees directement|Resultat.*true|Touch down"