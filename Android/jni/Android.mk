LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

COMMON_API_DIR   := $(LOCAL_PATH)/../../CommonAPI

LOCAL_MODULE     := CommonAPI
LOCAL_C_INCLUDES := $(COMMON_API_DIR)/include
LOCAL_SRC_FILES  := ../../CommonAPI/src/DirectPrint.cpp
LOCAL_SRC_FILES  += CommonAPI_wrap.cpp

include $(BUILD_SHARED_LIBRARY)
