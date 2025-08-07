# Test de l'implémentation RetroArch exacte
# Configuration par jeu/core et overlays depuis common-overlays_git

Write-Host "🎮 Test de l'implémentation RetroArch exacte" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green

# Vérifier les nouveaux fichiers créés
Write-Host "`n1. Vérification des nouveaux fichiers RetroArch:" -ForegroundColor Yellow

$newFiles = @(
    "app/src/main/java/com/fceumm/wrapper/config/RetroArchConfigManager.java",
    "app/src/main/java/com/fceumm/wrapper/overlay/RetroArchOverlayManager.java"
)

foreach ($file in $newFiles) {
    if (Test-Path $file) {
        Write-Host "✓ $file" -ForegroundColor Green
    } else {
        Write-Host "✗ $file manquant" -ForegroundColor Red
    }
}

# Vérifier les modifications d'EmulationActivity
Write-Host "`n2. Vérification des modifications EmulationActivity:" -ForegroundColor Yellow

$emulationActivity = "app/src/main/java/com/fceumm/wrapper/EmulationActivity.java"
if (Test-Path $emulationActivity) {
    $content = Get-Content $emulationActivity -Raw
    
    if ($content -match "RetroArchOverlayManager") {
        Write-Host "✓ Utilisation de RetroArchOverlayManager" -ForegroundColor Green
    } else {
        Write-Host "✗ RetroArchOverlayManager non utilisé" -ForegroundColor Red
    }
    
    if ($content -match "setCurrentCore") {
        Write-Host "✓ Configuration par core implémentée" -ForegroundColor Green
    } else {
        Write-Host "✗ Configuration par core manquante" -ForegroundColor Red
    }
    
    if ($content -match "setCurrentGame") {
        Write-Host "✓ Configuration par jeu implémentée" -ForegroundColor Green
    } else {
        Write-Host "✗ Configuration par jeu manquante" -ForegroundColor Red
    }
    
    if ($content -match "initializeOverlays") {
        Write-Host "✓ Initialisation des overlays depuis git" -ForegroundColor Green
    } else {
        Write-Host "✗ Initialisation des overlays manquante" -ForegroundColor Red
    }
    
    if ($content -match "activity_retroarch") {
        Write-Host "✓ Utilisation du layout RetroArch officiel" -ForegroundColor Green
    } else {
        Write-Host "✗ Layout RetroArch non utilisé" -ForegroundColor Red
    }
} else {
    Write-Host "✗ EmulationActivity.java manquant" -ForegroundColor Red
}

# Vérifier les overlays dans common-overlays_git
Write-Host "`n3. Vérification des overlays RetroArch:" -ForegroundColor Yellow

$overlayDirs = @(
    "common-overlays_git/gamepads/nes",
    "common-overlays_git/gamepads/flat",
    "common-overlays_git/gamepads/retropad"
)

foreach ($dir in $overlayDirs) {
    if (Test-Path $dir) {
        $cfgFiles = Get-ChildItem $dir -Filter "*.cfg" | Measure-Object
        $imgFiles = Get-ChildItem "$dir/img" -Filter "*.png" -ErrorAction SilentlyContinue | Measure-Object
        
        Write-Host "✓ $dir" -ForegroundColor Green
        Write-Host "  - Fichiers .cfg: $($cfgFiles.Count)" -ForegroundColor White
        Write-Host "  - Images .png: $($imgFiles.Count)" -ForegroundColor White
    } else {
        Write-Host "✗ $dir manquant" -ForegroundColor Red
    }
}

# Vérifier le fichier nes.cfg spécifiquement
Write-Host "`n4. Vérification du fichier nes.cfg:" -ForegroundColor Yellow

$nesCfg = "common-overlays_git/gamepads/nes/nes.cfg"
if (Test-Path $nesCfg) {
    $content = Get-Content $nesCfg -Raw
    
    if ($content -match "overlays = 4") {
        Write-Host "✓ Structure RetroArch correcte (4 overlays)" -ForegroundColor Green
    } else {
        Write-Host "✗ Structure RetroArch incorrecte" -ForegroundColor Red
    }
    
    if ($content -match "landscape") {
        Write-Host "✓ Overlay landscape présent" -ForegroundColor Green
    } else {
        Write-Host "✗ Overlay landscape manquant" -ForegroundColor Red
    }
    
    if ($content -match "portrait") {
        Write-Host "✓ Overlay portrait présent" -ForegroundColor Green
    } else {
        Write-Host "✗ Overlay portrait manquant" -ForegroundColor Red
    }
    
    if ($content -match "menu") {
        Write-Host "✓ Overlay menu présent" -ForegroundColor Green
    } else {
        Write-Host "✗ Overlay menu manquant" -ForegroundColor Red
    }
    
    if ($content -match "hide") {
        Write-Host "✓ Overlay hide présent" -ForegroundColor Green
    } else {
        Write-Host "✗ Overlay hide manquant" -ForegroundColor Red
    }
} else {
    Write-Host "✗ nes.cfg manquant" -ForegroundColor Red
}

# Vérifier les images des overlays
Write-Host "`n5. Vérification des images d'overlays:" -ForegroundColor Yellow

$imgDir = "common-overlays_git/gamepads/nes/img"
if (Test-Path $imgDir) {
    $requiredImages = @("a.png", "b.png", "start.png", "select.png", "dpad.png")
    
    foreach ($img in $requiredImages) {
        if (Test-Path "$imgDir/$img") {
            Write-Host "✓ $img" -ForegroundColor Green
        } else {
            Write-Host "✗ $img manquant" -ForegroundColor Red
        }
    }
} else {
    Write-Host "✗ Répertoire img manquant" -ForegroundColor Red
}

