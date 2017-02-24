LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

include D:\PROJECT\OpenCVSDK\OpenCV-android-sdk\sdk\native\jni\OpenCV.mk

LOCAL_MODULE    := native-lib
LOCAL_SRC_FILES := native-lib.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)