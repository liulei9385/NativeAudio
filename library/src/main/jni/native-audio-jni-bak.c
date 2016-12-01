/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/* This is a JNI example where we use native methods to play sounds
 * using OpenSL ES. See the corresponding Java source file located at:
 *
 *   src/com/example/nativeaudio/NativeAudioBak/NativeAudioBak.java
 */

#include <assert.h>
#include <jni.h>
#include <string.h>

// for __android_log_print(ANDROID_LOG_INFO, "YourApp", "formatted message");
// #include <android/log.h>

// for native audio
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

// for native asset manager
#include <sys/types.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "include/file-find.h"

// engine interfaces
static SLObjectItf engineObject = NULL;
static SLEngineItf engineEngine;

// output mix interfaces
static SLObjectItf outputMixObject = NULL;
static SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;

// buffer queue player interfaces
static SLObjectItf bqPlayerObject = NULL;
static SLPlayItf bqPlayerPlay;
static SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue;
static SLEffectSendItf bqPlayerEffectSend;
static SLMuteSoloItf bqPlayerMuteSolo;
static SLVolumeItf bqPlayerVolume;

// aux effect on the output mix, used by the buffer queue player
static const SLEnvironmentalReverbSettings reverbSettings =
        SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

// URI player interfaces
static SLObjectItf uriPlayerObject = NULL;
static SLPlayItf uriPlayerPlay;
static SLSeekItf uriPlayerSeek;
static SLMuteSoloItf uriPlayerMuteSolo;
static SLVolumeItf uriPlayerVolume;

// file descriptor player interfaces
static SLObjectItf fdPlayerObject = NULL;
static SLPlayItf fdPlayerPlay;
static SLSeekItf fdPlayerSeek;
static SLMuteSoloItf fdPlayerMuteSolo;
static SLVolumeItf fdPlayerVolume;

// recorder interfaces
static SLObjectItf recorderObject = NULL;
static SLRecordItf recorderRecord;
static SLAndroidSimpleBufferQueueItf recorderBufferQueue;

// synthesized sawtooth clip
#define SAWTOOTH_FRAMES 8000
static short sawtoothBuffer[SAWTOOTH_FRAMES];

// 5 seconds of recorded audio at 16 kHz mono, 16-bit signed little endian
#define RECORDER_FRAMES (16000 * 5)
static short recorderBuffer[RECORDER_FRAMES];
static unsigned recorderSize = 0;
static SLmilliHertz recorderSR;

// pointer and size of the next player buffer to enqueue, and number of remaining buffers
static short *nextBuffer;
static unsigned nextSize;
static int nextCount;

JavaVM *gJavaVM;
jobject *gNativeAudioObj;

// this callback handler is called every time a buffer finishes playing
void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    assert(bq == bqPlayerBufferQueue);
    assert(NULL == context);
    // for streaming playback, replace this test by logic to find and fill the next buffer
    if (--nextCount > 0 && NULL != nextBuffer && 0 != nextSize) {
        SLresult result;
        // enqueue another buffer
        result = (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, nextBuffer, nextSize);
        // the most likely other result is SL_RESULT_BUFFER_INSUFFICIENT,
        // which for this code example would indicate a programming error
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }
}

void bqUriPlayOverCallback(SLPlayItf caller,
                           void *pContext,
                           SLuint32 event) {

    if (caller == uriPlayerPlay && (event & SL_PLAYEVENT_HEADATEND)) {
        JNIEnv *env;
        jboolean isAttached = JNI_FALSE;
        if (gJavaVM != NULL) {
            (*gJavaVM)->GetEnv(gJavaVM, (void **) &env, JNI_VERSION_1_6);
            if (env == NULL) {
                LOGE("12");
                int status = (*gJavaVM)->AttachCurrentThread(gJavaVM, &env, NULL);
                if (status < 0) {
                    return;
                }
                isAttached = JNI_TRUE;
            }
            if (env != NULL) {
                LOGE("13");
                jclass clazz = (*env)->GetObjectClass(env, gNativeAudioObj);
                //jclass clazz = (*env)->FindClass(env, "hello/leilei/nativeaudio/NativeAudioBak");
                if (clazz != NULL) {
                    jmethodID callNBackId = (*env)->GetMethodID(env, clazz, "doCallBack", "()V");
                    if (callNBackId != NULL) {
                        LOGE("doCallBack 15");
                        (*env)->CallVoidMethod(env, gNativeAudioObj, callNBackId);
                    }
                }
            }
            if (isAttached) {
                (*gJavaVM)->DetachCurrentThread(gJavaVM);
            }
        }
    }
}

