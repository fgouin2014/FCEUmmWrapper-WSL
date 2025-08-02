# Guide de Dépannage Audio - FCEUmm Wrapper

## 🔊 Problème : Pas de son dans l'application

### Diagnostic Rapide

1. **Vérifiez le volume de l'appareil**
   - Appuyez sur les boutons volume de votre appareil
   - Vérifiez que le volume n'est pas à 0
   - Vérifiez que l'appareil n'est pas en mode silencieux

2. **Testez avec un casque**
   - Branchez un casque pour isoler le problème
   - Vérifiez si le son fonctionne avec d'autres applications

3. **Redémarrez l'application**
   - Fermez complètement l'application
   - Relancez-la

### Solutions par Ordre de Priorité

#### Solution 1 : Correction Automatique
```powershell
# Exécutez le script de correction
.\fix_audio_no_sound.ps1
```

#### Solution 2 : Diagnostic Détaillé
```powershell
# Exécutez le script de diagnostic
.\diagnose_audio_no_sound.ps1
```

#### Solution 3 : Test Simple
```powershell
# Exécutez le script de test
.\test_audio_simple.ps1
```

### Problèmes Courants et Solutions

#### 1. Volume Système à 0
**Symptôme :** Aucun son, même avec d'autres applications
**Solution :**
```bash
adb shell settings put system volume_music 7
adb shell input keyevent 25  # Volume up
```

#### 2. Permissions Audio Manquantes
**Symptôme :** Erreurs de permissions dans les logs
**Solution :**
- Réinstallez l'application
- Vérifiez les permissions dans les paramètres Android

#### 3. OpenSL ES Non Initialisé
**Symptôme :** Logs "OpenSL ES initialisé avec succès" manquants
**Solution :**
```bash
adb shell am force-stop com.fceumm.wrapper
adb shell am start -n com.fceumm.wrapper/.MainActivity
```

#### 4. Services Audio Système Défaillants
**Symptôme :** Aucun son sur tout l'appareil
**Solution :**
```bash
adb shell stop audioserver
adb shell start audioserver
```

#### 5. Core Libretro Non Chargé
**Symptôme :** Application se lance mais pas d'audio
**Solution :**
- Vérifiez que les fichiers core sont présents dans `/assets/coresCompiled/`
- Redémarrez l'application

### Vérifications Manuelles

#### 1. Vérifier les Logs Audio
```bash
adb logcat -d | grep -i "audio\|OpenSL\|SLES"
```

#### 2. Vérifier les Permissions
```bash
adb shell dumpsys package com.fceumm.wrapper | grep -i "permission"
```

#### 3. Vérifier les Processus Audio
```bash
adb shell ps | grep -i "audio\|mediaserver"
```

#### 4. Tester l'Audio Système
```bash
adb shell am start -a android.intent.action.MAIN -n com.android.music/.MediaPlaybackActivity
```

### Configuration Audio Optimale

#### Paramètres Recommandés
- **Sample Rate :** 48000 Hz
- **Buffer Size :** 2048 samples
- **Volume :** 80-100%
- **Qualité :** High ou Very High

#### Variables d'Environnement
```bash
# Volume audio
adb shell settings put system volume_music 7

# Qualité audio
adb shell settings put system audio_quality 2

# Latence audio
adb shell settings put system audio_latency 1
```

### Dépannage Avancé

#### 1. Réinitialisation Complète
```bash
# Arrêter l'application
adb shell am force-stop com.fceumm.wrapper

# Nettoyer les caches
adb shell pm clear com.fceumm.wrapper

# Redémarrer les services audio
adb shell stop audioserver
adb shell start audioserver

# Relancer l'application
adb shell am start -n com.fceumm.wrapper/.MainActivity
```

#### 2. Vérification des Cores
```bash
# Vérifier la présence des cores
adb shell ls /data/data/com.fceumm.wrapper/files/coresCompiled/

# Vérifier les permissions des cores
adb shell ls -la /data/data/com.fceumm.wrapper/files/coresCompiled/
```

#### 3. Test Audio Direct
```bash
# Générer un signal de test
adb shell am start -n com.fceumm.wrapper/.AudioLatencyTestActivity
```

### Logs Importants à Surveiller

#### Logs de Succès
```
OpenSL ES initialisé avec succès
Audio configuré: sample_rate=48000, enabled=true
Core initialisé
ROM chargée avec succès
```

#### Logs d'Erreur à Identifier
```
ERROR: Erreur lors de l'initialisation audio
ERROR: OpenSL ES non initialisé
ERROR: Core libretro non chargé
ERROR: Permissions audio manquantes
```

### Contact et Support

Si les solutions ci-dessus ne résolvent pas le problème :

1. **Collectez les logs complets :**
   ```bash
   adb logcat -d > logs_complets.txt
   ```

2. **Exécutez le diagnostic complet :**
   ```powershell
   .\diagnose_audio_no_sound.ps1
   ```

3. **Fournissez les informations suivantes :**
   - Modèle d'appareil Android
   - Version Android
   - Logs d'erreur
   - Étapes déjà essayées

### Notes Techniques

- L'application utilise OpenSL ES pour l'audio
- L'audio est géré par le code natif C++
- Les paramètres audio sont persistants entre les sessions
- Le mode basse latence est activé par défaut
- Le buffer audio circulaire est utilisé pour optimiser les performances 