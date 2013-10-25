LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_LIB_TYPE        := STATIC
OPENCV_INSTALL_MODULES := on
OPENCV_CAMERA_MODULES  := off


#OPENCV_MK_PATH := ../../OpenCV-2.4/sdk/native/jni/
OPENCV_MK_PATH := native/jni/

include $(OPENCV_MK_PATH)/OpenCV.mk

LOCAL_MODULE    := cyclisttrack_opencv_jni
LOCAL_SRC_FILES := PlateDetect.cpp
LOCAL_LDLIBS    +=  -llog -ldl
LOCAL_LDFLAGS 	+= -O3 


include $(BUILD_SHARED_LIBRARY)
