# Script de recompilation de l'application
Write-Host "=== Recompilation de l'application FCEUmm ==="

# Charger l'environnement
. .\android_env_windows.ps1

Write-Host "1. Nettoyage complet..."
Remove-Item -Path "app\build" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path ".gradle" -Recurse -Force -ErrorAction SilentlyContinue

Write-Host "`n2. Recompilation..."
try {
    & .\gradlew clean assembleDebug --info
    Write-Host "✅ Compilation réussie"
} catch {
    Write-Host "❌ Erreur de compilation: $_"
    exit 1
}

Write-Host "`n3. Vérification des bibliothèques natives..."
$nativeLibs = Get-ChildItem -Path "app\build\intermediates" -Recurse -Filter "*fceummwrapper*" -ErrorAction SilentlyContinue
if ($nativeLibs.Count -gt 0) {
    Write-Host "✅ Bibliothèques natives trouvées:"
    foreach ($lib in $nativeLibs) {
        $size = [math]::Round($lib.Length / 1KB, 2)
        Write-Host "  - $($lib.Name) ($size KB)"
    }
} else {
    Write-Host "❌ Aucune bibliothèque native trouvée"
}

Write-Host "`n4. Réinstallation sur l'émulateur..."
& adb -s emulator-5554 uninstall com.fceumm.wrapper
& adb -s emulator-5554 install "app\build\outputs\apk\debug\app-x86_64-debug.apk"

Write-Host "`n5. Test de lancement..."
& adb -s emulator-5554 shell am start -n com.fceumm.wrapper/.MainActivity

Write-Host "`n=== Recompilation terminée ===" 