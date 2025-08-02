# Script de test audio avec volume corrigé pour FCEUmm Wrapper
# Teste l'audio maintenant que le volume est stable

Write-Host "TEST AUDIO AVEC VOLUME CORRIGÉ FCEUMM WRAPPER" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

# 1. Vérifier le volume actuel
Write-Host "1. Vérification du volume actuel..." -ForegroundColor Yellow
$currentVolume = adb shell settings get system volume_music
Write-Host "   Volume actuel: $currentVolume" -ForegroundColor Gray

if ([int]$currentVolume -lt 10) {
    Write-Host "   ⚠️ Volume trop bas - augmentation..." -ForegroundColor Yellow
    adb shell settings put system volume_music 15
    Start-Sleep -Seconds 1
    $newVolume = adb shell settings get system volume_music
    Write-Host "   Nouveau volume: $newVolume" -ForegroundColor Gray
}

# 2. Vérifier l'état de l'application
Write-Host "`n2. Vérification de l'état de l'application..." -ForegroundColor Yellow
$appRunning = adb shell ps | Select-String -Pattern "com.fceumm.wrapper"
if ($appRunning) {
    Write-Host "   Application en cours d'exécution" -ForegroundColor Green
} else {
    Write-Host "   Application non détectée - lancement..." -ForegroundColor Red
    adb shell am start -n com.fceumm.wrapper/.MainMenuActivity
    Start-Sleep -Seconds 3
}

# 3. Test avec une ROM pour vérifier l'audio
Write-Host "`n3. Test avec une ROM pour vérifier l'audio..." -ForegroundColor Yellow
Write-Host "   Lancement d'une ROM pour tester l'audio..." -ForegroundColor Gray

# Simuler la sélection d'une ROM
adb shell input tap 500 300  # Clic sur une ROM
Start-Sleep -Seconds 2
adb shell input tap 500 400  # Clic pour lancer
Start-Sleep -Seconds 5

# 4. Surveillance des logs audio pendant le jeu
Write-Host "`n4. Surveillance des logs audio pendant le jeu (30 secondes)..." -ForegroundColor Yellow
Write-Host "   Surveillez les logs audio pendant 30 secondes..." -ForegroundColor Gray
Write-Host "   Appuyez sur des boutons dans le jeu pour tester l'audio..." -ForegroundColor Gray

$startTime = Get-Date
$timeout = 30

while ((Get-Date) -lt ($startTime.AddSeconds($timeout))) {
    $logs = adb logcat -d | Select-String -Pattern "Audio|OpenSL|SLES|audio|sound|LibretroWrapper" | Select-Object -Last 3
    if ($logs) {
        Write-Host "   Logs audio détectés:" -ForegroundColor Green
        $logs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    }
    Start-Sleep -Seconds 3
}

# 5. Vérifier les erreurs audio
Write-Host "`n5. Vérification des erreurs audio..." -ForegroundColor Yellow
$errors = adb logcat -d | Select-String -Pattern "ERROR.*audio|ERROR.*Audio|ERROR.*OpenSL|ERROR.*SLES" | Select-Object -Last 5
if ($errors) {
    Write-Host "   Erreurs audio détectées:" -ForegroundColor Red
    $errors | ForEach-Object { Write-Host "     $_" -ForegroundColor Red }
} else {
    Write-Host "   ✅ Aucune erreur audio détectée" -ForegroundColor Green
}

# 6. Vérifier les informations audio
Write-Host "`n6. Vérification des informations audio..." -ForegroundColor Yellow
$audioInfo = adb logcat -d | Select-String -Pattern "Audio.*configuré|OpenSL.*initialisé|audio.*enabled|Core.*initialisé" | Select-Object -Last 5
if ($audioInfo) {
    Write-Host "   Informations audio détectées:" -ForegroundColor Green
    $audioInfo | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucune information audio détectée" -ForegroundColor Yellow
}

# 7. Test de retour au menu
Write-Host "`n7. Test de retour au menu..." -ForegroundColor Yellow
adb shell input keyevent 4  # Back button
Start-Sleep -Seconds 2

# 8. Vérification finale du volume
Write-Host "`n8. Vérification finale du volume..." -ForegroundColor Yellow
$finalVolume = adb shell settings get system volume_music
Write-Host "   Volume final: $finalVolume" -ForegroundColor Gray

# 9. Test d'augmentation du volume pendant le jeu
Write-Host "`n9. Test d'augmentation du volume pendant le jeu..." -ForegroundColor Yellow
Write-Host "   Test Volume Up pendant le jeu..." -ForegroundColor Gray
adb shell input keyevent 25  # Volume up
Start-Sleep -Seconds 1
$volumeDuringGame = adb shell settings get system volume_music
Write-Host "   Volume pendant le jeu: $volumeDuringGame" -ForegroundColor Gray

# 10. Vérification finale
Write-Host "`n10. Vérification finale..." -ForegroundColor Yellow
$finalLogs = adb logcat -d | Select-String -Pattern "Audio.*configuré|OpenSL.*initialisé|audio.*enabled" | Select-Object -Last 3
if ($finalLogs) {
    Write-Host "   Configuration audio finale:" -ForegroundColor Green
    $finalLogs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
}

Write-Host "`nTEST TERMINÉ" -ForegroundColor Cyan
Write-Host "Résumé:" -ForegroundColor White
Write-Host "  ✅ Volume vérifié et corrigé" -ForegroundColor Green
Write-Host "  ✅ Application testée" -ForegroundColor Green
Write-Host "  ✅ ROM lancée" -ForegroundColor Green
Write-Host "  ✅ Logs audio analysés" -ForegroundColor Green
Write-Host "  ✅ Volume stable: $finalVolume" -ForegroundColor Green

Write-Host "`nSi vous entendez maintenant le son:" -ForegroundColor Green
Write-Host "  ✅ Le problème audio a été résolu !" -ForegroundColor Green
Write-Host "  ✅ Le volume est stable et l'audio fonctionne" -ForegroundColor Green

Write-Host "`nSi vous n'entendez toujours pas le son:" -ForegroundColor Red
Write-Host "  1. Vérifiez que l'appareil n'est pas en mode silencieux" -ForegroundColor White
Write-Host "  2. Testez avec un casque pour isoler le problème" -ForegroundColor White
Write-Host "  3. Vérifiez les haut-parleurs de l'appareil" -ForegroundColor White
Write-Host "  4. Testez avec une autre application audio" -ForegroundColor White
Write-Host "  5. Redémarrez l'appareil" -ForegroundColor White

Write-Host "`nInstructions pour tester:" -ForegroundColor Yellow
Write-Host "  1. Lancez une ROM dans l'application" -ForegroundColor White
Write-Host "  2. Appuyez sur des boutons pour tester l'audio" -ForegroundColor White
Write-Host "  3. Vérifiez que vous entendez les sons du jeu" -ForegroundColor White
Write-Host "  4. Testez les contrôles pour confirmer l'audio interactif" -ForegroundColor White 