# Test des corrections du spam de logs
Write-Host "=== TEST DES CORRECTIONS DU SPAM DE LOGS ===" -ForegroundColor Green

# Compiler l'application
Write-Host "1. Compilation de l'application..." -ForegroundColor Yellow
./gradlew assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Compilation réussie" -ForegroundColor Green
} else {
    Write-Host "✗ Erreur de compilation" -ForegroundColor Red
    exit 1
}

# Installer sur l'émulateur
Write-Host "2. Installation sur l'émulateur..." -ForegroundColor Yellow
adb install -r app/build/outputs/apk/debug/app-debug.apk

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Installation réussie" -ForegroundColor Green
} else {
    Write-Host "✗ Erreur d'installation" -ForegroundColor Red
    exit 1
}

# Démarrer l'application
Write-Host "3. Démarrage de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre un peu
Start-Sleep -Seconds 3

# Surveiller les logs pour vérifier la réduction du spam
Write-Host "4. Surveillance des logs (30 secondes)..." -ForegroundColor Yellow
Write-Host "Les logs devraient maintenant être beaucoup moins spammy!" -ForegroundColor Cyan

$logCount = 0
$startTime = Get-Date

# Surveiller les logs pendant 30 secondes
while ((Get-Date) -lt ($startTime.AddSeconds(30))) {
    $logs = adb logcat -d | Select-String "CompatWrapper|EmulatorRenderer|MainActivity" | Select-Object -Last 10
    
    if ($logs) {
        $logCount += $logs.Count
        Write-Host "Logs trouvés: $($logs.Count) (Total: $logCount)" -ForegroundColor Magenta
        
        # Afficher quelques logs pour vérifier
        foreach ($log in $logs | Select-Object -Last 3) {
            Write-Host "  $($log.Line)" -ForegroundColor Gray
        }
    }
    
    Start-Sleep -Seconds 2
}

Write-Host "=== RÉSULTATS ===" -ForegroundColor Green
Write-Host "Nombre total de logs capturés: $logCount" -ForegroundColor Cyan

if ($logCount -lt 50) {
    Write-Host "✓ Spam de logs considérablement réduit!" -ForegroundColor Green
} else {
    Write-Host "⚠ Logs encore trop nombreux, vérification nécessaire" -ForegroundColor Yellow
}

Write-Host "=== CORRECTIONS APPLIQUÉES ===" -ForegroundColor Green
Write-Host "1. Logs de boutons: seulement lors du changement d'état" -ForegroundColor Cyan
Write-Host "2. Logs de surface: seulement lors du changement de dimensions" -ForegroundColor Cyan
Write-Host "3. Logs d'orientation: seulement lors du premier changement" -ForegroundColor Cyan
Write-Host "4. Logs de DPI: seulement lors du changement de DPI" -ForegroundColor Cyan
Write-Host "5. Logs d'échelle: seulement lors du changement d'échelle" -ForegroundColor Cyan

Write-Host "Test terminé!" -ForegroundColor Green 