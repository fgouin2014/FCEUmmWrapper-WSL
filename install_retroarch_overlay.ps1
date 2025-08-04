# Script pour installer l'APK avec l'overlay RetroArch
Write-Host "=== INSTALLATION DE L'APK AVEC OVERLAY RETROARCH ===" -ForegroundColor Green

# Vérifier si un appareil est connecté
Write-Host "Vérification des appareils connectés..." -ForegroundColor Yellow
adb devices

# Installer l'APK
Write-Host "Installation de l'APK..." -ForegroundColor Yellow
adb install -r app/build/outputs/apk/debug/app-arm64-v8a-debug.apk

Write-Host "=== INSTALLATION TERMINÉE ===" -ForegroundColor Green 