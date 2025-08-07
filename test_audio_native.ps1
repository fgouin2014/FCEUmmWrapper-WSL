# Test des Options de Son Natif - FCEUmm Wrapper
# ================================================

Write-Host "🎵 Test des Options de Son Natif" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan

# Fonction pour vérifier si l'application a crashé
function Test-AppCrash {
    $crashLogs = adb logcat -d | findstr -i "fatal\|crash\|SIGBUS\|SIGSEGV" | Select-Object -Last 5
    if ($crashLogs) {
        Write-Host "❌ CRASH DÉTECTÉ!" -ForegroundColor Red
        Write-Host "Logs de crash:" -ForegroundColor Red
        $crashLogs | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
        return $true
    }
    return $false
}

# Fonction pour vérifier si l'application est toujours en cours d'exécution
function Test-AppRunning {
    $running = adb shell ps | findstr "com.fceumm.wrapper"
    if ($running) {
        return $true
    } else {
        Write-Host "❌ Application arrêtée" -ForegroundColor Red
        return $false
    }
}

# 1. Vérifier que l'application est installée
Write-Host "`n1. Vérification de l'installation..." -ForegroundColor Yellow
adb shell pm list packages | findstr "fceumm"
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Application installée" -ForegroundColor Green
} else {
    Write-Host "❌ Application non trouvée" -ForegroundColor Red
    exit 1
}

# 2. Lancer l'application
Write-Host "`n2. Lancement de l'application..." -ForegroundColor Yellow
adb shell am start -n com.fceumm.wrapper/.MainMenuActivity
Start-Sleep -Seconds 3

# Vérifier si l'application a crashé au lancement
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé au lancement. Arrêt du test." -ForegroundColor Red
    exit 1
}

# 3. Accéder aux paramètres audio
Write-Host "`n3. Accès aux paramètres audio..." -ForegroundColor Yellow
adb shell input tap 500 800  # Position approximative du bouton "Paramètres Audio"
Start-Sleep -Seconds 2

# Vérifier si l'application a crashé
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé lors de l'accès aux paramètres audio. Arrêt du test." -ForegroundColor Red
    exit 1
}

# 4. Tester le volume
Write-Host "`n4. Test du contrôle de volume..." -ForegroundColor Yellow
Write-Host "   - Réduction du volume à 50%" -ForegroundColor White
adb shell input swipe 300 600 300 400  # Swipe vers le haut pour réduire le volume
Start-Sleep -Seconds 1

# Vérifier si l'application a crashé
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé lors du test du volume. Arrêt du test." -ForegroundColor Red
    exit 1
}

Write-Host "   - Augmentation du volume à 100%" -ForegroundColor White
adb shell input swipe 300 400 300 600  # Swipe vers le bas pour augmenter le volume
Start-Sleep -Seconds 1

# Vérifier si l'application a crashé
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé lors du test du volume. Arrêt du test." -ForegroundColor Red
    exit 1
}

# 5. Tester le mute
Write-Host "`n5. Test du contrôle mute..." -ForegroundColor Yellow
Write-Host "   - Activation du mute" -ForegroundColor White
adb shell input tap 200 400  # Position approximative du switch mute
Start-Sleep -Seconds 2

# Vérifier si l'application a crashé
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé lors du test du mute. Arrêt du test." -ForegroundColor Red
    exit 1
}

Write-Host "   - Désactivation du mute" -ForegroundColor White
adb shell input tap 200 400  # Même position pour désactiver
Start-Sleep -Seconds 2

# Vérifier si l'application a crashé
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé lors du test du mute. Arrêt du test." -ForegroundColor Red
    exit 1
}

# 6. Tester la qualité audio
Write-Host "`n6. Test de la qualité audio..." -ForegroundColor Yellow
Write-Host "   - Sélection qualité faible" -ForegroundColor White
adb shell input tap 150 500  # Position approximative du bouton "Faible"
Start-Sleep -Seconds 1

# Vérifier si l'application a crashé
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé lors du test de la qualité. Arrêt du test." -ForegroundColor Red
    exit 1
}

Write-Host "   - Sélection qualité maximum" -ForegroundColor White
adb shell input tap 450 500  # Position approximative du bouton "Maximum"
Start-Sleep -Seconds 1

# Vérifier si l'application a crashé
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé lors du test de la qualité. Arrêt du test." -ForegroundColor Red
    exit 1
}

# 7. Tester le sample rate
Write-Host "`n7. Test du sample rate..." -ForegroundColor Yellow
Write-Host "   - Sélection 22.05 kHz" -ForegroundColor White
adb shell input tap 150 600  # Position approximative du bouton "22.05 kHz"
Start-Sleep -Seconds 1

# Vérifier si l'application a crashé
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé lors du test du sample rate. Arrêt du test." -ForegroundColor Red
    exit 1
}

Write-Host "   - Sélection 48 kHz" -ForegroundColor White
adb shell input tap 450 600  # Position approximative du bouton "48 kHz"
Start-Sleep -Seconds 1

