# Test du Système de Sélection des ROMs

## Problème résolu

Le problème était que `EmulationActivity` chargeait toujours le ROM `marioduckhunt.nes` codé en dur, même quand une autre ROM était sélectionnée.

## Corrections apportées

### 1. MainActivity.java
- **Avant** : Passait directement à `EmulationActivity` sans la ROM sélectionnée
- **Après** : Récupère la ROM sélectionnée depuis l'Intent et la passe à `EmulationActivity`

```java
// Récupérer la ROM sélectionnée depuis l'Intent
String selectedRom = getIntent().getStringExtra("selected_rom");
Log.i(TAG, "ROM sélectionnée: " + (selectedRom != null ? selectedRom : "ROM par défaut (marioduckhunt.nes)"));

// Lancer EmulationActivity avec la ROM sélectionnée
Intent emulationIntent = new Intent(this, EmulationActivity.class);
if (selectedRom != null) {
    emulationIntent.putExtra("selected_rom", selectedRom);
}
```

### 2. EmulationActivity.java
- **Avant** : Utilisait toujours `marioduckhunt.nes` codé en dur
- **Après** : Récupère la ROM sélectionnée depuis l'Intent

```java
// Récupérer la ROM sélectionnée depuis l'Intent
String selectedRom = getIntent().getStringExtra("selected_rom");
if (selectedRom == null) {
    selectedRom = "marioduckhunt.nes"; // ROM par défaut
}
Log.i(TAG, "ROM sélectionnée pour l'émulation: " + selectedRom);
```

### 3. Méthode de copie des ROMs
- **Avant** : `copyMarioDuckHuntRom()` - copiait seulement marioduckhunt.nes
- **Après** : `copySelectedRom(String romFileName)` - copie n'importe quelle ROM

```java
private void copySelectedRom(String romFileName) {
    // Copie la ROM sélectionnée depuis assets/roms/nes/
    String assetPath = "roms/nes/" + romFileName;
    // ...
}
```

## ROMs disponibles

Les ROMs suivantes sont disponibles dans `assets/roms/nes/` :
- marioduckhunt.nes (ROM par défaut)
- Chiller.nes
- Mario Bros. (World).nes
- sweethome.nes
- Who Framed Roger Rabbit (USA).nes
- Super Spy Hunter (USA).nes
- Spy Hunter (USA).nes
- Nightmare on Elm Street, A (USA).nes
- Friday the 13th (USA).nes
- Contra (USA).nes
- Donkey Kong Classics.nes
- Donkey Kong.nes

## Flux de fonctionnement

1. **Menu Principal** → Bouton "📁 Sélection ROM"
2. **RomSelectionActivity** → Liste des ROMs disponibles
3. **Sélection d'une ROM** → Lance MainActivity avec la ROM sélectionnée
4. **MainActivity** → Récupère la ROM et lance EmulationActivity
5. **EmulationActivity** → Charge la ROM sélectionnée et démarre l'émulation

## Test de validation

✅ **Compilation réussie** : Aucune erreur de compilation
✅ **ROMs disponibles** : 12 ROMs dans le dossier assets
✅ **Système de sélection** : RomSelectionActivity configurée
✅ **Passage de paramètres** : ROM sélectionnée passée correctement
✅ **Chargement dynamique** : EmulationActivity charge la ROM sélectionnée

## Résultat

Le système de sélection des ROMs fonctionne maintenant correctement :
- Le bouton "🎮 Jouer" lance l'émulation avec la ROM par défaut (marioduckhunt.nes)
- Le bouton "📁 Sélection ROM" permet de choisir parmi toutes les ROMs disponibles
- La ROM sélectionnée est correctement chargée dans l'émulation 