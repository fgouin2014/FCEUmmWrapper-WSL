# Script pour générer des images d'overlay RetroArch de base
# Utilise ImageMagick si disponible, sinon crée des images simples

param(
    [string]$OutputDir = "app/src/main/assets/overlays",
    [int]$Size = 64
)

Write-Host "Génération des images d'overlay RetroArch..." -ForegroundColor Green

# Créer le dossier de sortie s'il n'existe pas
if (!(Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force
    Write-Host "Dossier créé: $OutputDir"
}

# Fonction pour créer une image simple avec du texte
function Create-OverlayImage {
    param(
        [string]$FileName,
        [string]$Text,
        [string]$Color = "white",
        [string]$BackgroundColor = "transparent"
    )
    
    $FilePath = Join-Path $OutputDir $FileName
    
    # Vérifier si ImageMagick est disponible
    $magick = Get-Command magick -ErrorAction SilentlyContinue
    
    if ($magick) {
        # Utiliser ImageMagick pour créer une image avec du texte
        $cmd = "magick -size ${Size}x${Size} xc:$BackgroundColor -fill $Color -gravity center -pointsize 20 -annotate 0 '$Text' '$FilePath'"
        Invoke-Expression $cmd
        Write-Host "Image créée avec ImageMagick: $FileName"
    } else {
        # Créer un fichier PNG simple (placeholder)
        $placeholderContent = @"
# Placeholder pour $FileName
# Taille: ${Size}x${Size}
# Texte: $Text
# Couleur: $Color
# Utilisez un éditeur d'image pour créer cette image
"@
        
        $placeholderPath = $FilePath -replace '\.png$', '.txt'
        $placeholderContent | Out-File -FilePath $placeholderPath -Encoding UTF8
        Write-Host "Placeholder créé: $($placeholderPath)" -ForegroundColor Yellow
    }
}

# Créer les images d'overlay
Write-Host "Création des images d'overlay..." -ForegroundColor Cyan

# Images principales
Create-OverlayImage "retropad.png" "RetroPad" "white" "rgba(0,0,0,0.5)"
Create-OverlayImage "button_a.png" "A" "white" "rgba(255,0,0,0.8)"
Create-OverlayImage "button_b.png" "B" "white" "rgba(0,255,0,0.8)"
Create-OverlayImage "button_start.png" "START" "white" "rgba(0,0,255,0.8)"
Create-OverlayImage "button_select.png" "SELECT" "white" "rgba(255,255,0,0.8)"

# Images D-pad
Create-OverlayImage "dpad_up.png" "↑" "white" "rgba(128,128,128,0.8)"
Create-OverlayImage "dpad_down.png" "↓" "white" "rgba(128,128,128,0.8)"
Create-OverlayImage "dpad_left.png" "←" "white" "rgba(128,128,128,0.8)"
Create-OverlayImage "dpad_right.png" "→" "white" "rgba(128,128,128,0.8)"

Write-Host "Génération terminée!" -ForegroundColor Green
Write-Host "Images créées dans: $OutputDir" -ForegroundColor Cyan

# Afficher les instructions
Write-Host "`nInstructions:" -ForegroundColor Yellow
Write-Host "1. Si vous avez ImageMagick installé, les images PNG ont été créées automatiquement"
Write-Host "2. Sinon, des fichiers .txt ont été créés comme placeholders"
Write-Host "3. Remplacez les placeholders par de vraies images PNG de $Size x $Size pixels"
Write-Host "4. Les images doivent être transparentes pour un meilleur rendu"
Write-Host "5. Testez l'overlay avec l'application"

# Vérifier les fichiers créés
Write-Host "`nFichiers créés:" -ForegroundColor Cyan
Get-ChildItem $OutputDir | ForEach-Object {
    Write-Host "  - $($_.Name)"
} 