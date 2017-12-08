#include <jni.h>
#include <malloc.h>
#include <string.h>
#include "include/file-find.h"

void Java_hello_leilei_nativeaudio_FileFind_scanDir(JNIEnv *env, jclass clazz, jstring path) {
    const char *pathUtf8 = (*env)->GetStringUTFChars(env, path, 0);
    int result = scan_dir_only((char *) pathUtf8, 0);
    LOGE("result %d", result);
    (*env)->ReleaseStringUTFChars(env, path, pathUtf8);
}

jstring stoJstring(JNIEnv *env, char *pat) {
    jclass strClass = (*env)->FindClass(env, "java/lang/String");
    // public String(byte bytes[], String charsetName)
    jmethodID ctorID = (*env)->GetMethodID(env, strClass, "<init>", "([BLjava/lang/String;)V");
    jbyteArray bytes = (*env)->NewByteArray(env, strlen(pat));
    (*env)->SetByteArrayRegion(env, bytes, 0, strlen(pat), (jbyte *) pat);
    jstring encoding = (*env)->NewStringUTF(env, "utf-8");
    return (jstring) (*env)->NewObject(env, strClass, ctorID, bytes, encoding);
}

jobject Java_hello_leilei_nativeaudio_FileFind_searchMp3File(
        JNIEnv *env,
        jclass clazz, jstring path) {

    const char *pathUtf8 = (*env)->GetStringUTFChars(env, path, 0);

    Filetemp *filetemp = (Filetemp *) malloc(sizeof(Filetemp));
    search_mp3file((char *) pathUtf8, filetemp);

    if (filetemp == NULL)
        return NULL;

    char **array = filetemp->filename;
    int size = filetemp->len;
    if (size <= 0)
        return NULL;

    jclass arraylist = (*env)->FindClass(env, "java/util/ArrayList");
    jmethodID list_costruct = (*env)->GetMethodID(env, arraylist, "<init>", "()V");
    jmethodID addMethod = (*env)->GetMethodID(env, arraylist, "add", "(Ljava/lang/Object;)Z");
    jobject list = (*env)->NewObject(env, arraylist, list_costruct);

    /*jclass newStringUtfClazz = (*env)->FindClass(env, "hello/leilei/nativeaudio/FileFind");
    jmethodID newStringMid = (*env)->GetStaticMethodID(env, newStringUtfClazz, "newStringUtf8",
                                                       "([B)Ljava/lang/String;");*/

    for (int len = 0; len < size; len++) {
        jstring str = stoJstring(env, *(array + len));
        //JNI DETECTED ERROR IN APPLICATION: input is not valid Modified UTF-8: illegal start byte 0xff
        //jstring str = (*env)->NewStringUTF(env, bytes);
        (*env)->CallBooleanMethod(env, list, addMethod, str);
        // Warning: 这里如果不手动释放局部引用，很有可能造成局部引用表溢出
        (*env)->DeleteLocalRef(env, str);
    }

    free(array);
    free(filetemp);
    (*env)->ReleaseStringUTFChars(env, path, pathUtf8);

    return (*env)->NewGlobalRef(env, list);
}