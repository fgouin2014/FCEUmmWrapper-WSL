# Script de diagnostic approfondi audio pour FCEUmm Wrapper
# Identifie pourquoi l'audio ne fonctionne toujours pas

Write-Host "DIAGNOSTIC APPROFONDI AUDIO FCEUMM WRAPPER" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# 1. Vérifier l'état de l'application
Write-Host "1. Vérification de l'état de l'application..." -ForegroundColor Yellow
$appRunning = adb shell ps | Select-String -Pattern "com.fceumm.wrapper"
if ($appRunning) {
    Write-Host "   Application en cours d'exécution" -ForegroundColor Green
} else {
    Write-Host "   Application non détectée - lancement..." -ForegroundColor Red
    adb shell am start -n com.fceumm.wrapper/.MainMenuActivity
    Start-Sleep -Seconds 3
}

# 2. Vérifier le volume actuel
Write-Host "`n2. Vérification du volume actuel..." -ForegroundColor Yellow
$currentVolume = adb shell settings get system volume_music
Write-Host "   Volume musique: $currentVolume" -ForegroundColor Gray

# 3. Vérifier les autres volumes
Write-Host "`n3. Vérification des autres volumes..." -ForegroundColor Yellow
$notificationVolume = adb shell settings get system volume_notification
$ringVolume = adb shell settings get system volume_ring
Write-Host "   Volume notifications: $notificationVolume" -ForegroundColor Gray
Write-Host "   Volume sonnerie: $ringVolume" -ForegroundColor Gray

# 4. Vérifier le mode silencieux
Write-Host "`n4. Vérification du mode silencieux..." -ForegroundColor Yellow
$ringerMode = adb shell settings get global zen_mode
Write-Host "   Mode zen: $ringerMode" -ForegroundColor Gray

# 5. Vérifier les permissions audio
Write-Host "`n5. Vérification des permissions audio..." -ForegroundColor Yellow
$permissions = adb shell dumpsys package com.fceumm.wrapper | Select-String -Pattern "RECORD_AUDIO.*granted|MODIFY_AUDIO_SETTINGS.*granted|WAKE_LOCK.*granted"
if ($permissions) {
    Write-Host "   Permissions audio:" -ForegroundColor Green
    $permissions | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Permissions audio non détectées" -ForegroundColor Yellow
}

# 6. Test audio système
Write-Host "`n6. Test audio système..." -ForegroundColor Yellow
Write-Host "   Test avec notification sonore..." -ForegroundColor Gray
adb shell am start -a android.intent.action.MAIN -n com.android.settings/.Settings
Start-Sleep -Seconds 2
adb shell input keyevent 4  # Back button

# 7. Vérifier les processus audio
Write-Host "`n7. Vérification des processus audio..." -ForegroundColor Yellow
$audioProcesses = adb shell ps | Select-String -Pattern "audioserver|mediaserver|audio"
if ($audioProcesses) {
    Write-Host "   Processus audio actifs:" -ForegroundColor Green
    $audioProcesses | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucun processus audio détecté" -ForegroundColor Yellow
}

# 8. Vérifier les services audio
Write-Host "`n8. Vérification des services audio..." -ForegroundColor Yellow
$audioServices = adb shell dumpsys audio | Select-String -Pattern "volume|stream|music" | Select-Object -First 5
if ($audioServices) {
    Write-Host "   Services audio détectés:" -ForegroundColor Green
    $audioServices | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucun service audio détecté" -ForegroundColor Yellow
}

# 9. Test avec une ROM et surveillance des logs
Write-Host "`n9. Test avec une ROM et surveillance des logs..." -ForegroundColor Yellow
Write-Host "   Lancement d'une ROM pour tester l'audio..." -ForegroundColor Gray

# Simuler la sélection d'une ROM
adb shell input tap 500 300  # Clic sur une ROM
Start-Sleep -Seconds 2
adb shell input tap 500 400  # Clic pour lancer
Start-Sleep -Seconds 5

# 10. Surveillance des logs audio en temps réel
Write-Host "`n10. Surveillance des logs audio en temps réel (20 secondes)..." -ForegroundColor Yellow
Write-Host "   Surveillez les logs audio pendant 20 secondes..." -ForegroundColor Gray
Write-Host "   Appuyez sur des boutons dans le jeu pour tester l'audio..." -ForegroundColor Gray

$startTime = Get-Date
$timeout = 20

