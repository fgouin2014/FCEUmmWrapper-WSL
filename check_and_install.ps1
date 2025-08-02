# Script de vérification et installation des APKs
Write-Host "=== Vérification des APKs ==="

# Charger l'environnement
. .\android_env_windows.ps1

# Chercher tous les APKs
Write-Host "Recherche des APKs générés..."
$apks = Get-ChildItem -Path "app\build\outputs\apk" -Recurse -Filter "*.apk" -ErrorAction SilentlyContinue

if ($apks.Count -gt 0) {
    Write-Host "✅ APKs trouvés :"
    foreach ($apk in $apks) {
        $size = [math]::Round($apk.Length / 1MB, 2)
        Write-Host "  - $($apk.Name) ($size MB)"
    }
    
    # Installer l'APK principal (arm64-v8a)
    $mainApk = $apks | Where-Object { $_.Name -like "*arm64-v8a*" } | Select-Object -First 1
    if ($mainApk) {
        Write-Host "`nInstallation de $($mainApk.Name)..."
        try {
            & "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe" install -r $mainApk.FullName
            Write-Host "✅ APK installé avec succès !"
        } catch {
            Write-Host "❌ Erreur d'installation : $_"
        }
    }
} else {
    Write-Host "❌ Aucun APK trouvé !"
}

Write-Host "`n=== Fin vérification ===" 