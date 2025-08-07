# Script de test pour le mode Split Screen
# Teste l'implémentation du mode Split Screen avec viewport personnalisé

Write-Host "=== Test du Mode Split Screen ===" -ForegroundColor Green

# Vérifier que les fichiers de configuration existent
$splitScreenConfig = "app/src/main/assets/overlays/gamepads/flat/nes_split_screen.cfg"
if (Test-Path $splitScreenConfig) {
    Write-Host "✓ Configuration Split Screen trouvée: $splitScreenConfig" -ForegroundColor Green
} else {
    Write-Host "✗ Configuration Split Screen manquante: $splitScreenConfig" -ForegroundColor Red
    exit 1
}

# Vérifier que les classes Java ont été créées
$splitScreenManager = "app/src/main/java/com/fceumm/wrapper/overlay/SplitScreenManager.java"
if (Test-Path $splitScreenManager) {
    Write-Host "✓ SplitScreenManager.java créé" -ForegroundColor Green
} else {
    Write-Host "✗ SplitScreenManager.java manquant" -ForegroundColor Red
    exit 1
}

# Vérifier les modifications dans RetroArchOverlayLoader
$overlayLoader = "app/src/main/java/com/fceumm/wrapper/overlay/RetroArchOverlayLoader.java"
if (Test-Path $overlayLoader) {
    Write-Host "✓ RetroArchOverlayLoader.java modifié" -ForegroundColor Green
    
    # Vérifier que les nouvelles méthodes sont présentes
    $content = Get-Content $overlayLoader -Raw
    if ($content -match "setSplitScreenMode") {
        Write-Host "✓ Méthode setSplitScreenMode() trouvée" -ForegroundColor Green
    } else {
        Write-Host "✗ Méthode setSplitScreenMode() manquante" -ForegroundColor Red
    }
    
    if ($content -match "isSplitScreenMode") {
        Write-Host "✓ Méthode isSplitScreenMode() trouvée" -ForegroundColor Green
    } else {
        Write-Host "✗ Méthode isSplitScreenMode() manquante" -ForegroundColor Red
    }
    
    if ($content -match "getGameViewport") {
        Write-Host "✓ Méthode getGameViewport() trouvée" -ForegroundColor Green
    } else {
        Write-Host "✗ Méthode getGameViewport() manquante" -ForegroundColor Red
    }
    
} else {
    Write-Host "✗ RetroArchOverlayLoader.java manquant" -ForegroundColor Red
    exit 1
}

# Vérifier les structures RetroArch
$structures = "app/src/main/java/com/fceumm/wrapper/overlay/RetroArchExactStructures.java"
if (Test-Path $structures) {
    Write-Host "✓ RetroArchExactStructures.java modifié" -ForegroundColor Green
    
    $content = Get-Content $structures -Raw
    if ($content -match "SplitScreenViewport") {
        Write-Host "✓ Structure SplitScreenViewport trouvée" -ForegroundColor Green
    } else {
        Write-Host "✗ Structure SplitScreenViewport manquante" -ForegroundColor Red
    }
    
    if ($content -match "SplitScreenConfig") {
        Write-Host "✓ Structure SplitScreenConfig trouvée" -ForegroundColor Green
    } else {
        Write-Host "✗ Structure SplitScreenConfig manquante" -ForegroundColor Red
    }
} else {
    Write-Host "✗ RetroArchExactStructures.java manquant" -ForegroundColor Red
    exit 1
}

# Vérifier les fonctions RetroArch
$functions = "app/src/main/java/com/fceumm/wrapper/overlay/RetroArchExactFunctions.java"
if (Test-Path $functions) {
    Write-Host "✓ RetroArchExactFunctions.java modifié" -ForegroundColor Green
    
    $content = Get-Content $functions -Raw
    if ($content -match "createSplitScreenConfig") {
        Write-Host "✓ Fonction createSplitScreenConfig() trouvée" -ForegroundColor Green
    } else {
        Write-Host "✗ Fonction createSplitScreenConfig() manquante" -ForegroundColor Red
    }
    
    if ($content -match "applySplitScreenViewport") {
        Write-Host "✓ Fonction applySplitScreenViewport() trouvée" -ForegroundColor Green
    } else {
        Write-Host "✗ Fonction applySplitScreenViewport() manquante" -ForegroundColor Red
    }
    
    if ($content -match "loadSplitScreenConfigFromCfg") {
        Write-Host "✓ Fonction loadSplitScreenConfigFromCfg() trouvée" -ForegroundColor Green
    } else {
        Write-Host "✗ Fonction loadSplitScreenConfigFromCfg() manquante" -ForegroundColor Red
    }
} else {
    Write-Host "✗ RetroArchExactFunctions.java manquant" -ForegroundColor Red
    exit 1
}

