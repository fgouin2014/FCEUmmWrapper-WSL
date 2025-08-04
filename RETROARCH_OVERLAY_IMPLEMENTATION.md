# Implémentation des Overlays Tactiles RetroArch

## Vue d'ensemble

Cette implémentation ajoute un système d'overlays tactiles complet compatible avec RetroArch au projet FCEUmm Wrapper. Le système supporte le format de fichier .cfg de RetroArch et permet une gestion avancée des contrôles tactiles.

## Architecture

### Classes Principales

1. **RetroArchOverlayManager** - Gestionnaire principal des overlays
2. **RetroArchOverlayView** - Vue OpenGL pour le rendu des overlays
3. **RetroArchInputManager** - Gestionnaire d'input intégré
4. **OverlayPreferences** - Gestion des préférences utilisateur
5. **OverlayConfig** - Configuration d'un overlay
6. **OverlayButton** - Représentation d'un bouton d'overlay

### Structure des Fichiers

```
app/src/main/java/com/fceumm/wrapper/overlay/
├── RetroArchOverlayManager.java
├── RetroArchOverlayView.java
├── RetroArchInputManager.java
├── OverlayPreferences.java
├── OverlayConfig.java
├── OverlayButton.java
├── OverlayType.java
└── OverlayHitbox.java

app/src/main/assets/overlays/
├── retropad.cfg
├── rgpad.cfg
├── README.md
└── [images PNG]

app/src/main/res/layout/
└── activity_emulation.xml (modifié)
```

## Fonctionnalités Implémentées

### 1. Parser de Configuration RetroArch

- **Format supporté** : Compatible avec les fichiers .cfg de RetroArch
- **Paramètres** : rect, alpha_mod, range_x/y, hitbox, type
- **Mapping** : Conversion automatique vers les inputs libretro

### 2. Système de Rendu OpenGL

- **Shaders** : Vertex et fragment shaders pour le rendu des textures
- **Transparence** : Support de l'alpha blending
- **Performance** : Rendu optimisé pour mobile
- **Cache** : Gestion du cache des textures

### 3. Détection Tactile Avancée

- **Multi-touch** : Support de plusieurs doigts simultanés
- **Hitboxes** : Rectangulaires et circulaires
- **Ranges** : Zones de détection étendues
- **Diagonales** : Support des combinaisons up+left, etc.

### 4. Mapping vers libretro

- **Inputs supportés** : Tous les inputs libretro (A, B, X, Y, D-pad, etc.)
- **Combinaisons** : Support des inputs multiples (diagonales)
- **Interface native** : Communication avec le code C++ libretro

### 5. Gestion des Préférences

- **Configuration** : Sauvegarde/chargement des préférences
- **Paramètres** : Opacité, échelle, position, hitbox size
- **Overlays** : Sélection et gestion des overlays disponibles

## Format de Fichier .cfg

### Structure de Base

```ini
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

### Paramètres Supportés

- **rect** : Coordonnées normalisées "x,y,width,height"
- **alpha_mod** : Transparence (0.0-1.0)
- **range_x/y** : Extension de la zone de détection
- **hitbox** : Type de hitbox ("rect" ou "circle")
- **type** : Type de bouton ("button", "dpad", "analog")

## Mapping des Inputs

### Constantes libretro

```java
RETRO_DEVICE_ID_JOYPAD_B = 0
RETRO_DEVICE_ID_JOYPAD_Y = 1
RETRO_DEVICE_ID_JOYPAD_SELECT = 2
RETRO_DEVICE_ID_JOYPAD_START = 3
RETRO_DEVICE_ID_JOYPAD_UP = 4
RETRO_DEVICE_ID_JOYPAD_DOWN = 5
RETRO_DEVICE_ID_JOYPAD_LEFT = 6
RETRO_DEVICE_ID_JOYPAD_RIGHT = 7
RETRO_DEVICE_ID_JOYPAD_A = 8
RETRO_DEVICE_ID_JOYPAD_X = 9
// ... etc
```

### Conversion des Descriptions

- "a" → RETRO_DEVICE_ID_JOYPAD_A
- "up,left" → [RETRO_DEVICE_ID_JOYPAD_UP, RETRO_DEVICE_ID_JOYPAD_LEFT]
- "start" → RETRO_DEVICE_ID_JOYPAD_START

## Intégration avec le Projet

### Modifications de MainActivity

1. **Initialisation** : Chargement des overlays au démarrage
2. **Gestion des préférences** : Application des paramètres utilisateur
3. **Fallback** : Retour vers l'ancien système si nécessaire
4. **Nettoyage** : Libération des ressources OpenGL

### Layout Modifié

```xml
<!-- Overlay des contrôles tactiles RetroArch -->
<com.fceumm.wrapper.overlay.RetroArchOverlayView
    android:id="@+id/retroarch_overlay_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_margin="8dp"
    android:background="@android:color/transparent" />
    
