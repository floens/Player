#pragma once

#include <pthread.h>

#include <mpv/client.h>
#include <mpv/opengl_cb.h>

struct render_thread {
    pthread_t thread;

    pthread_mutex_t lock;
    pthread_cond_t ready;

    int running;

    mpv_handle *mpv;
    mpv_opengl_cb_context *opengl_cb_context;
    EGLDisplay *egl_display;
    EGLSurface *egl_surface;
    EGLContext *egl_context;
    int width, height;

    pthread_cond_t frame_wait;
    pthread_mutex_t frame_wait_lock;
    int frame_available;
};

struct render_thread *render_thread_create(
        mpv_handle *mpv,
        mpv_opengl_cb_context *opengl_cb_context,
        EGLDisplay *egl_display,
        EGLSurface *egl_surface,
        EGLContext *egl_context
);

void render_thread_start(struct render_thread *render_thread);

void render_thread_stop(struct render_thread *render_thread);

void render_thread_resize(struct render_thread *render_thread, int width, int height);

void render_thread_destroy(struct render_thread *render_thread);
