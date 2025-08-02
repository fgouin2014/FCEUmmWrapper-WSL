#!/bin/bash
set -e

cd FCEUmmWrapper
./gradlew clean assembleRelease
cd ..

APK=FCEUmmWrapper/app/build/outputs/apk/release/app-release.apk
if [ -f "$APK" ]; then
  echo "APK généré : $APK"
else
  echo "Erreur : APK non généré !"; exit 1;
fi 