Write-Host "`n=== Résumé des fonctionnalités Split Screen ===" -ForegroundColor Yellow

Write-Host "1. Mode Full Screen Overlay (existant):" -ForegroundColor Cyan
Write-Host "   - Contrôles transparents par-dessus le jeu" -ForegroundColor White
Write-Host "   - overlay0_full_screen = true" -ForegroundColor White

Write-Host "`n2. Mode Split Screen (nouveau):" -ForegroundColor Cyan
Write-Host "   - Zone de jeu dédiée" -ForegroundColor White
Write-Host "   - Zone de contrôles séparée" -ForegroundColor White
Write-Host "   - overlay0_full_screen = false" -ForegroundColor White
Write-Host "   - custom_viewport_width/height configurés" -ForegroundColor White

Write-Host "`n3. Layouts disponibles:" -ForegroundColor Cyan
Write-Host "   - Portrait: Jeu en haut (70%), contrôles en bas (30%)" -ForegroundColor White
Write-Host "   - Landscape: Contrôles sur les côtés (20%), jeu au centre (60%)" -ForegroundColor White
Write-Host "   - Custom: Viewport personnalisé via CFG" -ForegroundColor White

Write-Host "`n4. Configuration via CFG:" -ForegroundColor Cyan
Write-Host "   - custom_viewport_width = 1280" -ForegroundColor White
Write-Host "   - custom_viewport_height = 720" -ForegroundColor White
Write-Host "   - custom_viewport_x = 0" -ForegroundColor White
Write-Host "   - custom_viewport_y = 0" -ForegroundColor White
Write-Host "   - split_screen_enabled = true" -ForegroundColor White
Write-Host "   - overlay_opacity = 0.9" -ForegroundColor White

Write-Host "`n5. Avantages du mode Split Screen:" -ForegroundColor Cyan
Write-Host "   - Résout les problèmes d'affichage" -ForegroundColor White
Write-Host "   - Meilleure visibilité des contrôles" -ForegroundColor White
Write-Host "   - Zone de jeu optimisée" -ForegroundColor White
Write-Host "   - Compatible avec RetroArch" -ForegroundColor White

Write-Host "`n=== Test de compilation ===" -ForegroundColor Green

# Tenter de compiler le projet
try {
    Write-Host "Compilation en cours..." -ForegroundColor Yellow
    & ./gradlew assembleDebug 2>&1 | Tee-Object -FilePath "split_screen_compile.log"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Compilation réussie!" -ForegroundColor Green
    } else {
        Write-Host "✗ Erreurs de compilation détectées" -ForegroundColor Red
        Write-Host "Vérifiez le fichier split_screen_compile.log" -ForegroundColor Yellow
    }
} catch {
    Write-Host "✗ Erreur lors de la compilation: $_" -ForegroundColor Red
}

Write-Host "`n=== Instructions d'utilisation ===" -ForegroundColor Yellow

Write-Host "1. Activer le mode Split Screen:" -ForegroundColor White
Write-Host "   overlayLoader.setSplitScreenMode(true);" -ForegroundColor Gray

Write-Host "`n2. Charger une configuration spécifique:" -ForegroundColor White
Write-Host "   overlayLoader.loadSplitScreenConfig('nes_split_screen.cfg');" -ForegroundColor Gray

Write-Host "`n3. Changer le type de layout:" -ForegroundColor White
Write-Host "   overlayLoader.setSplitScreenLayoutType('portrait');" -ForegroundColor Gray
Write-Host "   overlayLoader.setSplitScreenLayoutType('landscape');" -ForegroundColor Gray

Write-Host "`n4. Obtenir les viewports:" -ForegroundColor White
Write-Host "   Rect gameViewport = overlayLoader.getGameViewport();" -ForegroundColor Gray
Write-Host "   Rect overlayViewport = overlayLoader.getOverlayViewport();" -ForegroundColor Gray

Write-Host "`n=== Test terminé ===" -ForegroundColor Green 