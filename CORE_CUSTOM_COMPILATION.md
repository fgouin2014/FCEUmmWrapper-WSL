# Guide de Compilation de Cores Personnalisés

## 📋 Vue d'ensemble

Ce guide explique comment compiler des cores libretro personnalisés avec alignement 16 KB pour Android 16, sans affecter les cores existants dans `coresCompiled/`.

## 🗂️ Structure des répertoires

```
app/src/main/assets/
├── coresCompiled/          # Cores précompilés (NE PAS MODIFIER)
│   ├── arm64-v8a/
│   ├── armeabi-v7a/
│   ├── x86/
│   └── x86_64/
└── coreCustom/             # Cores personnalisés (NOUVEAU)
    ├── arm64-v8a/
    ├── armeabi-v7a/
    ├── x86/
    └── x86_64/
```

## 🔧 Scripts disponibles

### 1. `build_custom_core.ps1`
**Compile un core personnalisé avec alignement 16 KB**

```powershell
# Compiler pour arm64-v8a (par défaut)
.\build_custom_core.ps1

# Compiler pour une architecture spécifique
.\build_custom_core.ps1 -Architecture "armeabi-v7a"

# Compiler un core spécifique
.\build_custom_core.ps1 -Architecture "arm64-v8a" -CoreName "fceumm"
```

### 2. `test_custom_core.ps1`
**Test complet : compilation + installation + lancement**

```powershell
.\test_custom_core.ps1
```

## 🚀 Processus de compilation

### Étape 1 : Compilation du core
```powershell
.\build_custom_core.ps1 -Architecture "arm64-v8a"
```

**Ce qui se passe :**
- ✅ Clone FCEUmm depuis GitHub
- ✅ Configure le toolchain Android NDK
- ✅ Compile avec alignement 16 KB (`-Wl,-z,relro,-z,now`)
- ✅ Copie le core vers `app/src/main/assets/coreCustom/arm64-v8a/`

### Étape 2 : Compilation de l'application
```powershell
.\gradlew clean assembleDebug
```

### Étape 3 : Installation et test
```powershell
adb install -r app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
adb shell am start -n com.fceumm.wrapper/.MainMenuActivity
```

## 🔍 Priorité des cores

L'application utilise les cores dans cet ordre :

1. **`coreCustom/`** (priorité haute) - Cores personnalisés
2. **`coresCompiled/`** (fallback) - Cores précompilés

```java
// Code dans MainActivity.java
try {
    // Essayer d'abord le core personnalisé
    inputStream = getAssets().open("coreCustom/" + arch + "/fceumm_libretro_android.so");
    Log.i(TAG, "Utilisation du core personnalisé");
} catch (IOException e) {
    // Fallback vers le core par défaut
    inputStream = getAssets().open("coresCompiled/" + arch + "/fceumm_libretro_android.so");
    Log.i(TAG, "Utilisation du core par défaut");
}
```

## 📊 Avantages des cores personnalisés

### ✅ Alignement 16 KB
- Compatible Android 16
- Pas d'erreurs d'alignement
- Performance optimisée

### ✅ Contrôle total
- Modifications du code source
- Optimisations personnalisées
- Debugging avancé

### ✅ Sécurité
- Cores précompilés préservés
- Fallback automatique
- Pas de risque de corruption

## 🛠️ Configuration avancée

### Modifier les flags de compilation
Dans `build_custom_core.ps1` :

```powershell
# Flags de compilation avec alignement 16 KB
$CFLAGS = "-fPIC -DANDROID -D__ANDROID_API__=$API_LEVEL -O3 -ffast-math -Wl,-z,relro,-z,now"
$CXXFLAGS = "$CFLAGS -std=c++17"
$LDFLAGS = "-shared -Wl,--no-undefined -Wl,-z,relro,-z,now"
```

### Ajouter des optimisations
```powershell
# Optimisations supplémentaires
$CFLAGS += " -march=native -mtune=native"
$CFLAGS += " -DHAVE_NEON=1"  # Pour ARM
```

## 🔍 Vérification

### Vérifier la présence du core personnalisé
```powershell
ls app/src/main/assets/coreCustom/arm64-v8a/
```

### Vérifier les logs de l'application
```powershell
adb logcat -s MainActivity:V | findstr "core"
```

## 🚨 Dépannage

### Erreur : "Core non trouvé"
- Vérifier que le répertoire `coreCustom/` existe
- Vérifier que le core a été compilé correctement
- Vérifier l'architecture cible

### Erreur : "Compilation échouée"
- Vérifier que Android NDK est installé
- Vérifier le chemin du NDK dans le script
- Vérifier que Git est installé

### Erreur : "Alignement 16 KB"
- Les cores personnalisés sont automatiquement alignés
- Vérifier que `targetSdkVersion = 33` dans `build.gradle`

## 📝 Notes importantes

1. **Ne jamais modifier `coresCompiled/`** - Ces cores sont préservés
2. **Toujours utiliser `coreCustom/`** pour les nouvelles compilations
3. **L'application utilise automatiquement** le core personnalisé s'il existe
4. **Fallback automatique** vers le core par défaut si nécessaire

## 🎯 Résultat

Avec cette configuration, vous pouvez :
- ✅ Compiler des cores personnalisés avec alignement 16 KB
- ✅ Préserver vos cores existants
- ✅ Avoir un fallback automatique
- ✅ Être compatible Android 16
- ✅ Optimiser les performances selon vos besoins 