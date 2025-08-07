# Script pour tester les hitboxes restaurées du commit qui fonctionnait
Write-Host "🔧 Test des hitboxes restaurées du commit 52566d8..." -ForegroundColor Green

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
Write-Host "  ✅ Les hitboxes optimisées sont appliquées selon le type de bouton" -ForegroundColor White
Write-Host "  ✅ Les boutons directionnels ont hitbox x1.15" -ForegroundColor White
Write-Host "  ✅ Les boutons d'action ont hitbox x1.1" -ForegroundColor White
Write-Host "  ✅ Les boutons combinés ont hitbox x1.8 (portrait) ou x2.5 (paysage)" -ForegroundColor White
Write-Host "  ✅ Au moins un bouton est détecté (pas tous false)" -ForegroundColor White
Write-Host "  ✅ Le personnage saute quand vous appuyez sur les boutons" -ForegroundColor Green

# Filtrer les logs pertinents avec plus de contexte
adb logcat | Select-String "RetroArchOverlaySystem|TouchEvent|onOverlayInput|Touch down|RESTAURATION"