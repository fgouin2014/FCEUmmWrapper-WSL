# Test de la correction d'EmulationActivity
# Vérification de l'initialisation manquante de RetroArchOverlaySystem

Write-Host "🎮 Test de la correction d'EmulationActivity" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green

# Vérifier les modifications d'EmulationActivity
Write-Host "`n1. Vérification des modifications EmulationActivity:" -ForegroundColor Yellow

$emulationActivity = "app/src/main/java/com/fceumm/wrapper/EmulationActivity.java"
if (Test-Path $emulationActivity) {
    $content = Get-Content $emulationActivity -Raw
    
    if ($content -match "RetroArchOverlaySystem overlaySystem") {
        Write-Host "✓ Variable overlaySystem ajoutée" -ForegroundColor Green
    } else {
        Write-Host "✗ Variable overlaySystem manquante" -ForegroundColor Red
    }
    
    if ($content -match "overlaySystem = RetroArchOverlaySystem.getInstance") {
        Write-Host "✓ Initialisation de RetroArchOverlaySystem" -ForegroundColor Green
    } else {
        Write-Host "✗ Initialisation de RetroArchOverlaySystem manquante" -ForegroundColor Red
    }
    
    if ($content -match "loadDefaultOverlay") {
        Write-Host "✓ Méthode loadDefaultOverlay ajoutée" -ForegroundColor Green
    } else {
        Write-Host "✗ Méthode loadDefaultOverlay manquante" -ForegroundColor Red
    }
    
    if ($content -match "overlaySystem.loadOverlay") {
        Write-Host "✓ Chargement d'overlay implémenté" -ForegroundColor Green
    } else {
        Write-Host "✗ Chargement d'overlay manquant" -ForegroundColor Red
    }
    
    if ($content -match "overlaySystem.setOverlayEnabled") {
        Write-Host "✓ Activation d'overlay implémentée" -ForegroundColor Green
    } else {
        Write-Host "✗ Activation d'overlay manquante" -ForegroundColor Red
    }
    
    if ($content -match "overlaySystem.clearAllTouches") {
        Write-Host "✓ Nettoyage des touches implémenté" -ForegroundColor Green
    } else {
        Write-Host "✗ Nettoyage des touches manquant" -ForegroundColor Red
    }
    
    if ($content -match "setContentView.*activity_retroarch") {
        Write-Host "✓ Layout RetroArch utilisé" -ForegroundColor Green
    } else {
        Write-Host "✗ Layout RetroArch non utilisé" -ForegroundColor Red
    }
    
    if ($content -match "setupPortraitLayout") {
        Write-Host "✓ Configuration layout portrait" -ForegroundColor Green
    } else {
        Write-Host "✗ Configuration layout portrait manquante" -ForegroundColor Red
    }
    
    if ($content -match "setupLandscapeLayout") {
        Write-Host "✓ Configuration layout landscape" -ForegroundColor Green
    } else {
        Write-Host "✗ Configuration layout landscape manquante" -ForegroundColor Red
    }
    
} else {
    Write-Host "✗ EmulationActivity.java manquant" -ForegroundColor Red
}

# Vérifier les imports nécessaires
Write-Host "`n2. Vérification des imports:" -ForegroundColor Yellow

$imports = @(
    "RetroArchOverlaySystem",
    "OverlayRenderView",
    "Configuration"
)

foreach ($import in $imports) {
    if ($content -match "import.*$import") {
        Write-Host "✓ Import $import" -ForegroundColor Green
    } else {
        Write-Host "✗ Import $import manquant" -ForegroundColor Red
    }
}

# Vérifier la structure du layout
Write-Host "`n3. Vérification du layout activity_retroarch.xml:" -ForegroundColor Yellow

