# Test rapide de l'audio avec le nouveau buffer
Write-Host "=== TEST AUDIO AVEC NOUVEAU BUFFER ===" -ForegroundColor Green

# Nettoyer les logs
adb logcat -c

# Lancer l'application
Write-Host "Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre que l'application se lance
Start-Sleep -Seconds 8

# Récupérer les logs audio
Write-Host "Récupération des logs audio..." -ForegroundColor Yellow
$logs = adb logcat -d -s MainActivity -s AudioTrack -s PlayerBase -s SoundCraft -s SGM -v time

# Afficher les logs pertinents
Write-Host "=== LOGS AUDIO ===" -ForegroundColor Cyan
$logs | Where-Object { $_ -match "Audio|PlayerBase|SoundCraft|Buffer|initialisé|succès" } | ForEach-Object {
    Write-Host $_ -ForegroundColor White
}

Write-Host "=== TEST TERMINÉ ===" -ForegroundColor Green
Write-Host "Vérifiez que l'audio fonctionne maintenant." -ForegroundColor Yellow 