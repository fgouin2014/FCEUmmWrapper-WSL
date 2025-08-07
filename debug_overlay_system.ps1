# Script de debug pour le système d'overlay
Write-Host "🔍 Debug du système d'overlay..." -ForegroundColor Green

# Vérifier que l'application est installée
Write-Host "📱 Vérification de l'application..." -ForegroundColor Yellow
adb shell pm list packages | findstr "com.fceumm.wrapper"

# Vérifier les logs en temps réel
Write-Host "Logs en temps reel (Ctrl+C pour arreter):" -ForegroundColor Yellow
Write-Host "Appuyez sur des boutons dans l'application pour voir les logs..." -ForegroundColor Cyan

# Filtrer les logs pertinents
adb logcat -s RetroArchOverlaySystem:V OverlayRenderView:V MainActivity:V SettingsActivity:V RetroArchExact:V 