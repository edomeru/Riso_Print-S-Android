#include <string.h>
#include <jni.h>
#include <android/log.h>
#include "common.h"
#include "common_manager.h"
#include <stdlib.h>

#define LOG_TAG "JNI_SNMPMANAGER"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// Callbacks
void end_callback(snmp_context *context, int result);
void found_callback(snmp_context *context, snmp_device *device);

JNIEXPORT void 
Java_jp_co_riso_smartdeviceapp_common_SNMPManager_initializeSNMPManager(JNIEnv *env, jobject object, jstring community_name)
{
    // Create cache object
    CommonJNIState *state = (CommonJNIState *)malloc(sizeof(CommonJNIState));
    (*env)->GetJavaVM(env, &state->java_vm);
    state->instance = (*env)->NewGlobalRef(env, object);

    // Create community name
    const char *native_community_name = (*env)->GetStringUTFChars(env, community_name, 0);

    // Create snmp context
    snmp_context *context = snmp_context_new(end_callback, found_callback, native_community_name);
    snmp_context_set_caller_data(context, (void *)state);
    
    // Set context reference to java object
    (*env)->SetLongField(env, object, snmp_context_field_id, (jlong)context);

    (*env)->ReleaseStringUTFChars(env, community_name, native_community_name);
}

JNIEXPORT void
Java_jp_co_riso_smartdeviceapp_common_SNMPManager_finalizeSNMPManager(JNIEnv *env, jobject object)
{
    jlong m_context = (*env)->GetLongField(env, object, snmp_context_field_id);
    if (m_context == 0)
    {
        return;
    }

    snmp_context *context = (snmp_context *)m_context;
    CommonJNIState *state = (CommonJNIState *)snmp_context_get_caller_data(context);
    (*env)->DeleteGlobalRef(env, state->instance);
    snmp_context_free(context);
    (*env)->SetLongField(env, object, snmp_context_field_id, 0);
}

JNIEXPORT void
Java_jp_co_riso_smartdeviceapp_common_SNMPManager_deviceDiscovery(JNIEnv *env, jobject object)
{
    jlong m_context = (*env)->GetLongField(env, object, snmp_context_field_id);
    if (m_context == 0)
    {
        return;
    }

    snmp_context *context = (snmp_context *)m_context;
    snmp_device_discovery(context);
}

JNIEXPORT void
Java_jp_co_riso_smartdeviceapp_common_SNMPManager_manualDiscovery(JNIEnv *env, jobject object, jstring ip_address)
{
    jlong m_context = (*env)->GetLongField(env, object, snmp_context_field_id);
    if (m_context == 0)
    {
        return;
    }

    const char *native_ip_address = (*env)->GetStringUTFChars(env, ip_address, 0);

    snmp_context *context = (snmp_context *)m_context;
    snmp_manual_discovery(context, native_ip_address);
    
    (*env)->ReleaseStringUTFChars(env, ip_address, native_ip_address);
}

JNIEXPORT void
Java_jp_co_riso_smartdeviceapp_common_SNMPManager_cancel(JNIEnv *env, jobject object)
{
    jlong m_context = (*env)->GetLongField(env, object, snmp_context_field_id);
    if (m_context == 0)
    {
        return;
    }

    snmp_context *context = (snmp_context *)m_context;
    snmp_cancel(context);
}

void end_callback(snmp_context *context, int result)
{
    CommonJNIState *state = (CommonJNIState *)snmp_context_get_caller_data(context);
    JNIEnv *env;
    JavaVM *java_vm = state->java_vm;
    (*java_vm)->AttachCurrentThread(java_vm, (JNIEnv **)&env, 0);

    (*env)->CallVoidMethod(env, state->instance, snmp_end_callback_method_id, (jint)result);

    (*java_vm)->DetachCurrentThread(java_vm);
}

void found_callback(snmp_context *context, snmp_device *device)
{
    CommonJNIState *state = (CommonJNIState *)snmp_context_get_caller_data(context);
    JNIEnv *env;
    JavaVM *java_vm = state->java_vm;
    (*java_vm)->AttachCurrentThread(java_vm, (JNIEnv **)&env, 0);

    jstring jni_ip_address = (*env)->NewStringUTF(env, snmp_device_get_ip_address(device));
    jstring jni_name = (*env)->NewStringUTF(env, snmp_device_get_name(device));
    jbooleanArray jni_caps = (*env)->NewBooleanArray(env, kSnmpCapabilityCount);
    jboolean tempCaps[kSnmpCapabilityCount];
    int i;
    for (i = 0; i < kSnmpCapabilityCount; i++)
    {
        tempCaps[i] = snmp_device_get_capability_status(device, i);
    }
    (*env)->SetBooleanArrayRegion(env, jni_caps, 0, kSnmpCapabilityCount, tempCaps);

    (*env)->CallVoidMethod(env, state->instance, snmp_found_callback_method_id, jni_ip_address, jni_name, jni_caps);

    (*java_vm)->DetachCurrentThread(java_vm);
}
