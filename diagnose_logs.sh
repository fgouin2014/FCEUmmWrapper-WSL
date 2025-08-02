#!/bin/bash

# Script de diagnostic des logs FCEUmm Wrapper
# Analyse les logs pour identifier les problèmes et proposer des solutions

set -e

echo "=== Diagnostic des logs FCEUmm Wrapper ==="

# Fonction pour analyser les logs
analyze_logs() {
    local log_file="$1"
    
    if [ ! -f "$log_file" ]; then
        echo "❌ Fichier de logs non trouvé: $log_file"
        echo "Utilisation: $0 [fichier_logs]"
        echo "Ou capturez les logs avec: adb logcat > logs.txt"
        exit 1
    fi
    
    echo "📋 Analyse du fichier: $log_file"
    echo ""
    
    # Analyser les SIGSEGV
    echo "🔍 Analyse des SIGSEGV..."
    local sigsegv_count=$(grep -c "SIGSEGV\|segmentation fault" "$log_file" 2>/dev/null || echo "0")
    if [ "$sigsegv_count" -gt 0 ]; then
        echo "❌ $sigsegv_count SIGSEGV détecté(s)"
        echo "   Problème: Le core LibRetro plante lors du chargement"
        echo "   Solution: Utiliser le wrapper amélioré avec gestion des signaux"
    else
        echo "✅ Aucun SIGSEGV détecté"
    fi
    
    # Analyser les commandes non supportées
    echo ""
    echo "🔍 Analyse des commandes non supportées..."
    local unsupported_count=$(grep -c "commande non supportée\|Environment callback non géré" "$log_file" 2>/dev/null || echo "0")
    if [ "$unsupported_count" -gt 0 ]; then
        echo "⚠️  $unsupported_count commande(s) non supportée(s)"
        echo "   Problème: Le wrapper ne gère pas toutes les commandes LibRetro"
        echo "   Solution: Améliorer le callback environment"
    else
        echo "✅ Toutes les commandes sont supportées"
    fi
    
    # Analyser les erreurs de chargement de ROM
    echo ""
    echo "🔍 Analyse des erreurs de chargement de ROM..."
    local rom_errors=$(grep -c "ROM.*échoue\|Échec.*ROM\|ROM.*non trouvée" "$log_file" 2>/dev/null || echo "0")
    if [ "$rom_errors" -gt 0 ]; then
        echo "❌ $rom_errors erreur(s) de chargement de ROM"
        echo "   Problème: Les ROMs ne se chargent pas correctement"
        echo "   Solution: Vérifier le format et les permissions des ROMs"
    else
        echo "✅ Aucune erreur de chargement de ROM"
    fi
    
    # Analyser les problèmes de permissions
    echo ""
    echo "🔍 Analyse des problèmes de permissions..."
    local permission_errors=$(grep -c "Permission denied\|access.*failed\|stat.*failed" "$log_file" 2>/dev/null || echo "0")
    if [ "$permission_errors" -gt 0 ]; then
        echo "❌ $permission_errors erreur(s) de permissions"
        echo "   Problème: L'application n'a pas les permissions nécessaires"
        echo "   Solution: Vérifier les permissions Android"
    else
        echo "✅ Aucun problème de permissions"
    fi
    
    # Analyser les problèmes de mémoire
    echo ""
    echo "🔍 Analyse des problèmes de mémoire..."
    local memory_errors=$(grep -c "out of memory\|malloc.*failed\|new.*failed" "$log_file" 2>/dev/null || echo "0")
    if [ "$memory_errors" -gt 0 ]; then
        echo "❌ $memory_errors erreur(s) de mémoire"
        echo "   Problème: L'application manque de mémoire"
        echo "   Solution: Optimiser l'utilisation mémoire"
    else
        echo "✅ Aucun problème de mémoire"
    fi
    
    # Analyser les problèmes de threads
    echo ""
    echo "🔍 Analyse des problèmes de threads..."
    local thread_errors=$(grep -c "thread.*failed\|pthread.*error\|mutex.*error" "$log_file" 2>/dev/null || echo "0")
    if [ "$thread_errors" -gt 0 ]; then
        echo "❌ $thread_errors erreur(s) de threads"
        echo "   Problème: Problèmes de synchronisation entre threads"
        echo "   Solution: Améliorer la gestion des threads"
    else
        echo "✅ Aucun problème de threads"
    fi
    
    # Analyser les performances
    echo ""
    echo "🔍 Analyse des performances..."
    local performance_issues=$(grep -c "Skipped.*frames\|too much work\|performance.*issue" "$log_file" 2>/dev/null || echo "0")
    if [ "$performance_issues" -gt 0 ]; then
        echo "⚠️  $performance_issues problème(s) de performance"
        echo "   Problème: L'application fait trop de travail sur le thread principal"
        echo "   Solution: Optimiser le rendu et déplacer le travail vers les threads secondaires"
    else
        echo "✅ Aucun problème de performance détecté"
    fi
    
    # Analyser les problèmes de format vidéo
    echo ""
    echo "🔍 Analyse des problèmes de format vidéo..."
    local video_errors=$(grep -c "pixel format\|video.*error\|format.*unsupported" "$log_file" 2>/dev/null || echo "0")
    if [ "$video_errors" -gt 0 ]; then
        echo "❌ $video_errors erreur(s) de format vidéo"
        echo "   Problème: Format vidéo non supporté"
        echo "   Solution: Supporter plus de formats vidéo"
    else
        echo "✅ Aucun problème de format vidéo"
    fi
    
    # Analyser les problèmes audio
    echo ""
    echo "🔍 Analyse des problèmes audio..."
    local audio_errors=$(grep -c "audio.*error\|sample.*rate\|audio.*failed" "$log_file" 2>/dev/null || echo "0")
    if [ "$audio_errors" -gt 0 ]; then
        echo "❌ $audio_errors erreur(s) audio"
        echo "   Problème: Problèmes avec l'audio"
        echo "   Solution: Améliorer la gestion audio"
    else
        echo "✅ Aucun problème audio"
    fi
}

