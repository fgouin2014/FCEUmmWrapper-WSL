# Script de réparation automatique Android
Write-Host "=== Réparation de l'environnement Android ==="

# 1. Vérifier et corriger cmdline-tools
$cmdlinePath = "$env:USERPROFILE\AppData\Local\Android\Sdk\cmdline-tools"
if (Test-Path "$cmdlinePath\latest") {
    Write-Host "Correction du lien cmdline-tools..."
    if (Test-Path "$cmdlinePath\latest") {
        Remove-Item "$cmdlinePath\latest" -Force
    }
    #New-Item -ItemType SymbolicLink -Path "$cmdlinePath\latest" -Target "$cmdlinePath\latest-2"
    #Write-Host "Lien cmdline-tools corrigé"
}

# 2. Réparer le NDK
Write-Host "Réparation du NDK..."
$ndkPath = "$env:USERPROFILE\AppData\Local\Android\Sdk\ndk\28.2.13676358"
if (Test-Path $ndkPath) {
    # Créer source.properties manquant
    $sourceProperties = @"
Pkg.UserSrc=false
Pkg.Revision=28.2.13676358
Pkg.Dependencies=
Pkg.BuildTools=28.2.13676358
"@
    $sourceProperties | Out-File -FilePath "$ndkPath\source.properties" -Encoding UTF8
    Write-Host "source.properties créé pour le NDK"
}

# 3. Charger l'environnement
Write-Host "Chargement de l'environnement..."
. .\android_env_windows.ps1

# 4. Tester sdkmanager
Write-Host "Test de sdkmanager..."
try {
    & "$env:ANDROID_SDK_ROOT\cmdline-tools\latest\bin\sdkmanager.bat" --version
    Write-Host "sdkmanager fonctionne correctement"
} catch {
    Write-Host "ERREUR: sdkmanager ne fonctionne pas. Vérifiez l'installation Java"
}

Write-Host "=== Réparation terminée ===" 