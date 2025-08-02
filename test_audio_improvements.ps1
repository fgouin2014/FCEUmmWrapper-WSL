# Script de test pour les améliorations audio
Write-Host "=== TEST DES AMÉLIORATIONS AUDIO ===" -ForegroundColor Green

# Nettoyer les logs
Write-Host "Nettoyage des logs..." -ForegroundColor Yellow
adb logcat -c

# Démarrer la surveillance des logs audio
Write-Host "Démarrage de la surveillance des logs audio..." -ForegroundColor Yellow
Start-Job -ScriptBlock {
    adb logcat -s MainActivity -s AudioTrack -s PlayerBase -s SoundCraft -s SGM -v time
} | Out-Null

# Attendre un peu
Start-Sleep -Seconds 2

Write-Host "Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre que l'application se lance
Start-Sleep -Seconds 5

Write-Host "Test de pause/reprise de l'application..." -ForegroundColor Yellow
Write-Host "Appuyez sur une touche pour continuer..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Simuler une pause
Write-Host "Simulation d'une pause..." -ForegroundColor Yellow
adb shell input keyevent KEYCODE_HOME
Start-Sleep -Seconds 3

# Revenir à l'application
Write-Host "Retour à l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity
Start-Sleep -Seconds 3

# Récupérer les logs
Write-Host "Récupération des logs..." -ForegroundColor Yellow
$logs = Receive-Job -Name "*" | Select-Object -First 50

# Afficher les logs pertinents
Write-Host "=== LOGS AUDIO ===" -ForegroundColor Cyan
$logs | Where-Object { $_ -match "Audio|PlayerBase|SoundCraft" } | ForEach-Object {
    Write-Host $_ -ForegroundColor White
}

# Arrêter les jobs
Get-Job | Stop-Job
Get-Job | Remove-Job

Write-Host "=== TEST TERMINÉ ===" -ForegroundColor Green
Write-Host "Vérifiez les logs ci-dessus pour les améliorations audio." -ForegroundColor Yellow 