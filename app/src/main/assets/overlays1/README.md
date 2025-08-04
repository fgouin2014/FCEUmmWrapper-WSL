# Overlays Tactiles RetroArch

Ce dossier contient les fichiers de configuration et les images pour les overlays tactiles RetroArch.

## Structure

- `retropad.cfg` - Configuration de l'overlay principal
- `button_a.png` - Image du bouton A
- `button_b.png` - Image du bouton B
- `button_start.png` - Image du bouton Start
- `button_select.png` - Image du bouton Select
- `dpad_up.png` - Image du D-pad Up
- `dpad_down.png` - Image du D-pad Down
- `dpad_left.png` - Image du D-pad Left
- `dpad_right.png` - Image du D-pad Right
- `retropad.png` - Image de fond de l'overlay

## Format de Configuration

Le format de fichier .cfg suit la spécification RetroArch :

```
overlays = 1
overlay0_overlay = "overlays/retropad.png"
overlay0_full_screen = true
overlay0_descs = 8

# Bouton A
overlay0_desc0 = "a"
overlay0_desc0_overlay = "overlays/button_a.png"
overlay0_desc0_rect = "0.8,0.7,0.15,0.15"
overlay0_desc0_alpha_mod = 1.0
overlay0_desc0_range_x = 0.02
overlay0_desc0_range_y = 0.02
overlay0_desc0_hitbox = "rect"
```

## Paramètres

- `rect` : Coordonnées normalisées "x,y,width,height"
- `alpha_mod` : Transparence (0.0-1.0)
- `range_x/y` : Extension de la zone de détection
- `hitbox` : Type de hitbox ("rect" ou "circle")

## Compatibilité

Les overlays sont compatibles avec :
- RetroArch
- Les overlays populaires (RGPAD, etc.)
- Les différentes résolutions d'écran 