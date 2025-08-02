# Script de test audio simple pour FCEUmm Wrapper
# Teste l'audio de maniere basique

Write-Host "TEST AUDIO SIMPLE FCEUMM WRAPPER" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 1. Verifier l'etat de l'application
Write-Host "1. Verification de l'etat de l'application..." -ForegroundColor Yellow
$appRunning = adb shell ps | Select-String -Pattern "com.fceumm.wrapper"
if ($appRunning) {
    Write-Host "   Application en cours d'execution" -ForegroundColor Green
} else {
    Write-Host "   Application non detectee" -ForegroundColor Red
    Write-Host "   Demarrage de l'application..." -ForegroundColor Yellow
    adb shell am start -n com.fceumm.wrapper/.MainActivity
    Start-Sleep -Seconds 3
}

# 2. Verifier les volumes
Write-Host "`n2. Verification des volumes..." -ForegroundColor Yellow
$musicVolume = adb shell settings get system volume_music
$notificationVolume = adb shell settings get system volume_notification
Write-Host "   Volume musique: $musicVolume" -ForegroundColor Gray
Write-Host "   Volume notifications: $notificationVolume" -ForegroundColor Gray

if ([int]$musicVolume -eq 0) {
    Write-Host "   Volume musique a 0 - correction..." -ForegroundColor Yellow
    adb shell settings put system volume_music 5
    adb shell input keyevent 25
    adb shell input keyevent 25
}

# 3. Test audio systeme
Write-Host "`n3. Test audio systeme..." -ForegroundColor Yellow
Write-Host "   Test avec l'application musique..." -ForegroundColor Gray
adb shell am start -a android.intent.action.MAIN -n com.android.music/.MediaPlaybackActivity
Start-Sleep -Seconds 2
adb shell input keyevent 4

# 4. Verifier les logs audio en temps reel
Write-Host "`n4. Surveillance des logs audio (10 secondes)..." -ForegroundColor Yellow
Write-Host "   Appuyez sur une touche dans l'application pour tester l'audio..." -ForegroundColor Gray
$startTime = Get-Date
$timeout = 10

while ((Get-Date) -lt ($startTime.AddSeconds($timeout))) {
    $logs = adb logcat -d | Select-String -Pattern "Audio|OpenSL|SLES|audio|sound|volume" | Select-Object -Last 5
    if ($logs) {
        Write-Host "   Logs audio detectes:" -ForegroundColor Green
        $logs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    }
    Start-Sleep -Seconds 1
}

# 5. Test specifique FCEUmm
Write-Host "`n5. Test specifique FCEUmm..." -ForegroundColor Yellow
Write-Host "   Recherche de logs FCEUmm audio..." -ForegroundColor Gray
$fceummLogs = adb logcat -d | Select-String -Pattern "FCEUmm.*audio|LibretroWrapper.*audio|OpenSL.*audio" | Select-Object -Last 10
if ($fceummLogs) {
    Write-Host "   Logs FCEUmm audio trouves:" -ForegroundColor Green
    $fceummLogs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   Aucun log FCEUmm audio detecte" -ForegroundColor Yellow
}

# 6. Verifier les erreurs audio
Write-Host "`n6. Verification des erreurs audio..." -ForegroundColor Yellow
$audioErrors = adb logcat -d | Select-String -Pattern "ERROR.*audio|ERROR.*Audio|ERROR.*OpenSL|ERROR.*SLES" | Select-Object -Last 5
if ($audioErrors) {
    Write-Host "   Erreurs audio detectees:" -ForegroundColor Red
    $audioErrors | ForEach-Object { Write-Host "     $_" -ForegroundColor Red }
} else {
    Write-Host "   Aucune erreur audio detectee" -ForegroundColor Green
}

# 7. Test de reinitialisation audio
Write-Host "`n7. Test de reinitialisation audio..." -ForegroundColor Yellow
Write-Host "   Arret et redemarrage de l'application..." -ForegroundColor Gray
adb shell am force-stop com.fceumm.wrapper
Start-Sleep -Seconds 2
adb shell am start -n com.fceumm.wrapper/.MainActivity
Start-Sleep -Seconds 3

# 8. Verification finale
Write-Host "`n8. Verification finale..." -ForegroundColor Yellow
$finalLogs = adb logcat -d | Select-String -Pattern "Audio.*initialized|OpenSL.*initialized|audio.*enabled" | Select-Object -Last 5
if ($finalLogs) {
    Write-Host "   Initialisation audio detectee:" -ForegroundColor Green
    $finalLogs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   Aucune initialisation audio detectee" -ForegroundColor Yellow
}

Write-Host "`nTEST TERMINE" -ForegroundColor Cyan
Write-Host "Resume:" -ForegroundColor White
Write-Host "  - Verifiez que le volume de l'appareil n'est pas a 0" -ForegroundColor Yellow
Write-Host "  - Verifiez que l'appareil n'est pas en mode silencieux" -ForegroundColor Yellow
Write-Host "  - Testez avec un casque pour isoler le probleme" -ForegroundColor Yellow
Write-Host "  - Redemarrez l'appareil si le probleme persiste" -ForegroundColor Yellow 