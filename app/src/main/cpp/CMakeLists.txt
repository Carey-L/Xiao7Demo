cmake_minimum_required(VERSION 3.22.1)

project("TextMetrics")

add_library(${CMAKE_PROJECT_NAME} SHARED
        TextMetrics.cpp)

target_link_libraries(${CMAKE_PROJECT_NAME}
    android
    log)
