# 🔧 Correction des Coordonnées d'Overlay RetroArch

## 🎯 **Problème identifié**

Les boutons d'overlay apparaissaient aux mauvaises positions :
- **Portrait** : D-pad et boutons A/B/X/Y en haut, Start/Select en bas
- **Landscape** : Même problème d'inversion

## 🔍 **Analyse du problème**

### Coordonnées dans les fichiers CFG RetroArch
```
overlay0_desc0 = "left,0.07188,0.77778,radial,0.05364,0.08148"
overlay0_desc8 = "a,0.91667,0.85185,radial,0.05000,0.08889"
overlay0_desc12 = "start,0.63958,0.90000,radial,0.04583,0.03889"
```

**Interprétation :**
- **D-pad** : Y ≈ 0.77 (bas de l'écran)
- **Boutons A/B** : Y ≈ 0.85 (bas de l'écran)  
- **Start/Select** : Y ≈ 0.90 (bas de l'écran)

### Erreur initiale
J'avais appliqué une inversion de l'axe Y `(1.0f - desc.mod_y)` pensant que les coordonnées RetroArch étaient inversées par rapport à Android.

**Résultat :** Double inversion → positions incorrectes

## ✅ **Solution appliquée**

### 1. **Suppression de l'inversion dans le rendu**
```java
// AVANT (incorrect) :
float pixelY = (1.0f - desc.mod_y) * canvasHeight;

// APRÈS (correct) :
float pixelY = desc.mod_y * canvasHeight; // Coordonnées directes
```

### 2. **Suppression de l'inversion dans les hitboxes**
```java
// AVANT (incorrect) :
desc.y_hitbox = (1.0f - desc.mod_y);

// APRÈS (correct) :
desc.y_hitbox = desc.mod_y; // Coordonnées directes
```

## 🎮 **Résultat attendu**

Maintenant les boutons devraient apparaître aux bonnes positions :
- **D-pad** : Bas de l'écran (Y ≈ 0.77)
- **Boutons A/B/X/Y** : Bas de l'écran (Y ≈ 0.85)
- **Start/Select** : Bas de l'écran (Y ≈ 0.90)
- **Menu** : Haut de l'écran (Y ≈ 0.09)

## 📝 **Leçon apprise**

Les coordonnées dans les fichiers CFG RetroArch sont **déjà dans le système de coordonnées Android** (0 = haut, 1 = bas), donc aucune transformation n'est nécessaire.

## 🧪 **Test**

L'application a été recompilée et installée. Les overlays devraient maintenant s'afficher correctement en portrait et landscape.
