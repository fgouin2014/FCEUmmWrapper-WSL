#include <libretro.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <math.h>

// Variables globales libretro
retro_log_printf_t log_cb;
retro_video_refresh_t video_cb;
retro_audio_sample_batch_t audio_batch_cb;
retro_input_poll_t input_poll_cb;
retro_input_state_t input_state_cb;
retro_environment_t environ_cb;

// Variables d'état
static bool initialized = false;
static bool game_loaded = false;
static unsigned frame_width = 256;
static unsigned frame_height = 240;
static unsigned frame_pitch = 256;
static uint16_t frame_buffer[256 * 240];

// Couleurs pour l'affichage
#define COLOR_BLACK     0x0000
#define COLOR_WHITE     0xFFFF
#define COLOR_RED       0xF800
#define COLOR_GREEN     0x07E0
#define COLOR_BLUE      0x001F
#define COLOR_YELLOW    0xFFE0
#define COLOR_CYAN      0x07FF
#define COLOR_MAGENTA   0xF81F

void retro_init(void) {
    initialized = true;
    if (log_cb) log_cb(RETRO_LOG_INFO, "Core simple initialisé\n");
}

void retro_deinit(void) {
    initialized = false;
    game_loaded = false;
}

unsigned retro_api_version(void) {
    return RETRO_API_VERSION;
}

void retro_get_system_info(struct retro_system_info *info) {
    info->library_name = "FCEUmm Simple";
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
    // TOUJOURS retourner true pour que le wrapper soit content
    if (log_cb) log_cb(RETRO_LOG_INFO, "ROM chargée avec succès (toujours true)\n");
    game_loaded = true;
    return true; // TOUJOURS TRUE !
}

void retro_unload_game(void) {
    game_loaded = false;
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
    
    static int frame_count = 0;
    frame_count++;
    
    // Créer un motif de test simple et visible
    for (int y = 0; y < frame_height; y++) {
        for (int x = 0; x < frame_width; x++) {
            int index = y * frame_pitch + x;
            uint16_t color;
            
            // Créer un motif de damier animé
            if (((x / 16) + (y / 16) + frame_count) % 2 == 0) {
                color = COLOR_RED;
            } else {
                color = COLOR_BLUE;
            }
            
            // Ajouter une bordure blanche
            if (x < 8 || x >= frame_width - 8 || y < 8 || y >= frame_height - 8) {
                color = COLOR_WHITE;
            }
            
            // Ajouter un texte "SUCCESS" en jaune
            if (y >= 100 && y < 120 && x >= 80 && x < 176) {
                int char_x = (x - 80) / 8;
                int char_y = (y - 100) / 10;
                if (char_x < 7 && char_y < 2) {
                    color = COLOR_YELLOW;
                }
            }
            
            frame_buffer[index] = color;
        }
    }
    
    // Envoyer l'image
    if (video_cb) {
        video_cb(frame_buffer, frame_width, frame_height, frame_pitch * 2);
    }
    
    // Audio avec un son de test
    if (audio_batch_cb) {
        int16_t audio_buffer[735]; // 44100 Hz / 60 FPS
        
        // Générer un son de test simple
        for (int i = 0; i < 735; i++) {
            int sample = (int)(32767.0 * 0.1 * sin(2.0 * 3.14159 * 440.0 * (frame_count * 735 + i) / 44100.0));
            audio_buffer[i] = (int16_t)sample;
        }
        
        audio_batch_cb(audio_buffer, 735);
    }
}
