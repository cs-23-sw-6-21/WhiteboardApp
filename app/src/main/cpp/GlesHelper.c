#include <jni.h>
#include <GLES2/gl2.h>
JNIEXPORT void JNICALL


// Change
dk_scuffed_whiteboardapp_helper_GlesHelper_glReadPixels(JNIEnv *env, jobject instance, jint x,
                                                        jint y, jint width, jint height,
                                                        jint format, jint type) {
    // TODO
    glReadPixels(x, y, width, height, format, type, 0);
}