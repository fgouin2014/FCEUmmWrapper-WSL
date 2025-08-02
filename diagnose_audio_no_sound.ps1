# Script de diagnostic audio pour FCEUmm Wrapper
# Diagnostique les problèmes de son

Write-Host "🔊 DIAGNOSTIC AUDIO FCEUMM WRAPPER" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 1. Vérifier les permissions audio
Write-Host "1. Vérification des permissions audio..." -ForegroundColor Yellow
adb shell dumpsys media_session | Select-String -Pattern "FCEUmm|audio|permission" | ForEach-Object {
    Write-Host "   $_" -ForegroundColor Gray
}

# 2. Vérifier les processus audio
Write-Host "`n2. Vérification des processus audio..." -ForegroundColor Yellow
adb shell ps | Select-String -Pattern "audio|mediaserver|audioserver" | ForEach-Object {
    Write-Host "   $_" -ForegroundColor Gray
}

# 3. Vérifier les logs audio spécifiques
Write-Host "`n3. Logs audio spécifiques..." -ForegroundColor Yellow
adb logcat -d | Select-String -Pattern "Audio|OpenSL|SLES|audio|sound|volume" | Select-Object -Last 20 | ForEach-Object {
    Write-Host "   $_" -ForegroundColor Gray
}

# 4. Vérifier les paramètres audio du système
Write-Host "`n4. Paramètres audio système..." -ForegroundColor Yellow
adb shell settings get system volume_music
adb shell settings get system volume_notification
adb shell settings get system volume_ring

# 5. Vérifier les permissions de l'application
Write-Host "`n5. Permissions de l'application..." -ForegroundColor Yellow
adb shell dumpsys package com.fceumm.wrapper | Select-String -Pattern "permission|audio|record" | ForEach-Object {
    Write-Host "   $_" -ForegroundColor Gray
}

# 6. Test audio simple
Write-Host "`n6. Test audio simple..." -ForegroundColor Yellow
adb shell am start -a android.intent.action.MAIN -n com.android.music/.MediaPlaybackActivity

# 7. Vérifier les logs de l'application
Write-Host "`n7. Logs de l'application FCEUmm..." -ForegroundColor Yellow
adb logcat -d | Select-String -Pattern "FCEUmm|LibretroWrapper|Audio|InstantAudio|OpenSL" | Select-Object -Last 30 | ForEach-Object {
    Write-Host "   $_" -ForegroundColor Gray
}

# 8. Vérifier les erreurs audio
Write-Host "`n8. Erreurs audio..." -ForegroundColor Yellow
adb logcat -d | Select-String -Pattern "ERROR.*audio|ERROR.*Audio|ERROR.*OpenSL|ERROR.*SLES" | Select-Object -Last 10 | ForEach-Object {
    Write-Host "   $_" -ForegroundColor Red
}

# 9. Vérifier les warnings audio
Write-Host "`n9. Warnings audio..." -ForegroundColor Yellow
adb logcat -d | Select-String -Pattern "WARN.*audio|WARN.*Audio|WARN.*OpenSL|WARN.*SLES" | Select-Object -Last 10 | ForEach-Object {
    Write-Host "   $_" -ForegroundColor Yellow
}

# 10. Vérifier les informations audio
Write-Host "`n10. Informations audio..." -ForegroundColor Yellow
adb logcat -d | Select-String -Pattern "INFO.*audio|INFO.*Audio|INFO.*OpenSL|INFO.*SLES" | Select-Object -Last 10 | ForEach-Object {
    Write-Host "   $_" -ForegroundColor Green
}

# 11. Test de réinitialisation audio
Write-Host "`n11. Test de réinitialisation audio..." -ForegroundColor Yellow
adb shell am force-stop com.fceumm.wrapper
Start-Sleep -Seconds 2
adb shell am start -n com.fceumm.wrapper/.MainActivity

# 12. Vérifier les logs après redémarrage
Write-Host "`n12. Logs après redémarrage..." -ForegroundColor Yellow
Start-Sleep -Seconds 3
adb logcat -d | Select-String -Pattern "FCEUmm|LibretroWrapper|Audio|OpenSL" | Select-Object -Last 20 | ForEach-Object {
    Write-Host "   $_" -ForegroundColor Gray
}

Write-Host "`n🎵 DIAGNOSTIC TERMINÉ" -ForegroundColor Cyan
Write-Host "Vérifiez les logs ci-dessus pour identifier le problème audio." -ForegroundColor White
Write-Host "Problèmes courants:" -ForegroundColor Yellow
Write-Host "  - Permissions audio manquantes" -ForegroundColor White
Write-Host "  - OpenSL ES non initialisé" -ForegroundColor White
Write-Host "  - Volume système à 0" -ForegroundColor White
Write-Host "  - Core libretro non chargé" -ForegroundColor White
Write-Host "  - Buffer audio vide" -ForegroundColor White 