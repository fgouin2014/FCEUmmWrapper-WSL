#!/bin/bash

# Script pour jouer immédiatement !
set -e

echo "🎮 Lancement de l'émulation pour jouer !"
echo "========================================"

# Lancer l'application
echo "🚀 Lancement de l'application..."
adb -s emulator-5554 shell am start -n com.fceumm.wrapper/.MainActivity

# Attendre un peu pour l'initialisation
sleep 3

# Surveiller les logs en temps réel
echo "📊 Surveillance des logs d'émulation..."
echo "Appuyez sur Ctrl+C pour arrêter la surveillance"
echo ""

# Surveiller les logs spécifiques à l'émulation
adb -s emulator-5554 logcat -s com.fceumm.wrapper | grep -E "(retro_run|retro_load_game|SIGSEGV|fallback|ROM|émulation|Video|Audio|Input)"

echo ""
echo "🎮 L'émulation est lancée !"
echo "   - Utilisez les contrôles tactiles sur l'écran"
echo "   - Les ROMs NES sont automatiquement chargées"
echo "   - L'application gère les erreurs automatiquement" 