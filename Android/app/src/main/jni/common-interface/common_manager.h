#include <jni.h>

#ifndef _common_manager_h_
#define _common_manager_h_

// Direct print
#define DP_CLASS_NAME "jp/co/riso/smartdeviceapp/common/DirectPrintManager"
#define DP_JOB_FIELD_NAME "mJob"
#define DP_JOB_FIELD_TYPE "J"
#define DP_CALLBACK_METHOD_NAME "onNotifyProgress"
#define DP_CALLBACK_METHOD_TYPE "(IF)V"

jfieldID dp_job_field_id;
jmethodID dp_callback_method_id;

// SNMP
#define SNMP_CLASS_NAME "jp/co/riso/smartdeviceapp/common/SNMPManager"
#define SNMP_CONTEXT_FIELD_NAME "mContext"
#define SNMP_CONTEXT_FIELD_TYPE "J"
#define SNMP_END_CALLBACK_METHOD_NAME "onEndDiscovery"
#define SNMP_END_CALLBACK_METHOD_TYPE "(I)V"
#define SNMP_FOUND_CALLBACK_METHOD_NAME "onFoundDevice"
#define SNMP_FOUND_CALLBACK_METHOD_TYPE "(Ljava/lang/String;Ljava/lang/String;[Z)V"

jfieldID snmp_context_field_id;
jmethodID snmp_end_callback_method_id;
jmethodID snmp_found_callback_method_id;

typedef struct
{
    JavaVM *java_vm;
    jobject instance;
} CommonJNIState;

#endif
