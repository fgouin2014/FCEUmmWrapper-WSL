# Script de build complet (cores + application)
Write-Host "=== Build complet avec cores ===" -ForegroundColor Green

# Intégrer les cores d'abord
Write-Host "📦 Intégration des cores..." -ForegroundColor Cyan
& .\integrate_cores.ps1

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Erreur lors de l'intégration des cores" -ForegroundColor Red
    exit 1
}

# Build complet de l'application
Write-Host "🔨 Compilation complète de l'application..." -ForegroundColor Cyan
& .\gradlew.bat clean assembleDebug --parallel --max-workers=8 --daemon

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Build complet réussi !" -ForegroundColor Green
    Write-Host "📱 APK généré dans : app\build\outputs\apk\debug\" -ForegroundColor Cyan
} else {
    Write-Host "❌ Erreur lors du build" -ForegroundColor Red
    exit 1
}
