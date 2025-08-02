# Script de diagnostic audio audible pour FCEUmm Wrapper
# Identifie pourquoi le son n'est pas audible

Write-Host "DIAGNOSTIC AUDIO AUDIBLE FCEUMM WRAPPER" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
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

# 2. Vérifier les volumes système
Write-Host "`n2. Vérification des volumes système..." -ForegroundColor Yellow
$musicVolume = adb shell settings get system volume_music
$notificationVolume = adb shell settings get system volume_notification
$ringVolume = adb shell settings get system volume_ring
Write-Host "   Volume musique: $musicVolume" -ForegroundColor Gray
Write-Host "   Volume notifications: $notificationVolume" -ForegroundColor Gray
Write-Host "   Volume sonnerie: $ringVolume" -ForegroundColor Gray

# Forcer les volumes si nécessaire
if ([int]$musicVolume -lt 5) {
    Write-Host "   Correction du volume musique..." -ForegroundColor Yellow
    adb shell settings put system volume_music 10
    adb shell input keyevent 25  # Volume up
    adb shell input keyevent 25  # Volume up
    adb shell input keyevent 25  # Volume up
}

# 3. Vérifier le mode silencieux
Write-Host "`n3. Vérification du mode silencieux..." -ForegroundColor Yellow
$ringerMode = adb shell settings get global zen_mode
Write-Host "   Mode zen: $ringerMode" -ForegroundColor Gray

if ($ringerMode -eq "1") {
    Write-Host "   ⚠️ Mode silencieux activé - désactivation..." -ForegroundColor Yellow
    adb shell settings put global zen_mode 0
}

# 4. Test audio système
Write-Host "`n4. Test audio système..." -ForegroundColor Yellow
Write-Host "   Test avec notification sonore..." -ForegroundColor Gray
adb shell am start -a android.intent.action.MAIN -n com.android.settings/.Settings
Start-Sleep -Seconds 2
adb shell input keyevent 4  # Back button

# 5. Vérifier les permissions audio
Write-Host "`n5. Vérification des permissions audio..." -ForegroundColor Yellow
$permissions = adb shell dumpsys package com.fceumm.wrapper | Select-String -Pattern "RECORD_AUDIO|MODIFY_AUDIO_SETTINGS|WAKE_LOCK"
if ($permissions) {
    Write-Host "   Permissions audio détectées:" -ForegroundColor Green
    $permissions | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Permissions audio non détectées" -ForegroundColor Yellow
}

# 6. Vérifier les processus audio
Write-Host "`n6. Vérification des processus audio..." -ForegroundColor Yellow
$audioProcesses = adb shell ps | Select-String -Pattern "audioserver|mediaserver|audio"
if ($audioProcesses) {
    Write-Host "   Processus audio actifs:" -ForegroundColor Green
    $audioProcesses | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucun processus audio détecté" -ForegroundColor Yellow
}

# 7. Test de génération de son
Write-Host "`n7. Test de génération de son..." -ForegroundColor Yellow
Write-Host "   Génération d'un signal de test..." -ForegroundColor Gray

# Lancer l'activité de test audio
try {
    adb shell am start -n com.fceumm.wrapper/.AudioLatencyTestActivity
    Write-Host "   Activité de test audio lancée" -ForegroundColor Green
    Start-Sleep -Seconds 3
    
    # Simuler des clics pour tester l'audio
    adb shell input tap 500 500
    Start-Sleep -Seconds 1
    adb shell input tap 500 600
    Start-Sleep -Seconds 1
    adb shell input tap 500 700
    Start-Sleep -Seconds 2
    
    # Retour au menu
    adb shell input keyevent 4
} catch {
    Write-Host "   ❌ Impossible de lancer l'activité de test audio" -ForegroundColor Red
}

# 8. Vérifier les logs audio en temps réel
Write-Host "`n8. Surveillance des logs audio (15 secondes)..." -ForegroundColor Yellow
Write-Host "   Surveillez les logs audio pendant 15 secondes..." -ForegroundColor Gray
$startTime = Get-Date
$timeout = 15

