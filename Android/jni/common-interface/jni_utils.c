#include <string.h>
#include <jni.h>
#include <android/log.h>
#include "common.h"
#include "common_manager.h"

#define LOG_TAG "JNI_UTILS"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define IP_BUFFER_SIZE 64

JNIEXPORT jstring
Java_jp_co_riso_smartdeviceapp_common_JniUtils_validateIp(JNIEnv *env, jobject object, jstring ip_address)
{
	if (ip_address == 0)
	{
		return 0;
	}
    const char *native_ip_address = (*env)->GetStringUTFChars(env, ip_address, 0);
    char *native_new_address = (char *) malloc(IP_BUFFER_SIZE);
    jstring new_ip = 0;
    int ret = util_validate_ip(native_ip_address, native_new_address, IP_BUFFER_SIZE);

    if (ret != 1)
    {
    	new_ip = (*env)->NewStringUTF(env, "");
    }
    else
    {
    	new_ip = (*env)->NewStringUTF(env, native_new_address);
    }
	free(native_new_address);
    (*env)->ReleaseStringUTFChars(env, ip_address, native_ip_address);

    return new_ip;
}
