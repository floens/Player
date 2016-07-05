#include "player_interface.h"

#include "player.h"

JNIEXPORT jint JNICALL
Java_org_floens_mpv_MpvCore_nativeInitialize(
        JNIEnv *env, jobject instance) {
    return player_initialize(env);
}

JNIEXPORT int JNICALL
Java_org_floens_mpv_MpvCore_nativeBind(
        JNIEnv *env, jobject instance) {
    return player_bind();
}

JNIEXPORT void
JNICALL Java_org_floens_mpv_MpvCore_nativeResize(
        JNIEnv *env, jobject instance, jint width, jint height) {
    player_resize(width, height);
}

JNIEXPORT void JNICALL
Java_org_floens_mpv_MpvCore_nativeUnbind(
        JNIEnv *env, jobject instance) {
    player_unbind();
}

JNIEXPORT void
JNICALL Java_org_floens_mpv_MpvCore_nativeCommand(
        JNIEnv *env, jobject instance, jobjectArray array) {
    int length = (*env)->GetArrayLength(env, array);

    const char *command[length + 1];
    command[length] = NULL;

    for (int i = 0; i < length; i++) {
        command[i] = (*env)->GetStringUTFChars(
                env, (*env)->GetObjectArrayElement(env, array, i), 0);
    }

    player_handle_command(command);

    for (int i = 0; i < length; i++) {
        (*env)->ReleaseStringUTFChars(
                env, (*env)->GetObjectArrayElement(env, array, i), command[i]);
    }
}
