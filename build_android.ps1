# Script de compilation Android pour Windows
Write-Host "=== Compilation Android ==="

# Charger l'environnement
. .\android_env_windows.ps1

# Nettoyer et compiler
Write-Host "Nettoyage et compilation..."
& .\gradlew clean assembleRelease

# Vérifier l'APK
$APK = "app\build\outputs\apk\release\app-release.apk"
if (Test-Path $APK) {
    Write-Host "✅ APK généré : $APK"
    $size = (Get-Item $APK).Length / 1MB
    Write-Host "Taille : $([math]::Round($size, 2)) MB"
} else {
    Write-Host "❌ Erreur : APK non généré !"
    exit 1
}

Write-Host "=== Compilation terminée ===" 