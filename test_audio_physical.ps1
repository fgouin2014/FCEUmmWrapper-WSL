# Test audio sur appareil physique
Write-Host "🎵 Test audio sur appareil physique" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan

# Vérifier que l'appareil est connecté
Write-Host "📱 Vérification de l'appareil..." -ForegroundColor Yellow
$devices = adb devices
if ($devices -notmatch "R5CT11TCQ1W") {
    Write-Host "❌ Appareil physique non détecté" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Appareil physique détecté" -ForegroundColor Green

# Installer l'APK sur l'appareil physique
Write-Host "📦 Installation sur appareil physique..." -ForegroundColor Yellow
adb -s R5CT11TCQ1W install -r app/build/outputs/apk/debug/app-arm64-v8a-debug.apk

# Lancer l'application
Write-Host "🚀 Lancement de l'application..." -ForegroundColor Yellow
adb -s R5CT11TCQ1W shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre que l'application se lance
Start-Sleep -Seconds 5

# Capturer les logs audio
Write-Host "📊 Capture des logs audio..." -ForegroundColor Yellow
adb -s R5CT11TCQ1W logcat -c
Start-Sleep -Seconds 2

# Capturer les logs pendant 30 secondes
Write-Host "⏱️ Capture des logs pendant 30 secondes..." -ForegroundColor Yellow
$job = Start-Job -ScriptBlock {
    adb -s R5CT11TCQ1W logcat | Select-String -Pattern "libOpenSLES|SL_RESULT_BUFFER_INSUFFICIENT|Audio initialisé|Audio nettoyé|fceumm|MainActivity" | Tee-Object -FilePath "audio_test_physical_device.txt"
}

Start-Sleep -Seconds 30
Stop-Job $job
Remove-Job $job

Write-Host "✅ Test terminé - Vérifiez audio_test_physical_device.txt" -ForegroundColor Green
Write-Host ""
Write-Host "🎯 Instructions de test:" -ForegroundColor Cyan
Write-Host "1. Ouvrez l'application sur votre téléphone" -ForegroundColor White
Write-Host "2. Chargez un jeu NES (Mario/Duck Hunt)" -ForegroundColor White
Write-Host "3. Vérifiez si vous entendez du son" -ForegroundColor White
Write-Host "4. Consultez audio_test_physical_device.txt pour les logs" -ForegroundColor White 