// this callback handler is called every time a buffer finishes recording
void bqRecorderCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    assert(bq == recorderBufferQueue);
    assert(NULL == context);
    // for streaming recording, here we would call Enqueue to give recorder the next buffer to fill
    // but instead, this is a one-time buffer so we stop recording
    SLresult result;
    result = (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_STOPPED);
    if (SL_RESULT_SUCCESS == result) {
        recorderSize = RECORDER_FRAMES * sizeof(short);
        recorderSR = SL_SAMPLINGRATE_16;
    }
}

void getNativeAudioObj(JNIEnv *env) {
    jclass clazz = (*env)->FindClass(env, "hello/leilei/nativeaudio/NativeAudioBak");
    if (clazz != NULL) {
        jmethodID getinstanceId = (*env)->GetStaticMethodID(env, clazz,
                                                            "getInstance",
                                                            "()Lhello/leilei/nativeaudio/NativeAudioBak;");
        if (getinstanceId != NULL) {
            jobject nativeAudioObj = (*env)->CallStaticObjectMethod(env,
                                                                    clazz, getinstanceId);
            gNativeAudioObj = (*env)->NewGlobalRef(env, nativeAudioObj);
            LOGE("gNativeAudioObj success");
        }
    }
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGE("JNI_OnLoad");
    JNIEnv *env;
    gJavaVM = vm;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        LOGE("Failed to get the environment using GetEnv()");
        return -1;
    }
    //initClassHelper(env, kYouDAApiPath, &gYouDaApiObj);
    getNativeAudioObj(env);
    return JNI_VERSION_1_6;
}

// create the engine and output mix objects
void Java_hello_leilei_nativeaudio_NativeAudioBak_createEngine(JNIEnv *env, jclass clazz) {
    SLresult result;

    // create engine
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // realize the engine
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // get the engine interface, which is needed in order to create other objects
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // create output mix, with environmental reverb specified as a non-required interface
    const SLInterfaceID ids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, ids, req);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // realize the output mix
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // get the environmental reverb interface
    // this could fail if the environmental reverb effect is not available,
    // either because the feature is not present, excessive CPU load, or
    // the required MODIFY_AUDIO_SETTINGS permission was not requested and granted
    result = (*outputMixObject)->GetInterface(outputMixObject, SL_IID_ENVIRONMENTALREVERB,
                                              &outputMixEnvironmentalReverb);
    if (SL_RESULT_SUCCESS == result) {
        result = (*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(
                outputMixEnvironmentalReverb, &reverbSettings);
        (void) result;
    }
    // ignore unsuccessful result codes for environmental reverb, as it is optional for this example

}