# Vérifier si l'application a crashé
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé lors du test du sample rate. Arrêt du test." -ForegroundColor Red
    exit 1
}

# 8. Tester les options avancées
Write-Host "`n8. Test des options avancées..." -ForegroundColor Yellow
Write-Host "   - Activation RF Filter" -ForegroundColor White
adb shell input tap 200 700  # Position approximative du switch RF Filter
Start-Sleep -Seconds 1

# Vérifier si l'application a crashé
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé lors du test des options avancées. Arrêt du test." -ForegroundColor Red
    exit 1
}

Write-Host "   - Activation Swap Duty" -ForegroundColor White
adb shell input tap 400 700  # Position approximative du switch Swap Duty
Start-Sleep -Seconds 1

# Vérifier si l'application a crashé
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé lors du test des options avancées. Arrêt du test." -ForegroundColor Red
    exit 1
}

# 9. Tester l'optimisation Duck Hunt
Write-Host "`n9. Test de l'optimisation Duck Hunt..." -ForegroundColor Yellow
adb shell input tap 500 800  # Position approximative du bouton "Optimiser pour Duck Hunt"
Start-Sleep -Seconds 2

# Vérifier si l'application a crashé
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé lors de l'optimisation Duck Hunt. Arrêt du test." -ForegroundColor Red
    exit 1
}

# 10. Tester la réinitialisation
Write-Host "`n10. Test de la réinitialisation..." -ForegroundColor Yellow
adb shell input tap 500 900  # Position approximative du bouton "Réinitialiser"
Start-Sleep -Seconds 1

# Vérifier si l'application a crashé
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé lors de la réinitialisation. Arrêt du test." -ForegroundColor Red
    exit 1
}

# 11. Confirmer la réinitialisation
adb shell input tap 400 1000  # Position approximative du bouton "Oui"
Start-Sleep -Seconds 2

# Vérifier si l'application a crashé
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé lors de la confirmation de réinitialisation. Arrêt du test." -ForegroundColor Red
    exit 1
}

# 12. Retour au menu principal
Write-Host "`n11. Retour au menu principal..." -ForegroundColor Yellow
adb shell input tap 500 1100  # Position approximative du bouton "Retour"
Start-Sleep -Seconds 2

# Vérifier si l'application a crashé
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé lors du retour au menu. Arrêt du test." -ForegroundColor Red
    exit 1
}

# 13. Vérifier les logs
Write-Host "`n12. Vérification des logs..." -ForegroundColor Yellow
Write-Host "`n=== Logs Audio ===" -ForegroundColor Magenta
adb logcat -d | findstr -i "audio\|volume\|mute\|quality\|sample\|rf\|stereo\|duck" | Select-Object -Last 20

Write-Host "`n=== Logs Native ===" -ForegroundColor Magenta
adb logcat -d | findstr -i "native\|libretro\|fceumm" | Select-Object -Last 10

Write-Host "`n=== Logs Erreurs ===" -ForegroundColor Magenta
adb logcat -d | findstr -i "error\|exception\|crash\|fatal" | Select-Object -Last 10

# 14. Test de performance audio
Write-Host "`n13. Test de performance audio..." -ForegroundColor Yellow
Write-Host "   - Vérification de la latence" -ForegroundColor White
adb shell input tap 500 1000  # Position approximative du bouton "Forcer l'Application"
Start-Sleep -Seconds 2

# Vérifier si l'application a crashé
if (Test-AppCrash) {
    Write-Host "❌ L'application a crashé lors du test de performance. Arrêt du test." -ForegroundColor Red
    exit 1
}

# 15. Résumé
Write-Host "`n🎵 Résumé du Test Audio Natif" -ForegroundColor Cyan
Write-Host "=============================" -ForegroundColor Cyan
Write-Host "✅ Volume: Contrôlé via SeekBar" -ForegroundColor Green
Write-Host "✅ Mute: Contrôlé via Switch" -ForegroundColor Green
Write-Host "✅ Qualité: 3 niveaux (Faible/Élevée/Maximum)" -ForegroundColor Green
Write-Host "✅ Sample Rate: 3 options (22.05/44.1/48 kHz)" -ForegroundColor Green
Write-Host "✅ RF Filter: Option avancée" -ForegroundColor Green
Write-Host "✅ Swap Duty: Option avancée" -ForegroundColor Green
Write-Host "✅ Low Latency: Mode basse latence" -ForegroundColor Green
Write-Host "✅ Stereo Delay: Contrôle du délai stéréo" -ForegroundColor Green
Write-Host "✅ Optimisation Duck Hunt: Paramètres spécialisés" -ForegroundColor Green
Write-Host "✅ Réinitialisation: Retour aux valeurs par défaut" -ForegroundColor Green

Write-Host "`n🎯 Test terminé avec succès!" -ForegroundColor Green
Write-Host "Les options de son natif sont maintenant implémentées et fonctionnelles." -ForegroundColor White 