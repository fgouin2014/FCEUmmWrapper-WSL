# Test Audio Activation - FCEUmmWrapper
# Script pour tester l'activation du son

Write-Host "=== TEST ACTIVATION AUDIO ===" -ForegroundColor Green

# Lancer l'application
Write-Host "1. Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainMenuActivity

# Attendre un peu pour que l'application démarre
Start-Sleep -Seconds 3

# Capturer les logs audio avec focus sur l'activation
Write-Host "2. Capture des logs audio..." -ForegroundColor Yellow
adb logcat -c
adb logcat | Select-String -Pattern "audio|Audio|AUDIO|LibretroWrapper|Volume|volume|ENABLE|DISABLE" | Out-File -FilePath "audio_activation_test.txt"

Write-Host "✅ Test terminé. Vérifiez le fichier audio_activation_test.txt pour les logs." -ForegroundColor Green
Write-Host ""
Write-Host "Instructions de test:" -ForegroundColor Cyan
Write-Host "1. Lancez l'application sur l'émulateur" -ForegroundColor White
Write-Host "2. Chargez une ROM NES" -ForegroundColor White
Write-Host "3. Vérifiez que le son est activé automatiquement" -ForegroundColor White
Write-Host "4. Testez les contrôles de volume si disponibles" -ForegroundColor White
Write-Host "5. Consultez les logs dans audio_activation_test.txt" -ForegroundColor White
Write-Host ""
Write-Host "Logs attendus:" -ForegroundColor Yellow
Write-Host "- 'Audio activé avec succès'" -ForegroundColor White
Write-Host "- 'Volume audio défini à 1.00'" -ForegroundColor White
Write-Host "- 'Audio callback appelé'" -ForegroundColor White 