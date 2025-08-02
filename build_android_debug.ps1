# Script de compilation Android avec debug
Write-Host "=== Compilation Android (Mode Debug) ==="

# Charger l'environnement
. .\android_env_windows.ps1

Write-Host "Vérification des fichiers..."
Write-Host "Gradlew existe: $(Test-Path 'gradlew')"
Write-Host "build.gradle existe: $(Test-Path 'build.gradle')"
Write-Host "app/build.gradle existe: $(Test-Path 'app/build.gradle')"

Write-Host "`nTest de Gradle..."
try {
    $result = & .\gradlew --version 2>&1
    Write-Host "Gradle version: $result"
} catch {
    Write-Host "Erreur Gradle: $_"
}

Write-Host "`nNettoyage..."
try {
    & .\gradlew clean --info
} catch {
    Write-Host "Erreur clean: $_"
}

Write-Host "`nCompilation..."
try {
    & .\gradlew assembleRelease --info --stacktrace
} catch {
    Write-Host "Erreur compilation: $_"
}

Write-Host "`nVérification APK..."
$APK = "app\build\outputs\apk\release\app-release.apk"
if (Test-Path $APK) {
    Write-Host "✅ APK généré : $APK"
    $size = (Get-Item $APK).Length / 1MB
    Write-Host "Taille : $([math]::Round($size, 2)) MB"
} else {
    Write-Host "❌ Erreur : APK non généré !"
    Write-Host "Recherche d'autres APKs..."
    Get-ChildItem -Path "app\build\outputs\apk" -Recurse -Filter "*.apk" -ErrorAction SilentlyContinue
}

Write-Host "=== Fin compilation ===" 