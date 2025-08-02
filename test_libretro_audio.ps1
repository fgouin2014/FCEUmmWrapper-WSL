# Test de l'approche officielle Libretro pour éviter les redémarrages AudioTrack
Write-Host "=== TEST APPROCHE OFFICIELLE LIBRETRO ===" -ForegroundColor Green

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
$logs | Where-Object { $_ -match "initialisé|succès|échec|Audio|Buffer|Core|ROM|Wrapper|stop" } | ForEach-Object {
    Write-Host $_ -ForegroundColor White
}

# Compter les redémarrages AudioTrack
$stopCount = ($logs | Where-Object { $_ -match "AudioTrack.*stop" }).Count
Write-Host "=== ANALYSE REDÉMARRAGES ===" -ForegroundColor Magenta
Write-Host "Nombre de redémarrages AudioTrack détectés: $stopCount" -ForegroundColor Yellow

if ($stopCount -lt 10) {
    Write-Host "✅ SUCCÈS: Peu de redémarrages AudioTrack détectés" -ForegroundColor Green
} else {
    Write-Host "⚠️  ATTENTION: Encore trop de redémarrages AudioTrack ($stopCount)" -ForegroundColor Red
}

Write-Host "=== TEST TERMINÉ ===" -ForegroundColor Green
Write-Host "L'approche officielle Libretro devrait réduire drastiquement les redémarrages AudioTrack." -ForegroundColor Yellow 