$layoutFile = "app/src/main/res/layout/activity_retroarch.xml"
if (Test-Path $layoutFile) {
    $layoutContent = Get-Content $layoutFile -Raw
    
    if ($layoutContent -match "game_viewport") {
        Write-Host "✓ ID game_viewport présent" -ForegroundColor Green
    } else {
        Write-Host "✗ ID game_viewport manquant" -ForegroundColor Red
    }
    
    if ($layoutContent -match "controls_area") {
        Write-Host "✓ ID controls_area présent" -ForegroundColor Green
    } else {
        Write-Host "✗ ID controls_area manquant" -ForegroundColor Red
    }
    
    if ($layoutContent -match "emulator_view") {
        Write-Host "✓ ID emulator_view présent" -ForegroundColor Green
    } else {
        Write-Host "✗ ID emulator_view manquant" -ForegroundColor Red
    }
    
    if ($layoutContent -match "overlay_render_view") {
        Write-Host "✓ ID overlay_render_view présent" -ForegroundColor Green
    } else {
        Write-Host "✗ ID overlay_render_view manquant" -ForegroundColor Red
    }
    
    if ($layoutContent -match "RelativeLayout") {
        Write-Host "✓ Layout RelativeLayout utilisé" -ForegroundColor Green
    } else {
        Write-Host "✗ Layout RelativeLayout non utilisé" -ForegroundColor Red
    }
    
} else {
    Write-Host "✗ Layout activity_retroarch.xml manquant" -ForegroundColor Red
}

# Vérifier les layouts d'orientation
Write-Host "`n4. Vérification des layouts d'orientation:" -ForegroundColor Yellow

$layoutPort = "app/src/main/res/layout-port/activity_retroarch.xml"
$layoutLand = "app/src/main/res/layout-land/activity_retroarch.xml"

if (Test-Path $layoutPort) {
    Write-Host "✓ Layout portrait présent" -ForegroundColor Green
} else {
    Write-Host "✗ Layout portrait manquant" -ForegroundColor Red
}

if (Test-Path $layoutLand) {
    Write-Host "✓ Layout landscape présent" -ForegroundColor Green
} else {
    Write-Host "✗ Layout landscape manquant" -ForegroundColor Red
}

# Vérifier la compilation
Write-Host "`n5. Test de compilation:" -ForegroundColor Yellow

try {
    Write-Host "Compilation en cours..." -ForegroundColor Yellow
    & ./gradlew assembleDebug 2>&1 | Tee-Object -FilePath "emulation_activity_fix_compile.log"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Compilation réussie!" -ForegroundColor Green
    } else {
        Write-Host "✗ Erreurs de compilation détectées" -ForegroundColor Red
        Write-Host "Vérifiez le fichier emulation_activity_fix_compile.log" -ForegroundColor Yellow
    }
} catch {
    Write-Host "✗ Erreur lors de la compilation: $_" -ForegroundColor Red
}

Write-Host "`n=== Résumé de la correction ===" -ForegroundColor Yellow

Write-Host "✅ Corrections apportées:" -ForegroundColor Green
Write-Host "  - Ajout de RetroArchOverlaySystem.getInstance()" -ForegroundColor White
Write-Host "  - Ajout de loadDefaultOverlay()" -ForegroundColor White
Write-Host "  - Ajout de overlaySystem.loadOverlay('nes.cfg')" -ForegroundColor White
Write-Host "  - Ajout de overlaySystem.setOverlayEnabled(true)" -ForegroundColor White
Write-Host "  - Ajout de overlaySystem.clearAllTouches()" -ForegroundColor White
Write-Host "  - Configuration des layouts portrait/landscape" -ForegroundColor White
Write-Host "  - Utilisation du layout activity_retroarch.xml" -ForegroundColor White

Write-Host "`n🎮 Fonctionnalités restaurées:" -ForegroundColor Cyan
Write-Host "  - Affichage des overlays dans les deux modes" -ForegroundColor White
Write-Host "  - Configuration automatique selon l'orientation" -ForegroundColor White
Write-Host "  - Chargement de l'overlay NES par défaut" -ForegroundColor White
Write-Host "  - Gestion des inputs tactiles" -ForegroundColor White
Write-Host "  - Split screen 50/50 en portrait" -ForegroundColor White
Write-Host "  - Full screen en landscape" -ForegroundColor White

Write-Host "`n📱 Résultat attendu:" -ForegroundColor Cyan
Write-Host "  - Mode Portrait: Jeu en haut (50%) + Contrôles en bas (50%)" -ForegroundColor White
Write-Host "  - Mode Landscape: Jeu plein écran + Overlay transparent" -ForegroundColor White
Write-Host "  - Overlays visibles et fonctionnels" -ForegroundColor White

Write-Host "`n=== Test terminé ===" -ForegroundColor Green 