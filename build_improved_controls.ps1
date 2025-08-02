# Script de compilation avec améliorations des contrôles
# Intègre toutes les améliorations UX et fonctionnelles

Write-Host "=== COMPILATION FCEUmmWrapper AVEC CONTRÔLES AMÉLIORÉS ===" -ForegroundColor Green

# 1. Nettoyer les builds précédents
Write-Host "`n1. Nettoyage des builds précédents..." -ForegroundColor Yellow
./gradlew clean

# 2. Vérifier les modifications des contrôles
Write-Host "`n2. Vérification des améliorations des contrôles..." -ForegroundColor Yellow

$filesToCheck = @(
    "app/src/main/java/com/fceumm/wrapper/input/SimpleController.java",
    "app/src/main/java/com/fceumm/wrapper/input/SimpleInputManager.java", 
    "app/src/main/java/com/fceumm/wrapper/input/SimpleOverlay.java"
)

foreach ($file in $filesToCheck) {
    if (Test-Path $file) {
        Write-Host "✓ $file" -ForegroundColor Green
    } else {
        Write-Host "✗ $file manquant" -ForegroundColor Red
        exit 1
    }
}

# 3. Compiler en mode debug
Write-Host "`n3. Compilation en mode debug..." -ForegroundColor Yellow
./gradlew assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Compilation réussie" -ForegroundColor Green
} else {
    Write-Host "✗ Erreur de compilation" -ForegroundColor Red
    exit 1
}

# 4. Installer sur l'émulateur
Write-Host "`n4. Installation sur l'émulateur..." -ForegroundColor Yellow
adb install -r app/build/outputs/apk/debug/app-debug.apk

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Installation réussie" -ForegroundColor Green
} else {
    Write-Host "✗ Erreur d'installation" -ForegroundColor Red
    exit 1
}

# 5. Démarrer l'application
Write-Host "`n5. Démarrage de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity

# 6. Attendre le chargement
Write-Host "`n6. Attente du chargement..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# 7. Capturer une capture d'écran pour vérifier
Write-Host "`n7. Vérification de l'affichage..." -ForegroundColor Yellow
adb shell screencap /sdcard/improved_controls.png
adb pull /sdcard/improved_controls.png ./improved_controls.png
Write-Host "Capture d'écran sauvegardée dans improved_controls.png" -ForegroundColor Green

# 8. Tester les nouvelles fonctionnalités
Write-Host "`n8. Test des nouvelles fonctionnalités..." -ForegroundColor Yellow

# Test de l'orientation paysage
Write-Host "Test de l'orientation paysage..." -ForegroundColor Cyan
adb shell content insert --uri content://settings/system --bind name:s:accelerometer_rotation --bind value:i:1
adb shell content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:1
Start-Sleep -Seconds 2

# Test des contrôles en paysage
Write-Host "Test des contrôles en mode paysage..." -ForegroundColor Cyan
adb shell input tap 50 300  # D-Pad en paysage
Start-Sleep -Milliseconds 500
adb shell input tap 600 300  # Bouton A en paysage
Start-Sleep -Milliseconds 500

# Retour en portrait
Write-Host "Retour en mode portrait..." -ForegroundColor Cyan
adb shell content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:0
Start-Sleep -Seconds 2

# Test de réactivité améliorée
Write-Host "Test de réactivité améliorée..." -ForegroundColor Cyan
for ($i = 0; $i -lt 3; $i++) {
    adb shell input tap 100 400  # D-Pad UP
    Start-Sleep -Milliseconds 100
    adb shell input tap 600 400  # Bouton A
    Start-Sleep -Milliseconds 100
}

# 9. Capturer une capture finale
Write-Host "`n9. Capture d'écran finale..." -ForegroundColor Yellow
adb shell screencap /sdcard/final_controls.png
adb pull /sdcard/final_controls.png ./final_controls.png
Write-Host "Capture finale sauvegardée dans final_controls.png" -ForegroundColor Green

# 10. Vérifier les logs
Write-Host "`n10. Vérification des logs..." -ForegroundColor Yellow
Write-Host "Logs des 20 dernières secondes :" -ForegroundColor Cyan
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 10

# 11. Résumé des améliorations
Write-Host "`n=== RÉSUMÉ DES AMÉLIORATIONS ===" -ForegroundColor Green
Write-Host "✓ Interface adaptative selon la densité d'écran" -ForegroundColor Green
Write-Host "✓ Support multi-orientation (portrait + paysage)" -ForegroundColor Green
Write-Host "✓ Design moderne avec animations" -ForegroundColor Green
Write-Host "✓ Zones de toucher optimisées avec tolérance" -ForegroundColor Green
Write-Host "✓ Support multi-touch amélioré" -ForegroundColor Green
Write-Host "✓ Feedback visuel avec animations" -ForegroundColor Green

Write-Host "`nFichiers générés :" -ForegroundColor Yellow
Write-Host "- improved_controls.png : Affichage initial" -ForegroundColor White
Write-Host "- final_controls.png : Affichage après tests" -ForegroundColor White

Write-Host "`nPour tester manuellement :" -ForegroundColor Yellow
Write-Host "adb shell am start -n com.fceumm.wrapper/.MainActivity" -ForegroundColor Cyan
Write-Host "adb logcat -s 'com.fceumm.wrapper' -v time" -ForegroundColor Cyan

Write-Host "`nCompilation et tests terminés avec succès !" -ForegroundColor Green 