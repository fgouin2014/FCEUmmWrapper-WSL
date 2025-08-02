#!/bin/bash

# Script pour comparer les builds officiels avec les builds personnalisés
set -e

echo "🔍 Comparaison détaillée des builds..."

# Fonction pour analyser un core
analyze_core() {
    local core_path=$1
    local description=$2
    
    echo ""
    echo "📊 Analyse de $description"
    echo "=================================="
    
    if [ ! -f "$core_path" ]; then
        echo "❌ Core non trouvé: $core_path"
        return 1
    fi
    
    echo "📁 Fichier: $core_path"
    echo "📏 Taille: $(ls -lh "$core_path" | awk '{print $5}')"
    echo "🏗️ Architecture: $(file "$core_path")"
    
    echo ""
    echo "🔗 Dépendances:"
    ldd "$core_path" 2>/dev/null | head -10 || echo "  Impossible de lire les dépendances"
    
    echo ""
    echo "📚 Symboles libretro principaux:"
    nm -D "$core_path" 2>/dev/null | grep -E "(retro_init|retro_run|retro_load_game|retro_get_system_info)" | head -8 || echo "  Impossible de lire les symboles"
    
    echo ""
    echo "🔧 Informations de build:"
    strings "$core_path" 2>/dev/null | grep -E "(GCC|clang|Android|NDK)" | head -5 || echo "  Aucune info de build trouvée"
    
    echo ""
    echo "📋 Sections ELF:"
    readelf -S "$core_path" 2>/dev/null | grep -E "(\.text|\.data|\.bss|\.rodata)" | head -5 || echo "  Impossible de lire les sections"
}

# Analyser les cores officiels
echo "📱 CORES OFFICIELS"
analyze_core "official_cores/x86_64/fceumm_libretro_android.so" "FCEUmm officiel x86_64"
analyze_core "official_cores/arm64-v8a/fceumm_libretro_android.so" "FCEUmm officiel arm64-v8a"

# Analyser vos builds personnalisés (si ils existent)
echo ""
echo "🔄 VOS BUILDS PERSONNALISÉS"

if [ -f "FCEUmmWrapper/app/src/main/jniLibs/x86_64/libfceumm.so.backup" ]; then
    analyze_core "FCEUmmWrapper/app/src/main/jniLibs/x86_64/libfceumm.so.backup" "Votre build x86_64 (backup)"
fi

if [ -f "FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/libfceumm.so.backup" ]; then
    analyze_core "FCEUmmWrapper/app/src/main/jniLibs/arm64-v8a/libfceumm.so.backup" "Votre build arm64-v8a (backup)"
fi

# Comparer les tailles
echo ""
echo "📊 COMPARAISON DES TAILLES"
echo "=========================="

echo "Cores officiels:"
ls -lh official_cores/*/fceumm_libretro_android.so 2>/dev/null || echo "  Aucun core officiel trouvé"

echo ""
echo "Vos builds (backup):"
ls -lh FCEUmmWrapper/app/src/main/jniLibs/*/libfceumm.so.backup 2>/dev/null || echo "  Aucun backup trouvé"

echo ""
echo "🎯 RECOMMANDATIONS:"
echo "1. Si les tailles sont très différentes, vérifiez vos flags de compilation"
echo "2. Si les symboles diffèrent, il y a un problème de compilation"
echo "3. Les cores officiels fonctionnent, gardez-les pour le moment"
echo "4. Analysez les différences pour corriger vos builds personnalisés" 