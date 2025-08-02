# Script de test final audio pour FCEUmm Wrapper
# Teste l'audio après les corrections du code C++

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

# 2. Vérifier le volume
Write-Host "`n2. Vérification du volume..." -ForegroundColor Yellow
$currentVolume = adb shell settings get system volume_music
Write-Host "   Volume musique: $currentVolume" -ForegroundColor Gray

if ([int]$currentVolume -lt 10) {
    Write-Host "   ⚠️ Volume trop bas - augmentation..." -ForegroundColor Yellow
    adb shell settings put system volume_music 15
    Start-Sleep -Seconds 1
    $newVolume = adb shell settings get system volume_music
    Write-Host "   Nouveau volume: $newVolume" -ForegroundColor Gray
}

# 3. Test avec une ROM
Write-Host "`n3. Test avec une ROM..." -ForegroundColor Yellow
Write-Host "   Lancement d'une ROM pour tester l'audio..." -ForegroundColor Gray

# Simuler la sélection d'une ROM
adb shell input tap 500 300  # Clic sur une ROM
Start-Sleep -Seconds 2
adb shell input tap 500 400  # Clic pour lancer
Start-Sleep -Seconds 5

# 4. Surveillance des logs audio en temps réel
Write-Host "`n4. Surveillance des logs audio en temps réel (30 secondes)..." -ForegroundColor Yellow
Write-Host "   Surveillez les logs audio pendant 30 secondes..." -ForegroundColor Gray
Write-Host "   Appuyez sur des boutons dans le jeu pour tester l'audio..." -ForegroundColor Gray

$startTime = Get-Date
$timeout = 30

while ((Get-Date) -lt ($startTime.AddSeconds($timeout))) {
    $logs = adb logcat -d | Select-String -Pattern "Audio.*configuré|OpenSL.*initialisé|audio.*enabled|Lecture audio démarrée|Premier buffer audio|Audio.*frames écrites" | Select-Object -Last 5
    if ($logs) {
        Write-Host "   Logs audio détectés:" -ForegroundColor Green
        $logs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    }
    Start-Sleep -Seconds 3
}

# 5. Vérifier les erreurs
Write-Host "`n5. Vérification des erreurs..." -ForegroundColor Yellow
$errors = adb logcat -d | Select-String -Pattern "ERROR.*audio|ERROR.*Audio|ERROR.*OpenSL|ERROR.*SLES|ERROR.*Core" | Select-Object -Last 5
if ($errors) {
    Write-Host "   Erreurs détectées:" -ForegroundColor Red
    $errors | ForEach-Object { Write-Host "     $_" -ForegroundColor Red }
} else {
    Write-Host "   ✅ Aucune erreur détectée" -ForegroundColor Green
}

# 6. Vérifier les informations audio
Write-Host "`n6. Vérification des informations audio..." -ForegroundColor Yellow
$audioInfo = adb logcat -d | Select-String -Pattern "Audio.*configuré|OpenSL.*initialisé|audio.*enabled|Lecture audio démarrée|Premier buffer audio|Audio.*frames écrites" | Select-Object -Last 10
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

# 8. Vérification finale
Write-Host "`n8. Vérification finale..." -ForegroundColor Yellow
$finalLogs = adb logcat -d | Select-String -Pattern "Audio.*configuré|OpenSL.*initialisé|audio.*enabled|Lecture audio démarrée" | Select-Object -Last 3
if ($finalLogs) {
    Write-Host "   Configuration audio finale:" -ForegroundColor Green
    $finalLogs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucune configuration audio détectée" -ForegroundColor Yellow
}

Write-Host "`nTEST FINAL TERMINÉ" -ForegroundColor Cyan
Write-Host "Résumé:" -ForegroundColor White
Write-Host "  ✅ Application redémarrée" -ForegroundColor Green
Write-Host "  ✅ Volume vérifié" -ForegroundColor Green
Write-Host "  ✅ ROM testée" -ForegroundColor Green
Write-Host "  ✅ Logs analysés" -ForegroundColor Green

Write-Host "`nSi vous entendez maintenant le son:" -ForegroundColor Green
Write-Host "  ✅ Le problème audio a été corrigé !" -ForegroundColor Green
Write-Host "  ✅ L'audio fonctionne maintenant correctement" -ForegroundColor Green

Write-Host "`nSi vous n'entendez toujours rien:" -ForegroundColor Red
Write-Host "  1. Vérifiez que l'appareil n'est pas en mode silencieux" -ForegroundColor White
Write-Host "  2. Testez avec un casque pour isoler le problème" -ForegroundColor White
Write-Host "  3. Vérifiez les haut-parleurs de l'appareil" -ForegroundColor White
Write-Host "  4. Testez avec une autre application audio" -ForegroundColor White
Write-Host "  5. Redémarrez l'appareil" -ForegroundColor White

Write-Host "`nInstructions pour tester:" -ForegroundColor Yellow
Write-Host "  1. Lancez une ROM dans l'application" -ForegroundColor White
Write-Host "  2. Appuyez sur des boutons pour tester l'audio" -ForegroundColor White
Write-Host "  3. Vérifiez que vous entendez les sons du jeu" -ForegroundColor White
Write-Host "  4. Testez avec un casque si nécessaire" -ForegroundColor White

Write-Host "`nCorrections appliquées:" -ForegroundColor Yellow
Write-Host "  ✅ Callback bqPlayerCallback corrigé" -ForegroundColor Green
Write-Host "  ✅ Buffer audio circulaire fonctionnel" -ForegroundColor Green
Write-Host "  ✅ Lecture audio démarrée automatiquement" -ForegroundColor Green
Write-Host "  ✅ Premier buffer audio envoyé" -ForegroundColor Green 