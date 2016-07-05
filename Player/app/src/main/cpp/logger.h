#pragma once

#include <android/log.h>
#include <mpv/client.h>

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "Player", __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "Player", __VA_ARGS__))

void log_mpv_error(const char *tag, int error);
