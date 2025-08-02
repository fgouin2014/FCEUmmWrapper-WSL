void libretro_run() {
    // Protection SIGSEGV pour retro_run
    struct sigaction old_action;
    struct sigaction new_action;
    memset(&new_action, 0, sizeof(new_action));
    new_action.sa_handler = [](int sig) {
        LOGE("Signal SIGSEGV capturé dans retro_run - sortie propre");
        segv_occurred = true;
        longjmp(segv_jmp_buf, 1);
    };
    sigaction(SIGSEGV, &new_action, &old_action);
    
    // Utiliser setjmp pour capturer les erreurs
    if (setjmp(segv_jmp_buf) == 0) {
        if (retro_run_func) {
            retro_run_func();
        }
        
        // Restaurer le handler par défaut
        sigaction(SIGSEGV, &old_action, nullptr);
    } else {
        // SIGSEGV dans retro_run
        sigaction(SIGSEGV, &old_action, nullptr);
        LOGE("SIGSEGV dans retro_run - passage en mode fallback");
        
        // Mode fallback : afficher un écran noir avec un message
        // ou utiliser un core alternatif
        usleep(16667); // ~60 FPS
    }
}
