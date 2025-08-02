#!/bin/bash

# Script pour diagnostiquer et corriger le problème d'émulation
set -e

echo "🔧 Diagnostic et correction du problème d'émulation..."
echo "=================================================="

# Vérifier les cores actuels
echo ""
echo "📱 Analyse des cores actuels..."
for arch in x86_64 arm64-v8a; do
    core_path="FCEUmmWrapper/app/src/main/jniLibs/$arch/libfceumm.so"
    if [ -f "$core_path" ]; then
        echo "✅ $arch: $(ls -lh "$core_path" | awk '{print $5}')"
        
        # Vérifier les symboles libretro
        echo "   Symboles libretro:"
        nm -D "$core_path" 2>/dev/null | grep -E "(retro_init|retro_load_game|retro_run)" | head -5 || echo "   ❌ Impossible de lire les symboles"
    else
        echo "❌ $arch: Core manquant"
    fi
done

# Vérifier les cores officiels
echo ""
echo "📥 Vérification des cores officiels..."
if [ -f "official_cores/x86_64/fceumm_libretro_android.so" ]; then
    echo "✅ Core officiel x86_64 disponible"
    echo "   Taille: $(ls -lh official_cores/x86_64/fceumm_libretro_android.so | awk '{print $5}')"
    
    # Comparer les symboles
    echo "   Symboles officiels:"
    nm -D official_cores/x86_64/fceumm_libretro_android.so 2>/dev/null | grep -E "(retro_init|retro_load_game|retro_run)" | head -5 || echo "   ❌ Impossible de lire les symboles"
else
    echo "❌ Core officiel x86_64 manquant"
fi

# Créer un script de test de compatibilité
echo ""
echo "🧪 Création d'un test de compatibilité..."

cat > test_core_compatibility.c << 'EOF'
#include <stdio.h>
#include <dlfcn.h>
#include <signal.h>

void signal_handler(int sig) {
    printf("SIGSEGV capturé - Core incompatible\n");
    exit(1);
}

int main() {
    signal(SIGSEGV, signal_handler);
    
    void *handle = dlopen("./test_core.so", RTLD_LAZY);
    if (!handle) {
        printf("❌ Impossible de charger le core: %s\n", dlerror());
        return 1;
    }
    
    printf("✅ Core chargé avec succès\n");
    
    // Test des fonctions critiques
    void (*retro_init)(void) = dlsym(handle, "retro_init");
    if (!retro_init) {
        printf("❌ retro_init non trouvé\n");
        return 1;
    }
    
    printf("✅ retro_init trouvé\n");
    
    // Test d'initialisation
    retro_init();
    printf("✅ retro_init exécuté sans crash\n");
    
    dlclose(handle);
    printf("✅ Test terminé avec succès\n");
    return 0;
}
EOF

# Compiler le test
gcc -o test_core_compatibility test_core_compatibility.c -ldl

echo "✅ Test de compatibilité créé"

# Créer un script de correction automatique
echo ""
echo "🔧 Création d'un script de correction..."

cat > fix_core_issue.sh << 'EOF'
#!/bin/bash

# Script de correction du problème de core
set -e

echo "🔧 Correction du problème de core..."

# Option 1: Utiliser un core plus stable
echo "📥 Téléchargement d'un core plus stable..."
wget -O stable_core.zip "https://buildbot.libretro.com/nightly/android/latest/x86_64/fceumm_libretro_android.so.zip"
unzip -o stable_core.zip -d stable_cores/

# Option 2: Compiler un core personnalisé avec des flags de sécurité
echo "🔨 Compilation d'un core sécurisé..."
mkdir -p secure_build
cd secure_build

# Cloner FCEUmm si nécessaire
if [ ! -d "fceumm" ]; then
    git clone https://github.com/libretro/libretro-fceumm.git fceumm
fi

cd fceumm

# Compiler avec des flags de sécurité
make -f Makefile.libretro platform=android \
    CFLAGS="-O2 -fPIC -DANDROID -D__ANDROID__ -DHAVE_NEON=0 -DHAVE_SSE=0" \
    LDFLAGS="-shared -Wl,--no-undefined -Wl,--version-script=link.T"

# Copier le core sécurisé
cp fceumm_libretro_android.so ../../secure_core.so

echo "✅ Core sécurisé compilé"

# Option 3: Modifier le wrapper pour gérer les SIGSEGV
echo "🔧 Modification du wrapper pour gérer les SIGSEGV..."

cat > wrapper_patch.c << 'PATCH_EOF'
// Patch pour gérer les SIGSEGV dans le wrapper
#include <signal.h>
#include <setjmp.h>

static jmp_buf sigsegv_env;

void sigsegv_handler(int sig) {
    longjmp(sigsegv_env, 1);
}

// Fonction sécurisée pour charger une ROM
int safe_load_rom(const char* rom_path) {
    signal(SIGSEGV, sigsegv_handler);
    
    if (setjmp(sigsegv_env) == 0) {
        // Tentative de chargement sécurisée
        return retro_load_game_func(&game_info);
    } else {
        // SIGSEGV capturé - utiliser un fallback
        printf("SIGSEGV capturé, utilisation du mode fallback\n");
        return 0;
    }
}
PATCH_EOF'

echo "✅ Patch de sécurité créé"
EOF

chmod +x fix_core_issue.sh

echo ""
echo "🎯 Solutions proposées :"
echo "1. Tester la compatibilité des cores"
echo "2. Utiliser un core plus stable"
echo "3. Compiler un core personnalisé sécurisé"
echo "4. Modifier le wrapper pour gérer les SIGSEGV"

echo ""
echo "Pour corriger le problème, exécutez :"
echo "  ./fix_core_issue.sh" 