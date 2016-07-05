#include <stdlib.h>

#include <EGL/egl.h>
#include <mpv/client.h>
#include <mpv/opengl_cb.h>

#include "logger.h"

#include "render_thread.h"


static void *render_thread_run(void *data);

struct render_thread *render_thread_create(
        mpv_handle *mpv,
        mpv_opengl_cb_context *opengl_cb_context,
        EGLDisplay *egl_display,
        EGLSurface *egl_surface,
        EGLContext *egl_context
) {
    struct render_thread *render_thread = malloc(sizeof(struct render_thread));
    pthread_mutex_init(&render_thread->lock, NULL);
    pthread_cond_init(&render_thread->ready, NULL);

    pthread_mutex_init(&render_thread->frame_wait_lock, NULL);
    pthread_cond_init(&render_thread->frame_wait, NULL);
    render_thread->frame_available = 0;

    render_thread->running = 0;

    render_thread->mpv = mpv;
    render_thread->opengl_cb_context = opengl_cb_context;
    render_thread->egl_display = egl_display;
    render_thread->egl_surface = egl_surface;
    render_thread->egl_context = egl_context;
    render_thread->width = 0;
    render_thread->height = 0;

    return render_thread;
}

void render_thread_start(struct render_thread *render_thread) {
    pthread_mutex_lock(&render_thread->lock);
    pthread_create(&render_thread->thread, NULL, render_thread_run, render_thread);
    while (!render_thread->running) {
        pthread_cond_wait(&render_thread->ready, &render_thread->lock);
    }
    pthread_mutex_unlock(&render_thread->lock);
}

void render_thread_stop(struct render_thread *render_thread) {
    pthread_mutex_lock(&render_thread->lock);
    render_thread->running = 0;
    pthread_mutex_unlock(&render_thread->lock);
    pthread_join(render_thread->thread, NULL);
}

void render_thread_resize(struct render_thread *render_thread, int width, int height) {
    LOGI("render_thread_resize: w = %d h = %d", width, height);
    pthread_mutex_lock(&render_thread->lock);
    render_thread->width = width;
    render_thread->height = height;
    pthread_mutex_unlock(&render_thread->lock);
}

void render_thread_destroy(struct render_thread *render_thread) {
    pthread_mutex_destroy(&render_thread->lock);
    pthread_cond_destroy(&render_thread->ready);
    pthread_mutex_destroy(&render_thread->frame_wait_lock);
    pthread_cond_destroy(&render_thread->frame_wait);
    free(render_thread);
}

static void *mpv_get_proc_address(void *fn_ctx, const char *name) {
    return (void *) eglGetProcAddress(name);
}

static void update_callback(void *data) {
    struct render_thread *render_thread = data;
    pthread_mutex_lock(&render_thread->frame_wait_lock);
    render_thread->frame_available = 1;
    pthread_cond_broadcast(&render_thread->frame_wait);
    pthread_mutex_unlock(&render_thread->frame_wait_lock);
}

static void *render_thread_run(void *data) {
    struct render_thread *render_thread = data;

    EGLDisplay egl_display = NULL;
    EGLSurface egl_surface = NULL;
    EGLContext egl_context = NULL;
    // Gather egl params and bind to mpv opengl_cb
    {
        pthread_mutex_lock(&render_thread->lock);

        egl_display = render_thread->egl_display;
        egl_surface = render_thread->egl_surface;
        egl_context = render_thread->egl_context;

        eglMakeCurrent(egl_display, egl_surface, egl_surface, egl_context);
        eglSwapInterval(egl_display, 0);

        int res = mpv_opengl_cb_init_gl(render_thread->opengl_cb_context, NULL,
                                        mpv_get_proc_address, NULL);
        if (res < 0) {
            log_mpv_error("mpv_opengl_cb_init_gl failed", res);
        }

        mpv_opengl_cb_set_update_callback(render_thread->opengl_cb_context,
                                          update_callback, render_thread);

        render_thread->running = 1;
        pthread_cond_broadcast(&render_thread->ready);

        pthread_mutex_unlock(&render_thread->lock);
    }

    int64_t last_time = mpv_get_time_us(render_thread->mpv);
    int frames = 0;

    while (1) {
        // Check if still running
        int run, width, height;
        {
            pthread_mutex_lock(&render_thread->lock);
            run = render_thread->running;
            width = render_thread->width;
            height = render_thread->height;
            pthread_mutex_unlock(&render_thread->lock);
        }
        if (!run) {
            break;
        }

        // Wait for available frames
        {
            pthread_mutex_lock(&render_thread->frame_wait_lock);
            while (!render_thread->frame_available) {
                pthread_cond_wait(&render_thread->frame_wait, &render_thread->frame_wait_lock);
            }
            render_thread->frame_available = 0;
            pthread_mutex_unlock(&render_thread->frame_wait_lock);
        }

//        int64_t draw_start = mpv_get_time_us(render_thread->mpv);
        mpv_opengl_cb_draw(render_thread->opengl_cb_context, 0, width, -height);
//        LOGI("Render time %ld", (mpv_get_time_us(render_thread->mpv) - draw_start));
        if (!eglSwapBuffers(egl_display, egl_surface)) {
            LOGE("eglSwapBuffers failed %d", eglGetError());
        }
//        mpv_opengl_cb_report_flip(render_thread->opengl_cb_context, 0);

        frames++;

        int64_t now = mpv_get_time_us(render_thread->mpv);
        if (now - last_time >= 1000 * 1000) {
            last_time += 1000 * 1000;
            LOGI("Render fps %d", frames);
            frames = 0;
        }
    }

    // Release egl params and unbind from mpv opengl_cb
    {
        pthread_mutex_lock(&render_thread->lock);

        int res = mpv_opengl_cb_uninit_gl(render_thread->opengl_cb_context);
        if (res < 0) {
            log_mpv_error("mpv_opengl_cb_uninit_gl failed", res);
        }

        eglMakeCurrent(egl_display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);

        pthread_mutex_unlock(&render_thread->lock);
    }

    return NULL;
}
