# Script pour tester manuellement la sélection de ROM
Write-Host "🎮 Test manuel de la sélection de ROM" -ForegroundColor Green

# Nettoyer les logs
Write-Host "📝 Nettoyage des logs..." -ForegroundColor Yellow
adb -s emulator-5554 logcat -c

# Lancer l'application
Write-Host "🚀 Lancement de l'application..." -ForegroundColor Yellow
adb -s emulator-5554 shell am start -n com.fceumm.wrapper/.MainMenuActivity

Write-Host ""
Write-Host "📋 Instructions manuelles :" -ForegroundColor Cyan
Write-Host "1. Cliquez sur '📁 Sélection ROM'" -ForegroundColor White
Write-Host "2. Sélectionnez une ROM (ex: 'Contra (USA)')" -ForegroundColor White
Write-Host "3. Vérifiez que la ROM se charge correctement" -ForegroundColor White
Write-Host "4. Appuyez sur Entrée pour continuer..." -ForegroundColor Yellow

Read-Host

# Vérifier les logs
Write-Host "📊 Vérification des logs..." -ForegroundColor Yellow
$logs = adb -s emulator-5554 logcat -s MainActivity RomSelectionActivity -d
Write-Host $logs

Write-Host "✅ Test terminé !" -ForegroundColor Green 