// create URI audio player
jboolean Java_hello_leilei_nativeaudio_NativeAudioBak_createUriAudioPlayer(JNIEnv *env, jclass clazz,
                                                                        jstring uri) {
    SLresult result;

    // convert Java string to UTF-8
    const char *utf8 = (*env)->GetStringUTFChars(env, uri, NULL);
    assert(NULL != utf8);

    // configure audio source
    // (requires the INTERNET permission depending on the uri parameter)
    SLDataLocator_URI loc_uri = {SL_DATALOCATOR_URI, (SLchar *) utf8};
    SLDataFormat_MIME format_mime = {SL_DATAFORMAT_MIME, NULL, SL_CONTAINERTYPE_UNSPECIFIED};
    SLDataSource audioSrc = {&loc_uri, &format_mime};

    // configure audio sink
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};

    // create audio player
    const SLInterfaceID ids[3] = {SL_IID_SEEK, SL_IID_MUTESOLO, SL_IID_VOLUME};
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &uriPlayerObject, &audioSrc,
                                                &audioSnk, 3, ids, req);
    // note that an invalid URI is not detected here, but during prepare/prefetch on Android,
    // or possibly during Realize on other platforms
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // release the Java string and UTF-8
    (*env)->ReleaseStringUTFChars(env, uri, utf8);

    // realize the player
    result = (*uriPlayerObject)->Realize(uriPlayerObject, SL_BOOLEAN_FALSE);
    // this will always succeed on Android, but we check result for portability to other platforms
    if (SL_RESULT_SUCCESS != result) {
        (*uriPlayerObject)->Destroy(uriPlayerObject);
        uriPlayerObject = NULL;
        return JNI_FALSE;
    }

    // get the play interface
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_PLAY, &uriPlayerPlay);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // get the seek interface
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_SEEK, &uriPlayerSeek);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // get the mute/solo interface
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_MUTESOLO, &uriPlayerMuteSolo);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // get the volume interface
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_VOLUME, &uriPlayerVolume);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    (*uriPlayerPlay)->RegisterCallback(uriPlayerPlay, bqUriPlayOverCallback, NULL);
    (*uriPlayerPlay)->SetCallbackEventsMask(uriPlayerPlay, SL_PLAYEVENT_HEADATEND);

    return JNI_TRUE;
}


// set the playing state for the URI audio player
// to PLAYING (true) or PAUSED (false)
void Java_hello_leilei_nativeaudio_NativeAudioBak_setPlayingUriAudioPlayer(JNIEnv *env,
                                                                        jclass clazz,
                                                                        jboolean isPlaying) {
    SLresult result;

    // make sure the URI audio player was created
    if (NULL != uriPlayerPlay) {

        // set the player's state
        result = (*uriPlayerPlay)->SetPlayState(uriPlayerPlay, isPlaying ?
                                                               SL_PLAYSTATE_PLAYING
                                                                         : SL_PLAYSTATE_PAUSED);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }

}

//0 stoped 1 play 2 pause -1 error
jint Java_hello_leilei_nativeaudio_NativeAudioBak_getPlayingUriAudioPlayer(
        JNIEnv *env, jclass classz) {
    SLresult result;
    SLuint32 pState = SL_PLAYSTATE_STOPPED;
    if (NULL != uriPlayerPlay) {
        result = (*uriPlayerPlay)->GetPlayState(uriPlayerPlay, &pState);
        if (SL_RESULT_SUCCESS == result) {
            if (pState == SL_PLAYSTATE_PLAYING) {
                return 1;
            } else if (pState == SL_PLAYSTATE_PAUSED) {
                return 2;
            } else if (pState == SL_PLAYSTATE_STOPPED)
                return 0;
        } else {
            return -1;
        }
    }
    return -1;
}

jlong Java_hello_leilei_nativeaudio_NativeAudioBak_getDutration(
        JNIEnv *env, jclass classz) {
    SLresult result;
    jlong duration = 0L;
    SLmillisecond sLmillisecond = 0L;
    if (NULL != uriPlayerPlay) {
        result = (*uriPlayerPlay)->GetDuration(uriPlayerPlay, &sLmillisecond);
        if (SL_RESULT_SUCCESS == result) {
            duration = (unsigned long long) sLmillisecond;
        } else {
            return duration;
        }
    }
    return duration;
}

jlong Java_hello_leilei_nativeaudio_NativeAudioBak_getPostion(
        JNIEnv *env, jclass classz) {
    SLresult result;
    jlong postion = 0L;
    SLmillisecond sLmillisecond = 0L;
    if (NULL != uriPlayerPlay) {
        result = (*uriPlayerPlay)->GetPosition(uriPlayerPlay, &sLmillisecond);
        if (SL_RESULT_SUCCESS == result) {
            postion = (unsigned long long) sLmillisecond;
        } else {
            return postion;
        }
    }
    return postion;
}

// set the whole file looping state for the URI audio player
void Java_hello_leilei_nativeaudio_NativeAudioBak_setLoopingUriAudioPlayer(JNIEnv *env,
                                                                        jclass clazz,
                                                                        jboolean isLooping) {
    SLresult result;

    // make sure the URI audio player was created
    if (NULL != uriPlayerSeek) {

        // set the looping state
        result = (*uriPlayerSeek)->SetLoop(uriPlayerSeek, (SLboolean) isLooping, 0,
                                           SL_TIME_UNKNOWN);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }

}


