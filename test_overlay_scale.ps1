# Test du facteur d'échelle des overlays
Write-Host "🔧 Test du facteur d'échelle des overlays" -ForegroundColor Green

# Définir un facteur d'échelle plus grand pour tester
$scale = 1.5  # 50% plus grand
Write-Host "📏 Facteur d'échelle défini: $scale" -ForegroundColor Yellow

# Créer un fichier de configuration de test
$configContent = @"
# Configuration de test pour overlay scale
input_overlay_enable = true
input_overlay_path = "overlays/gamepads/nes/nes.cfg"
input_overlay_scale = $scale
input_overlay_opacity = 0.8
input_overlay_auto_scale = true
input_overlay_auto_rotate = true
"@

# Sauvegarder la configuration
$configContent | Out-File -FilePath "test_overlay_config.cfg" -Encoding UTF8
Write-Host "✅ Configuration de test créée: test_overlay_config.cfg" -ForegroundColor Green

Write-Host "🎯 Instructions de test:" -ForegroundColor Cyan
Write-Host "1. Compiler et installer l'APK" -ForegroundColor White
Write-Host "2. Les boutons devraient être $scale fois plus grands" -ForegroundColor White
Write-Host "3. Vérifier les logs pour voir le facteur d'échelle appliqué" -ForegroundColor White
