# Implémentation du Mode Split Screen

## Vue d'ensemble

L'implémentation du mode Split Screen résout les problèmes d'affichage en séparant la zone de jeu de la zone des contrôles, offrant une meilleure visibilité et une expérience utilisateur optimisée.

## Architecture

### 1. Structures de données

#### `SplitScreenViewport` (RetroArchExactStructures.java)
```java
public static class SplitScreenViewport {
    public int x, y;           // Position du viewport
    public int width, height;  // Taille du viewport
    public float game_scale;   // Échelle du jeu
    public boolean enabled;    // Mode activé
}
```

#### `SplitScreenConfig` (RetroArchExactStructures.java)
```java
public static class SplitScreenConfig {
    public SplitScreenViewport gameViewport;
    public SplitScreenViewport overlayViewport;
    public float overlayOpacity;
    public boolean autoAdjustViewport;
    public String layoutType; // "portrait", "landscape", "custom"
}
```

### 2. Gestionnaire principal

#### `SplitScreenManager.java`
- Gère la configuration du mode Split Screen
- Calcule les viewports de jeu et d'overlay
- Convertit les coordonnées touch
- Dessine les zones de debug

### 3. Intégration dans RetroArchOverlayLoader

#### Nouvelles méthodes ajoutées :
- `setSplitScreenMode(boolean enabled)`
- `isSplitScreenMode()`
- `loadSplitScreenConfig(String cfgFileName)`
- `getGameViewport()`
- `getOverlayViewport()`
- `setSplitScreenLayoutType(String layoutType)`

## Modes de disposition

### 1. Mode Full Screen Overlay (existant)
```ini
overlay0_full_screen = true
```
- Contrôles transparents par-dessus le jeu
- Jeu occupe tout l'écran
- Contrôles avec alpha/transparence

### 2. Mode Split Screen (nouveau)
```ini
overlay0_full_screen = false
custom_viewport_width = 1280
custom_viewport_height = 720
split_screen_enabled = true
```

#### Layout Portrait :
```
┌─────────────────────┐
│                     │
│    Zone de jeu      │ ← 70% de l'écran
│    (viewport)       │
│                     │
├─────────────────────┤ ← Séparation
│ D-PAD    A B X Y    │ ← 30% pour contrôles
│       SELECT START  │
└─────────────────────┘
```

#### Layout Landscape :
```
┌─────────────────────────────────────┐
│ D-PAD │    Zone de jeu    │ A B X Y │
│   ↑   │   (viewport)      │   ○ ○   │
│ ← + → │                   │   ○ ○   │
│   ↓   │                   │    □    │
│SELECT │                   │  START  │
└─────────────────────────────────────┘
```

## Configuration via fichier CFG

### Fichier : `nes_split_screen.cfg`

```ini
# Configuration Split Screen pour NES
overlays = 4

# Configuration du viewport de jeu
custom_viewport_width = 1280
custom_viewport_height = 720
custom_viewport_x = 0
custom_viewport_y = 0
split_screen_enabled = true
overlay_opacity = 0.9

# Overlay 0 - Split Screen Portrait
overlay0_full_screen = false
overlay0_normalized = true
overlay0_name = "split-portrait"
overlay0_range_mod = 1.2
overlay0_alpha_mod = 1.5

# Positionnement des boutons dans la zone overlay
overlay0_desc0 = "left,0.12778,0.85625,radial,0.09630,0.04635"
overlay0_desc0_overlay = dpad-left.png
# ... autres boutons
```

## Fonctions RetroArch

### `createSplitScreenConfig()`
Crée une configuration Split Screen par défaut selon le type de layout.

### `applySplitScreenViewport()`
Applique le viewport Split Screen et ajuste les coordonnées des boutons.

### `loadSplitScreenConfigFromCfg()`
Charge la configuration depuis un fichier CFG.

## Utilisation dans le code

### 1. Activation du mode Split Screen
```java
RetroArchOverlayLoader overlayLoader = findViewById(R.id.retroarch_overlay_loader);
overlayLoader.setSplitScreenMode(true);
```

### 2. Chargement d'une configuration
```java
overlayLoader.loadSplitScreenConfig("nes_split_screen.cfg");
```

### 3. Changement de layout
```java
overlayLoader.setSplitScreenLayoutType("portrait");
overlayLoader.setSplitScreenLayoutType("landscape");
```

### 4. Obtenir les viewports
```java
Rect gameViewport = overlayLoader.getGameViewport();
Rect overlayViewport = overlayLoader.getOverlayViewport();
```

## Avantages

### 1. Résolution des problèmes d'affichage
- Zone de jeu dédiée sans superposition
- Contrôles toujours visibles
- Pas d'interférence avec l'image du jeu

### 2. Meilleure expérience utilisateur
- Contrôles plus accessibles
- Zone de jeu optimisée
- Adaptabilité selon l'orientation

### 3. Compatibilité RetroArch
- Utilise les mêmes structures que RetroArch
- Configuration via fichiers CFG
- Coordonnées normalisées

### 4. Flexibilité
- Layouts personnalisables
- Opacité configurable
- Viewports ajustables

## Debug et développement

### Zones de debug
Le mode Split Screen dessine automatiquement les zones pour le debug :
- Zone de jeu : rectangle semi-transparent
- Zone overlay : rectangle semi-transparent
- Bordures blanches pour délimitation

### Logs
```java
Log.d(TAG, "Split Screen config créé - Layout: " + layoutType);
Log.d(TAG, "Split Screen viewport appliqué - Game scale: " + gameScale);
Log.d(TAG, "Zones Split Screen dessinées - Game: " + gameZone + ", Overlay: " + overlayZone);
```

## Tests

### Script de test : `test_split_screen.ps1`
- Vérifie la présence des fichiers
- Teste la compilation
- Valide les nouvelles méthodes
- Affiche les instructions d'utilisation

### Compilation
```powershell
./test_split_screen.ps1
```

## Migration depuis le mode Full Screen

### 1. Configuration existante
```ini
# Mode Full Screen (existant)
overlay0_full_screen = true
overlay0_normalized = true
```

### 2. Configuration Split Screen
```ini
# Mode Split Screen (nouveau)
overlay0_full_screen = false
custom_viewport_width = 1280
custom_viewport_height = 720
split_screen_enabled = true
```

### 3. Ajustement des coordonnées
Les coordonnées des boutons doivent être ajustées pour la zone overlay :
```ini
# Full Screen : coordonnées sur tout l'écran
overlay0_desc0 = "left,0.07188,0.77778,radial,0.05364,0.08148"

# Split Screen : coordonnées dans la zone overlay
overlay0_desc0 = "left,0.12778,0.85625,radial,0.09630,0.04635"
```

## Conclusion

L'implémentation du mode Split Screen offre une solution robuste aux problèmes d'affichage tout en maintenant la compatibilité avec RetroArch. Elle permet une meilleure expérience utilisateur avec des contrôles plus visibles et une zone de jeu optimisée. 