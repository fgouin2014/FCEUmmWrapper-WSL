# Script pour tester les corrections du fullscreen et logs A/V
Write-Host "🔧 Test des corrections du fullscreen et logs A/V..." -ForegroundColor Green

# Nettoyer les logs
Write-Host "🧹 Nettoyage des logs..." -ForegroundColor Yellow
adb logcat -c

# Lancer l'application
Write-Host "🚀 Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre un peu pour que l'app se lance
Start-Sleep -Seconds 3

Write-Host "📊 Logs en temps réel (Ctrl+C pour arreter):" -ForegroundColor Yellow
Write-Host "Vérifiez que:" -ForegroundColor Cyan
Write-Host "  ✅ L'application utilise tout l'écran (pas de zone noire en haut)" -ForegroundColor White
Write-Host "  ✅ Les logs de 'Synchronisation A/V' n'apparaissent plus" -ForegroundColor White
Write-Host "  ✅ L'application reste en fullscreen après rotation" -ForegroundColor White
Write-Host "  🔍 Les logs d'audit d'overlay apparaissent quand vous appuyez sur des boutons" -ForegroundColor White

# Surveiller les logs (sans les logs de synchronisation A/V)
adb logcat -s MainActivity:I RetroArchOverlaySystem:I 