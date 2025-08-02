cmake_minimum_required(VERSION 3.22)
project(fceummwrapper)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3")

# Configuration pour l'alignement 16 KB requis par Android 16
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wl,-z,relro,-z,now")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wl,-z,relro,-z,now")

# Configuration pour l'alignement des sections
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,relro,-z,now")

# Sources principales
set(SOURCES
    native-lib.cpp
)

add_library(
    fceummwrapper
    SHARED
    ${SOURCES}
)

# Include directories
include_directories(
    ${CMAKE_SOURCE_DIR}
)

find_library(
    log-lib
    log
)

find_library(
    android-lib
    android
)

find_library(
    opensles-lib
    OpenSLES
)

target_link_libraries(
    fceummwrapper
    ${log-lib}
    ${android-lib}
    ${opensles-lib}
)

# Spécifier explicitement les architectures
set_target_properties(fceummwrapper PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/libs/${ANDROID_ABI}
) 