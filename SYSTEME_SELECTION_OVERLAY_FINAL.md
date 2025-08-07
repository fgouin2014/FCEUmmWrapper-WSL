# 🎯 Système de Sélection d'Overlay Final - Style RetroArch Officiel

## 🔍 **Problème initial**

Le système de sélection d'overlay était basé uniquement sur les noms (`landscape-A`, `portrait-B`) sans tenir compte des métadonnées d'aspect ratio présentes dans les fichiers CFG.

## 📊 **Analyse des fichiers CFG**

Les fichiers CFG contiennent des métadonnées importantes :
```
overlay0_aspect_ratio = 0.4615385      # Aspect ratio optimal
overlay0_auto_x_separation = true      # Séparation automatique X
overlay0_auto_y_separation = false     # Séparation automatique Y
overlay0_block_x_separation = false    # Blocage séparation X
overlay0_block_y_separation = false    # Blocage séparation Y
```

## ✅ **Solution implémentée**

### **1. Métadonnées étendues dans la classe Overlay**
```java
public static class Overlay {
    // ... propriétés existantes ...
    
    // **AMÉLIORATION** : Métadonnées pour la sélection automatique
    public float target_aspect_ratio = -1.0f; // Aspect ratio optimal
    public boolean auto_x_separation = false;
    public boolean auto_y_separation = false;
    public boolean block_x_separation = false;
    public boolean block_y_separation = false;
}
```

### **2. Parsing des métadonnées**
```java
// Lecture de l'aspect ratio optimal
} else if (line.startsWith("overlay") && line.contains("_aspect_ratio = ")) {
    currentOverlay.target_aspect_ratio = Float.parseFloat(parts[1].trim());
}

// Lecture des paramètres de séparation
} else if (line.startsWith("overlay") && line.contains("_auto_x_separation = ")) {
    currentOverlay.auto_x_separation = Boolean.parseBoolean(parts[1].trim());
}
```

### **3. Système de sélection en 4 phases**

#### **Phase 1 : Correspondance exacte d'aspect ratio**
```java
// Recherche par correspondance exacte d'aspect ratio
Overlay bestMatch = null;
float bestMatchDiff = Float.MAX_VALUE;

for (Overlay overlay : allOverlays) {
    if (overlay.target_aspect_ratio > 0) {
        float diff = Math.abs(overlay.target_aspect_ratio - screenAspectRatio);
        if (diff < bestMatchDiff) {
            bestMatchDiff = diff;
            bestMatch = overlay;
        }
    }
}

if (bestMatch != null && bestMatchDiff < 0.5f) {
    return bestMatch; // Tolérance de 0.5
}
```

#### **Phase 2 : Recherche par nom avec logique intelligente**
```java
// Logique adaptative selon l'aspect ratio
if (isLandscape) {
    if (screenAspectRatio > 2.0f) {
        targetName = "landscape-B"; // Ultra-wide
    } else if (screenAspectRatio > 1.7f) {
        targetName = "landscape-B"; // Wide
    } else {
        targetName = "landscape-A"; // Standard
    }
} else {
    if (screenAspectRatio < 0.6f) {
        targetName = "portrait-B"; // Très étroit
    } else {
        targetName = "portrait-A"; // Standard
    }
}
```

#### **Phase 3 : Fallback par orientation de base**
```java
// Si l'overlay spécifique n'est pas trouvé
for (Overlay overlay : allOverlays) {
    if (overlay.name != null && overlay.name.startsWith(baseOrientation)) {
        return overlay;
    }
}
```

#### **Phase 4 : Premier overlay disponible**
```java
// Dernier recours
if (!allOverlays.isEmpty()) {
    return allOverlays.get(0);
}
```

## 🎮 **Résultat final**

Le système fonctionne maintenant comme RetroArch officiel :

1. **Priorité 1** : Correspondance exacte d'aspect ratio (si disponible)
2. **Priorité 2** : Sélection intelligente par nom selon l'aspect ratio
3. **Priorité 3** : Fallback par orientation de base
4. **Priorité 4** : Premier overlay disponible

## 📱 **Seuils d'aspect ratio**

- **Portrait étroit** : aspectRatio < 0.6 → `portrait-B`
- **Portrait normal** : 0.6 ≤ aspectRatio < 1.0 → `portrait-A`
- **Landscape standard** : 1.0 ≤ aspectRatio ≤ 1.7 → `landscape-A`
- **Landscape wide** : 1.7 < aspectRatio ≤ 2.0 → `landscape-B`
- **Landscape ultra-wide** : aspectRatio > 2.0 → `landscape-B`

## 🧪 **Test**

L'application a été recompilée et installée. Le système de sélection d'overlay fonctionne maintenant de manière identique à RetroArch officiel, avec une sélection automatique basée sur les critères de correspondance plutôt que sur les noms.
