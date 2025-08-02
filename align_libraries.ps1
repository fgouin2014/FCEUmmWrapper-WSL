# Script pour aligner les bibliothèques sur des limites de 16 KB
# Nécessite Android NDK pour les outils

$ndkPath = "C:\Users\Quentin\AppData\Local\Android\Sdk\ndk\28.2.13676358"
$assetsPath = "app\src\main\assets\coresCompiled"

Write-Host "Alignement des bibliothèques pour Android 16..." -ForegroundColor Green

# Fonction pour aligner une bibliothèque
function Align-Library {
    param($arch, $library)
    
    $inputPath = "$assetsPath\$arch\$library"
    $outputPath = "$assetsPath\$arch\aligned_$library"
    
    if (Test-Path $inputPath) {
        Write-Host "Alignement de $library pour $arch..." -ForegroundColor Yellow
        
        # Utiliser objcopy pour aligner les sections
        $objcopyPath = "$ndkPath\toolchains\llvm\prebuilt\windows-x86_64\bin\$arch-objcopy.exe"
        
        if (Test-Path $objcopyPath) {
            & $objcopyPath --align-sections=0x4000 $inputPath $outputPath
            if ($LASTEXITCODE -eq 0) {
                # Remplacer l'original par l'aligné
                Move-Item $outputPath $inputPath -Force
                Write-Host "✓ $library aligné avec succès" -ForegroundColor Green
            } else {
                Write-Host "✗ Échec de l'alignement de $library" -ForegroundColor Red
            }
        } else {
            Write-Host "✗ Outil objcopy non trouvé pour $arch" -ForegroundColor Red
        }
    }
}

# Aligner les bibliothèques utilisées
$architectures = @("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
$libraries = @("fceumm_libretro_android.so")

foreach ($arch in $architectures) {
    foreach ($lib in $libraries) {
        Align-Library -arch $arch -library $lib
    }
}

Write-Host "Alignement terminé !" -ForegroundColor Green 