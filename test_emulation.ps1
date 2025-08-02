# Script de test de l'émulation
Write-Host "=== Test de l'émulation FCEUmm ==="

# Charger l'environnement
. .\android_env_windows.ps1

Write-Host "1. Vérification des appareils connectés..."
$devices = & adb devices
Write-Host $devices

Write-Host "`n2. Vérification de l'application..."
$appStatus = & adb -s emulator-5554 shell "ps | grep fceumm"
Write-Host $appStatus

Write-Host "`n3. Vérification des logs en temps réel..."
Write-Host "Appuyez sur Ctrl+C pour arrêter le monitoring..."

try {
    & adb -s emulator-5554 logcat -s "com.fceumm.wrapper" -v time
} catch {
    Write-Host "Monitoring arrêté"
}

Write-Host "`n4. Test de l'interface utilisateur..."
Write-Host "Vérifiez sur l'émulateur si l'application s'affiche correctement"

Write-Host "`n=== Test terminé ===" 