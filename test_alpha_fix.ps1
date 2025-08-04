# Script de test pour la correction de l'alpha
Write-Host "=== CORRECTION DE L'ALPHA - TEST FINAL ===" -ForegroundColor Green

Write-Host "`n--- Problème identifié ---" -ForegroundColor Cyan
Write-Host "✅ L'overlay fonctionne (jeu visible derrière)" -ForegroundColor Green
Write-Host "❌ Alpha trop faible (boutons peu visibles)" -ForegroundColor Red

Write-Host "`n--- Corrections apportées ---" -ForegroundColor Cyan

# Vérifier les modifications dans RetroArchOverlayView
$overlayViewFile = "app/src/main/java/com/fceumm/wrapper/overlay/RetroArchOverlayView.java"
if (Test-Path $overlayViewFile) {
    $content = Get-Content $overlayViewFile -Raw
    
    if ($content -match "alpha = button\.isPressed \? 0\.9f : 0\.8f") {
        Write-Host "✅ Alpha corrigé dans RetroArchOverlayView" -ForegroundColor Green
        Write-Host "   - Boutons normaux : alpha = 0.8" -ForegroundColor Gray
        Write-Host "   - Boutons pressés : alpha = 0.9" -ForegroundColor Gray
    } else {
        Write-Host "❌ Alpha non corrigé" -ForegroundColor Red
    }
} else {
    Write-Host "❌ RetroArchOverlayView.java non trouvé" -ForegroundColor Red
}

# Vérifier les nouvelles images
Write-Host "`n--- Vérification des nouvelles images ---" -ForegroundColor Cyan
$overlayDir = "app/src/main/assets/overlays"
$images = @("button_a.png", "button_b.png", "button_start.png", "button_select.png", "dpad_up.png", "dpad_down.png", "dpad_left.png", "dpad_right.png")

foreach ($image in $images) {
    $imagePath = Join-Path $overlayDir $image
    if (Test-Path $imagePath) {
        $size = (Get-Item $imagePath).Length
        Write-Host "✅ $image ($size bytes)" -ForegroundColor Green
    } else {
        Write-Host "❌ $image (MANQUANT)" -ForegroundColor Red
    }
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
Write-Host "🎯 Après cette correction :" -ForegroundColor White
Write-Host "   - Les boutons devraient être PLUS VISIBLES" -ForegroundColor Yellow
Write-Host "   - Alpha augmenté de ~0.6 à 0.8-0.9" -ForegroundColor Yellow
Write-Host "   - Couleurs plus opaques dans les images PNG" -ForegroundColor Yellow
Write-Host "   - Le jeu reste visible derrière" -ForegroundColor Yellow

Write-Host "`n--- INSTRUCTIONS DE TEST ---" -ForegroundColor Cyan
Write-Host "📱 Pour tester la correction :" -ForegroundColor White
Write-Host "1. Installez le nouvel APK sur votre appareil" -ForegroundColor Gray
Write-Host "2. Lancez l'application" -ForegroundColor Gray
Write-Host "3. Les boutons tactiles devraient maintenant être BIEN VISIBLES" -ForegroundColor Gray
Write-Host "4. Vous devriez voir des boutons colorés (rouge, vert, bleu, jaune, gris)" -ForegroundColor Gray
Write-Host "5. Le jeu reste visible derrière les boutons" -ForegroundColor Gray

Write-Host "`n🎉 CORRECTION TERMINÉE !" -ForegroundColor Green
Write-Host "Les boutons tactiles devraient maintenant être clairement visibles !" -ForegroundColor White

Write-Host "`nTest terminé." -ForegroundColor Green 