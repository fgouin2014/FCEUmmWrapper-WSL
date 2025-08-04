# Script de vérification de la sauvegarde des contrôles FCEUmm Wrapper
# Ce script vérifie que tous les fichiers nécessaires sont présents et valides

Write-Host "=== Vérification de la Sauvegarde des Contrôles ===" -ForegroundColor Green

$backupDir = Get-Location
$errorList = @()
$warnings = @()

# Vérifier la structure des dossiers
Write-Host "`nVérification de la structure des dossiers..." -ForegroundColor Cyan

$requiredDirs = @(
    "java",
    "java\input", 
    "res",
    "res\layout"
)

foreach ($dir in $requiredDirs) {
    if (Test-Path $dir) {
        Write-Host "✓ Dossier $dir présent" -ForegroundColor Green
    } else {
        $errorList += "Dossier manquant: $dir"
        Write-Host "✗ Dossier $dir manquant" -ForegroundColor Red
    }
}

# Vérifier les fichiers Java essentiels
Write-Host "`nVérification des fichiers Java..." -ForegroundColor Cyan

$requiredJavaFiles = @(
    "java\input\SimpleController.java",
    "java\input\SimpleInputManager.java", 
    "java\input\SimpleOverlay.java",
    "java\MainActivity.java"
)

foreach ($file in $requiredJavaFiles) {
    if (Test-Path $file) {
        $size = (Get-Item $file).Length
        if ($size -gt 0) {
            Write-Host "✓ $file présent ($size bytes)" -ForegroundColor Green
        } else {
            $errorList += "Fichier vide: $file"
            Write-Host "✗ $file vide" -ForegroundColor Red
        }
    } else {
        $errorList += "Fichier manquant: $file"
        Write-Host "✗ $file manquant" -ForegroundColor Red
    }
}

# Vérifier les fichiers de layout
Write-Host "`nVérification des fichiers de layout..." -ForegroundColor Cyan

$requiredLayoutFiles = @(
    "res\layout\activity_emulation.xml"
)

foreach ($file in $requiredLayoutFiles) {
    if (Test-Path $file) {
        $size = (Get-Item $file).Length
        if ($size -gt 0) {
            Write-Host "✓ $file présent ($size bytes)" -ForegroundColor Green
        } else {
            $errorList += "Fichier vide: $file"
            Write-Host "✗ $file vide" -ForegroundColor Red
        }
    } else {
        $errorList += "Fichier manquant: $file"
        Write-Host "✗ $file manquant" -ForegroundColor Red
    }
}

# Vérifier les fichiers de documentation
Write-Host "`nVérification des fichiers de documentation..." -ForegroundColor Cyan

$documentationFiles = @(
    "README_CONTROLS_BACKUP.md",
    "COMPARISON_SYSTEMS.md",
    "restore_controls.ps1"
)

foreach ($file in $documentationFiles) {
    if (Test-Path $file) {
        $size = (Get-Item $file).Length
        if ($size -gt 0) {
            Write-Host "✓ $file présent ($size bytes)" -ForegroundColor Green
        } else {
            $warnings += "Fichier vide: $file"
            Write-Host "⚠ $file vide" -ForegroundColor Yellow
        }
    } else {
        $warnings += "Fichier de documentation manquant: $file"
        Write-Host "⚠ $file manquant" -ForegroundColor Yellow
    }
}

# Vérifier la syntaxe Java (basique)
Write-Host "`nVérification de la syntaxe Java..." -ForegroundColor Cyan

$javaFiles = Get-ChildItem "java\input\*.java" -Recurse
foreach ($file in $javaFiles) {
    $content = Get-Content $file -Raw
    if ($content -match "package com\.fceumm\.wrapper\.input") {
        Write-Host "✓ $($file.Name) - package correct" -ForegroundColor Green
    } else {
        $warnings += "Package incorrect dans: $($file.Name)"
        Write-Host "⚠ $($file.Name) - package incorrect" -ForegroundColor Yellow
    }
}

# Vérifier la syntaxe XML (basique)
Write-Host "`nVérification de la syntaxe XML..." -ForegroundColor Cyan

$xmlFiles = Get-ChildItem "res\layout\*.xml" -Recurse
foreach ($file in $xmlFiles) {
    $content = Get-Content $file -Raw
    if ($content -match "<?xml version=") {
        Write-Host "✓ $($file.Name) - XML valide" -ForegroundColor Green
    } else {
        $errorList += "XML invalide dans: $($file.Name)"
        Write-Host "✗ $($file.Name) - XML invalide" -ForegroundColor Red
    }
}

# Résumé
Write-Host "`n=== Résumé de la Vérification ===" -ForegroundColor Green

if ($errorList.Count -eq 0) {
    Write-Host "✅ Sauvegarde VALIDE - Tous les fichiers essentiels sont présents" -ForegroundColor Green
} else {
    Write-Host "❌ Sauvegarde INCOMPLÈTE - Erreurs détectées:" -ForegroundColor Red
    foreach ($errorItem in $errorList) {
        Write-Host "  - $errorItem" -ForegroundColor Red
    }
}

if ($warnings.Count -gt 0) {
    Write-Host "`n⚠ Avertissements:" -ForegroundColor Yellow
    foreach ($warning in $warnings) {
        Write-Host "  - $warning" -ForegroundColor Yellow
    }
}

# Statistiques
$totalFiles = (Get-ChildItem -Recurse -File | Measure-Object).Count
$totalSize = (Get-ChildItem -Recurse -File | Measure-Object -Property Length -Sum).Sum

Write-Host "`n📊 Statistiques de la sauvegarde:" -ForegroundColor Cyan
Write-Host "  - Nombre total de fichiers: $totalFiles" -ForegroundColor White
Write-Host "  - Taille totale: $([math]::Round($totalSize/1KB, 2)) KB" -ForegroundColor White

if ($errorList.Count -eq 0) {
    Write-Host "`n🎉 La sauvegarde est prête pour la restauration!" -ForegroundColor Green
    Write-Host "Pour restaurer: .\restore_controls.ps1" -ForegroundColor Cyan
} else {
    Write-Host "`n⚠ Veuillez corriger les erreurs avant d'utiliser cette sauvegarde" -ForegroundColor Yellow
} 