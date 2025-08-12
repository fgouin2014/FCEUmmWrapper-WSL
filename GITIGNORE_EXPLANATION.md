# Explications du .gitignore

Ce fichier explique les exclusions importantes dans le `.gitignore` du projet FCEUmmWrapper.

## Exclusions critiques pour la taille du repository

### 1. Documentation RetroArch
```
retroarch_docs/
```
- **Pourquoi** : La documentation RetroArch fait ~400MB et peut être re-téléchargée
- **Impact** : Évite de gonfler inutilement le repository
- **Alternative** : Utiliser `REFERENCE_RETROARCH_OVERLAYS.md` pour les références essentielles

### 2. Dépôts Git externes
```
retroarch_git/
fceumm_git/
libretro-super/
```
- **Pourquoi** : Ces dépôts sont volumineux et peuvent être clonés séparément
- **Impact** : Réduit considérablement la taille du repository
- **Alternative** : Scripts de téléchargement automatique si nécessaire

### 3. Fichiers temporaires et de build
```
temp_*/
temp_retroarch/
temp_retroarch_*/
temp_libretro_super/
temp_common_overlays/
```
- **Pourquoi** : Ces dossiers sont générés automatiquement et peuvent être recréés
- **Impact** : Évite de commiter des fichiers temporaires

### 4. ROMs et cores
```
*.nes, *.smc, *.sfc, *.gba, *.gb, *.gbc
*.so, *.dll, *.dylib
app/src/main/assets/roms/
```
- **Pourquoi** : 
  - Les ROMs sont des fichiers propriétaires
  - Les cores sont des binaires volumineux
- **Impact** : Évite les problèmes de licence et réduit la taille

### 5. Fichiers de build Android
```
*.apk, *.ap_, *.aab
build/
.gradle/
.cxx/
```
- **Pourquoi** : Générés automatiquement par le build system
- **Impact** : Évite les conflits et réduit la taille

## Exclusions pour la sécurité

### 1. Fichiers de configuration locaux
```
local.properties
*.keystore
*.jks
```
- **Pourquoi** : Contiennent des chemins et clés spécifiques à l'environnement

### 2. Fichiers IDE
```
.idea/
.vscode/
*.iml
```
- **Pourquoi** : Configurations spécifiques à l'IDE de chaque développeur

## Exclusions pour la performance

### 1. Cache et logs
```
*.log
build_logs/
cache/
*.cache
```
- **Pourquoi** : Fichiers temporaires qui changent fréquemment

### 2. Fichiers système
```
.DS_Store
Thumbs.db
desktop.ini
```
- **Pourquoi** : Générés automatiquement par le système d'exploitation

## Comment gérer les exclusions

### Pour ajouter un fichier exclu temporairement
```bash
git add -f chemin/vers/fichier
```

### Pour vérifier ce qui est exclu
```bash
git status --ignored
```

### Pour voir les fichiers qui seraient commités
```bash
git add .
git status
```

## Recommandations

1. **Toujours tester** : Vérifiez que les fichiers nécessaires ne sont pas exclus
2. **Documenter** : Ajoutez des commentaires pour les exclusions complexes
3. **Réviser régulièrement** : Mettez à jour les exclusions selon les besoins du projet
