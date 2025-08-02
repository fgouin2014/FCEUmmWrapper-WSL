# Script de debug des zones de contrôle
# Affiche les zones de toucher et teste différentes coordonnées

Write-Host "=== DEBUG DES ZONES DE CONTRÔLE ===" -ForegroundColor Red

# 1. Démarrer l'application
Write-Host "`n1. Démarrage de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainActivity
Start-Sleep -Seconds 3

# 2. Nettoyer les logs
Write-Host "`n2. Nettoyage des logs..." -ForegroundColor Yellow
adb logcat -c

# 3. Test avec différentes coordonnées pour trouver les zones
Write-Host "`n3. Test avec différentes coordonnées..." -ForegroundColor Yellow

# Test en bas à gauche (D-Pad probable)
Write-Host "Test zone D-Pad (bas gauche)..." -ForegroundColor Cyan
for ($x = 50; $x -le 200; $x += 50) {
    for ($y = 400; $y -le 600; $y += 50) {
        Write-Host "Touch: $x, $y" -ForegroundColor Gray
        adb shell input tap $x $y
        Start-Sleep -Milliseconds 200
    }
}

# Test en bas à droite (Boutons A/B probable)
Write-Host "Test zone Boutons A/B (bas droite)..." -ForegroundColor Cyan
for ($x = 500; $x -le 700; $x += 50) {
    for ($y = 400; $y -le 600; $y += 50) {
        Write-Host "Touch: $x, $y" -ForegroundColor Gray
        adb shell input tap $x $y
        Start-Sleep -Milliseconds 200
    }
}

# Test en bas centre (START/SELECT probable)
Write-Host "Test zone START/SELECT (bas centre)..." -ForegroundColor Cyan
for ($x = 200; $x -le 500; $x += 50) {
    for ($y = 600; $y -le 800; $y += 50) {
        Write-Host "Touch: $x, $y" -ForegroundColor Gray
        adb shell input tap $x $y
        Start-Sleep -Milliseconds 200
    }
}

# 4. Vérifier les logs
Write-Host "`n4. Vérification des logs..." -ForegroundColor Yellow
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 20

# 5. Test spécifique des zones probables
Write-Host "`n5. Test spécifique des zones probables..." -ForegroundColor Yellow

# Test D-Pad avec coordonnées plus précises
Write-Host "Test D-Pad précis..." -ForegroundColor Cyan
adb shell input tap 90 420  # UP
Start-Sleep -Milliseconds 500
adb shell input tap 90 480  # DOWN
Start-Sleep -Milliseconds 500
adb shell input tap 70 450  # LEFT
Start-Sleep -Milliseconds 500
adb shell input tap 110 450 # RIGHT
Start-Sleep -Milliseconds 500

# Test boutons avec coordonnées plus précises
Write-Host "Test boutons précis..." -ForegroundColor Cyan
adb shell input tap 550 420  # A
Start-Sleep -Milliseconds 500
adb shell input tap 630 420  # B
Start-Sleep -Milliseconds 500

# Test START/SELECT avec coordonnées plus précises
Write-Host "Test START/SELECT précis..." -ForegroundColor Cyan
adb shell input tap 340 720  # START
Start-Sleep -Milliseconds 500
adb shell input tap 440 720  # SELECT
Start-Sleep -Milliseconds 500

# 6. Résumé final
Write-Host "`n6. Résumé final des logs..." -ForegroundColor Yellow
adb logcat -d -s "com.fceumm.wrapper" | Select-Object -Last 10

Write-Host "`nDebug terminé !" -ForegroundColor Green 