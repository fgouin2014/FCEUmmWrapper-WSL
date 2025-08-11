# Backup des Cores Supprimés - Optimisation APK

## 📅 Date de création
8 août 2025

## 🎯 Objectif
Réduction de la taille de l'APK en supprimant les cores inutiles tout en conservant une sauvegarde.

## 📊 Statistiques

### Cores supprimés (308 MB) :
- **mame2010_libretro_android.so** (68 MB par ABI) - Core MAME 2010
- **mesen_libretro_android.so** (4 MB par ABI) - Core Mesen
- **nestopia_libretro_android.so** (6 MB par ABI) - Core Nestopia
- **quicknes_libretro_android.so** (1 MB par ABI) - Core QuickNES

### ABIs supprimés :
- **Aucun** - Tous les ABIs conservés pour le débogage sur émulateur

### Cores conservés (183 MB) :
- **fceumm_libretro_android.so** (4 MB par ABI) - Core FCEUmm pour NES
- **ABIs conservés** : arm64-v8a, armeabi-v7a, x86, x86_64 (pour émulateur)

## 🔄 Restauration

Pour restaurer les cores supprimés :

```powershell
# Restaurer tous les cores
Copy-Item -Path "backup_cores_removed/coresCompiled_backup/*" -Destination "app/src/main/assets/coresCompiled/" -Recurse

# Ou restaurer un core spécifique
Copy-Item -Path "backup_cores_removed/coresCompiled_backup/arm64-v8a/mame2010_libretro_android.so" -Destination "app/src/main/assets/coresCompiled/arm64-v8a/"
```

## ✅ Résultat

- **APK avant** : ~320 MB
- **APK après** : ~200 MB
- **Économie** : ~120 MB (40% de réduction)
- **Débogage** : ✅ Compatible émulateur Android (x86, x86_64)

## 🎯 Justification

1. **FCEUmm suffit** pour l'émulation NES
2. **ABIs complets** : arm64-v8a, armeabi-v7a, x86, x86_64 pour débogage émulateur
3. **Fallback disponible** : coreCustom/ contient vos cores optimisés
4. **Backup sécurisé** : Tous les cores sont sauvegardés ici
5. **Débogage préservé** : Compatible émulateur Android Studio
