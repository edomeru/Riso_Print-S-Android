#include <string.h>
#include <jni.h>
#include <android/log.h>
#include "common.h"
#include "common_manager.h"
#include <stdlib.h>

#define LOG_TAG "JNI_DIRECTPRINTMANAGER"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

void print_callback(directprint_job *print_job, int status, float progress);

JNIEXPORT void 
// Ver.2.0.4.2 Start
//Java_jp_co_riso_smartdeviceapp_common_DirectPrintManager_initializeDirectPrint(JNIEnv *env, jobject object, jstring printer_name, jstring app_name,  jstring app_version,  jstring user_name, jstring job_name, jstring file_name, jstring print_setting, jstring ip_address)
//Java_jp_co_riso_smartdeviceapp_common_DirectPrintManager_initializeDirectPrint(JNIEnv *env, jobject object, jstring printer_name, jstring app_name,  jstring app_version,  jstring user_name, jstring job_name, jstring file_name, jstring print_setting, jstring ip_address, jstring host_name)
// Ver.2.0.4.2 End
// Ver.2.2.0.0 Start
Java_jp_co_riso_smartdeviceapp_common_DirectPrintManager_initializeDirectPrint(JNIEnv *env, jobject object, jstring printer_name, jstring app_name,  jstring app_version,  jstring user_name, jstring job_name, jstring file_name, jstring print_setting, jstring ip_address, jstring host_name, jint job_number)
// Ver.2.2.0.0 End
{
    // Create cache object
    CommonJNIState *state = (CommonJNIState *)malloc(sizeof(CommonJNIState));
    (*env)->GetJavaVM(env, &state->java_vm);
    // RM 856 Fix: Change global reference to weak reference so that it can be deleted elsewhere
    // The state is set in directprint.c via directprint_job_set_caller_data() and deleted in that module.
    state->instance = (*env)->NewWeakGlobalRef(env, object);

    // Create direct print job
    const char *native_printer_name =  (*env)->GetStringUTFChars(env, printer_name, 0);
    const char *native_app_name =  (*env)->GetStringUTFChars(env, app_name, 0);
    const char *native_app_version =  (*env)->GetStringUTFChars(env, app_version, 0);
    const char *native_user_name = (*env)->GetStringUTFChars(env, user_name, 0);
    const char *native_job_name = (*env)->GetStringUTFChars(env, job_name, 0);
    const char *native_file_name = (*env)->GetStringUTFChars(env, file_name, 0);
    const char *native_print_setting = (*env)->GetStringUTFChars(env, print_setting, 0);
    const char *native_ip_address = (*env)->GetStringUTFChars(env, ip_address, 0);
    // Ver.2.0.4.2 Start
    const char *native_host_name = (*env)->GetStringUTFChars(env, host_name, 0);
    // Ver.2.0.4.2 End
    // Ver.2.2.0.0 Start
    const int native_job_number = (int)job_number;
    // Ver.2.2.0.0 End

    // Ver.2.0.4.2 Start
    //directprint_job *job = directprint_job_new(native_printer_name, native_app_name, native_app_version, native_user_name, native_job_name, native_file_name, native_print_setting, native_ip_address, print_callback);
    //directprint_job *job = directprint_job_new(native_printer_name, native_host_name, native_app_name, native_app_version, native_user_name, native_job_name, native_file_name, native_print_setting, native_ip_address, print_callback);
    // Ver.2.0.4.2 End
    // Ver.2.2.0.0 Start
    directprint_job *job = directprint_job_new(native_printer_name, native_host_name, native_app_name, native_app_version, native_user_name, native_job_number, native_job_name, native_file_name, native_print_setting, native_ip_address, print_callback);
    // Ver.2.2.0.0 End
    (*env)->ReleaseStringUTFChars(env, printer_name, native_printer_name);
    // Ver.2.0.4.2 Start
    (*env)->ReleaseStringUTFChars(env, host_name, native_host_name);
    // Ver.2.0.4.2 End
    (*env)->ReleaseStringUTFChars(env, app_name, native_app_name);
    (*env)->ReleaseStringUTFChars(env, app_version, native_app_version);
    (*env)->ReleaseStringUTFChars(env, job_name, native_job_name);
    (*env)->ReleaseStringUTFChars(env, file_name, native_file_name);
    (*env)->ReleaseStringUTFChars(env, print_setting, native_print_setting);
    (*env)->ReleaseStringUTFChars(env, ip_address, native_ip_address);
    directprint_job_set_caller_data(job, state);

    // Set job reference to java object
    (*env)->SetLongField(env, object, dp_job_field_id, (jlong)job);
}

JNIEXPORT void
Java_jp_co_riso_smartdeviceapp_common_DirectPrintManager_finalizeDirectPrint(JNIEnv *env, jobject object)
{
    // Get job reference to Java object
    jlong m_job = (*env)->GetLongField(env, object, dp_job_field_id);
    if (m_job != 0)
    {
        // RM 856 Fix: Move free state to directprint_job_free()
        //directprint_job *job = (directprint_job *)m_job;
        //CommonJNIState *state = (CommonJNIState *)directprint_job_get_caller_data(job);
        //(*env)->DeleteGlobalRef(env, state->instance);
        //free(state);
        // RM 496 Fix: Move directprint_job_free to directprint.c
        //directprint_job_free(job);
        (*env)->SetLongField(env, object, dp_job_field_id, 0);
    }
}

JNIEXPORT void
Java_jp_co_riso_smartdeviceapp_common_DirectPrintManager_lprPrint(JNIEnv *env, jobject object)
{
    // Get job reference to Java object
    jlong m_job = (*env)->GetLongField(env, object, dp_job_field_id);
    if (m_job != 0)
    {
        directprint_job *job = (directprint_job *)m_job;
        
        directprint_job_lpr_print(job);
    }
}

JNIEXPORT void
Java_jp_co_riso_smartdeviceapp_common_DirectPrintManager_rawPrint(JNIEnv *env, jobject object)
{
    // Get job reference to Java object
    jlong m_job = (*env)->GetLongField(env, object, dp_job_field_id);
    if (m_job != 0)
    {
    	directprint_job *job = (directprint_job *)m_job;
    
    	directprint_job_raw_print(job);
    }
}

JNIEXPORT void
Java_jp_co_riso_smartdeviceapp_common_DirectPrintManager_cancel(JNIEnv *env, jobject object)
{
    // Get job reference to Java object
    jlong m_job = (*env)->GetLongField(env, object, dp_job_field_id);
    if (m_job != 0)
    {
        directprint_job *job = (directprint_job *)m_job;

        directprint_job_cancel(job);
    }
}

// Callback
void print_callback(directprint_job *print_job, int status, float progress)
{
    CommonJNIState *state = (CommonJNIState *)directprint_job_get_caller_data(print_job);
    JNIEnv *env;
    JavaVM *java_vm = state->java_vm;
    (*java_vm)->AttachCurrentThread(java_vm, (JNIEnv **)&env, 0);

    jint jni_status = status;
    jfloat jni_progress = progress;
    (*env)->CallVoidMethod(env, state->instance, dp_callback_method_id, jni_status, jni_progress);

    (*java_vm)->DetachCurrentThread(java_vm);
}
