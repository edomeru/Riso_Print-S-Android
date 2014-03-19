LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE     := libsnmp-prebuilt
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libsnmp.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../../CommonAPI/include/
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE     := librdpdf-prebuilt
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/librdpdf.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE     := snmpAPI
LOCAL_SRC_FILES := snmp-interface/SNMP_JNI_Wrapper.c
LOCAL_SHARED_LIBRARIES := snmp-prebuilt
LOCAL_CFLAGS += -std=c99
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
COMMON_API_DIR   := $(LOCAL_PATH)/../../CommonAPI
LOCAL_C_INCLUDES += $(COMMON_API_DIR)/include
LOCAL_MODULE     := CommonAPI
LOCAL_SRC_FILES  := ../../CommonAPI/src/DirectPrint.cpp 
LOCAL_SRC_FILES  += CommonAPI_wrap.cpp
include $(BUILD_SHARED_LIBRARY)