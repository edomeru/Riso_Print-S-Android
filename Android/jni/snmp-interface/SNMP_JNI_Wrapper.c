//
//  SNMP_IOS_Wrapper.c
//  SNMPDiscoveryApp
//
//  Created by Paulus on 10/7/13.
//  Copyright (c) 2013 a-LINK Group. All rights reserved.
//

#include <string.h>
#include <jni.h>
#include <android/log.h>
#include "SNMP_Manager.h"

/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Java source
 * file located at:
 *
 *   apps/samples/hello-jni/project/src/com/example/hellojni/HelloJni.java
 */
#define LOG_TAG "NDK_SNMPMANAGER"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

void on_discovery_ended(int result);
void on_printer_added(snmp_device *device);

JNIEnv* m_env;
jobject m_jobj;

JNIEXPORT void
Java_jp_co_riso_smartdeviceapp_controller_snmp_SnmpManager_snmpManualSearch( JNIEnv* env,
                                                  jobject this, jstring ipaddress)
{
	m_env = env;
	m_jobj = this;
	const char *nativeString = (*env)->GetStringUTFChars(env, ipaddress, 0);

	snmp_device_manualdiscovery(on_discovery_ended, on_printer_added, nativeString);
	(*env)->ReleaseStringUTFChars(env, ipaddress, nativeString);
}

JNIEXPORT void
Java_jp_co_riso_smartdeviceapp_controller_snmp_SnmpManager_startSnmpDeviceDiscovery( JNIEnv* env,
                                                  jobject this)
{
	m_env = env;
	m_jobj = this;
	snmp_device_discovery(on_discovery_ended, on_printer_added);
}

JNIEXPORT void
Java_jp_co_riso_smartdeviceapp_controller_snmp_SnmpManager_snmpDeviceDiscoveryCancel( JNIEnv* env,
                                                  jobject this)
{
	LOGI("Cancelling snmp...");
	snmp_device_discovery_cancel();
}

JNIEXPORT int
Java_jp_co_riso_smartdeviceapp_controller_snmp_SnmpManager_snmpCheckDeviceStatus( JNIEnv* env,
                                                  jobject this, jstring ipaddress)
{
	int ret = 0;
	m_env = env;
	m_jobj = this;
	const char *nativeString = (*env)->GetStringUTFChars(env, ipaddress, 0);

	ret = snmp_device_checkstatus(nativeString);
	(*env)->ReleaseStringUTFChars(env, ipaddress, nativeString);
	LOGI("snmpCheckDeviceStatus returned: %d", ret);

	return ret;
}

void on_discovery_ended(int result)
{
	LOGI("on_discovery_ended, result: %d\n", result);
	jclass cls = (*m_env)->GetObjectClass(m_env, m_jobj);
	jmethodID mid = (*m_env)->GetMethodID(m_env, cls, "searchPrinterEnd", "()V");
	if (mid == 0)
	{
		LOGI("Unable to find method for callback");
		return;
	}

	(*m_env)->CallVoidMethod(m_env, m_jobj, mid);

}

void on_printer_added(snmp_device *device)
{
	LOGI("on_printer_added\n");
	LOGI("    IP Address: %s\n", device->ip_addr);
	LOGI("    sysDesc: %s\n", device->device_info[MIB_SYS_DESC]);
	LOGI("    sysName: %s\n", device->device_info[MIB_SYS_NAME]);
	LOGI("    sysLocation: %s\n", device->device_info[MIB_SYS_LOCATION]);
	LOGI("    ifPhysAddress: %s\n", device->device_info[MIB_IF_PHYS_ADDRESS]);
	LOGI("    Capabilities: ");
    for (int i = 0; i < device->print_capabilities_count; i++)
    {
        LOGI("%d, ", device->print_capabilities[i]);
    }

	jclass cls = (*m_env)->GetObjectClass(m_env, m_jobj);
    jmethodID mid = (*m_env)->GetMethodID(m_env, cls, "printerAdded", "(Ljava/lang/String;Ljava/lang/String;)V");
    if (mid == 0)
    {
    	LOGI("Unable to find method for callback");
    	return;
    }

    jstring name = (*m_env)->NewStringUTF(m_env, device->device_info[MIB_SYS_DESC]);
    jstring ipaddress = (*m_env)->NewStringUTF(m_env, device->ip_addr);

    (*m_env)->CallVoidMethod(m_env, m_jobj, mid, name, ipaddress);
}
