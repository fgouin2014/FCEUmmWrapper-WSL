# Diagnostic audio complet
Write-Host "🔍 Diagnostic audio complet" -ForegroundColor Cyan
Write-Host "==========================" -ForegroundColor Cyan

# Vérifier l'appareil
Write-Host "📱 Appareil connecté:" -ForegroundColor Yellow
adb devices

# Installer l'application
Write-Host "📦 Installation..." -ForegroundColor Yellow
adb -s R5CT11TCQ1W install -r app/build/outputs/apk/debug/app-arm64-v8a-debug.apk

# Lancer l'application
Write-Host "🚀 Lancement..." -ForegroundColor Yellow
adb -s R5CT11TCQ1W shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre
Start-Sleep -Seconds 3

# Capturer les logs de démarrage
Write-Host "📊 Logs de démarrage..." -ForegroundColor Yellow
adb -s R5CT11TCQ1W logcat -c
Start-Sleep -Seconds 2

# Capturer les logs pendant 10 secondes
$logs = adb -s R5CT11TCQ1W logcat -d | Select-String -Pattern "fceumm|Audio|OpenSLES|SL_RESULT|MainActivity|initLibretro"

Write-Host "📋 Logs capturés:" -ForegroundColor Green
$logs | ForEach-Object { Write-Host $_ -ForegroundColor White }

# Sauvegarder les logs
$logs | Out-File -FilePath "diagnose_audio_logs.txt"

Write-Host ""
Write-Host "🎯 Analyse:" -ForegroundColor Cyan
Write-Host "- Vérifiez si 'Audio initialisé avec succès' apparaît" -ForegroundColor White
Write-Host "- Vérifiez s'il y a des erreurs OpenSLES" -ForegroundColor White
Write-Host "- Vérifiez si initLibretro est appelé" -ForegroundColor White
Write-Host ""
Write-Host "📄 Logs complets dans: diagnose_audio_logs.txt" -ForegroundColor Green 