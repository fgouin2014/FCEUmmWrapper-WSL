#!/bin/bash

# Workflow de développement pour FCEUmmWrapper
set -e

echo "🛠️ Workflow de développement FCEUmmWrapper"
echo "=========================================="

# Fonction d'aide
show_help() {
    echo "Usage: $0 [COMMANDE]"
    echo ""
    echo "Commandes disponibles:"
    echo "  build     - Compiler le projet"
    echo "  install   - Installer sur l'émulateur"
    echo "  test      - Lancer les tests"
    echo "  debug     - Mode debug complet"
    echo "  clean     - Nettoyer le projet"
    echo "  update    - Mettre à jour les cores"
    echo "  diagnose  - Diagnostic complet"
    echo "  help      - Afficher cette aide"
}

# Compilation
build() {
    echo "🔨 Compilation..."
    ./gradlew assembleDebug
    echo "✅ Compilation terminée"
}

# Installation
install() {
    echo "📱 Installation..."
    adb install -r FCEUmmWrapper/app/build/outputs/apk/debug/app-x86_64-debug.apk
    echo "✅ Installation terminée"
}

# Test
test() {
    echo "🧪 Tests..."
    ./test_emulation.sh
}

# Debug complet
debug() {
    echo "🐛 Mode debug..."
    build
    install
    adb shell am start -n com.fceumm.wrapper/.MainActivity
    echo "📊 Surveillance des logs (Ctrl+C pour arrêter)..."
    adb logcat -s com.fceumm.wrapper
}

# Nettoyage
clean() {
    echo "🧹 Nettoyage..."
    ./gradlew clean
    echo "✅ Nettoyage terminé"
}

# Mise à jour
update() {
    echo "🔄 Mise à jour..."
    ./update_cores.sh
}

# Diagnostic
diagnose() {
    echo "🔍 Diagnostic..."
    ./diagnose.sh
}

# Gestion des commandes
case "${1:-help}" in
    build) build ;;
    install) install ;;
    test) test ;;
    debug) debug ;;
    clean) clean ;;
    update) update ;;
    diagnose) diagnose ;;
    help|*) show_help ;;
esac