while ((Get-Date) -lt ($startTime.AddSeconds($timeout))) {
    $logs = adb logcat -d | Select-String -Pattern "Audio|OpenSL|SLES|audio|sound|LibretroWrapper|Core" | Select-Object -Last 5
    if ($logs) {
        Write-Host "   Logs audio détectés:" -ForegroundColor Green
        $logs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    }
    Start-Sleep -Seconds 2
}

# 11. Vérifier les erreurs spécifiques
Write-Host "`n11. Vérification des erreurs spécifiques..." -ForegroundColor Yellow
$errors = adb logcat -d | Select-String -Pattern "ERROR.*audio|ERROR.*Audio|ERROR.*OpenSL|ERROR.*SLES|ERROR.*Core|ERROR.*libretro" | Select-Object -Last 10
if ($errors) {
    Write-Host "   Erreurs audio détectées:" -ForegroundColor Red
    $errors | ForEach-Object { Write-Host "     $_" -ForegroundColor Red }
} else {
    Write-Host "   ✅ Aucune erreur audio détectée" -ForegroundColor Green
}

# 12. Vérifier les informations audio
Write-Host "`n12. Vérification des informations audio..." -ForegroundColor Yellow
$audioInfo = adb logcat -d | Select-String -Pattern "Audio.*configuré|OpenSL.*initialisé|audio.*enabled|Core.*initialisé|libretro.*initialisé" | Select-Object -Last 10
if ($audioInfo) {
    Write-Host "   Informations audio détectées:" -ForegroundColor Green
    $audioInfo | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucune information audio détectée" -ForegroundColor Yellow
}

# 13. Vérifier les logs de l'application spécifiquement
Write-Host "`n13. Vérification des logs de l'application spécifiquement..." -ForegroundColor Yellow
$appLogs = adb logcat -d | Select-String -Pattern "LibretroWrapper|FCEUmm" | Select-Object -Last 10
if ($appLogs) {
    Write-Host "   Logs de l'application détectés:" -ForegroundColor Green
    $appLogs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucun log de l'application détecté" -ForegroundColor Yellow
}

# 14. Test de retour au menu
Write-Host "`n14. Test de retour au menu..." -ForegroundColor Yellow
adb shell input keyevent 4  # Back button
Start-Sleep -Seconds 2

# 15. Vérification finale du volume
Write-Host "`n15. Vérification finale du volume..." -ForegroundColor Yellow
$finalVolume = adb shell settings get system volume_music
Write-Host "   Volume final: $finalVolume" -ForegroundColor Gray

# 16. Test d'augmentation du volume
Write-Host "`n16. Test d'augmentation du volume..." -ForegroundColor Yellow
Write-Host "   Test Volume Up..." -ForegroundColor Gray
adb shell input keyevent 25  # Volume up
Start-Sleep -Seconds 1
$volumeAfterUp = adb shell settings get system volume_music
Write-Host "   Volume après Volume Up: $volumeAfterUp" -ForegroundColor Gray

Write-Host "`nDIAGNOSTIC APPROFONDI TERMINÉ" -ForegroundColor Cyan
Write-Host "Résumé des vérifications:" -ForegroundColor White
Write-Host "  - Volume musique: $currentVolume" -ForegroundColor Yellow
Write-Host "  - Volume notifications: $notificationVolume" -ForegroundColor Yellow
Write-Host "  - Mode zen: $ringerMode" -ForegroundColor Yellow
Write-Host "  - Volume final: $finalVolume" -ForegroundColor Yellow

Write-Host "`nSi vous n'entendez toujours rien:" -ForegroundColor Red
Write-Host "  1. Vérifiez que l'appareil n'est pas en mode silencieux" -ForegroundColor White
Write-Host "  2. Testez avec un casque pour isoler le problème" -ForegroundColor White
Write-Host "  3. Vérifiez les haut-parleurs de l'appareil" -ForegroundColor White
Write-Host "  4. Testez avec une autre application audio" -ForegroundColor White
Write-Host "  5. Redémarrez l'appareil" -ForegroundColor White
Write-Host "  6. Vérifiez que l'appareil n'est pas en mode vibreur" -ForegroundColor White

Write-Host "`nProchaines étapes:" -ForegroundColor Yellow
Write-Host "  1. Testez avec un casque" -ForegroundColor White
Write-Host "  2. Testez avec une autre application audio" -ForegroundColor White
Write-Host "  3. Vérifiez les paramètres audio de l'appareil" -ForegroundColor White
Write-Host "  4. Redémarrez l'appareil" -ForegroundColor White 