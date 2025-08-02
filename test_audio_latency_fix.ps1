# Script pour tester la réduction de latence audio
Write-Host "🎯 Test de réduction de latence audio" -ForegroundColor Green

# Compiler l'application
Write-Host "📦 Compilation de l'application..." -ForegroundColor Yellow
./gradlew assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Compilation réussie" -ForegroundColor Green
    
    # Installer sur l'émulateur
    Write-Host "📱 Installation sur l'émulateur..." -ForegroundColor Yellow
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Installation réussie" -ForegroundColor Green
        
        # Lancer l'application
        Write-Host "🚀 Lancement de l'application..." -ForegroundColor Yellow
        adb shell am start -n com.fceumm.wrapper/.MainMenuActivity
        
        Write-Host ""
        Write-Host "🎮 Instructions de test :" -ForegroundColor Cyan
        Write-Host "1. Cliquez sur '🎮 Jouer (ROM par défaut)'" -ForegroundColor White
        Write-Host "2. Attendez que Duck Hunt se charge" -ForegroundColor White
        Write-Host "3. Testez le tir - la latence devrait être < 50ms" -ForegroundColor White
        Write-Host "4. Si encore lent, testez '🎵 Test Qualité Audio'" -ForegroundColor White
        Write-Host ""
        Write-Host "🎯 Objectif : Latence < 50ms entre clic et son" -ForegroundColor Green
        
        # Surveiller les logs
        Write-Host "📊 Surveillance des logs audio..." -ForegroundColor Yellow
        adb logcat -s UltraLowLatencyAudio:V MainActivity:V
    } else {
        Write-Host "❌ Échec de l'installation" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Échec de la compilation" -ForegroundColor Red
} 