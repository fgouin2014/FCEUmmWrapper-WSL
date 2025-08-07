# Script pour tester la correction avec debug des positions des boutons
Write-Host "🔧 Test de la correction avec debug des positions des boutons..." -ForegroundColor Green

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
Write-Host "  ✅ Les positions des boutons sont affichées dans les logs" -ForegroundColor White
Write-Host "  ✅ Les coordonnées touch correspondent aux positions des boutons" -ForegroundColor White
Write-Host "  ✅ Les boutons sont détectés et le personnage saute" -ForegroundColor White
Write-Host "  ✅ Les logs montrent 'Touch down DÉTECTÉ' quand vous appuyez" -ForegroundColor White
Write-Host "  🔍 Comparez les coordonnées touch avec les positions des boutons" -ForegroundColor White

# Surveiller les logs d'audit
adb logcat -s RetroArchOverlaySystem:I 