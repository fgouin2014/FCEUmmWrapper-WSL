# Script de nettoyage simple - Suppression des anciens gestionnaires
Write-Host "Nettoyage des anciens gestionnaires..." -ForegroundColor Green

# Supprimer les anciens gestionnaires audio
$audioFiles = @(
    "app/src/main/java/com/fceumm/wrapper/audio/SimpleLibretroAudioManager.java",
    "app/src/main/java/com/fceumm/wrapper/audio/UltraLowLatencyAudioManager.java",
    "app/src/main/java/com/fceumm/wrapper/audio/CleanAudioManager.java",
    "app/src/main/java/com/fceumm/wrapper/audio/LowLatencyAudioManager.java",
    "app/src/main/java/com/fceumm/wrapper/audio/InstantAudioManager.java",
    "app/src/main/java/com/fceumm/wrapper/audio/EmulatorAudioManager.java"
)

foreach ($file in $audioFiles) {
    if (Test-Path $file) {
        Remove-Item $file -Force
        Write-Host "Supprime: $file" -ForegroundColor Red
    }
}

# Supprimer les anciens drivers de menu
$menuFiles = @(
    "app/src/main/java/com/fceumm/wrapper/OzoneMenuDriver.java",
    "app/src/main/java/com/fceumm/wrapper/XMBMenuDriver.java",
    "app/src/main/java/com/fceumm/wrapper/RGuiMenuDriver.java",
    "app/src/main/java/com/fceumm/wrapper/MaterialUIMenuDriver.java"
)

foreach ($file in $menuFiles) {
    if (Test-Path $file) {
        Remove-Item $file -Force
        Write-Host "Supprime: $file" -ForegroundColor Red
    }
}

Write-Host "Nettoyage termine!" -ForegroundColor Green
