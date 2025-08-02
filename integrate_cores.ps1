# Script d'intégration des cores
Write-Host "=== Intégration des cores ==="

# Créer le dossier assets s'il n'existe pas
$assetsPath = "app\src\main\assets"
if (-not (Test-Path $assetsPath)) {
    New-Item -ItemType Directory -Path $assetsPath -Force
    Write-Host "Dossier assets créé"
}

# Créer le dossier coresCompiled
$coresPath = "$assetsPath\coresCompiled"
if (-not (Test-Path $coresPath)) {
    New-Item -ItemType Directory -Path $coresPath -Force
    Write-Host "Dossier coresCompiled créé"
}

# Copier les cores pour chaque architecture
$architectures = @("arm64-v8a", "armeabi-v7a", "x86", "x86_64")

foreach ($arch in $architectures) {
    $archPath = "$coresPath\$arch"
    if (-not (Test-Path $archPath)) {
        New-Item -ItemType Directory -Path $archPath -Force
    }
    
    # Copier le core correspondant
    $sourceCore = ""
    if ($arch -eq "arm64-v8a") {
        $sourceCore = "official_cores\arm64-v8a\fceumm_libretro_android.so"
    } elseif ($arch -eq "x86_64") {
        $sourceCore = "official_cores\x86_64\fceumm_libretro_android.so"
    } else {
        # Pour les autres architectures, utiliser le core arm64-v8a comme fallback
        $sourceCore = "official_cores\arm64-v8a\fceumm_libretro_android.so"
    }
    
    if (Test-Path $sourceCore) {
        Copy-Item -Path $sourceCore -Destination "$archPath\fceumm_libretro_android.so" -Force
        Write-Host "✅ Core copié pour $arch"
    } else {
        Write-Host "❌ Core non trouvé pour $arch"
    }
}

# Créer le dossier roms s'il n'existe pas
$romsPath = "$assetsPath\roms"
if (-not (Test-Path $romsPath)) {
    New-Item -ItemType Directory -Path $romsPath -Force
    Write-Host "Dossier roms créé"
}

$nesPath = "$romsPath\nes"
if (-not (Test-Path $nesPath)) {
    New-Item -ItemType Directory -Path $nesPath -Force
    Write-Host "Dossier nes créé"
}

Write-Host "=== Cores intégrés ==="
Write-Host "Vous pouvez maintenant recompiler l'APK avec : .\gradlew.bat clean assembleDebug" 