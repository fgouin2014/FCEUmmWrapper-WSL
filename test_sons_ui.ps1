# Test Sons UI - FCEUmmWrapper
# Script pour tester les sons UI dans les menus

Write-Host "=== TEST SONS UI ===" -ForegroundColor Green

# Lancer l'application
Write-Host "1. Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainMenuActivity

# Attendre un peu pour que l'application démarre
Start-Sleep -Seconds 3

# Capturer les logs des sons UI
Write-Host "2. Capture des logs des sons UI..." -ForegroundColor Yellow
adb logcat -c
adb logcat | Select-String -Pattern "Son UI|playUISound|UI|Bouton.*pressé" | Out-File -FilePath "test_sons_ui.txt"

Write-Host "✅ Test terminé. Vérifiez le fichier test_sons_ui.txt pour les logs." -ForegroundColor Green
Write-Host ""
Write-Host "Instructions de test:" -ForegroundColor Cyan
Write-Host "1. Lancez l'application sur l'émulateur" -ForegroundColor White
Write-Host "2. Cliquez sur les boutons du menu principal (Play, Settings, About)" -ForegroundColor White
Write-Host "3. Vérifiez que vous entendez un son à chaque clic" -ForegroundColor White
Write-Host "4. Testez les boutons de l'activité principale (menu, fermer)" -ForegroundColor White
Write-Host "5. Consultez les logs dans test_sons_ui.txt" -ForegroundColor White
Write-Host ""
Write-Host "Logs attendus:" -ForegroundColor Yellow
Write-Host "- 'Son UI joué pour le bouton Play'" -ForegroundColor White
Write-Host "- 'Son UI joué pour le bouton Settings'" -ForegroundColor White
Write-Host "- 'Son UI joué pour le bouton About'" -ForegroundColor White
Write-Host "- 'Son UI envoyé avec succès'" -ForegroundColor White
Write-Host ""
Write-Host "Résultat attendu: SONS UI AUDIBLES À CHAQUE CLIC" -ForegroundColor Green 