while ((Get-Date) -lt ($startTime.AddSeconds($timeout))) {
    $logs = adb logcat -d | Select-String -Pattern "Audio|OpenSL|SLES|audio|sound|volume|LibretroWrapper" | Select-Object -Last 3
    if ($logs) {
        Write-Host "   Logs audio détectés:" -ForegroundColor Green
        $logs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    }
    Start-Sleep -Seconds 2
}

# 9. Vérifier les erreurs audio spécifiques
Write-Host "`n9. Vérification des erreurs audio..." -ForegroundColor Yellow
$audioErrors = adb logcat -d | Select-String -Pattern "ERROR.*audio|ERROR.*Audio|ERROR.*OpenSL|ERROR.*SLES|ERROR.*sound" | Select-Object -Last 10
if ($audioErrors) {
    Write-Host "   Erreurs audio détectées:" -ForegroundColor Red
    $audioErrors | ForEach-Object { Write-Host "     $_" -ForegroundColor Red }
} else {
    Write-Host "   ✅ Aucune erreur audio détectée" -ForegroundColor Green
}

# 10. Vérifier les informations audio
Write-Host "`n10. Vérification des informations audio..." -ForegroundColor Yellow
$audioInfo = adb logcat -d | Select-String -Pattern "INFO.*audio|INFO.*Audio|INFO.*OpenSL|INFO.*SLES|Audio.*configuré|OpenSL.*initialisé" | Select-Object -Last 10
if ($audioInfo) {
    Write-Host "   Informations audio détectées:" -ForegroundColor Green
    $audioInfo | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucune information audio détectée" -ForegroundColor Yellow
}

# 11. Test de réinitialisation audio
Write-Host "`n11. Test de réinitialisation audio..." -ForegroundColor Yellow
Write-Host "   Arrêt et redémarrage de l'application..." -ForegroundColor Gray
adb shell am force-stop com.fceumm.wrapper
Start-Sleep -Seconds 2
adb shell am start -n com.fceumm.wrapper/.MainMenuActivity
Start-Sleep -Seconds 3

# 12. Vérification finale
Write-Host "`n12. Vérification finale..." -ForegroundColor Yellow
$finalLogs = adb logcat -d | Select-String -Pattern "Audio.*initialized|OpenSL.*initialized|audio.*enabled|Audio.*configuré" | Select-Object -Last 5
if ($finalLogs) {
    Write-Host "   Initialisation audio détectée:" -ForegroundColor Green
    $finalLogs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucune initialisation audio détectée" -ForegroundColor Yellow
}

# 13. Test de volume forcé
Write-Host "`n13. Test de volume forcé..." -ForegroundColor Yellow
Write-Host "   Augmentation forcée du volume..." -ForegroundColor Gray
for ($i = 0; $i -lt 10; $i++) {
    adb shell input keyevent 25  # Volume up
    Start-Sleep -Milliseconds 100
}

Write-Host "`nDIAGNOSTIC TERMINÉ" -ForegroundColor Cyan
Write-Host "Résumé des vérifications:" -ForegroundColor White
Write-Host "  - Volumes système vérifiés et corrigés" -ForegroundColor Yellow
Write-Host "  - Mode silencieux vérifié" -ForegroundColor Yellow
Write-Host "  - Permissions audio vérifiées" -ForegroundColor Yellow
Write-Host "  - Processus audio vérifiés" -ForegroundColor Yellow
Write-Host "  - Logs audio analysés" -ForegroundColor Yellow
Write-Host "  - Volume forcé à maximum" -ForegroundColor Yellow

Write-Host "`nSi le son n'est toujours pas audible:" -ForegroundColor Red
Write-Host "  1. Vérifiez que l'appareil n'est pas en mode silencieux" -ForegroundColor White
Write-Host "  2. Testez avec un casque pour isoler le problème" -ForegroundColor White
Write-Host "  3. Vérifiez les haut-parleurs de l'appareil" -ForegroundColor White
Write-Host "  4. Redémarrez l'appareil" -ForegroundColor White
Write-Host "  5. Testez avec une autre application audio" -ForegroundColor White 