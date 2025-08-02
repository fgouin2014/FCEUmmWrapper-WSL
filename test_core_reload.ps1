# Script pour tester le rechargement du core
Write-Host "🔄 Test du rechargement du core" -ForegroundColor Green

# Nettoyer les logs
Write-Host "📝 Nettoyage des logs..." -ForegroundColor Yellow
adb -s emulator-5554 logcat -c

# Supprimer le core en cache
Write-Host "🗑️ Suppression du core en cache..." -ForegroundColor Yellow
adb -s emulator-5554 shell rm -f /data/data/com.fceumm.wrapper/files/cores/fceumm_libretro_android.so

# Lancer l'application
Write-Host "🚀 Lancement de l'application..." -ForegroundColor Yellow
adb -s emulator-5554 shell am start -n com.fceumm.wrapper/.MainMenuActivity

# Attendre un peu
Start-Sleep -Seconds 3

# Aller dans la sélection de core
Write-Host "🔧 Accès à la sélection de core..." -ForegroundColor Yellow
adb -s emulator-5554 shell input tap 540 400

# Attendre un peu
Start-Sleep -Seconds 2

# Sélectionner les cores personnalisés
Write-Host "🔧 Sélection des cores personnalisés..." -ForegroundColor Yellow
adb -s emulator-5554 shell input tap 540 500

# Attendre un peu
Start-Sleep -Seconds 2

# Retour au menu principal
Write-Host "⬅️ Retour au menu principal..." -ForegroundColor Yellow
adb -s emulator-5554 shell input tap 540 700

# Attendre un peu
Start-Sleep -Seconds 2

# Lancer l'émulation
Write-Host "🎮 Lancement de l'émulation..." -ForegroundColor Yellow
adb -s emulator-5554 shell input tap 540 300

# Attendre que l'émulation démarre
Start-Sleep -Seconds 10

# Vérifier les logs
Write-Host "📊 Vérification des logs..." -ForegroundColor Yellow
$logs = adb -s emulator-5554 logcat -s MainActivity -d
Write-Host $logs

Write-Host "✅ Test terminé !" -ForegroundColor Green 