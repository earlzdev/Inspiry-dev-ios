#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_app_inspiry_helpers_NativeKeys_stringFromJNI1(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "your-photoroom-apiKey";
    return env->NewStringUTF(hello.c_str());
}