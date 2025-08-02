# Script de test pour compiler et tester un core personnalisé

Write-Host "🧪 Test de compilation d'un core personnalisé..." -ForegroundColor Green

# 1. Compiler le core personnalisé
Write-Host "📦 Étape 1: Compilation du core..." -ForegroundColor Yellow
& .\build_custom_core.ps1 -Architecture "arm64-v8a" -CoreName "fceumm"

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Core compilé avec succès!" -ForegroundColor Green
    
    # 2. Compiler l'application Android
    Write-Host "📱 Étape 2: Compilation de l'application..." -ForegroundColor Yellow
    & .\gradlew clean assembleDebug
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Application compilée avec succès!" -ForegroundColor Green
        
        # 3. Installer l'application
        Write-Host "📲 Étape 3: Installation de l'application..." -ForegroundColor Yellow
        & adb install -r app\build\outputs\apk\debug\app-arm64-v8a-debug.apk
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Application installée avec succès!" -ForegroundColor Green
            
            # 4. Lancer l'application
            Write-Host "🚀 Étape 4: Lancement de l'application..." -ForegroundColor Yellow
            & adb shell am start -n com.fceumm.wrapper/.MainMenuActivity
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "✅ Application lancée avec succès!" -ForegroundColor Green
                Write-Host "🎉 Test complet réussi!" -ForegroundColor Green
                Write-Host "📋 Le core personnalisé est maintenant utilisé par l'application" -ForegroundColor Cyan
            } else {
                Write-Host "❌ Échec du lancement de l'application" -ForegroundColor Red
            }
        } else {
            Write-Host "❌ Échec de l'installation de l'application" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ Échec de la compilation de l'application" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Échec de la compilation du core" -ForegroundColor Red
}

Write-Host "🏁 Test terminé!" -ForegroundColor Green 