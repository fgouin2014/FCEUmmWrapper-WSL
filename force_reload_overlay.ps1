# Script pour forcer le rechargement de l'overlay
Write-Host "🔄 Forçage du rechargement de l'overlay..." -ForegroundColor Green

# Nettoyer les logs
Write-Host "🧹 Nettoyage des logs..." -ForegroundColor Yellow
adb logcat -c

# Lancer l'application
Write-Host "🚀 Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre un peu
Start-Sleep -Seconds 3

# Afficher les logs en temps réel
Write-Host "Logs en temps reel (appuyez sur des boutons dans l'app):" -ForegroundColor Cyan
Write-Host "Appuyez sur Ctrl+C pour arreter" -ForegroundColor Red

# Filtrer les logs pertinents
adb logcat -s RetroArchOverlaySystem:V OverlayRenderView:V MainActivity:V SettingsActivity:V RetroArchExact:V 