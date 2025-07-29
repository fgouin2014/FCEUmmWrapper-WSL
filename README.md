# FCEUmm Wrapper - Émulateur NES Android

Un émulateur NES Android utilisant le core FCEUmm via l'API Libretro, avec affichage vidéo OpenGL et contrôles tactiles.

## 🎮 Fonctionnalités

- ✅ **Émulation NES complète** - Basée sur FCEUmm
- ✅ **Affichage vidéo en temps réel** - Rendu OpenGL avec shaders
- ✅ **Couleurs correctes** - Conversion RGB565→RGBA8888
- ✅ **Ratio d'aspect parfait** - 256:240 sans étirement
- ✅ **Performance optimisée** - 60 FPS fluide
- ✅ **Structure modulaire** - Code propre et maintenable

## 🚀 Installation

### Prérequis
- Android SDK
- Android NDK
- Gradle 8.2+

### Compilation
```bash
./gradlew assembleDebug
```

### Installation sur appareil
```bash
adb install app/build/outputs/apk/debug/app-x86_64-debug.apk
```

## 📁 Structure du projet

```
FCEUmmWrapper/
├── app/
│   ├── src/main/
│   │   ├── java/com/fceumm/wrapper/
│   │   │   ├── MainActivity.java          # Activité principale
│   │   │   └── EmulatorView.java         # Vue OpenGL pour l'affichage
│   │   ├── cpp/
│   │   │   ├── native-lib.cpp            # Wrapper Libretro natif
│   │   │   └── CMakeLists.txt            # Configuration CMake
│   │   ├── assets/roms/                  # ROMs NES
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── gradle/
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## 🔧 Configuration

### ROMs
Placez vos ROMs NES dans `app/src/main/assets/roms/` ou `app/src/main/assets/roms/nes/`

### Core Libretro
Le core FCEUmm doit être placé dans `/data/data/com.fceumm.wrapper/files/fceumm_libretro_android.so`

## 🎯 Utilisation

1. Lancez l'application
2. L'émulateur charge automatiquement Mario Duck Hunt
3. L'affichage vidéo démarre en temps réel
4. Les contrôles tactiles sont disponibles (en développement)

## 🛠️ Développement

### Architecture
- **Java/Kotlin** : Interface utilisateur et gestion des événements
- **C++/JNI** : Wrapper Libretro et gestion vidéo
- **OpenGL ES** : Rendu vidéo hardware-accelerated
- **Libretro API** : Interface standard pour l'émulation

### Callbacks Libretro implémentés
- `video_refresh_callback` : Affichage vidéo
- `environment_callback` : Configuration du core
- `audio_sample_batch_callback` : Audio (préparé)
- `input_poll_callback` : Entrées (préparé)
- `input_state_callback` : État des entrées (préparé)

## 📊 Performance

- **Résolution** : 256x240 (ratio NES original)
- **Framerate** : 60 FPS
- **Format vidéo** : RGB565 → RGBA8888
- **Rendu** : OpenGL ES 2.0 avec shaders

## 🔮 Roadmap

- [ ] Contrôles tactiles
- [ ] Support audio
- [ ] Sélecteur de ROMs
- [ ] Sauvegarde/Chargement
- [ ] Support gamepad
- [ ] Options d'émulation

## 📝 Licence

Ce projet est sous licence MIT. Voir le fichier LICENSE pour plus de détails.

## 🤝 Contribution

Les contributions sont les bienvenues ! N'hésitez pas à :
1. Fork le projet
2. Créer une branche feature
3. Commiter vos changements
4. Pousser vers la branche
5. Ouvrir une Pull Request

## 📞 Support

Pour toute question ou problème, ouvrez une issue sur GitHub.

---

**Développé avec ❤️ pour la communauté des émulateurs Android** 