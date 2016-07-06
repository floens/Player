#include "player.h"

#include "player_interface.h"

// Interface that glues to the MpvCore class

static jmethodID nativeEventNoData;

static jmethodID nativeEventPropertyString;
static jmethodID nativeEventPropertyFlag;
static jmethodID nativeEventPropertyLong;
static jmethodID nativeEventPropertyDouble;

void event_no_data(JNIEnv *env, jobject core_instance, const char *event_name) {
    jstring namestr = (*env)->NewStringUTF(env, event_name);
    (*env)->CallVoidMethod(env, core_instance, nativeEventNoData, namestr);
    (*env)->DeleteLocalRef(env, namestr);
}

void event_property_string(JNIEnv *env, jobject core_instance, uint64_t userdata, const char *name, const char *data) {
    jstring namestr = (*env)->NewStringUTF(env, name);
    jstring datastr = (*env)->NewStringUTF(env, data);
    (*env)->CallVoidMethod(env, core_instance, nativeEventPropertyString, userdata, namestr, datastr);
    (*env)->DeleteLocalRef(env, namestr);
    (*env)->DeleteLocalRef(env, datastr);
}

void event_property_flag(JNIEnv *env, jobject core_instance, uint64_t userdata, const char *name, int flag) {
    jstring namestr = (*env)->NewStringUTF(env, name);
    (*env)->CallVoidMethod(env, core_instance, nativeEventPropertyFlag, userdata, namestr, flag);
    (*env)->DeleteLocalRef(env, namestr);
}

void event_property_long(JNIEnv *env, jobject core_instance, uint64_t userdata, const char *name, int64_t value) {
    jstring namestr = (*env)->NewStringUTF(env, name);
    (*env)->CallVoidMethod(env, core_instance, nativeEventPropertyLong, userdata, namestr, value);
    (*env)->DeleteLocalRef(env, namestr);
}

void event_property_double(JNIEnv *env, jobject core_instance, uint64_t userdata, const char *name, double value) {
    jstring namestr = (*env)->NewStringUTF(env, name);
    (*env)->CallVoidMethod(env, core_instance, nativeEventPropertyDouble, userdata, namestr, value);
    (*env)->DeleteLocalRef(env, namestr);
}

static jint JNICALL jni_native_initialize(JNIEnv *env, jobject instance) {
    return player_initialize(env, instance);
}

static jint JNICALL jni_native_bind(JNIEnv *env, jobject instance) {
    return player_bind();
}

static void JNICALL jni_native_resize(JNIEnv *env, jobject instance, jint width, jint height) {
    player_resize(width, height);
}

static void JNICALL jni_native_unbind(JNIEnv *env, jobject instance) {
    player_unbind();
}

static void JNICALL jni_native_command(JNIEnv *env, jobject instance, jobjectArray array) {
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

static void JNICALL jni_native_observe_property
        (JNIEnv *env, jobject instance, jlong userdata, jstring name, jint format) {
    uint64_t mpvuserdata = (uint64_t) userdata;
    const char *namestr = (*env)->GetStringUTFChars(env, name, 0);
    mpv_format mpvformat = (mpv_format) format;
    player_observe_property(mpvuserdata, namestr, mpvformat);
    (*env)->ReleaseStringUTFChars(env, name, namestr);
}

static JNINativeMethod methods[] = {
        {"nativeInitialize",      "()I",                    &jni_native_initialize},
        {"nativeBind",            "()I",                    &jni_native_bind},
        {"nativeResize",          "(II)V",                  &jni_native_resize},
        {"nativeUnbind",          "()V",                    &jni_native_unbind},
        {"nativeCommand",         "([Ljava/lang/String;)V", &jni_native_command},
        {"nativeObserveProperty", "(JLjava/lang/String;I)V",  &jni_native_observe_property}
};

JNIEXPORT void JNICALL Java_org_floens_mpv_MpvCore_registerNatives(JNIEnv *env, jclass cls) {
    (*env)->RegisterNatives(env, cls, methods, sizeof(methods) / sizeof(methods[0]));

    nativeEventNoData =
            (*env)->GetMethodID(env, cls, "nativeEventNoData", "(Ljava/lang/String;)V");

    nativeEventPropertyString =
            (*env)->GetMethodID(env, cls, "nativeEventPropertyString",
                                "(JLjava/lang/String;Ljava/lang/String;)V");
    nativeEventPropertyFlag =
            (*env)->GetMethodID(env, cls, "nativeEventPropertyFlag", "(JLjava/lang/String;I)V");
    nativeEventPropertyLong =
            (*env)->GetMethodID(env, cls, "nativeEventPropertyLong", "(JLjava/lang/String;J)V");
    nativeEventPropertyDouble =
            (*env)->GetMethodID(env, cls, "nativeEventPropertyDouble", "(JLjava/lang/String;D)V");
}
