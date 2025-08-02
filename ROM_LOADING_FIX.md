# 🔧 CORRECTION DU PROBLÈME DE CHARGEMENT DES ROMS

## **PROBLÈMES IDENTIFIÉS**

### **1. Code désactivé pour diagnostic**
Le code de chargement des ROMs avait été temporairement désactivé dans `libretro_wrapper.cpp` :
```cpp
// TEMPORAIREMENT DÉSACTIVÉ pour diagnostiquer le problème
LOGI("Initialisation du core libretro désactivée pour diagnostic");
```

### **2. Chemin de ROM incorrect**
Le chemin pointait vers `/files/` au lieu de `/files/roms/` :
```cpp
const char* rom_path = "/data/data/com.fceumm.wrapper/files/sweethome.nes";
```

### **3. Copie des assets manquante**
Les ROMs étaient dans les assets mais n'étaient pas copiées vers le répertoire de fichiers.

## **SOLUTIONS IMPLÉMENTÉES**

### **1. Restauration du code de chargement**
```cpp
// Charger le core libretro
core_handle = dlopen(core_path, RTLD_LAZY);
if (!core_handle) {
    LOGE("Erreur: Impossible de charger le core: %s", dlerror());
    return;
}

// Charger la ROM
if (retro_load_game && rom_path) {
    struct retro_game_info game_info;
    game_info.path = rom_path;
    game_info.data = nullptr;
    game_info.size = 0;
    game_info.meta = nullptr;
    
    bool load_result = retro_load_game(&game_info);
    if (load_result) {
        LOGI("ROM chargée avec succès: %s", rom_path);
    }
}
```

### **2. Correction du chemin de ROM**
```cpp
const char* rom_path = "/data/data/com.fceumm.wrapper/files/roms/sweethome.nes";
```

### **3. Copie automatique des assets**
```java
private void copyRomsFromAssets() {
    String[] roms = {"sweethome.nes", "marioduckhunt.nes", "Chiller.nes"};
    File filesDir = getFilesDir();
    File romsDir = new File(filesDir, "roms");
    romsDir.mkdirs();
    
    for (String rom : roms) {
        File romFile = new File(romsDir, rom);
        if (!romFile.exists()) {
            // Copie depuis les assets
        }
    }
}
```

### **4. Fonctions JNI pour gestion dynamique**
```cpp
extern "C" JNIEXPORT jboolean JNICALL
Java_com_fceumm_wrapper_MainActivity_nativeLoadRom(JNIEnv* env, jobject thiz, jstring romPath) {
    const char* cpath = env->GetStringUTFChars(romPath, nullptr);
    libretro_deinit();
    const char* core_path = "/data/data/com.fceumm.wrapper/lib/libfceumm.so";
    libretro_init(core_path, cpath, false);
    env->ReleaseStringUTFChars(romPath, cpath);
    return JNI_TRUE;
}
```

## **TEST DE VALIDATION**

### **Script de test**
```bash
./test_rom_loading.sh
```

Ce script vérifie :
1. ✅ Build de l'application
2. ✅ Installation sur device
3. ✅ Présence des ROMs dans les assets
4. ✅ Lancement de l'application
5. ✅ Vérification des logs de chargement
6. ✅ Vérification des fichiers copiés

## **UTILISATION**

### **Chargement automatique**
L'application charge automatiquement `sweethome.nes` au démarrage.

### **Changement de ROM**
```java
// Charger une nouvelle ROM
nativeLoadRom("/data/data/com.fceumm.wrapper/files/roms/marioduckhunt.nes");
```

### **ROMs disponibles**
- `sweethome.nes` (256KB)
- `marioduckhunt.nes` (80KB)
- `Chiller.nes` (80KB)

## **DEBUGGING**

### **Logs à surveiller**
```bash
adb logcat | grep -E "(ROM|FCEUmm|libretro)"
```

### **Messages d'erreur courants**
- `"Erreur: Impossible de charger le core"` → Core non compilé
- `"Échec du chargement de la ROM"` → ROM corrompue ou chemin invalide
- `"Fonction retro_load_game non disponible"` → Core libretro invalide

## **PROCHAINES ÉTAPES**

1. **Interface utilisateur** : Ajouter un sélecteur de ROMs
2. **Validation ROMs** : Vérifier l'intégrité des fichiers NES
3. **Sauvegarde** : Implémenter les save states
4. **Performance** : Optimiser le rendu vidéo 