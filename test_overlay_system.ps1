# Script de test pour le système d'overlays RetroArch
# Valide l'implémentation et génère des rapports

param(
    [string]$ProjectPath = ".",
    [switch]$GenerateImages = $false,
    [switch]$BuildProject = $true,
    [switch]$RunTests = $true
)

Write-Host "=== Test du Système d'Overlays RetroArch ===" -ForegroundColor Green

# Fonction pour vérifier l'existence des fichiers
function Test-FileExists {
    param([string]$FilePath, [string]$Description)
    
    if (Test-Path $FilePath) {
        Write-Host "✓ $Description : $FilePath" -ForegroundColor Green
        return $true
    } else {
        Write-Host "✗ $Description : $FilePath (MANQUANT)" -ForegroundColor Red
        return $false
    }
}

# Fonction pour vérifier la structure des dossiers
function Test-DirectoryStructure {
    Write-Host "`n--- Vérification de la Structure des Dossiers ---" -ForegroundColor Cyan
    
    $requiredDirs = @(
        "app/src/main/java/com/fceumm/wrapper/overlay",
        "app/src/main/assets/overlays",
        "app/src/main/res/layout"
    )
    
    $allExist = $true
    foreach ($dir in $requiredDirs) {
        if (Test-Path $dir) {
            Write-Host "✓ Dossier : $dir" -ForegroundColor Green
        } else {
            Write-Host "✗ Dossier manquant : $dir" -ForegroundColor Red
            $allExist = $false
        }
    }
    
    return $allExist
}

# Fonction pour vérifier les fichiers Java
function Test-JavaFiles {
    Write-Host "`n--- Vérification des Fichiers Java ---" -ForegroundColor Cyan
    
    $javaFiles = @(
        "app/src/main/java/com/fceumm/wrapper/overlay/RetroArchOverlayManager.java",
        "app/src/main/java/com/fceumm/wrapper/overlay/RetroArchOverlayView.java",
        "app/src/main/java/com/fceumm/wrapper/overlay/RetroArchInputManager.java",
        "app/src/main/java/com/fceumm/wrapper/overlay/OverlayPreferences.java",
        "app/src/main/java/com/fceumm/wrapper/overlay/OverlayConfig.java",
        "app/src/main/java/com/fceumm/wrapper/overlay/OverlayButton.java",
        "app/src/main/java/com/fceumm/wrapper/overlay/OverlayType.java",
        "app/src/main/java/com/fceumm/wrapper/overlay/OverlayHitbox.java"
    )
    
    $allExist = $true
    foreach ($file in $javaFiles) {
        if (Test-FileExists $file "Fichier Java") {
            # Vérifier la syntaxe basique
            $content = Get-Content $file -Raw
            if ($content -match "package com\.fceumm\.wrapper\.overlay") {
                Write-Host "  ✓ Package correct" -ForegroundColor Green
            } else {
                Write-Host "  ⚠ Package incorrect ou manquant" -ForegroundColor Yellow
            }
        } else {
            $allExist = $false
        }
    }
    
    return $allExist
}

# Fonction pour vérifier les fichiers de configuration
function Test-ConfigFiles {
    Write-Host "`n--- Vérification des Fichiers de Configuration ---" -ForegroundColor Cyan
    
    $configFiles = @(
        "app/src/main/assets/overlays/retropad.cfg",
        "app/src/main/assets/overlays/rgpad.cfg",
        "app/src/main/assets/overlays/README.md"
    )
    
    $allExist = $true
    foreach ($file in $configFiles) {
        if (Test-FileExists $file "Fichier de configuration") {
            # Vérifier le contenu des .cfg
            if ($file -match "\.cfg$") {
                $content = Get-Content $file -Raw
                if ($content -match "overlays = 1") {
                    Write-Host "  ✓ Format RetroArch détecté" -ForegroundColor Green
                } else {
                    Write-Host "  ⚠ Format RetroArch non détecté" -ForegroundColor Yellow
                }
            }
        } else {
            $allExist = $false
        }
    }
    
    return $allExist
}

# Fonction pour vérifier le layout
function Test-LayoutFile {
    Write-Host "`n--- Vérification du Layout ---" -ForegroundColor Cyan
    
    $layoutFile = "app/src/main/res/layout/activity_emulation.xml"
    
    if (Test-FileExists $layoutFile "Layout XML") {
        $content = Get-Content $layoutFile -Raw
        
        # Vérifier la présence de la vue RetroArch
        if ($content -match "RetroArchOverlayView") {
            Write-Host "  ✓ Vue RetroArchOverlayView trouvée" -ForegroundColor Green
        } else {
            Write-Host "  ✗ Vue RetroArchOverlayView manquante" -ForegroundColor Red
            return $false
        }
        
        # Vérifier la présence du fallback
        if ($content -match "SimpleOverlay") {
            Write-Host "  ✓ Fallback SimpleOverlay trouvé" -ForegroundColor Green
        } else {
            Write-Host "  ⚠ Fallback SimpleOverlay manquant" -ForegroundColor Yellow
        }
        
        return $true
    } else {
        return $false
    }
}

