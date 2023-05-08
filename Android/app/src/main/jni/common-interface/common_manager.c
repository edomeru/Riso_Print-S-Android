#include <jni.h>
#include "common_manager.h"

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv *env;
    (*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_6);

    // Direct print
    jclass dp_class = (*env)->FindClass(env, DP_CLASS_NAME);
    dp_job_field_id = (*env)->GetFieldID(env, dp_class, DP_JOB_FIELD_NAME, DP_JOB_FIELD_TYPE);
    dp_callback_method_id = (*env)->GetMethodID(env, dp_class, DP_CALLBACK_METHOD_NAME, DP_CALLBACK_METHOD_TYPE);

    // SNMP
    jclass snmp_class = (*env)->FindClass(env, SNMP_CLASS_NAME);
    snmp_context_field_id = (*env)->GetFieldID(env, snmp_class, SNMP_CONTEXT_FIELD_NAME, SNMP_CONTEXT_FIELD_TYPE);
    snmp_end_callback_method_id = (*env)->GetMethodID(env, snmp_class, SNMP_END_CALLBACK_METHOD_NAME, SNMP_END_CALLBACK_METHOD_TYPE);
    snmp_found_callback_method_id = (*env)->GetMethodID(env, snmp_class, SNMP_FOUND_CALLBACK_METHOD_NAME, SNMP_FOUND_CALLBACK_METHOD_TYPE);
    snmp_mac_callback_method_id = (*env)->GetMethodID(env, snmp_class, SNMP_MAC_CALLBACK_METHOD_NAME, SNMP_MAC_CALLBACK_METHOD_TYPE);

    return JNI_VERSION_1_6;
}
