# Config spécifique FCEUmm
option(FCEUMM_PAL "Activer mode PAL" OFF)
option(FCEUMM_ZAPPER "Activer support zapper" ON)
option(FCEUMM_SAVE_STATES "Activer save states" ON)

# Définitions essentielles pour FCEUmm
add_definitions(-DFCEUMM_SAVE_STATES=1)
add_definitions(-DFCEUMM_ZAPPER=1)
add_definitions(-DFCEUMM_PAL=0)
add_definitions(-DFCEUMM_DEBUG=0)
add_definitions(-DFCEUMM_DEBUGGER=0)
add_definitions(-DFCEUMM_NETWORK=0)
add_definitions(-DFCEUMM_OPENGL=0)
add_definitions(-DFCEUMM_SDL=0)
add_definitions(-DFCEUMM_WIN=0)
add_definitions(-DFCEUMM_UNIX=0)
add_definitions(-DFCEUMM_ANDROID=1)
add_definitions(-DFCEUMM_LIBRETRO=1)
add_definitions(-DFCEUMM_LIBRETRO_ANDROID=1) 