#include <signal.h>
#include <setjmp.h>
#include <stdio.h>
#include <stdlib.h>

static jmp_buf sigsegv_env;
static int sigsegv_occurred = 0;

void sigsegv_handler(int sig) {
    sigsegv_occurred = 1;
    longjmp(sigsegv_env, 1);
}

// Fonction sécurisée pour initialiser le core
int safe_retro_init(void (*retro_init_func)(void)) {
    signal(SIGSEGV, sigsegv_handler);
    sigsegv_occurred = 0;
    
    if (setjmp(sigsegv_env) == 0) {
        retro_init_func();
        return 1; // Succès
    } else {
        printf("SIGSEGV capturé pendant retro_init\n");
        return 0; // Échec
    }
}

// Fonction sécurisée pour charger une ROM
int safe_retro_load_game(void (*retro_load_game_func)(const struct retro_game_info*), 
                         const struct retro_game_info* game_info) {
    signal(SIGSEGV, sigsegv_handler);
    sigsegv_occurred = 0;
    
    if (setjmp(sigsegv_env) == 0) {
        return retro_load_game_func(game_info);
    } else {
        printf("SIGSEGV capturé pendant retro_load_game\n");
        return 0; // Échec
    }
}

// Fonction sécurisée pour exécuter le core
int safe_retro_run(void (*retro_run_func)(void)) {
    signal(SIGSEGV, sigsegv_handler);
    sigsegv_occurred = 0;
    
    if (setjmp(sigsegv_env) == 0) {
        retro_run_func();
        return 1; // Succès
    } else {
        printf("SIGSEGV capturé pendant retro_run\n");
        return 0; // Échec
    }
}
