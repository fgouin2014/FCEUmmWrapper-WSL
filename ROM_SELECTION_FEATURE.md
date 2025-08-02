# Fonctionnalité de Sélection des ROMs

## Vue d'ensemble

La fonctionnalité de sélection des ROMs a été implémentée avec succès dans l'application FCEUmm Wrapper. Elle permet aux utilisateurs de choisir parmi les ROMs NES disponibles dans le dossier `assets/roms/nes`.

## Fonctionnalités implémentées

### 1. Menu Principal Modifié
- **Bouton "📁 Sélection ROM"** : Nouveau bouton principal pour accéder à la sélection des ROMs
- **Bouton "🎮 Jouer (ROM par défaut)"** : Bouton existant modifié pour lancer l'émulation avec la ROM par défaut (marioduckhunt.nes)

### 2. Activité de Sélection des ROMs
- **RomSelectionActivity.java** : Activité principale pour afficher la liste des ROMs
- **Interface moderne** : Design avec fond noir et texte blanc
- **Liste personnalisée** : Utilisation d'un adaptateur personnalisé pour un affichage élégant
- **Navigation** : Bouton retour pour revenir au menu principal

### 3. Adaptateur Personnalisé
- **RomListAdapter.java** : Adaptateur personnalisé pour la liste des ROMs
- **Layout personnalisé** : `rom_list_item.xml` avec icônes et style moderne
- **Affichage optimisé** : Chaque ROM affichée avec une icône de jeu et une flèche

### 4. Chargement Dynamique des ROMs
- **Lecture automatique** : Scan du dossier `assets/roms/nes` au démarrage
- **Filtrage** : Seules les fichiers `.nes` sont listés
- **Nommage** : Affichage des noms sans l'extension `.nes`

### 5. Intégration avec l'Émulation
- **Passage de paramètres** : La ROM sélectionnée est passée à MainActivity
- **Copie dynamique** : La ROM sélectionnée est copiée vers le dossier de l'application
- **Chargement** : L'émulation se lance avec la ROM choisie

## Fichiers créés/modifiés

### Nouveaux fichiers
- `app/src/main/java/com/fceumm/wrapper/RomSelectionActivity.java`
- `app/src/main/java/com/fceumm/wrapper/RomListAdapter.java`
- `app/src/main/res/layout/activity_rom_selection.xml`
- `app/src/main/res/layout/rom_list_item.xml`

### Fichiers modifiés
- `app/src/main/java/com/fceumm/wrapper/MainMenuActivity.java`
- `app/src/main/java/com/fceumm/wrapper/MainActivity.java`
- `app/src/main/res/layout/activity_main_menu.xml`
- `app/src/main/AndroidManifest.xml`

## ROMs disponibles

Les ROMs suivantes sont actuellement disponibles dans `assets/roms/nes/` :
- Chiller.nes
- Contra (USA).nes
- Mario Bros. (World).nes
- marioduckhunt.nes
- sweethome.nes
- test.nes

## Instructions d'utilisation

1. **Lancer l'application** : L'application démarre sur le menu principal
2. **Sélectionner une ROM** : Cliquer sur "📁 Sélection ROM"
3. **Choisir un jeu** : Sélectionner une ROM dans la liste affichée
4. **Lancer l'émulation** : L'émulation se lance automatiquement avec la ROM choisie
5. **Retour au menu** : Utiliser le bouton "⬅️ Retour" pour revenir au menu principal

## Fonctionnalités techniques

### Gestion des erreurs
- Vérification de l'existence des ROMs
- Messages d'erreur si aucune ROM n'est trouvée
- Gestion des exceptions lors de la copie des fichiers

### Performance
- Copie des ROMs uniquement si nécessaire
- Mise en cache des ROMs déjà copiées
- Interface responsive avec liste optimisée

### Interface utilisateur
- Mode fullscreen pour une expérience immersive
- Design cohérent avec le reste de l'application
- Animations et transitions fluides

## Tests et validation

- ✅ Compilation réussie sans erreurs
- ✅ Tous les fichiers nécessaires créés
- ✅ Activité déclarée dans le manifeste Android
- ✅ ROMs disponibles dans le dossier assets
- ✅ Interface utilisateur fonctionnelle

## Prochaines améliorations possibles

1. **Prévisualisation** : Ajouter des captures d'écran pour chaque ROM
2. **Recherche** : Fonction de recherche dans la liste des ROMs
3. **Favoris** : Système de favoris pour les ROMs préférées
4. **Catégories** : Organisation des ROMs par catégories
5. **Métadonnées** : Affichage d'informations sur chaque ROM (année, éditeur, etc.) 