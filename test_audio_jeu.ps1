# Test Audio du Jeu - FCEUmmWrapper
# Script pour tester l'audio des jeux (Mario, Duck Hunt)

Write-Host "=== TEST AUDIO DU JEU ===" -ForegroundColor Green

# Lancer l'application
Write-Host "1. Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainMenuActivity

# Attendre un peu pour que l'application démarre
Start-Sleep -Seconds 3

# Capturer les logs de l'audio du jeu
Write-Host "2. Capture des logs de l'audio du jeu..." -ForegroundColor Yellow
adb logcat -c
adb logcat | Select-String -Pattern "Audio du jeu|forceGameAudioActivation|keepGameAudioActive|Audio du jeu traité" | Out-File -FilePath "test_audio_jeu.txt"

Write-Host "✅ Test terminé. Vérifiez le fichier test_audio_jeu.txt pour les logs." -ForegroundColor Green
Write-Host ""
Write-Host "Instructions de test:" -ForegroundColor Cyan
Write-Host "1. Lancez l'application sur l'émulateur" -ForegroundColor White
Write-Host "2. Cliquez sur 'Play' pour lancer l'émulation" -ForegroundColor White
Write-Host "3. Chargez Mario ou Duck Hunt" -ForegroundColor White
Write-Host "4. Vérifiez que vous entendez le son du jeu en continu" -ForegroundColor White
Write-Host "5. Testez les contrôles pour confirmer l'audio interactif" -ForegroundColor White
Write-Host "6. Consultez les logs dans test_audio_jeu.txt" -ForegroundColor White
Write-Host ""
Write-Host "Logs attendus:" -ForegroundColor Yellow
Write-Host "- 'Audio du jeu activé avec succès'" -ForegroundColor White
Write-Host "- 'Audio du jeu maintenu actif'" -ForegroundColor White
Write-Host "- 'Audio du jeu traité: X frames'" -ForegroundColor White
Write-Host "- 'Buffer de maintien jeu X envoyé'" -ForegroundColor White
Write-Host ""
Write-Host "Résultat attendu: AUDIO DU JEU CONTINU ET STABLE" -ForegroundColor Green 