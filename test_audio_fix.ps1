# Test des corrections audio OpenSLES
# Script pour vérifier que les problèmes de buffer sont résolus

Write-Host "🔧 Test des corrections audio OpenSLES" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# Compiler et installer l'application
Write-Host "📦 Compilation et installation..." -ForegroundColor Yellow
& .\gradlew clean assembleDebug installDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Échec de la compilation/installation" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Compilation et installation réussies" -ForegroundColor Green

# Lancer l'application
Write-Host "🚀 Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre un peu
Start-Sleep -Seconds 3

# Capturer les logs audio
Write-Host "📊 Capture des logs audio..." -ForegroundColor Yellow
adb logcat -c
adb logcat | Select-String -Pattern "libOpenSLES|SL_RESULT_BUFFER_INSUFFICIENT|Audio initialisé|Audio nettoyé" | Tee-Object -FilePath "audio_test_logs.txt"

Write-Host "✅ Test terminé - Vérifiez audio_test_logs.txt pour les résultats" -ForegroundColor Green
Write-Host ""
Write-Host "🎯 Résultats attendus:" -ForegroundColor Cyan
Write-Host "- Plus d'erreurs SL_RESULT_BUFFER_INSUFFICIENT" -ForegroundColor White
Write-Host "- Audio initialisé avec succès" -ForegroundColor White
Write-Host "- Son fonctionnel dans les jeux" -ForegroundColor White 