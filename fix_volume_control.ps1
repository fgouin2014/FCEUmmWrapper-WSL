# Script de correction du contrôle du volume pour FCEUmm Wrapper
# Corrige le problème où le volume descend à 0

Write-Host "CORRECTION CONTRÔLE VOLUME FCEUMM WRAPPER" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Vérifier l'état actuel du volume
Write-Host "1. Vérification de l'état actuel du volume..." -ForegroundColor Yellow
$currentVolume = adb shell settings get system volume_music
Write-Host "   Volume actuel: $currentVolume" -ForegroundColor Gray

# 2. Vérifier les processus qui contrôlent le volume
Write-Host "`n2. Vérification des processus de contrôle du volume..." -ForegroundColor Yellow
$volumeProcesses = adb shell ps | Select-String -Pattern "audio|media|volume|sound"
if ($volumeProcesses) {
    Write-Host "   Processus audio détectés:" -ForegroundColor Green
    $volumeProcesses | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   Aucun processus audio détecté" -ForegroundColor Yellow
}

# 3. Vérifier les services audio
Write-Host "`n3. Vérification des services audio..." -ForegroundColor Yellow
$audioServices = adb shell dumpsys audio | Select-String -Pattern "volume|stream|music"
if ($audioServices) {
    Write-Host "   Services audio détectés:" -ForegroundColor Green
    $audioServices | Select-Object -First 10 | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   Aucun service audio détecté" -ForegroundColor Yellow
}

# 4. Arrêter l'application pour libérer le contrôle du volume
Write-Host "`n4. Arrêt de l'application pour libérer le contrôle du volume..." -ForegroundColor Yellow
adb shell am force-stop com.fceumm.wrapper
Start-Sleep -Seconds 3

# 5. Vérifier le volume après arrêt de l'application
Write-Host "`n5. Vérification du volume après arrêt de l'application..." -ForegroundColor Yellow
$volumeAfterStop = adb shell settings get system volume_music
Write-Host "   Volume après arrêt: $volumeAfterStop" -ForegroundColor Gray

# 6. Forcer le volume à un niveau élevé
Write-Host "`n6. Forçage du volume à un niveau élevé..." -ForegroundColor Yellow
adb shell settings put system volume_music 15
Start-Sleep -Seconds 1

# 7. Vérifier le volume après forçage
Write-Host "`n7. Vérification du volume après forçage..." -ForegroundColor Yellow
$volumeAfterForce = adb shell settings get system volume_music
Write-Host "   Volume après forçage: $volumeAfterForce" -ForegroundColor Gray

# 8. Test des boutons de volume
Write-Host "`n8. Test des boutons de volume..." -ForegroundColor Yellow
Write-Host "   Test augmentation volume..." -ForegroundColor Gray
adb shell input keyevent 25  # Volume up
Start-Sleep -Seconds 1
$volumeAfterUp = adb shell settings get system volume_music
Write-Host "   Volume après Volume Up: $volumeAfterUp" -ForegroundColor Gray

# 9. Vérifier les logs de contrôle du volume
Write-Host "`n9. Vérification des logs de contrôle du volume..." -ForegroundColor Yellow
$volumeLogs = adb logcat -d | Select-String -Pattern "volume|Volume|VOLUME" | Select-Object -Last 10
if ($volumeLogs) {
    Write-Host "   Logs de volume détectés:" -ForegroundColor Green
    $volumeLogs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   Aucun log de volume détecté" -ForegroundColor Yellow
}

# 10. Vérifier les permissions de contrôle du volume
Write-Host "`n10. Vérification des permissions de contrôle du volume..." -ForegroundColor Yellow
$volumePermissions = adb shell dumpsys package com.fceumm.wrapper | Select-String -Pattern "MODIFY_AUDIO_SETTINGS|volume|audio"
if ($volumePermissions) {
    Write-Host "   Permissions de volume détectées:" -ForegroundColor Green
    $volumePermissions | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   Aucune permission de volume détectée" -ForegroundColor Yellow
}

# 11. Redémarrer l'application avec volume contrôlé
Write-Host "`n11. Redémarrage de l'application avec volume contrôlé..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainMenuActivity
Start-Sleep -Seconds 3

# 12. Test du volume après redémarrage
Write-Host "`n12. Test du volume après redémarrage..." -ForegroundColor Yellow
$volumeAfterRestart = adb shell settings get system volume_music
Write-Host "   Volume après redémarrage: $volumeAfterRestart" -ForegroundColor Gray

# 13. Test d'augmentation du volume pendant que l'app est active
Write-Host "`n13. Test d'augmentation du volume pendant que l'app est active..." -ForegroundColor Yellow
Write-Host "   Test Volume Up pendant que l'application est active..." -ForegroundColor Gray
adb shell input keyevent 25  # Volume up
Start-Sleep -Seconds 1
$volumeDuringApp = adb shell settings get system volume_music
Write-Host "   Volume pendant l'application: $volumeDuringApp" -ForegroundColor Gray

# 14. Vérifier les logs après test
Write-Host "`n14. Vérification des logs après test..." -ForegroundColor Yellow
$finalLogs = adb logcat -d | Select-String -Pattern "volume|Volume|VOLUME|audio|Audio" | Select-Object -Last 5
if ($finalLogs) {
    Write-Host "   Logs finaux détectés:" -ForegroundColor Green
    $finalLogs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   Aucun log final détecté" -ForegroundColor Yellow
}

Write-Host "`nCORRECTION TERMINÉE" -ForegroundColor Cyan
Write-Host "Résumé des vérifications:" -ForegroundColor White
Write-Host "  - Volume initial: $currentVolume" -ForegroundColor Yellow
Write-Host "  - Volume après arrêt: $volumeAfterStop" -ForegroundColor Yellow
Write-Host "  - Volume après forçage: $volumeAfterForce" -ForegroundColor Yellow
Write-Host "  - Volume après redémarrage: $volumeAfterRestart" -ForegroundColor Yellow
Write-Host "  - Volume pendant l'app: $volumeDuringApp" -ForegroundColor Yellow

Write-Host "`nSi le volume descend toujours à 0:" -ForegroundColor Red
Write-Host "  1. L'application interfère avec le contrôle du volume" -ForegroundColor White
Write-Host "  2. Vérifiez les paramètres audio de l'application" -ForegroundColor White
Write-Host "  3. Testez avec une autre application audio" -ForegroundColor White
Write-Host "  4. Redémarrez l'appareil" -ForegroundColor White

Write-Host "`nSi le volume fonctionne maintenant:" -ForegroundColor Green
Write-Host "  ✅ Le problème de contrôle du volume a été résolu !" -ForegroundColor Green 