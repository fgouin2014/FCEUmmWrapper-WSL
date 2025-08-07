# Script pour tester la correction des coordonnées touch
Write-Host "🔧 Test de la correction des coordonnées touch..." -ForegroundColor Green

# Nettoyer les logs
Write-Host "🧹 Nettoyage des logs..." -ForegroundColor Yellow
adb logcat -c

# Lancer l'application
Write-Host "🚀 Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre un peu pour que l'app se lance
Start-Sleep -Seconds 3

Write-Host "📊 Logs en temps reel (Ctrl+C pour arreter):" -ForegroundColor Yellow
Write-Host "Vérifiez que:" -ForegroundColor Cyan
Write-Host "  ✅ Les coordonnées touch sont normalisées par OverlayRenderView" -ForegroundColor White
Write-Host "  ✅ Les hitboxes sont détectées correctement" -ForegroundColor White
Write-Host "  ✅ Les boutons répondent aux touches" -ForegroundColor White
Write-Host "  🔍 Les logs montrent les dimensions OverlayRenderView vs écran" -ForegroundColor White

# Surveiller les logs d'audit
adb logcat -s RetroArchOverlaySystem:I 