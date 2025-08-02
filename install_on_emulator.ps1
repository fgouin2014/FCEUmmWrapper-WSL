# Script d'installation sur émulateur
Write-Host "=== Installation sur émulateur ==="

# Charger l'environnement
. .\android_env_windows.ps1

# Attendre que la compilation se termine
Write-Host "Attente de la fin de compilation..."

# Chercher l'APK pour émulateur (x86_64)
$emulatorApk = "app\build\outputs\apk\debug\app-x86_64-debug.apk"

# Attendre que l'APK soit disponible
$maxWait = 60
$waitCount = 0
while (-not (Test-Path $emulatorApk) -and $waitCount -lt $maxWait) {
    Write-Host "Attente de l'APK... ($waitCount/$maxWait)"
    Start-Sleep -Seconds 10
    $waitCount++
}

if (Test-Path $emulatorApk) {
    Write-Host "✅ APK trouvé : $emulatorApk"
    $size = [math]::Round((Get-Item $emulatorApk).Length / 1MB, 2)
    Write-Host "Taille : $size MB"
    
    # Installer sur l'émulateur
    Write-Host "Installation sur l'émulateur..."
    try {
        & "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe" -s emulator-5554 install -r $emulatorApk
        Write-Host "✅ APK installé sur l'émulateur !"
        
        # Lancer l'application
        Write-Host "Lancement de l'application..."
        & "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe" -s emulator-5554 shell am start -n com.fceumm.wrapper/.MainActivity
        Write-Host "✅ Application lancée !"
    } catch {
        Write-Host "❌ Erreur d'installation : $_"
    }
} else {
    Write-Host "❌ APK non trouvé après $maxWait secondes"
}

Write-Host "=== Fin installation ===" 