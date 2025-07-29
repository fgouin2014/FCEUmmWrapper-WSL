#ifndef FCEUMM_CORE_H
#define FCEUMM_CORE_H

#include <libretro.h>

#ifdef __cplusplus
extern "C" {
#endif

// Fonctions du core FCEUmm
void retro_init(void);
void retro_deinit(void);
void retro_run(void);
bool retro_load_game(const struct retro_game_info*);
void retro_unload_game(void);
void retro_get_system_info(struct retro_system_info*);
void retro_get_system_av_info(struct retro_system_av_info*);
void retro_set_environment(retro_environment_t);
void retro_set_video_refresh(retro_video_refresh_t);
void retro_set_audio_sample_batch(retro_audio_sample_batch_t);
void retro_set_input_poll(retro_input_poll_t);
void retro_set_input_state(retro_input_state_t);

#ifdef __cplusplus
}
#endif

#endif // FCEUMM_CORE_H 