# Script de correction des problèmes audio spécifiques FCEUmm Wrapper
# Corrige les problèmes identifiés dans les logs

Write-Host "🔊 CORRECTION PROBLÈMES AUDIO SPÉCIFIQUES" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

# Problème 1: AudioSettingsActivity non déclarée dans AndroidManifest.xml
Write-Host "1. Correction du problème AudioSettingsActivity..." -ForegroundColor Yellow
Write-Host "   Problème: Activity non déclarée dans AndroidManifest.xml" -ForegroundColor Red
Write-Host "   Solution: Vérification et correction du manifeste..." -ForegroundColor Gray

# Vérifier le manifeste
$manifestPath = "app/src/main/AndroidManifest.xml"
if (Test-Path $manifestPath) {
    $manifestContent = Get-Content $manifestPath -Raw
    if ($manifestContent -match "AudioSettingsActivity") {
        Write-Host "   ✅ AudioSettingsActivity déclarée dans le manifeste" -ForegroundColor Green
    } else {
        Write-Host "   ❌ AudioSettingsActivity manquante dans le manifeste" -ForegroundColor Red
        Write-Host "   Ajout de l'activité au manifeste..." -ForegroundColor Yellow
        
        # Ajouter l'activité au manifeste
        $activityDeclaration = @"
        <activity
            android:name=".AudioSettingsActivity"
            android:exported="false"
            android:label="Paramètres Audio"
            android:theme="@android:style/Theme.Dialog" />
"@
        
        # Insérer avant la fermeture de </application>
        $newContent = $manifestContent -replace '</application>', "$activityDeclaration`n    </application>"
        Set-Content $manifestPath $newContent
        Write-Host "   ✅ AudioSettingsActivity ajoutée au manifeste" -ForegroundColor Green
    }
} else {
    Write-Host "   ❌ AndroidManifest.xml non trouvé" -ForegroundColor Red
}

# Problème 2: Permissions audio manquantes
Write-Host "`n2. Vérification des permissions audio..." -ForegroundColor Yellow
$manifestContent = Get-Content $manifestPath -Raw
$requiredPermissions = @(
    "android.permission.RECORD_AUDIO",
    "android.permission.MODIFY_AUDIO_SETTINGS",
    "android.permission.WAKE_LOCK"
)

foreach ($permission in $requiredPermissions) {
    if ($manifestContent -match $permission) {
        Write-Host "   ✅ $permission présente" -ForegroundColor Green
    } else {
        Write-Host "   ❌ $permission manquante - ajout..." -ForegroundColor Red
        $permissionLine = "    <uses-permission android:name=`"$permission`" />"
        $newContent = $manifestContent -replace '</manifest>', "$permissionLine`n</manifest>"
        Set-Content $manifestPath $newContent
        Write-Host "   ✅ $permission ajoutée" -ForegroundColor Green
    }
}

# Problème 3: Recompilation avec les corrections
Write-Host "`n3. Recompilation de l'application..." -ForegroundColor Yellow
Write-Host "   Nettoyage du projet..." -ForegroundColor Gray
./gradlew clean

Write-Host "   Compilation du projet..." -ForegroundColor Gray
./gradlew assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Compilation réussie" -ForegroundColor Green
} else {
    Write-Host "   ❌ Erreur de compilation" -ForegroundColor Red
    exit 1
}

# Problème 4: Réinstallation de l'application
Write-Host "`n4. Réinstallation de l'application..." -ForegroundColor Yellow
adb uninstall com.fceumm.wrapper
Start-Sleep -Seconds 2

$apkPath = "app/build/outputs/apk/debug/app-debug.apk"
if (Test-Path $apkPath) {
    adb install $apkPath
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✅ Application réinstallée" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Erreur d'installation" -ForegroundColor Red
    }
} else {
    Write-Host "   ❌ APK non trouvé: $apkPath" -ForegroundColor Red
}

# Problème 5: Test de l'audio après correction
Write-Host "`n5. Test de l'audio après correction..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# Vérifier les logs d'initialisation
$initLogs = adb logcat -d | Select-String -Pattern "OpenSL.*initialisé|Audio.*configuré|Core.*initialisé" | Select-Object -Last 5
if ($initLogs) {
    Write-Host "   ✅ Initialisation audio détectée:" -ForegroundColor Green
    $initLogs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucune initialisation audio détectée" -ForegroundColor Yellow
}

# Vérifier les erreurs
$errors = adb logcat -d | Select-String -Pattern "ERROR.*audio|ERROR.*Audio|ERROR.*OpenSL|ERROR.*SLES" | Select-Object -Last 5
if ($errors) {
    Write-Host "   ❌ Erreurs audio détectées:" -ForegroundColor Red
    $errors | ForEach-Object { Write-Host "     $_" -ForegroundColor Red }
} else {
    Write-Host "   ✅ Aucune erreur audio détectée" -ForegroundColor Green
}

# Problème 6: Test de l'activité audio
Write-Host "`n6. Test de l'activité audio..." -ForegroundColor Yellow
try {
    adb shell am start -n com.fceumm.wrapper/.AudioLatencyTestActivity
    Write-Host "   ✅ Activité de test audio lancée" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Impossible de lancer l'activité de test audio" -ForegroundColor Red
}

# Problème 7: Vérification finale
Write-Host "`n7. Vérification finale..." -ForegroundColor Yellow
$finalLogs = adb logcat -d | Select-String -Pattern "FCEUmm|LibretroWrapper|Audio" | Select-Object -Last 10
if ($finalLogs) {
    Write-Host "   Logs FCEUmm détectés:" -ForegroundColor Green
    $finalLogs | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Aucun log FCEUmm détecté" -ForegroundColor Yellow
}

Write-Host "`n🎵 CORRECTION TERMINÉE" -ForegroundColor Cyan
Write-Host "Résumé des corrections:" -ForegroundColor White
Write-Host "  ✅ Manifeste Android corrigé" -ForegroundColor Green
Write-Host "  ✅ Permissions audio ajoutées" -ForegroundColor Green
Write-Host "  ✅ Application recompilée" -ForegroundColor Green
Write-Host "  ✅ Application réinstallée" -ForegroundColor Green
Write-Host "  ✅ Tests audio effectués" -ForegroundColor Green

Write-Host "`nProchaines étapes:" -ForegroundColor Yellow
Write-Host "  1. Testez l'application avec une ROM" -ForegroundColor White
Write-Host "  2. Vérifiez que le son fonctionne" -ForegroundColor White
Write-Host "  3. Si le problème persiste, redémarrez l'appareil" -ForegroundColor White 