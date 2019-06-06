LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libnetsnmp-prebuilt
LOCAL_SRC_FILES := 3rdParty/net-snmp/lib/android/$(TARGET_ARCH_ABI)/libsnmp.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libcommon-static
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include $(LOCAL_PATH)/3rdParty/net-snmp/include/android
LOCAL_SRC_FILES := src/directprint.c src/snmp.c src/printsettings.c src/util.c src/printsettings_fw.c src/printsettings_gd.c src/printsettings_rag.c src/printsettings_lio.c
LOCAL_CFLAGS += -std=c99
LOCAL_STATIC_LIBRARIES := libnetsnmp-prebuilt
include $(BUILD_STATIC_LIBRARY)
