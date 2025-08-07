# Test du respect des layout_weight XML
Write-Host "Test des proportions XML (5+5 = 50%+50%)..." -ForegroundColor Green

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
Write-Host "  - 'Vraies dimensions controls_area: [largeur]x[hauteur]'" -ForegroundColor White
Write-Host "  - La hauteur doit maintenant reflecter 50% avec layout_weight=5" -ForegroundColor White
Write-Host "  - 'Fallback ecran complet' = probleme, les vues ne sont pas mesurees" -ForegroundColor Red
Write-Host "  - Verifiez visuellement que la section grise fait 50% de l'ecran !" -ForegroundColor Green

# Filtrer les logs pertinents
adb logcat | Select-String "Vraies dimensions|Fallback|controls_area|Portrait.*dimensions"