// expose the mute/solo APIs to Java for one of the 3 players

static SLMuteSoloItf getMuteSolo() {
    if (uriPlayerMuteSolo != NULL)
        return uriPlayerMuteSolo;
    else if (fdPlayerMuteSolo != NULL)
        return fdPlayerMuteSolo;
    else
        return bqPlayerMuteSolo;
}

void Java_hello_leilei_nativeaudio_NativeAudioBak_setChannelMuteUriAudioPlayer(JNIEnv *env,
                                                                            jclass clazz,
                                                                            jint chan,
                                                                            jboolean mute) {
    SLresult result;
    SLMuteSoloItf muteSoloItf = getMuteSolo();
    if (NULL != muteSoloItf) {
        result = (*muteSoloItf)->SetChannelMute(muteSoloItf, chan, mute);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }
}

void Java_hello_leilei_nativeaudio_NativeAudioBak_setChannelSoloUriAudioPlayer(JNIEnv *env,
                                                                            jclass clazz,
                                                                            jint chan,
                                                                            jboolean solo) {
    SLresult result;
    SLMuteSoloItf muteSoloItf = getMuteSolo();
    if (NULL != muteSoloItf) {
        result = (*muteSoloItf)->SetChannelSolo(muteSoloItf, chan, solo);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }
}

int Java_hello_leilei_nativeaudio_NativeAudioBak_getNumChannelsUriAudioPlayer(JNIEnv *env,
                                                                           jclass clazz) {
    SLuint8 numChannels;
    SLresult result;
    SLMuteSoloItf muteSoloItf = getMuteSolo();
    if (NULL != muteSoloItf) {
        result = (*muteSoloItf)->GetNumChannels(muteSoloItf, &numChannels);
        if (SL_RESULT_PRECONDITIONS_VIOLATED == result) {
            // channel count is not yet known
            numChannels = 0;
        } else {
            assert(SL_RESULT_SUCCESS == result);
        }
    } else {
        numChannels = 0;
    }
    return numChannels;
}

// expose the volume APIs to Java for one of the 3 players

static SLVolumeItf getVolume() {
    if (uriPlayerVolume != NULL)
        return uriPlayerVolume;
    else if (fdPlayerVolume != NULL)
        return fdPlayerVolume;
    else
        return bqPlayerVolume;
}

void Java_hello_leilei_nativeaudio_NativeAudioBak_setVolumeUriAudioPlayer(JNIEnv *env,
                                                                       jclass clazz,
                                                                       jint millibel) {
    SLresult result;
    SLVolumeItf volumeItf = getVolume();
    if (NULL != volumeItf) {
        result = (*volumeItf)->SetVolumeLevel(volumeItf, millibel);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }
}

void Java_hello_leilei_nativeaudio_NativeAudioBak_setMuteUriAudioPlayer(JNIEnv *env, jclass clazz,
                                                                     jboolean mute) {
    SLresult result;
    SLVolumeItf volumeItf = getVolume();
    if (NULL != volumeItf) {
        result = (*volumeItf)->SetMute(volumeItf, mute);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }
}

void Java_hello_leilei_nativeaudio_NativeAudioBak_enableStereoPositionUriAudioPlayer(JNIEnv *env,
                                                                                  jclass clazz,
                                                                                  jboolean enable) {
    SLresult result;
    SLVolumeItf volumeItf = getVolume();
    if (NULL != volumeItf) {
        result = (*volumeItf)->EnableStereoPosition(volumeItf, enable);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }
}

void Java_hello_leilei_nativeaudio_NativeAudioBak_setStereoPositionUriAudioPlayer(JNIEnv *env,
                                                                               jclass clazz,
                                                                               jint permille) {
    SLresult result;
    SLVolumeItf volumeItf = getVolume();
    if (NULL != volumeItf) {
        result = (*volumeItf)->SetStereoPosition(volumeItf, permille);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }
}

