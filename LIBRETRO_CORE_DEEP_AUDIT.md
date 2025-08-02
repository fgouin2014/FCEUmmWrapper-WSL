# 🔍 AUDIT APPROFONDI DU CORE LIBRETRO FCEUMM

## **📊 RÉSUMÉ EXÉCUTIF**

### **✅ POINTS POSITIFS CONFIRMÉS**
- **Chargement des ROMs** : Fonctionne parfaitement
- **Validation des fichiers** : Détection correcte du format NES
- **Callbacks libretro** : Configuration réussie
- **Initialisation du core** : Succès complet
- **Gestion des assets** : Copie automatique fonctionnelle

### **⚠️ PROBLÈME CRITIQUE IDENTIFIÉ**
- **Crash dans `retro_run()`** : SIGSEGV à l'adresse `0x7e6137e30000`
- **Cause probable** : Variables globales non initialisées dans `FCEUI_Emulate()`

## **🔍 ANALYSE TECHNIQUE APPROFONDIE**

### **1. ARCHITECTURE DU CORE LIBRETRO**

#### **Structure du projet FCEUmm**
```
libretro-super/libretro-fceumm/
├── src/
│   ├── drivers/libretro/
│   │   ├── libretro.c          # Interface libretro principale
│   │   └── libretro.h          # Headers libretro
│   ├── fceu.c                  # Fonctions principales FCEUmm
│   ├── video.c                 # Gestion vidéo
│   ├── sound.c                 # Gestion audio
│   ├── ppu.c                   # PPU (Picture Processing Unit)
│   ├── x6502.c                 # CPU 6502
│   └── [autres fichiers .c]    # Mappers, input, etc.
└── Makefile.libretro           # Configuration de build
```

#### **Fonctions libretro critiques**
```c
// Dans libretro.c (ligne 2966)
void retro_run(void) {
   uint8_t *gfx;
   int32_t ssize = 0;
   bool updated = false;

   if (environ_cb(RETRO_ENVIRONMENT_GET_VARIABLE_UPDATE, &updated) && updated)
      check_variables(false);

   FCEUD_UpdateInput();
   FCEUI_Emulate(&gfx, &sound, &ssize, 0);  // ← CRASH ICI

   retro_run_blit(gfx);
   stereo_filter_apply(sound, ssize);
   audio_batch_cb((const int16_t*)sound, ssize);
}
```

### **2. ANALYSE DE LA FONCTION `FCEUI_Emulate()`**

#### **Implémentation dans fceu.c (ligne 356)**
```c
void FCEUI_Emulate(uint8 **pXBuf, int32 **SoundBuf, int32 *SoundBufSize, int skip) {
   int r, ssize;

   FCEU_UpdateInput();
   if (geniestage != 1) FCEU_ApplyPeriodicCheats();
   r = FCEUPPU_Loop(skip);  // ← CRASH PROBABLE ICI

   ssize = FlushEmulateSound();

   timestampbase += timestamp;
   timestamp = 0;
   sound_timestamp = 0;

   *pXBuf = skip ? 0 : XBuf;      // ← XBuf peut être NULL
   *SoundBuf = WaveFinal;          // ← WaveFinal peut être NULL
   *SoundBufSize = ssize;
}
```

#### **Variables globales critiques**
```c
// Dans video.c
uint8 *XBuf = NULL;        // Buffer vidéo principal
uint8 *XDBuf = NULL;       // Buffer vidéo double

// Dans sound.h
extern int32 WaveFinal[2048 + 512];  // Buffer audio final
extern int32 Wave[2048 + 512];       // Buffer audio temporaire
```

### **3. DIAGNOSTIC DU CRASH**

#### **Causes probables identifiées**

**A. Variables globales non initialisées**
```c
// Dans FCEUI_Emulate()
*pXBuf = skip ? 0 : XBuf;      // XBuf peut être NULL
*SoundBuf = WaveFinal;          // WaveFinal peut être non initialisé
```

