# Test des corrections d'overlay - Inversion de l'axe Y
Write-Host "🧪 Test des corrections d'overlay RetroArch" -ForegroundColor Green

# 1. Vérifier que l'APK est installé
Write-Host "📱 Vérification de l'installation..." -ForegroundColor Yellow
adb shell pm list packages | findstr "com.fceumm.wrapper"

# 2. Lancer l'application
Write-Host "🚀 Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.EmulationActivity

# 3. Attendre un peu puis vérifier les logs
Write-Host "⏳ Attente de 5 secondes..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# 4. Capturer les logs d'overlay
Write-Host "📊 Récupération des logs d'overlay..." -ForegroundColor Yellow
adb logcat -d | Select-String -Pattern "(RetroArchOverlaySystem|EmulationActivity|OverlayRenderView)" | Select-Object -Last 20

# 5. Vérifier les coordonnées spécifiques
Write-Host "🎯 Vérification des coordonnées L/R..." -ForegroundColor Yellow
adb logcat -d | Select-String -Pattern "🎯.*(l|r).*inversé" | Select-Object -Last 5

Write-Host "✅ Test terminé !" -ForegroundColor Green
