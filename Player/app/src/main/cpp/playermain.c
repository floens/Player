#include "playermain.h"

#include <stdio.h>

JNIEXPORT jint JNICALL
Java_org_floens_player_mpv_MpvCore_native_1initialize(JNIEnv *env, jobject instance) {
    return initialize(env);
}

static struct player_context context = {0};

int initialize(JNIEnv *env) {
    // Requirement of mpv, see client.h
    setlocale(LC_NUMERIC, "C");

    LOGI("Mpv client version %lx", mpv_client_api_version());

    if ((*env)->GetJavaVM(env, &context.vm)) {
        LOGE("Failed to get java vm");
        return 1;
    }

    context.mpv = mpv_create();
    if (!context.mpv) {
        LOGE("Failed to create mpv handle");
        return 1;
    }

    if (mpv_initialize(context.mpv)) {
        LOGE("Failed to initialize mpv");
        return 1;
    }

    return 0;
}
