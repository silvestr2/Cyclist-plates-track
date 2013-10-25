LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_LIB_TYPE        := STATIC
OPENCV_INSTALL_MODULES := on
OPENCV_CAMERA_MODULES  := off

OPENCV_MK_PATH := ../../OpenCV-2.4/share/OpenCV

include $(OPENCV_MK_PATH)/OpenCV.mk

LOCAL_MODULE    := objtrack_opencv_jni
LOCAL_SRC_FILES := objtrack.cpp
LOCAL_LDLIBS    +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)
