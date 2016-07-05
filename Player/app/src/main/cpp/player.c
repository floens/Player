#include <libavcodec/jni.h>

#include "logger.h"

#include "player.h"

static struct player_context context = {0};

int player_initialize(JNIEnv *env) {
    // Requirement of mpv, see client.h
    setlocale(LC_NUMERIC, "C");

    LOGI("Mpv client version %lx", mpv_client_api_version());

    if ((*env)->GetJavaVM(env, &context.vm)) {
        LOGE("Failed to get java vm");
        return 1;
    }

    av_jni_set_java_vm(context.vm, NULL);

    context.mpv = mpv_create();
    if (!context.mpv) {
        LOGE("Failed to create mpv handle");
        return 1;
    }

    int res = mpv_set_option_string(context.mpv, "vo", "opengl-cb");
    if (res < 0) {
        log_mpv_error("vo opengl-cb", res);
    }
    /*res = mpv_set_option_string(context.mpv, "hwdec", "mediacodec");
    if (res < 0) {
        log_mpv_error("hwdec mediacodec", res);
    }*/

    if (mpv_initialize(context.mpv)) {
        LOGE("Failed to initialize mpv");
        return 1;
    }

    mpv_request_log_messages(context.mpv, "info");

    context.event_thread = event_thread_create(context.mpv);
    event_thread_start(context.event_thread);

    context.opengl_cb_context = mpv_get_sub_api(context.mpv, MPV_SUB_API_OPENGL_CB);
    if (!context.opengl_cb_context) {
        LOGE("mpv_get_sub_api(context.mpv, MPV_SUB_API_OPENGL_CB) failed");
        return 1;
    }

    return 0;
}

int player_bind() {
    // Get the egl params and unbind them from this thread to give them to
    // the render thread
    EGLDisplay egl_display = eglGetCurrentDisplay();
    EGLSurface egl_surface = eglGetCurrentSurface(EGL_DRAW);
    EGLContext egl_context = eglGetCurrentContext();
    eglMakeCurrent(egl_display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);

    // Start and wait for the render thread
    LOGI("Starting render thread");
    context.render_thread = render_thread_create(context.mpv, context.opengl_cb_context,
                                                 egl_display, egl_surface, egl_context);
    render_thread_start(context.render_thread);

    return 0;
}

void player_resize(int width, int height) {
    render_thread_resize(context.render_thread, width, height);
}

void player_unbind() {
    LOGI("Stopping render thread");
    render_thread_stop(context.render_thread);
    render_thread_destroy(context.render_thread);
    context.render_thread = NULL;
}

void player_handle_command(const char **command) {
    mpv_command(context.mpv, command);
}
