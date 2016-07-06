#pragma once

#include <locale.h>

#include <android/log.h>
#include <android/native_window.h>
#include <jni.h>
#include <EGL/egl.h>

#include <mpv/client.h>
#include <mpv/opengl_cb.h>

#include "event_thread.h"
#include "render_thread.h"

struct player_context {
    JNIEnv *env;
    JavaVM *vm;
    jobject core_instance;

    mpv_handle *mpv;
    mpv_opengl_cb_context *opengl_cb_context;

    struct event_thread *event_thread;
    struct render_thread *render_thread;
};

int player_initialize(JNIEnv *env, jobject core_instance);

void player_destroy();

int player_bind();

void player_resize(int width, int height);

void player_unbind();

void player_observe_property(uint64_t userdata, const char *name, mpv_format format);

void player_handle_command(const char **command);
