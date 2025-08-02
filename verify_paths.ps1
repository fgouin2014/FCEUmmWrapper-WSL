# Script pour vérifier les chemins utilisés dans l'application
Write-Host "=== VÉRIFICATION DES CHEMINS DE L'APPLICATION ===" -ForegroundColor Green

Write-Host "`n📁 STRUCTURE DES ASSETS:" -ForegroundColor Yellow
Write-Host "Assets disponibles:" -ForegroundColor Cyan
Get-ChildItem -Path "app/src/main/assets" -Recurse -Directory | ForEach-Object {
    $relativePath = $_.FullName.Replace((Get-Location).Path + "\", "")
    Write-Host "  📂 $relativePath" -ForegroundColor White
}

Write-Host "`n🎮 ROMS DISPONIBLES:" -ForegroundColor Yellow
Get-ChildItem -Path "app/src/main/assets/roms/nes" -File | ForEach-Object {
    $size = [math]::Round($_.Length / 1KB, 1)
    Write-Host "  📄 $($_.Name) ($size KB)" -ForegroundColor White
}

Write-Host "`n🔧 CORES DISPONIBLES:" -ForegroundColor Yellow
Write-Host "Cores pré-compilés (coresCompiled):" -ForegroundColor Cyan
Get-ChildItem -Path "app/src/main/assets/coresCompiled" -Recurse -File | ForEach-Object {
    $relativePath = $_.FullName.Replace((Get-Location).Path + "\", "")
    $size = [math]::Round($_.Length / 1KB, 1)
    Write-Host "  📦 $relativePath ($size KB)" -ForegroundColor White
}

Write-Host "`nCores personnalisés (coreCustom):" -ForegroundColor Cyan
Get-ChildItem -Path "app/src/main/assets/coreCustom" -Recurse -File | ForEach-Object {
    $relativePath = $_.FullName.Replace((Get-Location).Path + "\", "")
    $size = [math]::Round($_.Length / 1KB, 1)
    Write-Host "  🔧 $relativePath ($size KB)" -ForegroundColor White
}

Write-Host "`n🔍 CHEMINS UTILISÉS DANS LE CODE:" -ForegroundColor Yellow

Write-Host "`n📱 JAVA (MainActivity.java):" -ForegroundColor Cyan
Write-Host "  • ROM par défaut: getFilesDir() + 'marioduckhunt.nes'" -ForegroundColor White
Write-Host "  • ROM sélectionnée: getFilesDir() + selected_rom" -ForegroundColor White
Write-Host "  • Core: getFilesDir() + 'cores/fceumm_libretro_android.so'" -ForegroundColor White
Write-Host "  • Assets ROMs: 'roms/nes/' + nom_fichier" -ForegroundColor White
Write-Host "  • Assets Cores pré-compilés: 'coresCompiled/' + arch + '/fceumm_libretro_android.so'" -ForegroundColor White
Write-Host "  • Assets Cores personnalisés: 'coreCustom/' + arch + '/fceumm_libretro_android.so'" -ForegroundColor White

Write-Host "`n⚙️ NATIF (native-lib.cpp):" -ForegroundColor Cyan
Write-Host "  • Core hardcodé: '/data/data/com.fceumm.wrapper/files/cores/fceumm_libretro_android.so'" -ForegroundColor White
Write-Host "  • ROM: chemin passé depuis Java" -ForegroundColor White

Write-Host "`n🎯 LOGIQUE DE SÉLECTION:" -ForegroundColor Yellow
Write-Host "1. Vérifier SharedPreferences pour core type (compiled/custom)" -ForegroundColor White
Write-Host "2. Si custom: essayer coreCustom/arch/fceumm_libretro_android.so" -ForegroundColor White
Write-Host "3. Si échec: fallback vers coresCompiled/arch/fceumm_libretro_android.so" -ForegroundColor White
Write-Host "4. Si compiled: utiliser directement coresCompiled/arch/fceumm_libretro_android.so" -ForegroundColor White

Write-Host "`n📊 ARCHITECTURES SUPPORTÉES:" -ForegroundColor Yellow
Write-Host "  • arm64-v8a (ARM 64-bit)" -ForegroundColor White
Write-Host "  • armeabi-v7a (ARM 32-bit)" -ForegroundColor White
Write-Host "  • x86_64 (Intel 64-bit)" -ForegroundColor White
Write-Host "  • x86 (Intel 32-bit)" -ForegroundColor White

Write-Host "`n✅ VÉRIFICATION TERMINÉE" -ForegroundColor Green
Write-Host "L'application utilise les chemins suivants:" -ForegroundColor Cyan
Write-Host "  • ROMs copiées vers: /data/data/com.fceumm.wrapper/files/" -ForegroundColor White
Write-Host "  • Cores copiés vers: /data/data/com.fceumm.wrapper/files/cores/" -ForegroundColor White
Write-Host "  • Core natif hardcodé: /data/data/com.fceumm.wrapper/files/cores/fceumm_libretro_android.so" -ForegroundColor White 