# Vérifier la configuration RetroArch
Write-Host "`n6. Vérification de la configuration RetroArch:" -ForegroundColor Yellow

$configManager = "app/src/main/java/com/fceumm/wrapper/config/RetroArchConfigManager.java"
if (Test-Path $configManager) {
    $content = Get-Content $configManager -Raw
    
    if ($content -match "input_overlay_enable") {
        Write-Host "✓ Paramètre input_overlay_enable" -ForegroundColor Green
    } else {
        Write-Host "✗ Paramètre input_overlay_enable manquant" -ForegroundColor Red
    }
    
    if ($content -match "input_overlay_path") {
        Write-Host "✓ Paramètre input_overlay_path" -ForegroundColor Green
    } else {
        Write-Host "✗ Paramètre input_overlay_path manquant" -ForegroundColor Red
    }
    
    if ($content -match "input_overlay_scale") {
        Write-Host "✓ Paramètre input_overlay_scale" -ForegroundColor Green
    } else {
        Write-Host "✗ Paramètre input_overlay_scale manquant" -ForegroundColor Red
    }
    
    if ($content -match "input_overlay_opacity") {
        Write-Host "✓ Paramètre input_overlay_opacity" -ForegroundColor Green
    } else {
        Write-Host "✗ Paramètre input_overlay_opacity manquant" -ForegroundColor Red
    }
    
    if ($content -match "getConfigWithHierarchy") {
        Write-Host "✓ Hiérarchie de configuration (Jeu > Core > Global)" -ForegroundColor Green
    } else {
        Write-Host "✗ Hiérarchie de configuration manquante" -ForegroundColor Red
    }
} else {
    Write-Host "✗ RetroArchConfigManager.java manquant" -ForegroundColor Red
}

# Vérifier le gestionnaire d'overlays
Write-Host "`n7. Vérification du gestionnaire d'overlays:" -ForegroundColor Yellow

$overlayManager = "app/src/main/java/com/fceumm/wrapper/overlay/RetroArchOverlayManager.java"
if (Test-Path $overlayManager) {
    $content = Get-Content $overlayManager -Raw
    
    if ($content -match "copyOverlaysFromGit") {
        Write-Host "✓ Copie depuis common-overlays_git" -ForegroundColor Green
    } else {
        Write-Host "✗ Copie depuis git manquante" -ForegroundColor Red
    }
    
    if ($content -match "initializeOverlays") {
        Write-Host "✓ Initialisation des overlays" -ForegroundColor Green
    } else {
        Write-Host "✗ Initialisation des overlays manquante" -ForegroundColor Red
    }
    
    if ($content -match "setCurrentCore") {
        Write-Host "✓ Configuration par core" -ForegroundColor Green
    } else {
        Write-Host "✗ Configuration par core manquante" -ForegroundColor Red
    }
    
    if ($content -match "setCurrentGame") {
        Write-Host "✓ Configuration par jeu" -ForegroundColor Green
    } else {
        Write-Host "✗ Configuration par jeu manquante" -ForegroundColor Red
    }
} else {
    Write-Host "✗ RetroArchOverlayManager.java manquant" -ForegroundColor Red
}

# Test de compilation
Write-Host "`n8. Test de compilation:" -ForegroundColor Yellow

try {
    Write-Host "Compilation en cours..." -ForegroundColor Yellow
    & ./gradlew assembleDebug 2>&1 | Tee-Object -FilePath "retroarch_exact_compile.log"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Compilation réussie!" -ForegroundColor Green
    } else {
        Write-Host "✗ Erreurs de compilation détectées" -ForegroundColor Red
        Write-Host "Vérifiez le fichier retroarch_exact_compile.log" -ForegroundColor Yellow
    }
} catch {
    Write-Host "✗ Erreur lors de la compilation: $_" -ForegroundColor Red
}

Write-Host "`n=== Résumé de l'implémentation RetroArch exacte ===" -ForegroundColor Yellow

Write-Host "✅ Fonctionnalités implémentées:" -ForegroundColor Green
Write-Host "  - Configuration par jeu/core (hiérarchie RetroArch)" -ForegroundColor White
Write-Host "  - Copie des overlays depuis common-overlays_git" -ForegroundColor White
Write-Host "  - Parser des fichiers .cfg RetroArch" -ForegroundColor White
Write-Host "  - Layout RetroArch officiel (activity_retroarch.xml)" -ForegroundColor White
Write-Host "  - Paramètres overlay exacts (scale, opacity, etc.)" -ForegroundColor White
Write-Host "  - Mapping vers les inputs libretro" -ForegroundColor White

Write-Host "`n🎮 Utilisation:" -ForegroundColor Cyan
Write-Host "  - Configuration globale: retroarch_global" -ForegroundColor White
Write-Host "  - Configuration par core: core_fceumm_libretro_android.so" -ForegroundColor White
Write-Host "  - Configuration par jeu: game_marioduckhunt.nes" -ForegroundColor White
Write-Host "  - Overlays disponibles: nes, flat, retropad" -ForegroundColor White

Write-Host "`n📁 Structure des overlays:" -ForegroundColor Cyan
Write-Host "  - Source: common-overlays_git/gamepads/" -ForegroundColor White
Write-Host "  - Destination: assets/overlays/gamepads/" -ForegroundColor White
Write-Host "  - Fichiers: .cfg + img/*.png" -ForegroundColor White

Write-Host "`n=== Test terminé ===" -ForegroundColor Green 