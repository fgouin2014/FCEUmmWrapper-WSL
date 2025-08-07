# Test des corrections overlay et layout
Write-Host "Test des corrections overlay et layout" -ForegroundColor Green

# Verifier les fichiers modifies
Write-Host "`nVérification des fichiers modifiés:" -ForegroundColor Yellow

# Layout portrait corrigé
$layoutPortrait = "app/src/main/res/layout-port/activity_retroarch.xml"
if (Test-Path $layoutPortrait) {
    $content = Get-Content $layoutPortrait -Raw
    if ($content -match "LinearLayout" -and $content -match "layout_weight" -and $content -match "controls_overlay_view") {
        Write-Host "Layout portrait corrigé (LinearLayout + layout_weight + controls_overlay_view)" -ForegroundColor Green
    } else {
        Write-Host "Layout portrait non corrigé" -ForegroundColor Red
    }
} else {
    Write-Host "Layout portrait introuvable" -ForegroundColor Red
}

# EmulationActivity avec les deux OverlayRenderView
$emulationActivity = "app/src/main/java/com/fceumm/wrapper/EmulationActivity.java"
if (Test-Path $emulationActivity) {
    $content = Get-Content $emulationActivity -Raw
    if ($content -match "controlsOverlayView" -and $content -match "LinearLayout\.LayoutParams") {
        Write-Host "EmulationActivity avec gestion des deux OverlayRenderView" -ForegroundColor Green
    } else {
        Write-Host "EmulationActivity non mis à jour" -ForegroundColor Red
    }
} else {
    Write-Host "EmulationActivity introuvable" -ForegroundColor Red
}

# Verifier la compilation
Write-Host "`nTest de compilation:" -ForegroundColor Yellow
try {
    $result = & ./gradlew assembleDebug 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Compilation réussie" -ForegroundColor Green
    } else {
        Write-Host "Erreur de compilation" -ForegroundColor Red
        Write-Host $result
    }
} catch {
    Write-Host "Erreur lors de la compilation" -ForegroundColor Red
}

Write-Host "`nRésumé des corrections:" -ForegroundColor Cyan
Write-Host "1. Layout portrait: LinearLayout avec layout_weight pour 50/50 split" -ForegroundColor White
Write-Host "2. Ajout de controls_overlay_view dans la zone de contrôles" -ForegroundColor White
Write-Host "3. EmulationActivity gère les deux OverlayRenderView" -ForegroundColor White
Write-Host "4. Correction des LayoutParams pour portrait (LinearLayout) et landscape (RelativeLayout)" -ForegroundColor White

Write-Host "`nPrêt pour le test sur l'appareil!" -ForegroundColor Green 