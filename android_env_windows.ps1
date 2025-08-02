# Configuration Android pour Windows PowerShell
# À exécuter: .\android_env_windows.ps1

# Variables d'environnement Android
$env:ANDROID_SDK_ROOT = "$env:USERPROFILE\AppData\Local\Android\Sdk"
$env:ANDROID_NDK_ROOT = "$env:ANDROID_SDK_ROOT\ndk\28.2.13676358"
$env:ANDROID_HOME = "$env:ANDROID_SDK_ROOT"

# Détection automatique de Java
$javaPaths = @(
    "C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot",
    "C:\Program Files\OpenJDK\jdk-17.0.1",
    "C:\Program Files\Java\jdk-17",
    "C:\Program Files\Java\jdk-11"
)

$javaFound = $false
foreach ($path in $javaPaths) {
    if (Test-Path $path) {
        $env:JAVA_HOME = $path
        $javaFound = $true
        Write-Host "Java trouvé: $path"
        break
    }
}

if (-not $javaFound) {
    Write-Host "ERREUR: Java JDK non trouvé. Installez Java JDK 17 ou 11"
    Write-Host "Téléchargez depuis: https://adoptium.net/temurin/releases/?version=17"
    exit 1
}

# Ajouter les outils Android au PATH
$env:PATH = "$env:ANDROID_SDK_ROOT\cmdline-tools\latest\bin;$env:ANDROID_SDK_ROOT\platform-tools;$env:PATH"

# Configuration des architectures cibles
$env:TARGET_ABIS = "armeabi-v7a arm64-v8a x86 x86_64"
$env:ANDROID_API_LEVEL = "36"

Write-Host "Configuration Android chargée :"
Write-Host "ANDROID_SDK_ROOT: $env:ANDROID_SDK_ROOT"
Write-Host "ANDROID_NDK_ROOT: $env:ANDROID_NDK_ROOT"
Write-Host "JAVA_HOME: $env:JAVA_HOME" 