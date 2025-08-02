# Script de test pour la sélection de core
Write-Host "🧪 Test de la fonctionnalité de sélection de core" -ForegroundColor Green

# Nettoyer les logs
Write-Host "📝 Nettoyage des logs..." -ForegroundColor Yellow
adb -s emulator-5554 logcat -c

# Lancer l'application
Write-Host "🚀 Lancement de l'application..." -ForegroundColor Yellow
adb -s emulator-5554 shell am start -n com.fceumm.wrapper/.MainMenuActivity

# Attendre un peu
Start-Sleep -Seconds 3

# Simuler un clic sur le bouton "Choix Core"
Write-Host "🔧 Test du bouton Choix Core..." -ForegroundColor Yellow
adb -s emulator-5554 shell input tap 540 400

# Attendre un peu
Start-Sleep -Seconds 2

# Vérifier les logs
Write-Host "📊 Vérification des logs..." -ForegroundColor Yellow
$logs = adb -s emulator-5554 logcat -s MainMenuActivity CoreSelectionActivity -d
Write-Host $logs

# Simuler un clic sur "Cores Personnalisés"
Write-Host "🔧 Sélection des cores personnalisés..." -ForegroundColor Yellow
adb -s emulator-5554 shell input tap 540 500

# Attendre un peu
Start-Sleep -Seconds 2

# Retour au menu principal
Write-Host "⬅️ Retour au menu principal..." -ForegroundColor Yellow
adb -s emulator-5554 shell input tap 540 700

# Attendre un peu
Start-Sleep -Seconds 2

# Lancer l'émulation pour tester le core sélectionné
Write-Host "🎮 Test de l'émulation avec le core sélectionné..." -ForegroundColor Yellow
adb -s emulator-5554 shell input tap 540 300

# Attendre un peu
Start-Sleep -Seconds 5

# Vérifier les logs finaux
Write-Host "📊 Logs finaux..." -ForegroundColor Yellow
$finalLogs = adb -s emulator-5554 logcat -s MainActivity CoreSelectionActivity -d
Write-Host $finalLogs

Write-Host "✅ Test terminé !" -ForegroundColor Green 