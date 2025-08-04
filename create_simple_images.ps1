# Script pour créer des images PNG simples pour les overlays
# Utilise .NET pour créer des images basiques

Add-Type -AssemblyName System.Drawing

$outputDir = "app/src/main/assets/overlays"

# Créer le dossier s'il n'existe pas
if (!(Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir -Force
}

# Fonction pour créer une image PNG simple
function Create-SimpleImage {
    param(
        [string]$FileName,
        [int]$Width = 64,
        [int]$Height = 64,
        [System.Drawing.Color]$BackgroundColor = [System.Drawing.Color]::Transparent
    )
    
    $filePath = Join-Path $outputDir $FileName
    
    # Créer une nouvelle image
    $bitmap = New-Object System.Drawing.Bitmap($Width, $Height)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    
    # Rendre l'arrière-plan avec la couleur spécifiée
    $graphics.Clear($BackgroundColor)
    
    # Sauvegarder en PNG
    $bitmap.Save($filePath, [System.Drawing.Imaging.ImageFormat]::Png)
    
    # Libérer les ressources
    $graphics.Dispose()
    $bitmap.Dispose()
    
    Write-Host "Image créée: $FileName" -ForegroundColor Green
}

Write-Host "Création d'images PNG simples pour les overlays..." -ForegroundColor Cyan

# Créer les images principales (transparentes pour éviter l'écran noir)
Create-SimpleImage "retropad.png" 256 256 ([System.Drawing.Color]::Transparent)
Create-SimpleImage "button_a.png" 64 64 ([System.Drawing.Color]::FromArgb(200, 255, 0, 0))
Create-SimpleImage "button_b.png" 64 64 ([System.Drawing.Color]::FromArgb(200, 0, 255, 0))
Create-SimpleImage "button_start.png" 64 64 ([System.Drawing.Color]::FromArgb(200, 0, 0, 255))
Create-SimpleImage "button_select.png" 64 64 ([System.Drawing.Color]::FromArgb(200, 255, 255, 0))

# Créer les images D-pad
Create-SimpleImage "dpad_up.png" 64 64 ([System.Drawing.Color]::FromArgb(180, 128, 128, 128))
Create-SimpleImage "dpad_down.png" 64 64 ([System.Drawing.Color]::FromArgb(180, 128, 128, 128))
Create-SimpleImage "dpad_left.png" 64 64 ([System.Drawing.Color]::FromArgb(180, 128, 128, 128))
Create-SimpleImage "dpad_right.png" 64 64 ([System.Drawing.Color]::FromArgb(180, 128, 128, 128))

Write-Host "Images créées dans: $outputDir" -ForegroundColor Green
Write-Host "Vous pouvez maintenant tester l'application avec les overlays!" -ForegroundColor Yellow 