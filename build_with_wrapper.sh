#!/bin/bash

echo "=== Build avec wrapper amélioré ==="

# Nettoyer le build précédent
./gradlew clean

# Build avec le nouveau wrapper
./gradlew assembleDebug

echo "✅ Build terminé"
echo ""
echo "Pour installer sur l'appareil :"
echo "adb install app/build/outputs/apk/debug/app-debug.apk"