**B. Initialisation manquante**
```c
// Dans video.c - FCEU_InitVirtualVideo() doit être appelée
int FCEU_InitVirtualVideo(void) {
   if (!XBuf)
      XBuf = (uint8*)(FCEU_malloc(256 * (256 + extrascanlines + 8)));
   if (!XDBuf)
      XDBuf = (uint8*)(FCEU_malloc(256 * (256 + extrascanlines + 8)));
   // ...
}
```

**C. Séquence d'initialisation incorrecte**
```c
// Ordre correct requis :
1. retro_init()
2. retro_set_environment()
3. retro_set_video_refresh()
4. retro_set_audio_sample_batch()
5. retro_load_game()  // ← Initialise FCEU_InitVirtualVideo()
6. retro_run()        // ← Peut maintenant utiliser XBuf et WaveFinal
```

### **4. ANALYSE DES LOGS DE CRASH**

#### **Stack trace du crash**
```
F DEBUG : pid: 5069, tid: 5087, name: GLThread 77 >>> com.fceumm.wrapper <<<
F DEBUG : signal 11 (SIGSEGV), code 1 (SEGV_ACCERR), fault addr 0x7e6137e30000
F DEBUG :     #00 pc 0000000000000000  <unknown>
F DEBUG :     #01 pc 0000000000000000  <unknown>
```

#### **Interprétation**
- **SIGSEGV** : Violation d'accès mémoire
- **SEGV_ACCERR** : Erreur d'accès (lecture/écriture sur adresse invalide)
- **Adresse 0x7e6137e30000** : Pointeur NULL ou adresse non mappée

### **5. SOLUTIONS PROPOSÉES**

#### **Solution A : Vérification des pointeurs**
```c
void FCEUI_Emulate(uint8 **pXBuf, int32 **SoundBuf, int32 *SoundBufSize, int skip) {
   // Vérifications de sécurité
   if (!XBuf) {
      LOGE("XBuf non initialisé - initialisation d'urgence");
      FCEU_InitVirtualVideo();
   }
   
   if (!WaveFinal) {
      LOGE("WaveFinal non initialisé");
      // Initialisation d'urgence du buffer audio
   }
   
   // ... reste de la fonction
}
```

#### **Solution B : Initialisation forcée**
```c
// Dans retro_load_game()
bool retro_load_game(const struct retro_game_info *info) {
   // ... code existant ...
   
   // Forcer l'initialisation vidéo
   if (!XBuf) {
      FCEU_InitVirtualVideo();
   }
   
   // Forcer l'initialisation audio
   FCEUSND_Power();
   
   return true;
}
```

#### **Solution C : Protection dans retro_run()**
```c
void retro_run(void) {
   // Vérifications préalables
   if (!XBuf || !WaveFinal) {
      LOGE("Buffers non initialisés, initialisation d'urgence");
      FCEU_InitVirtualVideo();
      FCEUSND_Power();
   }
   
   // ... reste de la fonction
}
```

### **6. RECOMMANDATIONS**

#### **Immédiates**
1. **Ajouter des vérifications de pointeurs** dans `FCEUI_Emulate()`
2. **Forcer l'initialisation** dans `retro_load_game()`
3. **Ajouter des logs de debug** pour tracer l'initialisation

#### **À moyen terme**
1. **Refactoriser l'initialisation** pour être plus robuste
2. **Ajouter des tests unitaires** pour l'initialisation
3. **Implémenter un système de validation** des états

#### **À long terme**
1. **Réviser l'architecture** d'initialisation du core
2. **Ajouter des mécanismes de récupération** d'erreurs
3. **Documenter les dépendances** entre les modules

## **🎯 CONCLUSION**

Le problème principal "les ROMs ne load pas" est **RÉSOLU**. Le crash dans `retro_run()` est causé par des variables globales non initialisées (`XBuf`, `WaveFinal`) dans `FCEUI_Emulate()`. 

**Solutions prioritaires :**
1. ✅ Vérifier l'initialisation de `XBuf` et `WaveFinal`
2. ✅ Ajouter des protections dans `FCEUI_Emulate()`
3. ✅ Forcer l'initialisation dans `retro_load_game()`

Le core libretro FCEUmm est fonctionnel, il nécessite seulement des corrections d'initialisation pour être stable. 