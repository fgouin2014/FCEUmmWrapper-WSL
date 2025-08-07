# Script pour copier tous les overlays importants
Write-Host "Copie de tous les overlays importants..." -ForegroundColor Green

# Créer les dossiers de destination
$destinations = @(
    "app/src/main/assets/overlays/gamepads/nes",
    "app/src/main/assets/overlays/gamepads/nes-small", 
    "app/src/main/assets/overlays/gamepads/n64",
    "app/src/main/assets/overlays/gamepads/snes",
    "app/src/main/assets/overlays/gamepads/gameboy",
    "app/src/main/assets/overlays/gamepads/retropad",
    "app/src/main/assets/overlays/gamepads/rgpad",
    "app/src/main/assets/overlays/gamepads/named-overlays"
)

foreach ($dest in $destinations) {
    if (!(Test-Path $dest)) {
        New-Item -ItemType Directory -Path $dest -Force
        Write-Host "Créé: $dest" -ForegroundColor Yellow
    }
}

# Copier les overlays
$overlays = @(
    @{Source="common-overlays_git/gamepads/nes"; Dest="app/src/main/assets/overlays/gamepads/nes"},
    @{Source="common-overlays_git/gamepads/nes-small"; Dest="app/src/main/assets/overlays/gamepads/nes-small"},
    @{Source="common-overlays_git/gamepads/n64"; Dest="app/src/main/assets/overlays/gamepads/n64"},
    @{Source="common-overlays_git/gamepads/snes"; Dest="app/src/main/assets/overlays/gamepads/snes"},
    @{Source="common-overlays_git/gamepads/gameboy"; Dest="app/src/main/assets/overlays/gamepads/gameboy"},
    @{Source="common-overlays_git/gamepads/retropad"; Dest="app/src/main/assets/overlays/gamepads/retropad"},
    @{Source="common-overlays_git/gamepads/rgpad"; Dest="app/src/main/assets/overlays/gamepads/rgpad"}
)

foreach ($overlay in $overlays) {
    if (Test-Path $overlay.Source) {
        Copy-Item -Path "$($overlay.Source)/*" -Destination $overlay.Dest -Recurse -Force
        Write-Host "Copié: $($overlay.Source) -> $($overlay.Dest)" -ForegroundColor Green
    } else {
        Write-Host "Source non trouvée: $($overlay.Source)" -ForegroundColor Red
    }
}

# Copier les overlays nommés importants
$namedOverlays = @(
    "Nintendo - Nintendo Entertainment System.cfg",
    "Nintendo - Super Nintendo Entertainment System.cfg", 
    "Nintendo - Nintendo 64.cfg",
    "Nintendo - Game Boy.cfg",
    "Nintendo - Game Boy Advance.cfg",
    "Sony - PlayStation.cfg",
    "Sega - Mega Drive - Genesis.cfg",
    "SNK - Neo Geo.cfg"
)

foreach ($overlay in $namedOverlays) {
    $source = "common-overlays_git/gamepads/Named_Overlays/$overlay"
    $dest = "app/src/main/assets/overlays/gamepads/named-overlays/$overlay"
    if (Test-Path $source) {
        Copy-Item -Path $source -Destination $dest -Force
        Write-Host "Copié overlay nommé: $overlay" -ForegroundColor Green
    } else {
        Write-Host "Overlay nommé non trouvé: $overlay" -ForegroundColor Red
    }
}

Write-Host "Copie terminée!" -ForegroundColor Green 