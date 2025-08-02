#include <stdio.h>
#include <stdlib.h>

// Fonctions minimales pour un core libretro
void retro_init(void) { printf("Core initialized\n"); }
void retro_deinit(void) { printf("Core deinitialized\n"); }
unsigned retro_api_version(void) { return 1; }

void retro_get_system_info(void *info) {
    printf("System info requested\n");
}

void retro_get_system_av_info(void *info) {
    printf("AV info requested\n");
}

void retro_set_environment(void *callback) {
    printf("Environment set\n");
}

void retro_set_video_refresh(void *callback) {
    printf("Video refresh set\n");
}

void retro_set_audio_sample(void *callback) {
    printf("Audio sample set\n");
}

void retro_set_audio_sample_batch(void *callback) {
    printf("Audio batch set\n");
}

void retro_set_input_poll(void *callback) {
    printf("Input poll set\n");
}

void retro_set_input_state(void *callback) {
    printf("Input state set\n");
}

void retro_set_controller_port_device(unsigned port, unsigned device) {
    printf("Controller device set\n");
}

void retro_reset(void) {
    printf("Reset called\n");
}

void retro_run(void) {
    printf("Run called\n");
}

size_t retro_serialize_size(void) { return 0; }
int retro_serialize(void *data, size_t size) { return 0; }
int retro_unserialize(const void *data, size_t size) { return 0; }
void retro_cheat_reset(void) {}
void retro_cheat_set(unsigned index, int enabled, const char *code) {}
int retro_load_game(const void *game) { return 1; }
int retro_load_game_special(unsigned game_type, const void *info, size_t num_info) { return 0; }
void retro_unload_game(void) {}
unsigned retro_get_region(void) { return 0; }
void *retro_get_memory_data(unsigned id) { return NULL; }
size_t retro_get_memory_size(unsigned id) { return 0; }
