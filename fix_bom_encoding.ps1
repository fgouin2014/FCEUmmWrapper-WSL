# Script pour corriger l'encodage des fichiers Java (supprimer le BOM)
Write-Host "Correction de l'encodage des fichiers Java..." -ForegroundColor Yellow

# Liste des fichiers à corriger
$javaFiles = @(
    "app/src/main/java/com/fceumm/wrapper/input/GamepadDetectionManager.java",
    "app/src/main/java/com/fceumm/wrapper/input/InputRemappingSystem.java", 
    "app/src/main/java/com/fceumm/wrapper/input/TurboButtonSystem.java",
    "app/src/main/java/com/fceumm/wrapper/input/HapticFeedbackManager.java",
    "app/src/main/java/com/fceumm/wrapper/input/InputManager.java",
    "app/src/main/java/com/fceumm/wrapper/input/GamepadTestSuite.java"
)

foreach ($file in $javaFiles) {
    if (Test-Path $file) {
        Write-Host "Correction de $file..." -ForegroundColor Cyan
        
        # Lire le contenu du fichier
        $content = Get-Content $file -Raw -Encoding UTF8
        
        # Supprimer le BOM s'il existe
        if ($content.StartsWith([char]0xFEFF)) {
            $content = $content.Substring(1)
        }
        
        # Réécrire le fichier avec UTF-8 sans BOM
        $content | Out-File -FilePath $file -Encoding UTF8 -NoNewline
        
        Write-Host "OK - $file corrige" -ForegroundColor Green
    } else {
        Write-Host "Fichier non trouve: $file" -ForegroundColor Red
    }
}

Write-Host "Correction terminee!" -ForegroundColor Green
Write-Host "Vous pouvez maintenant compiler le projet avec: ./gradlew clean assembleDebug installDebug" -ForegroundColor Yellow
