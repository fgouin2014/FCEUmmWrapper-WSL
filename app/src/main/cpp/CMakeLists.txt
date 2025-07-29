cmake_minimum_required(VERSION 3.22)
project(fceummwrapper)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3")

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

target_link_libraries(
    fceummwrapper
    ${log-lib}
    ${android-lib}
) 