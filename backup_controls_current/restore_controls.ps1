# Script de restauration du système de contrôles FCEUmm Wrapper
# Ce script restaure le système de contrôles actuel depuis la sauvegarde

Write-Host "=== Restauration du Système de Contrôles FCEUmm Wrapper ===" -ForegroundColor Green

# Vérifier que nous sommes dans le bon répertoire
if (-not (Test-Path "app\src\main\java\com\fceumm\wrapper\input")) {
    Write-Host "Erreur: Ce script doit être exécuté depuis la racine du projet FCEUmm Wrapper" -ForegroundColor Red
    exit 1
}

# Créer une sauvegarde de l'état actuel avant restauration
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupDir = "backup_before_restore_$timestamp"
Write-Host "Création d'une sauvegarde de l'état actuel dans: $backupDir" -ForegroundColor Yellow

# Créer le dossier de sauvegarde
New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
New-Item -ItemType Directory -Path "$backupDir\java\input" -Force | Out-Null
New-Item -ItemType Directory -Path "$backupDir\res\layout" -Force | Out-Null

# Sauvegarder les fichiers actuels
if (Test-Path "app\src\main\java\com\fceumm\wrapper\input") {
    Copy-Item "app\src\main\java\com\fceumm\wrapper\input\*.java" "$backupDir\java\input\" -Force
    Write-Host "✓ Fichiers input actuels sauvegardés" -ForegroundColor Green
}

if (Test-Path "app\src\main\res\layout\activity_emulation.xml") {
    Copy-Item "app\src\main\res\layout\activity_emulation.xml" "$backupDir\res\layout\" -Force
    Write-Host "✓ Layout actuel sauvegardé" -ForegroundColor Green
}

if (Test-Path "app\src\main\java\com\fceumm\wrapper\MainActivity.java") {
    Copy-Item "app\src\main\java\com\fceumm\wrapper\MainActivity.java" "$backupDir\java\" -Force
    Write-Host "✓ MainActivity actuelle sauvegardée" -ForegroundColor Green
}

# Restaurer les fichiers de la sauvegarde
Write-Host "`nRestauration des fichiers de contrôles..." -ForegroundColor Cyan

# Restaurer les fichiers Java d'input
if (Test-Path "java\input\*.java") {
    Copy-Item "java\input\*.java" "app\src\main\java\com\fceumm\wrapper\input\" -Force
    Write-Host "✓ Fichiers SimpleInput restaurés" -ForegroundColor Green
} else {
    Write-Host "⚠ Aucun fichier input trouvé dans la sauvegarde" -ForegroundColor Yellow
}

# Restaurer le layout
if (Test-Path "res\layout\activity_emulation.xml") {
    Copy-Item "res\layout\activity_emulation.xml" "app\src\main\res\layout\" -Force
    Write-Host "✓ Layout restauré" -ForegroundColor Green
} else {
    Write-Host "⚠ Layout non trouvé dans la sauvegarde" -ForegroundColor Yellow
}

# Restaurer MainActivity (optionnel)
if (Test-Path "java\MainActivity.java") {
    $response = Read-Host "Voulez-vous restaurer MainActivity.java ? (o/n)"
    if ($response -eq "o" -or $response -eq "O") {
        Copy-Item "java\MainActivity.java" "app\src\main\java\com\fceumm\wrapper\" -Force
        Write-Host "✓ MainActivity restaurée" -ForegroundColor Green
    } else {
        Write-Host "MainActivity non restaurée (gardée actuelle)" -ForegroundColor Yellow
    }
}

Write-Host "`n=== Restauration terminée ===" -ForegroundColor Green
Write-Host "Sauvegarde de l'état précédent: $backupDir" -ForegroundColor Cyan
Write-Host "Pour compiler: ./gradlew clean assembleDebug installDebug" -ForegroundColor Cyan
Write-Host "Pour annuler: Copier les fichiers depuis $backupDir" -ForegroundColor Cyan 