#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_aloe_shike_generic_Native_hello(JNIEnv *env, jobject /*thiz*/) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