<!-- Overlay des contrôles tactiles (fallback) -->
<com.fceumm.wrapper.input.SimpleOverlay
    android:id="@+id/input_overlay"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_margin="8dp"
    android:background="@android:color/transparent"
    android:alpha="0.9"
    android:visibility="gone" />
```

## Performance et Optimisations

### Rendu OpenGL

- **Shaders optimisés** : Vertex et fragment shaders efficaces
- **Cache des textures** : Évite les rechargements
- **Rendu conditionnel** : Seulement quand nécessaire
- **Blending** : Support de la transparence

### Détection Tactile

- **Hitboxes optimisées** : Calculs rapides
- **Multi-touch** : Gestion efficace des événements
- **Ranges** : Zones de détection étendues
- **Diagonales** : Support des combinaisons

## Compatibilité

### Overlays Supportés

- **RetroPad** : Overlay standard RetroArch
- **RGPAD** : Overlay populaire optimisé
- **Custom** : Overlays personnalisés
- **Fallback** : Ancien système si nécessaire

### Formats de Fichier

- **.cfg** : Configuration RetroArch
- **.png** : Images des boutons
- **Préférences** : SharedPreferences Android

## Utilisation

### Chargement d'un Overlay

```java
RetroArchOverlayManager manager = new RetroArchOverlayManager(context);
boolean success = manager.loadOverlayConfig("overlays/retropad.cfg");
```

### Gestion des Événements Tactiles

```java
RetroArchInputManager inputManager = new RetroArchInputManager(manager);
view.setOnTouchListener(inputManager);
```

### Configuration des Préférences

```java
OverlayPreferences prefs = new OverlayPreferences(context);
prefs.setOverlayEnabled(true);
prefs.setSelectedOverlay("overlays/rgpad.cfg");
prefs.setOverlayOpacity(0.8f);
```

## Tests et Validation

### Tests Recommandés

1. **Chargement des overlays** : Vérifier le parsing des .cfg
2. **Rendu OpenGL** : Tester l'affichage des textures
3. **Détection tactile** : Valider les hitboxes
4. **Mapping des inputs** : Vérifier la conversion libretro
5. **Performance** : Mesurer les FPS et la latence
6. **Compatibilité** : Tester avec différents overlays

### Métriques de Performance

- **FPS** : Maintenir 60 FPS
- **Latence** : < 16ms pour la détection tactile
- **Mémoire** : Gestion efficace du cache des textures
- **CPU** : Optimisation des calculs de hitbox

## Extensions Futures

### Fonctionnalités Avancées

1. **Sous-overlays** : Support des overlays multiples
2. **Animations** : Transitions et effets visuels
3. **Haptic feedback** : Retour haptique
4. **Custom layouts** : Éditeur d'overlay intégré
5. **Cloud sync** : Synchronisation des préférences

### Optimisations

1. **Vulkan** : Support du rendu Vulkan
2. **Metal** : Support du rendu Metal (iOS)
3. **Compression** : Compression des textures
4. **LOD** : Level of Detail pour les overlays

## Conclusion

Cette implémentation fournit un système d'overlays tactiles complet et compatible avec RetroArch. Elle offre une expérience utilisateur riche tout en maintenant la performance et la compatibilité avec le projet existant.

Le système est extensible et peut facilement être adapté pour supporter de nouveaux types d'overlays et de fonctionnalités avancées. 