#pragma once

#include <locale.h>

#include <android/log.h>
#include <jni.h>

#include <mpv/client.h>
#include <mpv/opengl_cb.h>

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "Player", __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "Player", __VA_ARGS__))

struct player_context {
    JavaVM* vm;

    mpv_handle *mpv;
};

JNIEXPORT int JNICALL Java_org_floens_player_mpv_MpvCore_native_1initialize(JNIEnv *env, jobject instance);

int initialize(JNIEnv *env);
