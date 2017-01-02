#include <assert.h>
#include <stdlib.h>
#include <mpv/client.h>
#include "player.h"

#include "player_interface.h"
#include "logger.h"

// Interface that glues to the Mpv* classes

static jmethodID native_event_no_data_method;

static jmethodID native_event_property_method;

static jclass mpv_property_cls;
static jmethodID mpv_property_constructor;

static jclass mpv_node_cls;
static jmethodID mpv_node_constructor;
static jfieldID mpv_node_format_field;
static jfieldID mpv_node_data_field;

static jclass boolean_cls;
static jmethodID boolean_constructor;
static jmethodID boolean_value_method;

static jclass long_cls;
static jmethodID long_constructor;
static jmethodID long_value_method;

static jclass double_cls;
static jmethodID double_constructor;
static jmethodID double_value_method;

static jobject node_to_object(JNIEnv *env, mpv_node *node) {
    jobject boxed_value = NULL;
    jobject nodeobj = NULL;

    mpv_format format = node->format;
    switch (format) {
        case MPV_FORMAT_NONE: {
            // Keep it null
            break;
        }
        case MPV_FORMAT_STRING:
        case MPV_FORMAT_OSD_STRING: {
            jstring str = (*env)->NewStringUTF(env, node->u.string);
            boxed_value = str;
            break;
        }
        case MPV_FORMAT_FLAG: {
            jboolean boolean_value = (jboolean) node->u.flag;
            boxed_value = (*env)->NewObject(env, boolean_cls, boolean_constructor, boolean_value);
            break;
        }
        case MPV_FORMAT_INT64: {
            jlong long_value = (jlong) node->u.int64;
            boxed_value = (*env)->NewObject(env, long_cls, long_constructor, long_value);
            break;
        }
        case MPV_FORMAT_DOUBLE: {
            jdouble double_value = (jdouble) node->u.double_;
            boxed_value = (*env)->NewObject(env, double_cls, double_constructor, double_value);
            break;
        }
        default: {
            LOGE("node_to_object not handled, unknown format %d", format);
            goto done;
        }
    }

    nodeobj = (*env)->NewObject(env, mpv_node_cls, mpv_node_constructor, format, boxed_value);

    done:
    if (boxed_value)
        (*env)->DeleteLocalRef(env, boxed_value);
    return nodeobj;
}

static mpv_node *object_to_node(JNIEnv *env, jobject nodeobj) {
    mpv_format format = (mpv_format) (*env)->GetIntField(env, nodeobj, mpv_node_format_field);
    jobject data = (*env)->GetObjectField(env, nodeobj, mpv_node_data_field);

    mpv_node *result = malloc(sizeof(mpv_node));
    result->format = format;

    switch (format) {
        case MPV_FORMAT_STRING: {
            // Make a copy of the string so that we don't have to call ReleaseStringUTFChars later
            // and thus keep a reference to the jobject
            size_t length = (size_t) (*env)->GetStringUTFLength(env, data);

            const char *valuestr = (*env)->GetStringUTFChars(env, data, 0);

            char *valuestrcpy = malloc(length + 1);
            strcpy(valuestrcpy, valuestr);

            (*env)->ReleaseStringUTFChars(env, data, valuestr);

            result->u.string = valuestrcpy;

            break;
        }
        case MPV_FORMAT_FLAG: {
            int value = (*env)->CallBooleanMethod(env, data, boolean_value_method);

            result->u.flag = value;

            break;
        }
        case MPV_FORMAT_INT64: {
            int64_t value = (int64_t) (*env)->CallLongMethod(env, data, long_value_method);

            result->u.int64 = value;

            break;
        }
        case MPV_FORMAT_DOUBLE: {
            double value = (*env)->CallDoubleMethod(env, data, double_value_method);

            result->u.double_ = value;

            break;
        }
        default: {
            result->format = MPV_FORMAT_NONE;
            LOGE("object_to_node not handled, unknown format %d", format);
            break;
        }
    }

    return result;
}

// free a node created by object_to_node
static void destroy_node(mpv_node *node) {
    switch (node->format) {
        case MPV_FORMAT_STRING: {
            free(node->u.string);
            break;
        }
        default:
            break;
    }
    free(node);
}

/*
 * Translates the mpv_event_property to its java cousin and call MpvCore.nativeEventProperty()
 */
void event_property(JNIEnv *env, jobject core_instance, uint64_t userdata,
                    mpv_event_property *event_property) {
    // We only support nodes, because the other formats internally translate to node
    assert(event_property->format == MPV_FORMAT_NODE);

    jstring namestr = (*env)->NewStringUTF(env, event_property->name);

    // Can be NULL
    jobject nodeobj = node_to_object(env, (mpv_node *) event_property->data);

    jobject propertyobj = (*env)->NewObject(
            env, mpv_property_cls, mpv_property_constructor, userdata, namestr, nodeobj);

    (*env)->CallVoidMethod(env, core_instance, native_event_property_method, userdata, propertyobj);

    if (nodeobj)
        (*env)->DeleteLocalRef(env, nodeobj);
    if (propertyobj)
        (*env)->DeleteLocalRef(env, propertyobj);
    (*env)->DeleteLocalRef(env, namestr);
}

