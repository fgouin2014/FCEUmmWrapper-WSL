# Script de diagnostic de l'application FCEUmm
Write-Host "=== Diagnostic de l'application FCEUmm ==="

# Charger l'environnement
. .\android_env_windows.ps1

Write-Host "1. Vérification de l'installation..."
$package = & adb -s emulator-5554 shell pm list packages | findstr fceumm
Write-Host "Package installé: $package"

Write-Host "`n2. Vérification des bibliothèques natives..."
$nativeLibs = & adb -s emulator-5554 shell pm dump com.fceumm.wrapper | findstr "nativeLibrary"
Write-Host "Bibliothèques natives: $nativeLibs"

Write-Host "`n3. Vérification des assets..."
$assets = & adb -s emulator-5554 shell run-as com.fceumm.wrapper ls -la files/
Write-Host "Fichiers dans l'app: $assets"

Write-Host "`n4. Test de lancement avec logs détaillés..."
& adb -s emulator-5554 shell am force-stop com.fceumm.wrapper
Start-Sleep -Seconds 2

# Lancer l'app et capturer les logs en temps réel
Write-Host "Lancement de l'application..."
& adb -s emulator-5554 shell am start -n com.fceumm.wrapper/.MainActivity

Start-Sleep -Seconds 5

Write-Host "`n5. Logs de l'application:"
& adb -s emulator-5554 logcat -s "MainActivity" -s "AndroidRuntime" -s "System.err" -d -t 100

Write-Host "`n6. Vérification des processus..."
$processes = & adb -s emulator-5554 shell ps | findstr fceumm
Write-Host "Processus FCEUmm: $processes"

Write-Host "`n=== Diagnostic terminé ===" 