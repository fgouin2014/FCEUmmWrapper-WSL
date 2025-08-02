# Script de compilation Windows natif
Write-Host "=== Compilation Windows ==="

# Charger l'environnement
. .\android_env_windows.ps1

# Vérifier que gradlew existe
if (-not (Test-Path "gradlew")) {
    Write-Host "❌ gradlew non trouvé !"
    exit 1
}

Write-Host "Nettoyage..."
try {
    # Utiliser PowerShell pour exécuter gradlew
    $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
    $env:ANDROID_HOME = "C:\Users\Quentin\AppData\Local\Android\Sdk"
    
    # Exécuter gradlew avec PowerShell
    & .\gradlew clean --no-daemon
    Write-Host "✅ Nettoyage terminé"
} catch {
    Write-Host "❌ Erreur nettoyage: $_"
    exit 1
}

Write-Host "Compilation..."
try {
    & .\gradlew assembleDebug --no-daemon
    Write-Host "✅ Compilation terminée"
} catch {
    Write-Host "❌ Erreur compilation: $_"
    exit 1
}

# Vérifier l'APK
$APK = "app\build\outputs\apk\debug\app-arm64-v8a-debug.apk"
if (Test-Path $APK) {
    Write-Host "✅ APK généré : $APK"
    $size = [math]::Round((Get-Item $APK).Length / 1MB, 2)
    Write-Host "Taille : $size MB"
    
    # Installer sur l'appareil physique
    Write-Host "Installation sur appareil physique..."
    try {
        & "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe" -s R5CT11TCQ1W install -r $APK
        Write-Host "✅ APK installé sur appareil physique !"
    } catch {
        Write-Host "❌ Erreur installation appareil: $_"
    }
    
    # Installer sur émulateur
    $emulatorApk = "app\build\outputs\apk\debug\app-x86_64-debug.apk"
    if (Test-Path $emulatorApk) {
        Write-Host "Installation sur émulateur..."
        try {
            & "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe" -s emulator-5554 install -r $emulatorApk
            Write-Host "✅ APK installé sur émulateur !"
            
            # Lancer l'application
            & "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe" -s emulator-5554 shell am start -n com.fceumm.wrapper/.MainActivity
            Write-Host "✅ Application lancée sur émulateur !"
        } catch {
            Write-Host "❌ Erreur installation émulateur: $_"
        }
    }
} else {
    Write-Host "❌ Erreur : APK non généré !"
    exit 1
}

Write-Host "=== Compilation terminée ===" 