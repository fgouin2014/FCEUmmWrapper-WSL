#include <libretro.h>
#include <stdlib.h>
#include <string.h>

// Variables globales libretro
retro_log_printf_t log_cb;
retro_video_refresh_t video_cb;
retro_audio_sample_batch_t audio_batch_cb;
retro_input_poll_t input_poll_cb;
retro_input_state_t input_state_cb;
retro_environment_t environ_cb;

// Variables d'état
static bool initialized = false;
static unsigned frame_width = 256;
static unsigned frame_height = 240;
static unsigned frame_pitch = 256;
static uint16_t frame_buffer[256 * 240];

void retro_init(void) {
    // Initialisation minimale
    initialized = true;
}

void retro_deinit(void) {
    initialized = false;
}

unsigned retro_api_version(void) {
    return RETRO_API_VERSION;
}

void retro_get_system_info(struct retro_system_info *info) {
    info->library_name = "FCEUmm Minimal";
    info->library_version = "1.0.0";
    info->valid_extensions = "nes|NES";
    info->need_fullpath = false;
    info->block_extract = false;
}

void retro_get_system_av_info(struct retro_system_av_info *info) {
    info->geometry.base_width = frame_width;
    info->geometry.base_height = frame_height;
    info->geometry.max_width = frame_width;
    info->geometry.max_height = frame_height;
    info->geometry.aspect_ratio = 4.0f / 3.0f;
    
    info->timing.fps = 60.0;
    info->timing.sample_rate = 44100.0;
}

void retro_set_environment(retro_environment_t callback) {
    environ_cb = callback;
}

void retro_set_video_refresh(retro_video_refresh_t callback) {
    video_cb = callback;
}

void retro_set_audio_sample_batch(retro_audio_sample_batch_t callback) {
    audio_batch_cb = callback;
}

void retro_set_input_poll(retro_input_poll_t callback) {
    input_poll_cb = callback;
}

void retro_set_input_state(retro_input_state_t callback) {
    input_state_cb = callback;
}

void retro_set_controller_port_device(unsigned port, unsigned device) {
    // Pas d'implémentation pour le moment
}

void retro_reset(void) {
    // Reset minimal
}

bool retro_load_game(const struct retro_game_info *game) {
    // Chargement minimal
    return true;
}

void retro_unload_game(void) {
    // Déchargement minimal
}

unsigned retro_get_region(void) {
    return RETRO_REGION_NTSC;
}

void *retro_get_memory_data(unsigned id) {
    return NULL;
}

size_t retro_get_memory_size(unsigned id) {
    return 0;
}

void retro_cheat_reset(void) {
    // Pas d'implémentation
}

void retro_cheat_set(unsigned index, bool enabled, const char *code) {
    // Pas d'implémentation
}

void retro_run(void) {
    if (!initialized) return;
    
    // Générer une image de test simple
    static int frame_count = 0;
    frame_count++;
    
    // Créer un motif de test
    for (int y = 0; y < frame_height; y++) {
        for (int x = 0; x < frame_width; x++) {
            int index = y * frame_pitch + x;
            uint16_t color = ((frame_count + x + y) % 256) << 8;
            frame_buffer[index] = color;
        }
    }
    
    // Envoyer l'image
    if (video_cb) {
        video_cb(frame_buffer, frame_width, frame_height, frame_pitch * 2);
    }
    
    // Audio silencieux
    if (audio_batch_cb) {
        int16_t audio_buffer[735]; // 44100 Hz / 60 FPS
        memset(audio_buffer, 0, sizeof(audio_buffer));
        audio_batch_cb(audio_buffer, 735);
    }
}
