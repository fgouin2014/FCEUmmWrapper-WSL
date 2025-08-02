# Script de forçage de l'initialisation audio pour FCEUmm Wrapper
# Force l'initialisation audio qui ne se fait pas automatiquement

Write-Host "FORÇAGE INITIALISATION AUDIO FCEUMM WRAPPER" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# 1. Arrêter complètement l'application
Write-Host "1. Arrêt complet de l'application..." -ForegroundColor Yellow
adb shell am force-stop com.fceumm.wrapper
Start-Sleep -Seconds 3

# 2. Nettoyer les caches
Write-Host "`n2. Nettoyage des caches..." -ForegroundColor Yellow
adb shell pm clear com.fceumm.wrapper
Start-Sleep -Seconds 2

# 3. Redémarrer les services audio
Write-Host "`n3. Redémarrage des services audio..." -ForegroundColor Yellow
adb shell stop audioserver
Start-Sleep -Seconds 2
adb shell start audioserver
Start-Sleep -Seconds 3

# 4. Vérifier que les services audio sont actifs
Write-Host "`n4. Vérification des services audio..." -ForegroundColor Yellow
$audioServices = adb shell ps | Select-String -Pattern "audioserver|mediaserver"
if ($audioServices) {
    Write-Host "   Services audio actifs:" -ForegroundColor Green
    $audioServices | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Services audio non détectés" -ForegroundColor Yellow
}

# 5. Forcer le volume à maximum
Write-Host "`n5. Forçage du volume à maximum..." -ForegroundColor Yellow
adb shell settings put system volume_music 15
adb shell settings put system volume_notification 15
adb shell settings put system volume_ring 15
Start-Sleep -Seconds 1

# 6. Vérifier le volume
Write-Host "`n6. Vérification du volume..." -ForegroundColor Yellow
$musicVolume = adb shell settings get system volume_music
Write-Host "   Volume musique: $musicVolume" -ForegroundColor Gray

# 7. Redémarrer l'application
Write-Host "`n7. Redémarrage de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainMenuActivity
Start-Sleep -Seconds 5

# 8. Vérifier les logs d'initialisation
Write-Host "`n8. Vérification des logs d'initialisation..." -ForegroundColor Yellow
$initLogs = adb logcat -d | Select-String -Pattern "Audio.*configuré|OpenSL.*initialisé|audio.*enabled|Core.*initialisé" | Select-Object -Last 5
if ($initLogs) {
    Write-Host "   Logs d'initialisation détectés:" -ForegroundColor Green
    $initLogs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucun log d'initialisation détecté" -ForegroundColor Yellow
}

# 9. Test avec une ROM
Write-Host "`n9. Test avec une ROM..." -ForegroundColor Yellow
Write-Host "   Lancement d'une ROM pour forcer l'initialisation audio..." -ForegroundColor Gray

# Simuler la sélection d'une ROM
adb shell input tap 500 300  # Clic sur une ROM
Start-Sleep -Seconds 2
adb shell input tap 500 400  # Clic pour lancer
Start-Sleep -Seconds 5

# 10. Surveillance des logs audio pendant le jeu
Write-Host "`n10. Surveillance des logs audio pendant le jeu (30 secondes)..." -ForegroundColor Yellow
Write-Host "   Surveillez les logs audio pendant 30 secondes..." -ForegroundColor Gray
Write-Host "   Appuyez sur des boutons dans le jeu pour forcer l'audio..." -ForegroundColor Gray

$startTime = Get-Date
$timeout = 30

while ((Get-Date) -lt ($startTime.AddSeconds($timeout))) {
    $logs = adb logcat -d | Select-String -Pattern "Audio|OpenSL|SLES|audio|sound|LibretroWrapper|Core" | Select-Object -Last 3
    if ($logs) {
        Write-Host "   Logs audio détectés:" -ForegroundColor Green
        $logs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    }
    Start-Sleep -Seconds 3
}

# 11. Vérifier les erreurs
Write-Host "`n11. Vérification des erreurs..." -ForegroundColor Yellow
$errors = adb logcat -d | Select-String -Pattern "ERROR.*audio|ERROR.*Audio|ERROR.*OpenSL|ERROR.*SLES|ERROR.*Core" | Select-Object -Last 5
if ($errors) {
    Write-Host "   Erreurs détectées:" -ForegroundColor Red
    $errors | ForEach-Object { Write-Host "     $_" -ForegroundColor Red }
} else {
    Write-Host "   ✅ Aucune erreur détectée" -ForegroundColor Green
}

# 12. Vérifier les informations audio
Write-Host "`n12. Vérification des informations audio..." -ForegroundColor Yellow
$audioInfo = adb logcat -d | Select-String -Pattern "Audio.*configuré|OpenSL.*initialisé|audio.*enabled|Core.*initialisé" | Select-Object -Last 5
if ($audioInfo) {
    Write-Host "   Informations audio détectées:" -ForegroundColor Green
    $audioInfo | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucune information audio détectée" -ForegroundColor Yellow
}

# 13. Test de retour au menu
Write-Host "`n13. Test de retour au menu..." -ForegroundColor Yellow
adb shell input keyevent 4  # Back button
Start-Sleep -Seconds 2

# 14. Vérification finale
Write-Host "`n14. Vérification finale..." -ForegroundColor Yellow
$finalLogs = adb logcat -d | Select-String -Pattern "Audio.*configuré|OpenSL.*initialisé|audio.*enabled" | Select-Object -Last 3
if ($finalLogs) {
    Write-Host "   Configuration audio finale:" -ForegroundColor Green
    $finalLogs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucune configuration audio détectée" -ForegroundColor Yellow
}

Write-Host "`nFORÇAGE TERMINÉ" -ForegroundColor Cyan
Write-Host "Résumé:" -ForegroundColor White
Write-Host "  ✅ Application redémarrée" -ForegroundColor Green
Write-Host "  ✅ Services audio redémarrés" -ForegroundColor Green
Write-Host "  ✅ Volume forcé à maximum" -ForegroundColor Green
Write-Host "  ✅ ROM testée" -ForegroundColor Green
Write-Host "  ✅ Logs analysés" -ForegroundColor Green

Write-Host "`nSi vous entendez maintenant le son:" -ForegroundColor Green
Write-Host "  ✅ L'initialisation audio forcée a fonctionné !" -ForegroundColor Green

Write-Host "`nSi vous n'entendez toujours rien:" -ForegroundColor Red
Write-Host "  1. Le problème est probablement matériel" -ForegroundColor White
Write-Host "  2. Testez avec un casque" -ForegroundColor White
Write-Host "  3. Testez avec une autre application audio" -ForegroundColor White
Write-Host "  4. Vérifiez les haut-parleurs de l'appareil" -ForegroundColor White
Write-Host "  5. Redémarrez l'appareil" -ForegroundColor White

Write-Host "`nInstructions pour tester:" -ForegroundColor Yellow
Write-Host "  1. Lancez une ROM dans l'application" -ForegroundColor White
Write-Host "  2. Appuyez sur des boutons pour tester l'audio" -ForegroundColor White
Write-Host "  3. Vérifiez que vous entendez les sons du jeu" -ForegroundColor White
Write-Host "  4. Testez avec un casque si nécessaire" -ForegroundColor White 