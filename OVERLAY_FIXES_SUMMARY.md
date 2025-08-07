# Corrections du système d'overlays RetroArch

## Problème initial

L'utilisateur rapportait les problèmes suivants avec le système d'overlays :

1. **Overlays non visibles au démarrage en portrait** : Les contrôles n'apparaissaient qu'après avoir touché l'écran
2. **Mauvais overlay affiché lors des changements d'orientation** : Après rotation (portrait ↔ paysage), un overlay incorrect était affiché
3. **Nécessité de toucher l'écran pour corriger** : Un toucher était requis pour afficher le bon overlay

## Causes identifiées

1. **Dimensions d'écran codées en dur** : `applyOverlayLayout()` utilisait des dimensions fixes (1280x720) au lieu des dimensions réelles
2. **Pas de gestion des changements d'orientation** : L'activité ne détectait pas les rotations d'écran
3. **Pas de redessin automatique** : Le rendu ne se faisait que lors des touches
4. **Pas de mise à jour du layout** : Les overlays n'étaient pas recalculés lors des changements d'orientation

## Corrections apportées

### 1. Détection dynamique des dimensions d'écran

**Fichier modifié** : `RetroArchOverlaySystem.java`

```java
// Variables pour les dimensions d'écran
private int screenWidth = 1280;
private int screenHeight = 720;
private boolean isLandscape = true;

// Méthode pour mettre à jour les dimensions d'écran
public void updateScreenDimensions(int width, int height) {
    this.screenWidth = width;
    this.screenHeight = height;
    this.isLandscape = width > height;
    
    // Recalculer le layout si un overlay est actif
    if (activeOverlay != null) {
        applyOverlayLayout();
    }
}
```

### 2. Gestion des changements d'orientation

**Fichier modifié** : `OverlayIntegrationActivity.java`

```java
@Override
public void onConfigurationChanged(android.content.res.Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    
    // Mettre à jour les dimensions d'écran après le changement d'orientation
    renderView.post(new Runnable() {
        @Override
        public void run() {
            updateScreenDimensions();
            // Recharger l'overlay actuel pour s'assurer qu'il s'affiche correctement
            if (currentOverlay != null) {
                loadOverlay(currentOverlay);
            }
        }
    });
}
```

### 3. Redessin automatique

**Fichier modifié** : `OverlayIntegrationActivity.java` (OverlayRenderView)

```java
@Override
protected void onDraw(Canvas canvas) {
    // ... dessin des overlays ...
    
    // Forcer le redessin continu pour s'assurer que les overlays sont visibles
    postInvalidateDelayed(100); // Redessiner toutes les 100ms
}
```

### 4. Méthode de mise à jour forcée du layout

**Fichier modifié** : `RetroArchOverlaySystem.java`

```java
// Méthode pour forcer la mise à jour du layout
public void forceLayoutUpdate() {
    if (activeOverlay != null) {
        applyOverlayLayout();
        Log.d(TAG, "Layout forcé mis à jour");
    }
}
```

### 5. Mise à jour des dimensions au démarrage

**Fichier modifié** : `OverlayIntegrationActivity.java`

```java
private void updateScreenDimensions() {
    // Obtenir les dimensions d'écran réelles
    android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    
    int width = displayMetrics.widthPixels;
    int height = displayMetrics.heightPixels;
    
    // Mettre à jour le système d'overlays
    if (overlaySystem != null) {
        overlaySystem.updateScreenDimensions(width, height);
        overlaySystem.forceLayoutUpdate(); // Forcer la mise à jour du layout
    }
    
    // Forcer le redessin
    if (renderView != null) {
        renderView.invalidate();
    }
}
```

## Nouvelles méthodes ajoutées

### Dans RetroArchOverlaySystem.java
- `updateScreenDimensions(int width, int height)`
- `getScreenWidth()`
- `getScreenHeight()`
- `isLandscape()`
- `forceLayoutUpdate()`

### Dans OverlayIntegrationActivity.java
- `updateScreenDimensions()`
- `onConfigurationChanged(Configuration newConfig)` (public)

## Résultat attendu

Après ces corrections, le comportement devrait être :

1. ✅ **Overlays visibles immédiatement au démarrage** en portrait
2. ✅ **Bon overlay affiché automatiquement** lors des changements d'orientation
3. ✅ **Pas besoin de toucher l'écran** pour corriger l'affichage
4. ✅ **Redessin automatique** toutes les 100ms pour garantir la visibilité
5. ✅ **Recalcul automatique du layout** lors des changements d'orientation

## Test de compilation

La compilation réussit sans erreurs :
```
BUILD SUCCESSFUL in 16s
41 actionable tasks: 12 executed, 29 up-to-date
```

## Instructions de test

1. Compiler et installer l'application
2. Lancer `OverlayIntegrationActivity`
3. Vérifier que les overlays s'affichent immédiatement en portrait
4. Changer l'orientation en paysage
5. Vérifier que le bon overlay s'affiche sans toucher l'écran
6. Revenir en portrait
7. Vérifier que le bon overlay s'affiche immédiatement

## Fichiers modifiés

- `app/src/main/java/com/fceumm/wrapper/overlay/RetroArchOverlaySystem.java`
- `app/src/main/java/com/fceumm/wrapper/OverlayIntegrationActivity.java`

## Compatibilité

Ces corrections maintiennent la compatibilité avec :
- Tous les overlays RetroArch officiels (36 fichiers .cfg)
- Toutes les images d'overlays (165+ fichiers PNG)
- Le système de gestion des diagonales (Eightway)
- Les structures de données RetroArch exactes 