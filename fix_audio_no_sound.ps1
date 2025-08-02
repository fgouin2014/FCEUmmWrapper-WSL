# Script de correction audio pour FCEUmm Wrapper
# Corrige les problèmes de son courants

Write-Host "🔊 CORRECTION AUDIO FCEUMM WRAPPER" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

# 1. Arrêter l'application
Write-Host "1. Arrêt de l'application..." -ForegroundColor Yellow
adb shell am force-stop com.fceumm.wrapper
Start-Sleep -Seconds 2

# 2. Vérifier et corriger les volumes système
Write-Host "`n2. Correction des volumes système..." -ForegroundColor Yellow
adb shell settings put system volume_music 7
adb shell settings put system volume_notification 5
adb shell settings put system volume_ring 5
Write-Host "   Volumes système corrigés" -ForegroundColor Green

# 3. Vérifier les permissions audio
Write-Host "`n3. Vérification des permissions audio..." -ForegroundColor Yellow
$permissions = adb shell dumpsys package com.fceumm.wrapper | Select-String -Pattern "android.permission.RECORD_AUDIO|android.permission.MODIFY_AUDIO_SETTINGS"
if ($permissions) {
    Write-Host "   Permissions audio OK" -ForegroundColor Green
} else {
    Write-Host "   ⚠️ Permissions audio manquantes" -ForegroundColor Yellow
}

# 4. Nettoyer les caches audio
Write-Host "`n4. Nettoyage des caches audio..." -ForegroundColor Yellow
adb shell pm clear com.fceumm.wrapper
Start-Sleep -Seconds 1

# 5. Redémarrer les services audio
Write-Host "`n5. Redémarrage des services audio..." -ForegroundColor Yellow
adb shell stop audioserver
Start-Sleep -Seconds 1
adb shell start audioserver
Start-Sleep -Seconds 2

# 6. Vérifier les processus audio
Write-Host "`n6. Vérification des processus audio..." -ForegroundColor Yellow
$audioProcesses = adb shell ps | Select-String -Pattern "audioserver|mediaserver"
if ($audioProcesses) {
    Write-Host "   Services audio actifs" -ForegroundColor Green
} else {
    Write-Host "   ⚠️ Services audio non détectés" -ForegroundColor Yellow
}

# 7. Redémarrer l'application avec paramètres audio optimisés
Write-Host "`n7. Redémarrage de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity
Start-Sleep -Seconds 3

# 8. Forcer l'activation audio via ADB
Write-Host "`n8. Activation audio forcée..." -ForegroundColor Yellow
adb shell input keyevent 25  # Volume up
Start-Sleep -Seconds 1
adb shell input keyevent 25  # Volume up
Start-Sleep -Seconds 1

# 9. Vérifier les logs après correction
Write-Host "`n9. Vérification des logs après correction..." -ForegroundColor Yellow
Start-Sleep -Seconds 2
$audioLogs = adb logcat -d | Select-String -Pattern "Audio|OpenSL|SLES|audio|sound" | Select-Object -Last 10
if ($audioLogs) {
    Write-Host "   Logs audio détectés:" -ForegroundColor Green
    $audioLogs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucun log audio détecté" -ForegroundColor Yellow
}

# 10. Test audio simple
Write-Host "`n10. Test audio simple..." -ForegroundColor Yellow
adb shell am start -a android.intent.action.MAIN -n com.android.music/.MediaPlaybackActivity
Start-Sleep -Seconds 2
adb shell input keyevent 4  # Back button

# 11. Vérifier les erreurs finales
Write-Host "`n11. Vérification des erreurs finales..." -ForegroundColor Yellow
$errors = adb logcat -d | Select-String -Pattern "ERROR.*audio|ERROR.*Audio|ERROR.*OpenSL|ERROR.*SLES" | Select-Object -Last 5
if ($errors) {
    Write-Host "   Erreurs détectées:" -ForegroundColor Red
    $errors | ForEach-Object { Write-Host "     $_" -ForegroundColor Red }
} else {
    Write-Host "   ✅ Aucune erreur audio détectée" -ForegroundColor Green
}

Write-Host "`n🎵 CORRECTION TERMINÉE" -ForegroundColor Cyan
Write-Host "Si le problème persiste, essayez:" -ForegroundColor White
Write-Host "  1. Redémarrer l'appareil" -ForegroundColor Yellow
Write-Host "  2. Vérifier les paramètres audio de l'appareil" -ForegroundColor Yellow
Write-Host "  3. Tester avec un autre casque/haut-parleur" -ForegroundColor Yellow
Write-Host "  4. Vérifier que l'appareil n'est pas en mode silencieux" -ForegroundColor Yellow 