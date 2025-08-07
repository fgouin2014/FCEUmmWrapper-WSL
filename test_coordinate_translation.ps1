# Test de la traduction des coordonnées zone contrôles -> écran complet
Write-Host "Test de la traduction des coordonnees..." -ForegroundColor Green

# Nettoyer les logs
Write-Host "Nettoyage des logs..." -ForegroundColor Yellow
adb logcat -c

# Lancer l'application en mode émulation directement
Write-Host "Lancement en mode EmulatorActivity..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.EmulatorActivity

# Attendre un peu pour que l'app se lance
Start-Sleep -Seconds 3

Write-Host "Logs en temps reel (Ctrl+C pour arreter):" -ForegroundColor Yellow
Write-Host "Recherchez:" -ForegroundColor Cyan
Write-Host "  - 'Controls_area - Position Y: [Y], Dimensions: [W]x[H]'" -ForegroundColor White
Write-Host "  - 'Traduction: local(...) -> relative(...) -> fullScreen(...) -> normalized(...)'" -ForegroundColor White
Write-Host "  - 'Resultat pour b: true' quand vous touchez le bouton B !" -ForegroundColor Green
Write-Host "  - Verifiez que les coordonnees sont maintenant correctement traduites !" -ForegroundColor Green

# Filtrer les logs pertinents pour voir la traduction
adb logcat | Select-String "Controls_area.*Position|Traduction.*local|Resultat.*true|RESTAURATION.*isPointInHitbox"