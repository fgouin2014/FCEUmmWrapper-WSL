# Test des diagonales pour Contra
Write-Host "=== TEST DES DIAGONALES POUR CONTRA ===" -ForegroundColor Green

# Instructions pour l'utilisateur
Write-Host "`nInstructions pour tester les diagonales :" -ForegroundColor Yellow
Write-Host "1. Lancez l'application FCEUmm Wrapper" -ForegroundColor White
Write-Host "2. Chargez le jeu Contra" -ForegroundColor White
Write-Host "3. Testez les diagonales suivantes avec UN SEUL DOIGT :" -ForegroundColor White
Write-Host "   - Touchez entre UP et RIGHT (haut-droite)" -ForegroundColor Cyan
Write-Host "   - Touchez entre UP et LEFT (haut-gauche)" -ForegroundColor Cyan
Write-Host "   - Touchez entre DOWN et RIGHT (bas-droite)" -ForegroundColor Cyan
Write-Host "   - Touchez entre DOWN et LEFT (bas-gauche)" -ForegroundColor Cyan
Write-Host "4. Vérifiez que le personnage se déplace en diagonale" -ForegroundColor White

Write-Host "`nTest des combinaisons A+B avec un doigt :" -ForegroundColor Yellow
Write-Host "- Touchez entre les boutons A et B (positionnés en diagonale)" -ForegroundColor Cyan
Write-Host "- Vérifiez que les deux boutons sont pressés simultanément" -ForegroundColor White

Write-Host "`nAméliorations apportées :" -ForegroundColor Green
Write-Host "✅ Détection des diagonales avec un seul doigt" -ForegroundColor Green
Write-Host "✅ Support des combinaisons A+B simultanées" -ForegroundColor Green
Write-Host "✅ Gestion intelligente du multi-touch" -ForegroundColor Green
Write-Host "✅ Zones de tolérance étendues pour faciliter le toucher" -ForegroundColor Green

Write-Host "`nLogs de débogage disponibles dans logcat :" -ForegroundColor Yellow
Write-Host "adb logcat | findstr 'Bouton pressé\|Bouton relâché'" -ForegroundColor Gray

Write-Host "`nAppuyez sur Entrée pour continuer..." -ForegroundColor White
Read-Host 