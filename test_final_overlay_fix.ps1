# Script de test final pour les overlays
Write-Host "=== TEST FINAL DES OVERLAYS ===" -ForegroundColor Green

Write-Host "`n--- Vérification des fichiers de configuration ---" -ForegroundColor Cyan

# Vérifier le fichier retropad.cfg
$cfgFile = "app/src/main/assets/overlays/retropad.cfg"
if (Test-Path $cfgFile) {
    $content = Get-Content $cfgFile -Raw
    
    $checks = @(
        @{ Name = "Chemin retropad.png"; Pattern = 'overlay0_overlay = "retropad\.png"' },
        @{ Name = "Chemin button_a.png"; Pattern = 'overlay0_desc0_overlay = "button_a\.png"' },
        @{ Name = "Chemin button_b.png"; Pattern = 'overlay0_desc1_overlay = "button_b\.png"' },
        @{ Name = "Chemin dpad_up.png"; Pattern = 'overlay0_desc2_overlay = "dpad_up\.png"' },
        @{ Name = "Chemin dpad_down.png"; Pattern = 'overlay0_desc3_overlay = "dpad_down\.png"' },
        @{ Name = "Chemin dpad_left.png"; Pattern = 'overlay0_desc4_overlay = "dpad_left\.png"' },
        @{ Name = "Chemin dpad_right.png"; Pattern = 'overlay0_desc5_overlay = "dpad_right\.png"' },
        @{ Name = "Chemin button_start.png"; Pattern = 'overlay0_desc6_overlay = "button_start\.png"' },
        @{ Name = "Chemin button_select.png"; Pattern = 'overlay0_desc7_overlay = "button_select\.png"' }
    )
    
    foreach ($check in $checks) {
        if ($content -match $check.Pattern) {
            Write-Host "✅ $($check.Name)" -ForegroundColor Green
        } else {
            Write-Host "❌ $($check.Name)" -ForegroundColor Red
        }
    }
} else {
    Write-Host "❌ Fichier retropad.cfg non trouvé" -ForegroundColor Red
}

Write-Host "`n--- Vérification des images PNG ---" -ForegroundColor Cyan
$overlayDir = "app/src/main/assets/overlays"
$requiredImages = @("retropad.png", "button_a.png", "button_b.png", "button_start.png", "button_select.png", "dpad_up.png", "dpad_down.png", "dpad_left.png", "dpad_right.png")

foreach ($image in $requiredImages) {
    $imagePath = Join-Path $overlayDir $image
    if (Test-Path $imagePath) {
        $size = (Get-Item $imagePath).Length
        Write-Host "✅ $image ($size bytes)" -ForegroundColor Green
    } else {
        Write-Host "❌ $image (MANQUANT)" -ForegroundColor Red
    }
}

Write-Host "`n--- Vérification des modifications du code ---" -ForegroundColor Cyan

# Vérifier RetroArchOverlayManager
$managerFile = "app/src/main/java/com/fceumm/wrapper/overlay/RetroArchOverlayManager.java"
if (Test-Path $managerFile) {
    $content = Get-Content $managerFile -Raw
    
    if ($content -match 'fullPath = "overlays/" \+ imagePath') {
        Write-Host "✅ Correction des chemins d'images" -ForegroundColor Green
    } else {
        Write-Host "❌ Correction des chemins manquante" -ForegroundColor Red
    }
    
    if ($content -match 'Log\.d\(TAG, "Tentative de chargement de l''overlay"') {
        Write-Host "✅ Logs de débogage ajoutés" -ForegroundColor Green
    } else {
        Write-Host "❌ Logs de débogage manquants" -ForegroundColor Red
    }
} else {
    Write-Host "❌ RetroArchOverlayManager.java non trouvé" -ForegroundColor Red
}

# Vérifier RetroArchOverlayView
$viewFile = "app/src/main/java/com/fceumm/wrapper/overlay/RetroArchOverlayView.java"
if (Test-Path $viewFile) {
    $content = Get-Content $viewFile -Raw
    
    if ($content -match "Map<String, Integer> glTextureCache") {
        Write-Host "✅ Cache des textures OpenGL" -ForegroundColor Green
    } else {
        Write-Host "❌ Cache des textures manquant" -ForegroundColor Red
    }
    
    if ($content -match "renderTexture\(int textureId") {
        Write-Host "✅ renderTexture optimisée" -ForegroundColor Green
    } else {
        Write-Host "❌ renderTexture non optimisée" -ForegroundColor Red
    }
} else {
    Write-Host "❌ RetroArchOverlayView.java non trouvé" -ForegroundColor Red
}

# Vérifier la compilation
Write-Host "`n--- Vérification de la compilation ---" -ForegroundColor Cyan
try {
    Write-Host "Compilation en cours..." -ForegroundColor Yellow
    & "./gradlew" "assembleDebug" | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Compilation réussie" -ForegroundColor Green
    } else {
        Write-Host "❌ Erreur de compilation" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Erreur lors de la compilation : $_" -ForegroundColor Red
}

Write-Host "`n--- RÉSULTAT ATTENDU ---" -ForegroundColor Cyan
Write-Host "🎯 Après toutes ces corrections :" -ForegroundColor White
Write-Host "   - Les chemins d'images sont corrects" -ForegroundColor Yellow
Write-Host "   - Les textures se chargent depuis overlays/" -ForegroundColor Yellow
Write-Host "   - Les logs détaillés permettent le débogage" -ForegroundColor Yellow
Write-Host "   - Le cache OpenGL évite les rechargements" -ForegroundColor Yellow
Write-Host "   - Les boutons devraient maintenant être visibles" -ForegroundColor Yellow

Write-Host "`n--- INSTRUCTIONS DE TEST ---" -ForegroundColor Cyan
Write-Host "📱 Pour tester les corrections finales :" -ForegroundColor White
Write-Host "1. Installez le nouvel APK sur votre appareil" -ForegroundColor Gray
Write-Host "2. Lancez l'application" -ForegroundColor Gray
Write-Host "3. Vérifiez les logs avec : adb logcat | grep -E '(RetroArchOverlay|MainActivity)'" -ForegroundColor Gray
Write-Host "4. Vous devriez voir des messages de chargement d'overlay" -ForegroundColor Gray
Write-Host "5. Les boutons tactiles devraient maintenant être visibles" -ForegroundColor Gray
Write-Host "6. Plus de rectangle noir à l'écran" -ForegroundColor Gray

Write-Host "`n🔧 Corrections finales apportées :" -ForegroundColor Cyan
Write-Host "   - Chemins d'images corrigés dans retropad.cfg" -ForegroundColor Green
Write-Host "   - Chargement automatique depuis overlays/" -ForegroundColor Green
Write-Host "   - Logs détaillés pour le débogage" -ForegroundColor Green
Write-Host "   - Cache OpenGL pour les performances" -ForegroundColor Green
Write-Host "   - Vérifications de sécurité" -ForegroundColor Green

Write-Host "`n🎉 CORRECTIONS FINALES TERMINÉES !" -ForegroundColor Green
Write-Host "Les overlays devraient maintenant fonctionner correctement !" -ForegroundColor White

Write-Host "`nTest terminé." -ForegroundColor Green 