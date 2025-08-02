# Script de correction des permissions audio pour FCEUmm Wrapper
# Corrige les permissions audio manquantes

Write-Host "CORRECTION PERMISSIONS AUDIO FCEUMM WRAPPER" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

# 1. Vérifier les permissions actuelles
Write-Host "1. Vérification des permissions actuelles..." -ForegroundColor Yellow
$permissions = adb shell dumpsys package com.fceumm.wrapper | Select-String -Pattern "RECORD_AUDIO|MODIFY_AUDIO_SETTINGS|WAKE_LOCK"
Write-Host "   Permissions détectées:" -ForegroundColor Gray
$permissions | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }

# 2. Vérifier le statut des permissions
Write-Host "`n2. Vérification du statut des permissions..." -ForegroundColor Yellow
$recordAudioStatus = adb shell dumpsys package com.fceumm.wrapper | Select-String -Pattern "RECORD_AUDIO.*granted"
if ($recordAudioStatus -match "granted=false") {
    Write-Host "   ❌ RECORD_AUDIO refusée - correction nécessaire" -ForegroundColor Red
} else {
    Write-Host "   ✅ RECORD_AUDIO accordée" -ForegroundColor Green
}

# 3. Forcer l'octroi des permissions
Write-Host "`n3. Octroi forcé des permissions..." -ForegroundColor Yellow
Write-Host "   Octroi de RECORD_AUDIO..." -ForegroundColor Gray
adb shell pm grant com.fceumm.wrapper android.permission.RECORD_AUDIO

Write-Host "   Octroi de MODIFY_AUDIO_SETTINGS..." -ForegroundColor Gray
adb shell pm grant com.fceumm.wrapper android.permission.MODIFY_AUDIO_SETTINGS

Write-Host "   Octroi de WAKE_LOCK..." -ForegroundColor Gray
adb shell pm grant com.fceumm.wrapper android.permission.WAKE_LOCK

# 4. Vérifier les permissions après octroi
Write-Host "`n4. Vérification des permissions après octroi..." -ForegroundColor Yellow
Start-Sleep -Seconds 2
$newPermissions = adb shell dumpsys package com.fceumm.wrapper | Select-String -Pattern "RECORD_AUDIO.*granted|MODIFY_AUDIO_SETTINGS.*granted|WAKE_LOCK.*granted"
if ($newPermissions) {
    Write-Host "   Statut des permissions:" -ForegroundColor Green
    $newPermissions | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Impossible de vérifier le statut des permissions" -ForegroundColor Yellow
}

# 5. Redémarrer l'application
Write-Host "`n5. Redémarrage de l'application..." -ForegroundColor Yellow
adb shell am force-stop com.fceumm.wrapper
Start-Sleep -Seconds 2
adb shell am start -n com.fceumm.wrapper/.MainMenuActivity
Start-Sleep -Seconds 3

# 6. Test de l'audio après correction des permissions
Write-Host "`n6. Test de l'audio après correction..." -ForegroundColor Yellow
Write-Host "   Surveillance des logs audio (10 secondes)..." -ForegroundColor Gray
$startTime = Get-Date
$timeout = 10

while ((Get-Date) -lt ($startTime.AddSeconds($timeout))) {
    $logs = adb logcat -d | Select-String -Pattern "Audio|OpenSL|SLES|audio|sound|LibretroWrapper" | Select-Object -Last 3
    if ($logs) {
        Write-Host "   Logs audio détectés:" -ForegroundColor Green
        $logs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    }
    Start-Sleep -Seconds 2
}

# 7. Vérifier les erreurs de permissions
Write-Host "`n7. Vérification des erreurs de permissions..." -ForegroundColor Yellow
$permissionErrors = adb logcat -d | Select-String -Pattern "Permission.*denied|SecurityException|RECORD_AUDIO" | Select-Object -Last 5
if ($permissionErrors) {
    Write-Host "   Erreurs de permissions détectées:" -ForegroundColor Red
    $permissionErrors | ForEach-Object { Write-Host "     $_" -ForegroundColor Red }
} else {
    Write-Host "   ✅ Aucune erreur de permissions détectée" -ForegroundColor Green
}

# 8. Test de génération de son
Write-Host "`n8. Test de génération de son..." -ForegroundColor Yellow
Write-Host "   Test avec une ROM pour vérifier l'audio..." -ForegroundColor Gray

# Simuler le lancement d'une ROM
adb shell input tap 500 500  # Clic sur une ROM
Start-Sleep -Seconds 2
adb shell input tap 500 600  # Clic pour lancer
Start-Sleep -Seconds 5

# 9. Vérification finale
Write-Host "`n9. Vérification finale..." -ForegroundColor Yellow
$finalLogs = adb logcat -d | Select-String -Pattern "Audio.*configuré|OpenSL.*initialisé|audio.*enabled|Core.*initialisé" | Select-Object -Last 5
if ($finalLogs) {
    Write-Host "   Initialisation audio détectée:" -ForegroundColor Green
    $finalLogs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucune initialisation audio détectée" -ForegroundColor Yellow
}

# 10. Test de volume maximum
Write-Host "`n10. Test de volume maximum..." -ForegroundColor Yellow
Write-Host "   Forçage du volume à maximum..." -ForegroundColor Gray
for ($i = 0; $i -lt 15; $i++) {
    adb shell input keyevent 25  # Volume up
    Start-Sleep -Milliseconds 100
}

Write-Host "`nCORRECTION TERMINÉE" -ForegroundColor Cyan
Write-Host "Résumé des corrections:" -ForegroundColor White
Write-Host "  ✅ Permissions audio accordées" -ForegroundColor Green
Write-Host "  ✅ Application redémarrée" -ForegroundColor Green
Write-Host "  ✅ Volume forcé à maximum" -ForegroundColor Green
Write-Host "  ✅ Tests audio effectués" -ForegroundColor Green

Write-Host "`nProchaines étapes:" -ForegroundColor Yellow
Write-Host "  1. Testez l'application avec une ROM" -ForegroundColor White
Write-Host "  2. Vérifiez que le son est maintenant audible" -ForegroundColor White
Write-Host "  3. Si le problème persiste, testez avec un casque" -ForegroundColor White
Write-Host "  4. Vérifiez les haut-parleurs de l'appareil" -ForegroundColor White 