// enable reverb on the buffer queue player
jboolean Java_hello_leilei_nativeaudio_NativeAudioBak_enableReverb(JNIEnv *env, jclass clazz,
                                                                jboolean enabled) {
    SLresult result;

    // we might not have been able to add environmental reverb to the output mix
    if (NULL == outputMixEnvironmentalReverb) {
        return JNI_FALSE;
    }

    result = (*bqPlayerEffectSend)->EnableEffectSend(bqPlayerEffectSend,
                                                     outputMixEnvironmentalReverb,
                                                     (SLboolean) enabled, (SLmillibel) 0);
    // and even if environmental reverb was present, it might no longer be available
    if (SL_RESULT_SUCCESS != result) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

// create asset audio player
jboolean Java_hello_leilei_nativeaudio_NativeAudioBak_createAssetAudioPlayer(JNIEnv *env,
                                                                          jclass clazz,
                                                                          jobject assetManager,
                                                                          jstring filename) {
    SLresult result;

    // convert Java string to UTF-8
    const char *utf8 = (*env)->GetStringUTFChars(env, filename, NULL);
    assert(NULL != utf8);

    // use asset manager to open asset by filename
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
    assert(NULL != mgr);
    AAsset *asset = AAssetManager_open(mgr, utf8, AASSET_MODE_UNKNOWN);

    // release the Java string and UTF-8
    (*env)->ReleaseStringUTFChars(env, filename, utf8);

    // the asset might not be found
    if (NULL == asset) {
        return JNI_FALSE;
    }

    // open asset as file descriptor
    off_t start, length;
    int fd = AAsset_openFileDescriptor(asset, &start, &length);
    assert(0 <= fd);
    AAsset_close(asset);

    // configure audio source
    SLDataLocator_AndroidFD loc_fd = {SL_DATALOCATOR_ANDROIDFD, fd, start, length};
    SLDataFormat_MIME format_mime = {SL_DATAFORMAT_MIME, NULL, SL_CONTAINERTYPE_UNSPECIFIED};
    SLDataSource audioSrc = {&loc_fd, &format_mime};

    // configure audio sink
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};

    // create audio player
    const SLInterfaceID ids[3] = {SL_IID_SEEK, SL_IID_MUTESOLO, SL_IID_VOLUME};
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &fdPlayerObject, &audioSrc,
                                                &audioSnk,
                                                3, ids, req);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // realize the player
    result = (*fdPlayerObject)->Realize(fdPlayerObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // get the play interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_PLAY, &fdPlayerPlay);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // get the seek interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_SEEK, &fdPlayerSeek);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // get the mute/solo interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_MUTESOLO,
                                             &fdPlayerMuteSolo);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // get the volume interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_VOLUME, &fdPlayerVolume);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // enable whole file looping
    result = (*fdPlayerSeek)->SetLoop(fdPlayerSeek, SL_BOOLEAN_TRUE, 0, SL_TIME_UNKNOWN);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    return JNI_TRUE;
}


// set the playing state for the asset audio player
void Java_hello_leilei_nativeaudio_NativeAudioBak_setPlayingAssetAudioPlayer(JNIEnv *env,
                                                                          jclass clazz,
                                                                          jboolean isPlaying) {
    SLresult result;

    // make sure the asset audio player was created
    if (NULL != fdPlayerPlay) {

        // set the player's state
        result = (*fdPlayerPlay)->SetPlayState(fdPlayerPlay, isPlaying ?
                                                             SL_PLAYSTATE_PLAYING
                                                                       : SL_PLAYSTATE_PAUSED);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }

}


