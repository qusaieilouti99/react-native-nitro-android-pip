#include <jni.h>
#include "nitroandroidpipOnLoad.hpp"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
  return margelo::nitro::nitroandroidpip::initialize(vm);
}
