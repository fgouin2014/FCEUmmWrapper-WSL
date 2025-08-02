# Test rapide de la vidéo après correction
Write-Host "=== TEST VIDÉO APRÈS CORRECTION ===" -ForegroundColor Green

# Nettoyer les logs
adb logcat -c

# Lancer l'application
Write-Host "Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre que l'application se lance
Start-Sleep -Seconds 8

# Récupérer les logs
Write-Host "Récupération des logs..." -ForegroundColor Yellow
$logs = adb logcat -d -s MainActivity -s AudioTrack -s PlayerBase -s SoundCraft -s SGM -v time

# Afficher les logs pertinents
Write-Host "=== LOGS IMPORTANTS ===" -ForegroundColor Cyan
$logs | Where-Object { $_ -match "initialisé|succès|échec|Audio|Buffer|Core|ROM" } | ForEach-Object {
    Write-Host $_ -ForegroundColor White
}

Write-Host "=== TEST TERMINÉ ===" -ForegroundColor Green
Write-Host "Vérifiez que la vidéo et l'audio fonctionnent maintenant." -ForegroundColor Yellow 