// create audio recorder
jboolean Java_hello_leilei_nativeaudio_NativeAudioBak_createAudioRecorder(JNIEnv *env,
                                                                       jclass clazz) {
    SLresult result;

    // configure audio source
    SLDataLocator_IODevice loc_dev = {SL_DATALOCATOR_IODEVICE, SL_IODEVICE_AUDIOINPUT,
                                      SL_DEFAULTDEVICEID_AUDIOINPUT, NULL};
    SLDataSource audioSrc = {&loc_dev, NULL};

    // configure audio sink
    SLDataLocator_AndroidSimpleBufferQueue loc_bq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
                                                     2};
    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 1, SL_SAMPLINGRATE_16,
                                   SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
                                   SL_SPEAKER_FRONT_CENTER, SL_BYTEORDER_LITTLEENDIAN};
    SLDataSink audioSnk = {&loc_bq, &format_pcm};

    // create audio recorder
    // (requires the RECORD_AUDIO permission)
    const SLInterfaceID id[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};
    result = (*engineEngine)->CreateAudioRecorder(engineEngine, &recorderObject, &audioSrc,
                                                  &audioSnk, 1, id, req);
    if (SL_RESULT_SUCCESS != result) {
        return JNI_FALSE;
    }

    // realize the audio recorder
    result = (*recorderObject)->Realize(recorderObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        return JNI_FALSE;
    }

    // get the record interface
    result = (*recorderObject)->GetInterface(recorderObject, SL_IID_RECORD, &recorderRecord);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // get the buffer queue interface
    result = (*recorderObject)->GetInterface(recorderObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                             &recorderBufferQueue);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // register callback on the buffer queue
    result = (*recorderBufferQueue)->RegisterCallback(recorderBufferQueue, bqRecorderCallback,
                                                      NULL);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    return JNI_TRUE;
}


// set the recording state for the audio recorder
void Java_hello_leilei_nativeaudio_NativeAudioBak_startRecording(JNIEnv *env, jclass clazz) {
    SLresult result;

    // in case already recording, stop recording and clear buffer queue
    result = (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_STOPPED);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;
    result = (*recorderBufferQueue)->Clear(recorderBufferQueue);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // the buffer is not valid for playback yet
    recorderSize = 0;

    // enqueue an empty buffer to be filled by the recorder
    // (for streaming recording, we would enqueue at least 2 empty buffers to start things off)
    result = (*recorderBufferQueue)->Enqueue(recorderBufferQueue, recorderBuffer,
                                             RECORDER_FRAMES * sizeof(short));
    // the most likely other result is SL_RESULT_BUFFER_INSUFFICIENT,
    // which for this code example would indicate a programming error
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // start recording
    result = (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_RECORDING);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;
}


// shut down the native audio system
void Java_hello_leilei_nativeaudio_NativeAudioBak_shutdown(JNIEnv *env, jclass clazz) {

    // destroy buffer queue audio player object, and invalidate all associated interfaces
    if (bqPlayerObject != NULL) {
        (*bqPlayerObject)->Destroy(bqPlayerObject);
        bqPlayerObject = NULL;
        bqPlayerPlay = NULL;
        bqPlayerBufferQueue = NULL;
        bqPlayerEffectSend = NULL;
        bqPlayerMuteSolo = NULL;
        bqPlayerVolume = NULL;
    }

    // destroy file descriptor audio player object, and invalidate all associated interfaces
    if (fdPlayerObject != NULL) {
        (*fdPlayerObject)->Destroy(fdPlayerObject);
        fdPlayerObject = NULL;
        fdPlayerPlay = NULL;
        fdPlayerSeek = NULL;
        fdPlayerMuteSolo = NULL;
        fdPlayerVolume = NULL;
    }

    // destroy URI audio player object, and invalidate all associated interfaces
    if (uriPlayerObject != NULL) {
        (*uriPlayerObject)->Destroy(uriPlayerObject);
        uriPlayerObject = NULL;
        uriPlayerPlay = NULL;
        uriPlayerSeek = NULL;
        uriPlayerMuteSolo = NULL;
        uriPlayerVolume = NULL;
    }

    // destroy audio recorder object, and invalidate all associated interfaces
    if (recorderObject != NULL) {
        (*recorderObject)->Destroy(recorderObject);
        recorderObject = NULL;
        recorderRecord = NULL;
        recorderBufferQueue = NULL;
    }

    // destroy output mix object, and invalidate all associated interfaces
    if (outputMixObject != NULL) {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = NULL;
        outputMixEnvironmentalReverb = NULL;
    }

    // destroy engine object, and invalidate all associated interfaces
    if (engineObject != NULL) {
        (*engineObject)->Destroy(engineObject);
        engineObject = NULL;
        engineEngine = NULL;
    }

}