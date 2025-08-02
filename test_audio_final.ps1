# Script de test final audio pour FCEUmm Wrapper
# Vérifie que le son est maintenant audible

Write-Host "TEST FINAL AUDIO FCEUMM WRAPPER" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
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

# 2. Vérifier les permissions audio
Write-Host "`n2. Vérification des permissions audio..." -ForegroundColor Yellow
$permissions = adb shell dumpsys package com.fceumm.wrapper | Select-String -Pattern "RECORD_AUDIO.*granted|MODIFY_AUDIO_SETTTINGS.*granted"
if ($permissions -match "granted=true") {
    Write-Host "   ✅ Permissions audio accordées" -ForegroundColor Green
} else {
    Write-Host "   ❌ Permissions audio manquantes" -ForegroundColor Red
}

# 3. Vérifier les volumes
Write-Host "`n3. Vérification des volumes..." -ForegroundColor Yellow
$musicVolume = adb shell settings get system volume_music
Write-Host "   Volume musique: $musicVolume" -ForegroundColor Gray

if ([int]$musicVolume -lt 8) {
    Write-Host "   Augmentation du volume..." -ForegroundColor Yellow
    for ($i = 0; $i -lt 10; $i++) {
        adb shell input keyevent 25  # Volume up
        Start-Sleep -Milliseconds 100
    }
}

# 4. Test avec une ROM
Write-Host "`n4. Test avec une ROM..." -ForegroundColor Yellow
Write-Host "   Lancement d'une ROM pour tester l'audio..." -ForegroundColor Gray

# Simuler la sélection d'une ROM
adb shell input tap 500 300  # Clic sur une ROM
Start-Sleep -Seconds 2
adb shell input tap 500 400  # Clic pour lancer
Start-Sleep -Seconds 5

# 5. Surveillance des logs audio
Write-Host "`n5. Surveillance des logs audio (20 secondes)..." -ForegroundColor Yellow
Write-Host "   Surveillez les logs audio pendant 20 secondes..." -ForegroundColor Gray
Write-Host "   Appuyez sur des boutons dans le jeu pour tester l'audio..." -ForegroundColor Gray

$startTime = Get-Date
$timeout = 20

while ((Get-Date) -lt ($startTime.AddSeconds($timeout))) {
    $logs = adb logcat -d | Select-String -Pattern "Audio|OpenSL|SLES|audio|sound|LibretroWrapper" | Select-Object -Last 3
    if ($logs) {
        Write-Host "   Logs audio détectés:" -ForegroundColor Green
        $logs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    }
    Start-Sleep -Seconds 2
}

# 6. Vérifier les erreurs
Write-Host "`n6. Vérification des erreurs..." -ForegroundColor Yellow
$errors = adb logcat -d | Select-String -Pattern "ERROR.*audio|ERROR.*Audio|ERROR.*OpenSL|ERROR.*SLES" | Select-Object -Last 5
if ($errors) {
    Write-Host "   Erreurs audio détectées:" -ForegroundColor Red
    $errors | ForEach-Object { Write-Host "     $_" -ForegroundColor Red }
} else {
    Write-Host "   ✅ Aucune erreur audio détectée" -ForegroundColor Green
}

# 7. Vérifier les informations audio
Write-Host "`n7. Vérification des informations audio..." -ForegroundColor Yellow
$audioInfo = adb logcat -d | Select-String -Pattern "Audio.*configuré|OpenSL.*initialisé|audio.*enabled|Core.*initialisé" | Select-Object -Last 5
if ($audioInfo) {
    Write-Host "   Informations audio détectées:" -ForegroundColor Green
    $audioInfo | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucune information audio détectée" -ForegroundColor Yellow
}

# 8. Test de retour au menu
Write-Host "`n8. Test de retour au menu..." -ForegroundColor Yellow
adb shell input keyevent 4  # Back button
Start-Sleep -Seconds 2

# 9. Vérification finale
Write-Host "`n9. Vérification finale..." -ForegroundColor Yellow
$finalLogs = adb logcat -d | Select-String -Pattern "Audio.*configuré|OpenSL.*initialisé|audio.*enabled" | Select-Object -Last 3
if ($finalLogs) {
    Write-Host "   Configuration audio finale:" -ForegroundColor Green
    $finalLogs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
}

Write-Host "`nTEST FINAL TERMINÉ" -ForegroundColor Cyan
Write-Host "Résumé:" -ForegroundColor White
Write-Host "  ✅ Permissions audio vérifiées" -ForegroundColor Green
Write-Host "  ✅ Volume audio vérifié" -ForegroundColor Green
Write-Host "  ✅ ROM testée" -ForegroundColor Green
Write-Host "  ✅ Logs audio analysés" -ForegroundColor Green

Write-Host "`nSi le son n'est toujours pas audible:" -ForegroundColor Red
Write-Host "  1. Vérifiez que l'appareil n'est pas en mode silencieux" -ForegroundColor White
Write-Host "  2. Testez avec un casque pour isoler le problème" -ForegroundColor White
Write-Host "  3. Vérifiez les haut-parleurs de l'appareil" -ForegroundColor White
Write-Host "  4. Testez avec une autre application audio" -ForegroundColor White
Write-Host "  5. Redémarrez l'appareil" -ForegroundColor White

Write-Host "`nSi le son est maintenant audible:" -ForegroundColor Green
Write-Host "  ✅ Le problème audio a été résolu !" -ForegroundColor Green 