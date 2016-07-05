#include <jni.h>

JNIEXPORT int
JNICALL Java_org_floens_mpv_MpvCore_nativeInitialize(
        JNIEnv *env, jobject instance);

JNIEXPORT int
JNICALL Java_org_floens_mpv_MpvCore_nativeBind(
        JNIEnv *env, jobject instance);

JNIEXPORT void
JNICALL Java_org_floens_mpv_MpvCore_nativeResize(
        JNIEnv *env, jobject instance, jint width, jint height);

JNIEXPORT void
JNICALL Java_org_floens_mpv_MpvCore_nativeUnbind(
        JNIEnv *env, jobject instance);

JNIEXPORT void
JNICALL Java_org_floens_mpv_MpvCore_nativeCommand(
        JNIEnv *env, jobject instance, jobjectArray array);