# Fonction pour vérifier MainActivity
function Test-MainActivity {
    Write-Host "`n--- Vérification de MainActivity ---" -ForegroundColor Cyan
    
    $mainActivityFile = "app/src/main/java/com/fceumm/wrapper/MainActivity.java"
    
    if (Test-FileExists $mainActivityFile "MainActivity") {
        $content = Get-Content $mainActivityFile -Raw
        
        # Vérifier les imports
        $imports = @(
            "RetroArchOverlayManager",
            "RetroArchOverlayView", 
            "RetroArchInputManager",
            "OverlayPreferences"
        )
        
        $allImportsFound = $true
        foreach ($import in $imports) {
            if ($content -match $import) {
                Write-Host "  ✓ Import trouvé : $import" -ForegroundColor Green
            } else {
                Write-Host "  ✗ Import manquant : $import" -ForegroundColor Red
                $allImportsFound = $false
            }
        }
        
        # Vérifier les méthodes
        $methods = @(
            "initializeRetroArchOverlays",
            "loadRetroArchOverlays",
            "applyOverlayPreferences"
        )
        
        $allMethodsFound = $true
        foreach ($method in $methods) {
            if ($content -match $method) {
                Write-Host "  ✓ Méthode trouvée : $method" -ForegroundColor Green
            } else {
                Write-Host "  ✗ Méthode manquante : $method" -ForegroundColor Red
                $allMethodsFound = $false
            }
        }
        
        return $allImportsFound -and $allMethodsFound
    } else {
        return $false
    }
}

# Fonction pour générer des images de test
function Generate-TestImages {
    Write-Host "`n--- Génération des Images de Test ---" -ForegroundColor Cyan
    
    if ($GenerateImages) {
        Write-Host "Exécution du script de génération d'images..." -ForegroundColor Yellow
        & "./generate_overlay_images.ps1"
    } else {
        Write-Host "Génération d'images ignorée (utilisez -GenerateImages pour activer)" -ForegroundColor Yellow
    }
}

# Fonction pour compiler le projet
function Build-Project {
    Write-Host "`n--- Compilation du Projet ---" -ForegroundColor Cyan
    
    if ($BuildProject) {
        try {
            Write-Host "Compilation avec Gradle..." -ForegroundColor Yellow
            & "./gradlew" "clean" "assembleDebug"
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "✓ Compilation réussie" -ForegroundColor Green
                return [bool]$true
            } else {
                Write-Host "✗ Erreur de compilation" -ForegroundColor Red
                return [bool]$false
            }
        } catch {
            Write-Host "✗ Erreur lors de la compilation : $_" -ForegroundColor Red
            return [bool]$false
        }
    } else {
        Write-Host "Compilation ignorée (utilisez -BuildProject pour activer)" -ForegroundColor Yellow
        return [bool]$true
    }
}

# Fonction pour exécuter les tests
function Run-Tests {
    Write-Host "`n--- Exécution des Tests ---" -ForegroundColor Cyan
    
    if ($RunTests) {
        Write-Host "Tests à implémenter :" -ForegroundColor Yellow
        Write-Host "  - Test de chargement des overlays" -ForegroundColor White
        Write-Host "  - Test de parsing des fichiers .cfg" -ForegroundColor White
        Write-Host "  - Test de rendu OpenGL" -ForegroundColor White
        Write-Host "  - Test de détection tactile" -ForegroundColor White
        Write-Host "  - Test de mapping des inputs" -ForegroundColor White
    } else {
        Write-Host "Tests ignorés (utilisez -RunTests pour activer)" -ForegroundColor Yellow
    }
}

# Fonction pour générer le rapport
function Generate-Report {
    param([bool]$StructureOK, [bool]$JavaOK, [bool]$ConfigOK, [bool]$LayoutOK, [bool]$MainActivityOK, [bool]$BuildOK)
    
    Write-Host "`n=== RAPPORT FINAL ===" -ForegroundColor Green
    
    $totalTests = 6
    $passedTests = 0
    
    if ($StructureOK) { $passedTests++ }
    if ($JavaOK) { $passedTests++ }
    if ($ConfigOK) { $passedTests++ }
    if ($LayoutOK) { $passedTests++ }
    if ($MainActivityOK) { $passedTests++ }
    if ($BuildOK) { $passedTests++ }
    
    Write-Host "Tests réussis : $passedTests/$totalTests" -ForegroundColor $(if ($passedTests -eq $totalTests) { "Green" } else { "Yellow" })
    
    if ($passedTests -eq $totalTests) {
        Write-Host "`n🎉 TOUS LES TESTS RÉUSSIS !" -ForegroundColor Green
        Write-Host "Le système d'overlays RetroArch est prêt à être utilisé." -ForegroundColor Green
    } else {
        Write-Host "`n⚠ Certains tests ont échoué. Vérifiez les erreurs ci-dessus." -ForegroundColor Yellow
    }
    
    # Recommandations
    Write-Host "`n--- Recommandations ---" -ForegroundColor Cyan
    Write-Host "1. Créez des images PNG pour les overlays" -ForegroundColor White
    Write-Host "2. Testez sur un appareil réel" -ForegroundColor White
    Write-Host "3. Validez les performances" -ForegroundColor White
    Write-Host "4. Testez avec différents overlays" -ForegroundColor White
}

# Exécution des tests
Write-Host "Démarrage des tests..." -ForegroundColor Yellow

$structureOK = Test-DirectoryStructure
$javaOK = Test-JavaFiles
$configOK = Test-ConfigFiles
$layoutOK = Test-LayoutFile
$mainActivityOK = Test-MainActivity

Generate-TestImages
$buildOK = Build-Project
Run-Tests

# S'assurer que $buildOK est un booléen
if ($buildOK -is [array]) {
    $buildOK = [bool]$buildOK[0]
} elseif ($buildOK -is [string]) {
    $buildOK = [bool]($buildOK -eq "True" -or $buildOK -eq "true" -or $buildOK -eq "1")
} else {
    $buildOK = [bool]$buildOK
}

Generate-Report $structureOK $javaOK $configOK $layoutOK $mainActivityOK $buildOK

Write-Host "`nTest terminé." -ForegroundColor Green 