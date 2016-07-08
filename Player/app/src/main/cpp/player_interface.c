#include "player.h"

#include "player_interface.h"
#include "logger.h"

// Interface that glues to the MpvCore class

static jmethodID nativeEventNoData;

static jmethodID nativeEventProperty;

static jclass mpvPropertyCls;
static jmethodID mpvPropertyConstructor;

static jclass mpvFormatCls;
static jmethodID mpvFormatConstructor;

static jclass booleanCls;
static jclass booleanTrue;
static jclass booleanFalse;
static jclass longCls;
static jmethodID longConstructor;
static jclass doubleCls;
static jmethodID doubleConstructor;

/*
 * Translates the mpv_event_property to its java cousin and call MpvCore.nativeEventProperty()
 */
void event_property(JNIEnv *env, jobject core_instance, uint64_t userdata,
                    mpv_event_property *event_property) {
    jstring namestr = (*env)->NewStringUTF(env, event_property->name);
    jobject boxed_value = NULL;
    int clear_boxed_value = 0;
    jobject format = NULL;
    jobject property = NULL;

    int format_id = event_property->format;
    switch (format_id) {
        case MPV_FORMAT_NONE: {
            // Keep it null
            break;
        }
        case MPV_FORMAT_STRING:
        case MPV_FORMAT_OSD_STRING: {
            jstring str = (*env)->NewStringUTF(env, event_property->data);
            boxed_value = str;
            clear_boxed_value = 1;
            break;
        }
        case MPV_FORMAT_FLAG: {
            int boolean = *(int *) event_property->data;
            boxed_value = boolean ? booleanTrue : booleanFalse;
            break;
        }
        case MPV_FORMAT_INT64: {
            jlong long_value = *(int64_t *) event_property->data;
            boxed_value = (*env)->NewObject(env, longCls, longConstructor, long_value);
            clear_boxed_value = 1;
            break;
        }
        case MPV_FORMAT_DOUBLE: {
            jdouble double_value = *(double *) event_property->data;
            boxed_value = (*env)->NewObject(env, doubleCls, doubleConstructor, double_value);
            clear_boxed_value = 1;
            break;
        }
        default: {
            LOGE("event_property not handled, unknown format id");
            goto done;
        }
    }

    format = (*env)->NewObject(env, mpvFormatCls, mpvFormatConstructor, format_id, boxed_value);
    property = (*env)->NewObject(
            env, mpvPropertyCls, mpvPropertyConstructor, userdata, namestr, format);

    (*env)->CallVoidMethod(env, core_instance, nativeEventProperty, userdata, property);

    done:
    (*env)->DeleteLocalRef(env, namestr);
    if (boxed_value && clear_boxed_value)
        (*env)->DeleteLocalRef(env, boxed_value);
    if (format)
        (*env)->DeleteLocalRef(env, format);
    if (property)
        (*env)->DeleteLocalRef(env, property);
}

void event_no_data(JNIEnv *env, jobject core_instance, const char *event_name) {
    jstring namestr = (*env)->NewStringUTF(env, event_name);
    (*env)->CallVoidMethod(env, core_instance, nativeEventNoData, namestr);
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

static void JNICALL jni_native_observe_property(
        JNIEnv *env, jobject instance, jlong userdata, jstring name, jint format) {
    uint64_t mpvuserdata = (uint64_t) userdata;
    const char *namestr = (*env)->GetStringUTFChars(env, name, 0);
    mpv_format mpvformat = (mpv_format) format;
    player_observe_property(mpvuserdata, namestr, mpvformat);
    (*env)->ReleaseStringUTFChars(env, name, namestr);
}

static void JNICALL jni_native_unobserve_property(JNIEnv *env, jobject instance, jlong userdata) {
    player_unobserve_property((uint64_t) userdata);
}

static JNINativeMethod methods[] = {
        {"nativeInitialize",        "()I",                     &jni_native_initialize},
        {"nativeBind",              "()I",                     &jni_native_bind},
        {"nativeResize",            "(II)V",                   &jni_native_resize},
        {"nativeUnbind",            "()V",                     &jni_native_unbind},
        {"nativeCommand",           "([Ljava/lang/String;)V",  &jni_native_command},
        {"nativeObserveProperty",   "(JLjava/lang/String;I)V", &jni_native_observe_property},
        {"nativeUnobserveProperty", "(J)V",                    &jni_native_unobserve_property}
};

JNIEXPORT void JNICALL Java_org_floens_mpv_MpvCore_registerNatives(JNIEnv *env, jclass cls) {
    (*env)->RegisterNatives(env, cls, methods, sizeof(methods) / sizeof(methods[0]));

    booleanCls = (*env)->FindClass(env, "java/lang/Boolean");
    jfieldID booleanTrueID = (*env)->GetStaticFieldID(env, booleanCls, "TRUE",
                                                      "Ljava/lang/Boolean;");
    jfieldID booleanFalseID = (*env)->GetStaticFieldID(env, booleanCls, "FALSE",
                                                       "Ljava/lang/Boolean;");
    booleanTrue = (*env)->NewGlobalRef(
            env, (*env)->GetStaticObjectField(env, booleanCls, booleanTrueID));
    booleanFalse = (*env)->NewGlobalRef(
            env, (*env)->GetStaticObjectField(env, booleanCls, booleanFalseID));
    longCls = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "java/lang/Long"));
    longConstructor = (*env)->GetMethodID(env, longCls, "<init>", "(J)V");
    doubleCls = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "java/lang/Double"));
    doubleConstructor = (*env)->GetMethodID(env, doubleCls, "<init>", "(D)V");

    nativeEventNoData = (*env)->GetMethodID(
            env, cls, "nativeEventNoData", "(Ljava/lang/String;)V");

    nativeEventProperty = (*env)->GetMethodID(
            env, cls, "nativeEventProperty", "(JLorg/floens/mpv/MpvProperty;)V");

    mpvFormatCls = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "org/floens/mpv/MpvFormat"));
    mpvFormatConstructor = (*env)->GetMethodID(
            env, mpvFormatCls, "<init>", "(ILjava/lang/Object;)V");

    mpvPropertyCls = (*env)->NewGlobalRef(
            env, (*env)->FindClass(env, "org/floens/mpv/MpvProperty"));
    mpvPropertyConstructor = (*env)->GetMethodID(
            env, mpvPropertyCls, "<init>", "(JLjava/lang/String;Lorg/floens/mpv/MpvFormat;)V");
}
