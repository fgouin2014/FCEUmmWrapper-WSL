# Test du facteur d'échelle des overlays - CORRECTION APPLIQUÉE
Write-Host "🔧 Test du facteur d'échelle des overlays - CORRECTION APPLIQUÉE" -ForegroundColor Green

# Définir un facteur d'échelle plus grand pour tester
$scale = 2.0  # 100% plus grand (2x)
Write-Host "📏 Facteur d'échelle défini: $scale (2x plus grand)" -ForegroundColor Yellow

# Créer un fichier de configuration de test
$configContent = @"
# Configuration de test pour overlay scale - CORRECTION APPLIQUÉE
input_overlay_enable = true
input_overlay_path = "overlays/gamepads/nes/nes.cfg"
input_overlay_scale = $scale
input_overlay_opacity = 0.8
input_overlay_auto_scale = true
input_overlay_auto_rotate = true
"@

# Sauvegarder la configuration
$configContent | Out-File -FilePath "test_overlay_config_fixed.cfg" -Encoding UTF8
Write-Host "✅ Configuration de test créée: test_overlay_config_fixed.cfg" -ForegroundColor Green

Write-Host "🎯 Instructions de test - CORRECTION APPLIQUÉE:" -ForegroundColor Cyan
Write-Host "1. Compiler et installer l'APK" -ForegroundColor White
Write-Host "2. Les boutons devraient être $scale fois plus grands (2x)" -ForegroundColor White
Write-Host "3. Vérifier les logs pour voir 'Synchronisation avec RetroArchConfigManager'" -ForegroundColor White
Write-Host "4. Vérifier les logs pour voir 'Facteur d'échelle overlay défini: $scale'" -ForegroundColor White
Write-Host "5. Vérifier les logs pour voir '(scale: $scale)' dans les coordonnées" -ForegroundColor White

Write-Host "🔍 Logs à surveiller:" -ForegroundColor Magenta
Write-Host "  - '🔄 Synchronisation avec RetroArchConfigManager effectuée'" -ForegroundColor White
Write-Host "  - '🔄 Synchronisation avec RetroArch - Scale: $scale'" -ForegroundColor White
Write-Host "  - '(scale: $scale)' dans les coordonnées des boutons" -ForegroundColor White