void event_no_data(JNIEnv *env, jobject core_instance, const char *event_name) {
    jstring namestr = (*env)->NewStringUTF(env, event_name);
    (*env)->CallVoidMethod(env, core_instance, native_event_no_data_method, namestr);
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
        JNIEnv *env, jobject instance, jlong userdata, jstring name) {
    uint64_t mpvuserdata = (uint64_t) userdata;
    const char *namestr = (*env)->GetStringUTFChars(env, name, 0);
    player_observe_property(mpvuserdata, namestr);
    (*env)->ReleaseStringUTFChars(env, name, namestr);
}

static void JNICALL jni_native_unobserve_property(JNIEnv *env, jobject instance, jlong userdata) {
    player_unobserve_property((uint64_t) userdata);
}

static jclass JNICALL jni_get_property(JNIEnv *env, jobject instance, jstring name) {
    const char *namestr = (*env)->GetStringUTFChars(env, name, 0);

    mpv_node node;
    int res = player_get_property(namestr, &node);

    (*env)->ReleaseStringUTFChars(env, name, namestr);

    if (res < 0) {
        return NULL;
    }

    jclass nodeobj = node_to_object(env, &node);

    mpv_free_node_contents(&node);

    return nodeobj;
}

static void JNICALL jni_set_property(
        JNIEnv *env, jobject instance, jstring name, jobject nodeobj) {
    const char *namestr = (*env)->GetStringUTFChars(env, name, 0);

    mpv_node *node = object_to_node(env, nodeobj);
    player_set_property(namestr, node);
    destroy_node(node);

    (*env)->ReleaseStringUTFChars(env, name, namestr);
}

static JNINativeMethod methods[] = {
        {"nativeInitialize",        "()I",                    &jni_native_initialize},
        {"nativeBind",              "()I",                    &jni_native_bind},
        {"nativeResize",            "(II)V",                  &jni_native_resize},
        {"nativeUnbind",            "()V",                    &jni_native_unbind},
        {"nativeCommand",           "([Ljava/lang/String;)V", &jni_native_command},
        {"nativeObserveProperty",   "(JLjava/lang/String;)V", &jni_native_observe_property},
        {"nativeUnobserveProperty", "(J)V",                   &jni_native_unobserve_property},
        {"nativeGetProperty",       "(Ljava/lang/String;)Lorg/floens/mpv/MpvNode;",
                                                              &jni_get_property},
        {"nativeSetProperty",       "(Ljava/lang/String;Lorg/floens/mpv/MpvNode;)V",
                                                              &jni_set_property}
};

JNIEXPORT void JNICALL Java_org_floens_mpv_MpvCore_registerNatives(JNIEnv *env, jclass cls) {
    (*env)->RegisterNatives(env, cls, methods, sizeof(methods) / sizeof(methods[0]));

    // set up the classes, methodIDs, fieldIDs and cached objects
    native_event_no_data_method = (*env)->GetMethodID(
            env, cls, "nativeEventNoData", "(Ljava/lang/String;)V");

    native_event_property_method = (*env)->GetMethodID(
            env, cls, "nativeEventProperty", "(JLorg/floens/mpv/MpvProperty;)V");

    mpv_property_cls = (*env)->NewGlobalRef(
            env, (*env)->FindClass(env, "org/floens/mpv/MpvProperty"));
    mpv_property_constructor = (*env)->GetMethodID(
            env, mpv_property_cls, "<init>", "(JLjava/lang/String;Lorg/floens/mpv/MpvNode;)V");

    mpv_node_cls = (*env)->NewGlobalRef(
            env, (*env)->FindClass(env, "org/floens/mpv/MpvNode"));
    mpv_node_constructor = (*env)->GetMethodID(
            env, mpv_node_cls, "<init>", "(ILjava/lang/Object;)V");
    mpv_node_format_field = (*env)->GetFieldID(
            env, mpv_node_cls, "format", "I");
    mpv_node_data_field = (*env)->GetFieldID(
            env, mpv_node_cls, "value", "Ljava/lang/Object;");

    boolean_cls = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "java/lang/Boolean"));
    boolean_constructor = (*env)->GetMethodID(env, boolean_cls, "<init>", "(Z)V");
    boolean_value_method = (*env)->GetMethodID(env, boolean_cls, "booleanValue", "()Z");

    long_cls = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "java/lang/Long"));
    long_constructor = (*env)->GetMethodID(env, long_cls, "<init>", "(J)V");
    long_value_method = (*env)->GetMethodID(env, long_cls, "longValue", "()J");

    double_cls = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "java/lang/Double"));
    double_constructor = (*env)->GetMethodID(env, double_cls, "<init>", "(D)V");
    double_value_method = (*env)->GetMethodID(env, double_cls, "doubleValue", "()D");
}
