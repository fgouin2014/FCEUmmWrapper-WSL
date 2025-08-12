# Script de build rapide pour l'application (sans recompiler les cores)
Write-Host "=== Build rapide de l'application ===" -ForegroundColor Green

# Vérifier si les cores sont présents
$coresPath = "app\src\main\assets\coresCompiled"
$architectures = @("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
$coresMissing = $false

foreach ($arch in $architectures) {
    $coreFile = "$coresPath\$arch\fceumm_libretro_android.so"
    if (-not (Test-Path $coreFile)) {
        Write-Host "❌ Core manquant pour $arch : $coreFile" -ForegroundColor Red
        $coresMissing = $true
    }
}

if ($coresMissing) {
    Write-Host "⚠️  Certains cores sont manquants. Exécutez d'abord : .\integrate_cores.ps1" -ForegroundColor Yellow
    $response = Read-Host "Voulez-vous continuer quand même ? (o/n)"
    if ($response -ne "o" -and $response -ne "O") {
        Write-Host "Build annulé." -ForegroundColor Red
        exit 1
    }
}

# Nettoyer uniquement les fichiers de build Android (pas les cores)
Write-Host "🧹 Nettoyage des fichiers de build..." -ForegroundColor Cyan
& .\gradlew.bat clean

# Build rapide avec cache
Write-Host "🔨 Compilation de l'application..." -ForegroundColor Cyan
& .\gradlew.bat assembleDebug --parallel --max-workers=8 --daemon

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Build réussi !" -ForegroundColor Green
    Write-Host "📱 APK généré dans : app\build\outputs\apk\debug\" -ForegroundColor Cyan
} else {
    Write-Host "❌ Erreur lors du build" -ForegroundColor Red
    exit 1
}
