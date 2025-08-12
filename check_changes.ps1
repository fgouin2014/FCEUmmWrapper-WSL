# Script pour vérifier les modifications et déterminer le type de build nécessaire
Write-Host "=== Vérification des modifications ===" -ForegroundColor Green

$needsCoreRebuild = $false
$needsAppRebuild = $false

# Vérifier les modifications dans les sources des cores
$coreSourceDirs = @(
    "real_fceumm_build",
    "custom_core_build", 
    "simple_core_build",
    "advanced_core_build"
)

foreach ($dir in $coreSourceDirs) {
    if (Test-Path $dir) {
        $lastModified = (Get-ChildItem -Path $dir -Recurse | Sort-Object LastWriteTime -Descending | Select-Object -First 1).LastWriteTime
        $coreFile = "app\src\main\assets\coresCompiled\arm64-v8a\fceumm_libretro_android.so"
        
        if (Test-Path $coreFile) {
            $coreLastModified = (Get-Item $coreFile).LastWriteTime
            if ($lastModified -gt $coreLastModified) {
                Write-Host "🔄 Sources des cores modifiées dans $dir" -ForegroundColor Yellow
                $needsCoreRebuild = $true
            }
        } else {
            Write-Host "❌ Core manquant, reconstruction nécessaire" -ForegroundColor Red
            $needsCoreRebuild = $true
        }
    }
}

# Vérifier les modifications dans l'application
$appSourceDirs = @(
    "app\src\main\java",
    "app\src\main\cpp",
    "app\src\main\res"
)

foreach ($dir in $appSourceDirs) {
    if (Test-Path $dir) {
        $lastModified = (Get-ChildItem -Path $dir -Recurse | Sort-Object LastWriteTime -Descending | Select-Object -First 1).LastWriteTime
        $apkFile = "app\build\outputs\apk\debug\app-debug.apk"
        
        if (Test-Path $apkFile) {
            $apkLastModified = (Get-Item $apkFile).LastWriteTime
            if ($lastModified -gt $apkLastModified) {
                Write-Host "🔄 Sources de l'application modifiées dans $dir" -ForegroundColor Yellow
                $needsAppRebuild = $true
            }
        } else {
            Write-Host "❌ APK manquant, compilation nécessaire" -ForegroundColor Red
            $needsAppRebuild = $true
        }
    }
}

# Recommandation
Write-Host "`n=== Recommandation ===" -ForegroundColor Cyan
if ($needsCoreRebuild) {
    Write-Host "🔨 Build complet recommandé : .\build_complete.ps1" -ForegroundColor Yellow
} elseif ($needsAppRebuild) {
    Write-Host "⚡ Build rapide recommandé : .\build_app_fast.ps1" -ForegroundColor Green
} else {
    Write-Host "✅ Aucune modification détectée, build non nécessaire" -ForegroundColor Green
}
