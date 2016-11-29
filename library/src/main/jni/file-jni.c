#include <jni.h>
#include <stddef.h>
#include <malloc.h>
#include <string.h>
#include "include/file-find.h"

void Java_hello_leilei_nativeaudio_FileFind_scanDir(JNIEnv *env, jclass clazz, jstring path) {
    const char *pathUtf8 = (*env)->GetStringUTFChars(env, path, 0);
    int result = scan_dir_only((char *) pathUtf8, 0);
    LOGE("result %d", result);
    (*env)->ReleaseStringUTFChars(env, path, pathUtf8);
}

jobject Java_hello_leilei_nativeaudio_FileFind_searchMp3File(
        JNIEnv *env,
        jclass clazz, jstring path) {

    const char *pathUtf8 = (*env)->GetStringUTFChars(env, path, 0);

    Filetemp *filetemp;
    filetemp = malloc(256 * 256 * sizeof(char));
    search_mp3file((char *) pathUtf8, filetemp);

    if (filetemp == NULL)
        return NULL;

    char **array = filetemp->filename;
    int size = filetemp->len;
    int len = 0;
    if (size <= 0)
        return NULL;

    jclass arraylist = (*env)->FindClass(env, "java/util/ArrayList");
    jmethodID list_costruct = (*env)->GetMethodID(env, arraylist, "<init>", "()V");
    jmethodID addMethod = (*env)->GetMethodID(env, arraylist, "add", "(Ljava/lang/Object;)Z");
    jobject list = (*env)->NewObject(env, arraylist, list_costruct);

    for (; len < size; len++) {
        char *bytes = *(array + len);
        jstring str = (*env)->NewStringUTF(env, bytes);
        (*env)->CallBooleanMethod(env, list, addMethod, str);
    }

    free(array);
    (*env)->ReleaseStringUTFChars(env, path, pathUtf8);

    return (*env)->NewGlobalRef(env, list);
}