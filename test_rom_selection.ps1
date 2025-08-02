# Script pour tester la sélection de ROM
Write-Host "🎮 Test de la sélection de ROM" -ForegroundColor Green

# Nettoyer les logs
Write-Host "📝 Nettoyage des logs..." -ForegroundColor Yellow
adb -s emulator-5554 logcat -c

# Lancer l'application
Write-Host "🚀 Lancement de l'application..." -ForegroundColor Yellow
adb -s emulator-5554 shell am start -n com.fceumm.wrapper/.MainMenuActivity

# Attendre un peu
Start-Sleep -Seconds 3

# Aller dans la sélection de ROM
Write-Host "📁 Accès à la sélection de ROM..." -ForegroundColor Yellow
adb -s emulator-5554 shell input tap 540 200

# Attendre un peu
Start-Sleep -Seconds 3

# Vérifier les logs de sélection de ROM
Write-Host "📊 Logs de sélection de ROM..." -ForegroundColor Yellow
$romLogs = adb -s emulator-5554 logcat -s RomSelectionActivity -d
Write-Host $romLogs

# Sélectionner la première ROM (si disponible)
Write-Host "🎯 Sélection de la première ROM..." -ForegroundColor Yellow
adb -s emulator-5554 shell input tap 540 300

# Attendre que l'émulation démarre
Start-Sleep -Seconds 10

# Vérifier les logs de MainActivity
Write-Host "📊 Logs de MainActivity..." -ForegroundColor Yellow
$mainLogs = adb -s emulator-5554 logcat -s MainActivity -d
Write-Host $mainLogs

Write-Host "✅ Test terminé !" -ForegroundColor Green 