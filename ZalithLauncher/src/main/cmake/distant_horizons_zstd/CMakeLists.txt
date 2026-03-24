cmake_minimum_required(VERSION 3.22.1)

project(distant_horizons_zstd_wrapper C)

add_library(zalith_dh_zstd SHARED distant_horizons_zstd_wrapper.c)

target_link_libraries(zalith_dh_zstd PRIVATE dl log)

set_target_properties(
    zalith_dh_zstd
    PROPERTIES
        OUTPUT_NAME "zalith_dh_zstd"
        LIBRARY_OUTPUT_DIRECTORY "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}"
)
