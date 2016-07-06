#include <jni.h>

void event_no_data(JNIEnv *env, jobject core_instance, const char *event_name);

void event_property_string(JNIEnv *env, jobject core_instance, uint64_t userdata, const char *name, const char *data);

void event_property_flag(JNIEnv *env, jobject core_instance, uint64_t userdata, const char *name, int flag);

void event_property_long(JNIEnv *env, jobject core_instance, uint64_t userdata, const char *name, int64_t value);

void event_property_double(JNIEnv *env, jobject core_instance, uint64_t userdata, const char *name, double value);

JNIEXPORT void JNICALL Java_org_floens_mpv_MpvCore_registerNatives(JNIEnv *env, jclass cls);
