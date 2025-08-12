# Référence RetroArch Overlays

Ce fichier contient les informations de référence de la documentation officielle RetroArch pour les overlays.

## Source
Documentation officielle : `retroarch_docs/docs/development/retroarch/input/overlay.md`

## Format des fichiers de configuration (.cfg)

### Structure générale
```ini
overlays = 1
overlay0_overlay = "gamepads/nes/nes.png"
overlay0_full_screen = true
overlay0_descs = 0
overlay0_rect = "0.0, 0.0, 1.0, 1.0"
overlay0_alpha = "1.0"
```

### Paramètres principaux

#### `overlays`
- Nombre total d'overlays dans le fichier
- Exemple : `overlays = 1`

#### `overlayX_overlay`
- Chemin vers l'image de l'overlay
- Format : PNG recommandé
- Exemple : `overlay0_overlay = "gamepads/nes/nes.png"`

#### `overlayX_full_screen`
- Définit si l'overlay couvre tout l'écran
- Valeurs : `true` ou `false`
- Exemple : `overlay0_full_screen = true`

#### `overlayX_rect`
- Rectangle de positionnement de l'overlay
- Format : "x, y, width, height" (coordonnées normalisées 0.0-1.0)
- Exemple : `overlay0_rect = "0.0, 0.0, 1.0, 1.0"`

#### `overlayX_alpha`
- Transparence de l'overlay
- Valeurs : 0.0 (transparent) à 1.0 (opaque)
- Exemple : `overlay0_alpha = "1.0"`

### Configuration des boutons

#### `overlayX_descs`
- Nombre de descriptions de boutons
- Exemple : `overlay0_descs = 8`

#### `overlayX_desc_X`
- Description d'un bouton spécifique
- Format : "x, y, width, height, button_code"
- Exemple : `overlay0_desc_0 = "0.1, 0.8, 0.1, 0.1, a"`

### Codes de boutons RetroArch

#### Boutons d'action
- `a` - Bouton A
- `b` - Bouton B
- `x` - Bouton X
- `y` - Bouton Y

#### Boutons directionnels
- `up` - Haut
- `down` - Bas
- `left` - Gauche
- `right` - Droite

#### Boutons système
- `start` - Start
- `select` - Select
- `l` - L1/L
- `r` - R1/R
- `l2` - L2
- `r2` - R2
- `l3` - L3 (stick gauche)
- `r3` - R3 (stick droit)

#### Boutons sticks analogiques
- `left` - Stick gauche
- `right` - Stick droit

## Exemples de configurations

### Configuration NES simple
```ini
overlays = 1
overlay0_overlay = "gamepads/nes/nes.png"
overlay0_full_screen = true
overlay0_descs = 8
overlay0_desc_0 = "0.1, 0.8, 0.1, 0.1, a"
overlay0_desc_1 = "0.2, 0.8, 0.1, 0.1, b"
overlay0_desc_2 = "0.3, 0.8, 0.1, 0.1, select"
overlay0_desc_3 = "0.4, 0.8, 0.1, 0.1, start"
overlay0_desc_4 = "0.5, 0.8, 0.1, 0.1, up"
overlay0_desc_5 = "0.6, 0.8, 0.1, 0.1, down"
overlay0_desc_6 = "0.7, 0.8, 0.1, 0.1, left"
overlay0_desc_7 = "0.8, 0.8, 0.1, 0.1, right"
```

### Configuration avec plusieurs overlays
```ini
overlays = 2
overlay0_overlay = "gamepads/nes/nes_portrait.png"
overlay0_full_screen = true
overlay0_descs = 8
# ... descriptions des boutons

overlay1_overlay = "gamepads/nes/nes_landscape.png"
overlay1_full_screen = true
overlay1_descs = 8
# ... descriptions des boutons
```

## Notes importantes

1. **Coordonnées normalisées** : Toutes les coordonnées sont entre 0.0 et 1.0
2. **Format d'image** : PNG recommandé pour la transparence
3. **Résolution** : Les overlays s'adaptent automatiquement à la résolution de l'écran
4. **Orientation** : Support pour portrait et paysage
5. **Transparence** : Utilisez l'alpha pour les effets de transparence

## Liens utiles

- Documentation complète : `retroarch_docs/docs/development/retroarch/input/overlay.md`
- Exemples d'overlays : `retroarch_docs/docs/guides/`
- Standards de codage : `retroarch_docs/docs/development/coding-standards.md`
