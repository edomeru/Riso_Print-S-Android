LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE     := librdpdf-prebuilt
LOCAL_SRC_FILES := librdpdf/$(TARGET_ARCH_ABI)/librdpdf.so

include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)

COMMON_API_DIR   := $(LOCAL_PATH)/../../CommonAPI

LOCAL_MODULE     := CommonAPI
LOCAL_C_INCLUDES := $(COMMON_API_DIR)/include
LOCAL_SRC_FILES  := ../../CommonAPI/src/DirectPrint.cpp
LOCAL_SRC_FILES  += CommonAPI_wrap.cpp

include $(BUILD_SHARED_LIBRARY)
