# Script d'audit détaillé du système d'overlay
Write-Host "🔍 Audit détaillé du système d'overlay..." -ForegroundColor Green

# Nettoyer les logs
Write-Host "🧹 Nettoyage des logs..." -ForegroundColor Yellow
adb logcat -c

# Lancer l'application
Write-Host "🚀 Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre un peu pour que l'app se lance
Start-Sleep -Seconds 3

Write-Host "📊 Logs d'audit en temps réel (Ctrl+C pour arreter):" -ForegroundColor Yellow
Write-Host "Appuyez sur des boutons dans l'application pour voir les logs détaillés..." -ForegroundColor Cyan
Write-Host "Les logs montreront:" -ForegroundColor White
Write-Host "  - Coordonnées CFG originales" -ForegroundColor White
Write-Host "  - Dimensions écran et orientation" -ForegroundColor White
Write-Host "  - Layout applique (scale, separation, offset)" -ForegroundColor White
Write-Host "  - Coordonnees touch brutes et normalisees" -ForegroundColor White
Write-Host "  - Positions mod_x, mod_y calculees" -ForegroundColor White
Write-Host "  - Tests de hitbox detaillees" -ForegroundColor White

# Surveiller les logs d'audit
adb logcat -s RetroArchOverlaySystem:I 