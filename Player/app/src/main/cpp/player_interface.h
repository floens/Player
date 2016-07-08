#include <jni.h>

void event_property(JNIEnv *env, jobject core_instance, uint64_t userdata, mpv_event_property *event_property);

void event_no_data(JNIEnv *env, jobject core_instance, const char *event_name);

JNIEXPORT void JNICALL Java_org_floens_mpv_MpvCore_registerNatives(JNIEnv *env, jclass cls);
