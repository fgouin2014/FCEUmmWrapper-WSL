# Test Audio Ultra Simple - FCEUmmWrapper
# Script pour tester l'audio sans glitches

Write-Host "=== TEST AUDIO ULTRA SIMPLE ===" -ForegroundColor Green

# Compiler et installer
Write-Host "1. Compilation et installation..." -ForegroundColor Yellow
./gradlew assembleDebug installDebug

# Lancer l'application
Write-Host "2. Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainMenuActivity

# Attendre un peu pour que l'application démarre
Start-Sleep -Seconds 3

# Capturer les logs audio avec focus sur l'approche ultra simple
Write-Host "3. Capture des logs audio..." -ForegroundColor Yellow
adb logcat -c
adb logcat | Select-String -Pattern "ULTRA SIMPLE|audio|Audio|AUDIO|LibretroWrapper|Volume|volume|ENABLE|DISABLE" | Out-File -FilePath "audio_ultra_simple_test.txt"

Write-Host "✅ Test terminé. Vérifiez le fichier audio_ultra_simple_test.txt pour les logs." -ForegroundColor Green
Write-Host ""
Write-Host "Instructions de test:" -ForegroundColor Cyan
Write-Host "1. Lancez l'application sur l'émulateur" -ForegroundColor White
Write-Host "2. Chargez une ROM NES" -ForegroundColor White
Write-Host "3. Vérifiez que le son fonctionne SANS GLITCHES" -ForegroundColor White
Write-Host "4. Testez pendant au moins 30 secondes" -ForegroundColor White
Write-Host "5. Consultez les logs dans audio_ultra_simple_test.txt" -ForegroundColor White
Write-Host ""
Write-Host "Logs attendus:" -ForegroundColor Yellow
Write-Host "- 'INITIALISATION AUDIO ULTRA SIMPLE'" -ForegroundColor White
Write-Host "- 'AUDIO INITIALISÉ AVEC SUCCÈS - APPROCHE ULTRA SIMPLE'" -ForegroundColor White
Write-Host "- 'Audio activé avec succès'" -ForegroundColor White
Write-Host "- 'Latence audio optimisée'" -ForegroundColor White
Write-Host ""
Write-Host "Résultat attendu: AUDIO SANS GLITCHES" -ForegroundColor Green 