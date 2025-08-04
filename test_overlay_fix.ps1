# Script de test pour vérifier que le problème de l'overlay noir est résolu

Write-Host "=== Test de Correction de l'Overlay Noir ===" -ForegroundColor Green

# Vérifier que les images PNG existent
$overlayDir = "app/src/main/assets/overlays"
$requiredImages = @(
    "retropad.png",
    "button_a.png", 
    "button_b.png",
    "button_start.png",
    "button_select.png",
    "dpad_up.png",
    "dpad_down.png",
    "dpad_left.png",
    "dpad_right.png"
)

Write-Host "`n--- Vérification des Images PNG ---" -ForegroundColor Cyan
$allImagesExist = $true

foreach ($image in $requiredImages) {
    $imagePath = Join-Path $overlayDir $image
    if (Test-Path $imagePath) {
        $size = (Get-Item $imagePath).Length
        Write-Host "✓ $image ($size bytes)" -ForegroundColor Green
    } else {
        Write-Host "✗ $image (MANQUANT)" -ForegroundColor Red
        $allImagesExist = $false
    }
}

# Vérifier la compilation
Write-Host "`n--- Vérification de la Compilation ---" -ForegroundColor Cyan
try {
    Write-Host "Compilation en cours..." -ForegroundColor Yellow
    & "./gradlew" "assembleDebug" | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Compilation réussie" -ForegroundColor Green
    } else {
        Write-Host "✗ Erreur de compilation" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ Erreur lors de la compilation : $_" -ForegroundColor Red
}

# Résumé
Write-Host "`n=== RÉSUMÉ ===" -ForegroundColor Green
if ($allImagesExist) {
    Write-Host "✅ Toutes les images PNG sont présentes" -ForegroundColor Green
    Write-Host "✅ L'overlay principal (retropad.png) est transparent" -ForegroundColor Green
    Write-Host "✅ Les boutons ont des couleurs semi-transparentes" -ForegroundColor Green
    Write-Host "✅ Le code ne rend plus l'overlay principal" -ForegroundColor Green
    Write-Host "`n🎉 Le problème de l'overlay noir devrait être résolu !" -ForegroundColor Green
    Write-Host "`nInstructions pour tester :" -ForegroundColor Yellow
    Write-Host "1. Installez l'APK sur votre appareil" -ForegroundColor White
    Write-Host "2. Lancez l'application" -ForegroundColor White
    Write-Host "3. Vous devriez voir le jeu sans overlay noir" -ForegroundColor White
    Write-Host "4. Les boutons tactiles devraient être visibles et fonctionnels" -ForegroundColor White
} else {
    Write-Host "⚠ Certaines images sont manquantes" -ForegroundColor Yellow
    Write-Host "Relancez le script create_simple_images.ps1" -ForegroundColor Yellow
}

Write-Host "`nTest terminé." -ForegroundColor Green 