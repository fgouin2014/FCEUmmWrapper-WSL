#!/bin/bash

# Diagnostic avancé
set -e

echo "🔍 Diagnostic avancé..."
echo "======================"

# Vérifier les cores disponibles
echo ""
echo "📦 Cores disponibles :"
ls -la FCEUmmWrapper/app/src/main/jniLibs/*/libfceumm.so 2>/dev/null || echo "Aucun core trouvé"

# Vérifier les logs détaillés
echo ""
echo "📊 Logs détaillés :"
adb logcat -d | grep -E "(fceumm|libretro|SIGSEGV|retro_load_game)" | tail -20

# Test de compatibilité
echo ""
echo "🧪 Test de compatibilité..."
adb shell "cd /data/data/com.fceumm.wrapper/files && ls -la"

echo "✅ Diagnostic terminé"