# Fonction pour proposer des solutions
propose_solutions() {
    echo ""
    echo "=== Solutions proposées ==="
    echo ""
    
    # Vérifier si le wrapper amélioré est utilisé
    if grep -q "LibretroWrapper" "$1" 2>/dev/null; then
        echo "✅ Le wrapper amélioré est utilisé"
    else
        echo "❌ Le wrapper amélioré n'est pas utilisé"
        echo "   Solution: Exécuter ./integrate_wrapper_fix.sh"
    fi
    
    # Vérifier la gestion des signaux
    if grep -q "SIGSEGV.*capturé\|sigsetjmp" "$1" 2>/dev/null; then
        echo "✅ La gestion des SIGSEGV est active"
    else
        echo "❌ La gestion des SIGSEGV n'est pas active"
        echo "   Solution: Utiliser le wrapper avec gestion des signaux"
    fi
    
    # Vérifier les callbacks LibRetro
    if grep -q "Environment callback.*cmd=" "$1" 2>/dev/null; then
        echo "✅ Les callbacks LibRetro sont configurés"
    else
        echo "❌ Les callbacks LibRetro ne sont pas configurés"
        echo "   Solution: Configurer les callbacks LibRetro"
    fi
    
    # Vérifier le chargement des ROMs
    if grep -q "ROM.*chargée.*succès" "$1" 2>/dev/null; then
        echo "✅ Les ROMs se chargent correctement"
    else
        echo "❌ Problème de chargement des ROMs"
        echo "   Solution: Vérifier le format et les permissions des ROMs"
    fi
    
    echo ""
    echo "=== Recommandations ==="
    echo ""
    echo "1. Si des SIGSEGV sont détectés :"
    echo "   - Utiliser le wrapper amélioré avec gestion des signaux"
    echo "   - Vérifier la compatibilité du core LibRetro"
    echo ""
    echo "2. Si des commandes non supportées sont détectées :"
    echo "   - Améliorer le callback environment"
    echo "   - Ajouter le support des nouvelles commandes LibRetro"
    echo ""
    echo "3. Si des problèmes de performance sont détectés :"
    echo "   - Optimiser le rendu vidéo"
    echo "   - Déplacer le travail vers les threads secondaires"
    echo ""
    echo "4. Si des problèmes de permissions sont détectés :"
    echo "   - Vérifier les permissions Android"
    echo "   - Utiliser les chemins appropriés pour les fichiers"
    echo ""
    echo "5. Pour améliorer la stabilité générale :"
    echo "   - Utiliser le wrapper avec gestion d'erreurs robuste"
    echo "   - Ajouter des logs détaillés pour le debug"
    echo "   - Implémenter un système de fallback"
}

# Fonction pour générer un rapport
generate_report() {
    local log_file="$1"
    local report_file="diagnostic_report_$(date +%Y%m%d_%H%M%S).txt"
    
    echo "📊 Génération du rapport: $report_file"
    
    {
        echo "=== Rapport de diagnostic FCEUmm Wrapper ==="
        echo "Date: $(date)"
        echo "Fichier analysé: $log_file"
        echo ""
        
        echo "=== Résumé des problèmes ==="
        echo "SIGSEGV: $(grep -c "SIGSEGV\|segmentation fault" "$log_file" 2>/dev/null || echo "0")"
        echo "Commandes non supportées: $(grep -c "commande non supportée\|Environment callback non géré" "$log_file" 2>/dev/null || echo "0")"
        echo "Erreurs ROM: $(grep -c "ROM.*échoue\|Échec.*ROM\|ROM.*non trouvée" "$log_file" 2>/dev/null || echo "0")"
        echo "Erreurs permissions: $(grep -c "Permission denied\|access.*failed\|stat.*failed" "$log_file" 2>/dev/null || echo "0")"
        echo "Erreurs mémoire: $(grep -c "out of memory\|malloc.*failed\|new.*failed" "$log_file" 2>/dev/null || echo "0")"
        echo "Problèmes performance: $(grep -c "Skipped.*frames\|too much work\|performance.*issue" "$log_file" 2>/dev/null || echo "0")"
        echo ""
        
        echo "=== Logs d'erreur importants ==="
        grep -E "(ERROR|FATAL|SIGSEGV|Exception)" "$log_file" | tail -20
        echo ""
        
        echo "=== Logs de debug ==="
        grep -E "(DEBUG|INFO.*wrapper|LibretroWrapper)" "$log_file" | tail -20
        echo ""
        
        echo "=== Recommandations ==="
        echo "1. Utiliser le wrapper amélioré avec gestion des signaux"
        echo "2. Améliorer les callbacks LibRetro"
        echo "3. Optimiser les performances"
        echo "4. Vérifier les permissions et formats de fichiers"
        echo "5. Ajouter plus de logs pour le debug"
        
    } > "$report_file"
    
    echo "✅ Rapport généré: $report_file"
}

# Fonction principale
main() {
    local log_file="$1"
    
    if [ -z "$log_file" ]; then
        echo "Capture des logs en cours..."
        adb logcat -d > logs.txt
        log_file="logs.txt"
        echo "Logs capturés dans: $log_file"
        echo ""
    fi
    
    analyze_logs "$log_file"
    propose_solutions "$log_file"
    generate_report "$log_file"
    
    echo ""
    echo "🎯 Diagnostic terminé !"
    echo "Consultez le rapport généré pour plus de détails."
}

# Exécuter le diagnostic
main "$1" 