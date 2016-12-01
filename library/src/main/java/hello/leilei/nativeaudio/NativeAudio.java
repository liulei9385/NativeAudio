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
 */

package hello.leilei.nativeaudio;

import android.content.res.AssetManager;

public class NativeAudio {

    private static NativeAudio nativeAudio;

    public static NativeAudio getInstance() {
        if (nativeAudio == null)
            nativeAudio = new NativeAudio();
        return nativeAudio;
    }

    public static interface OnPlayOverListener {
        void onPlayOver();
    }

    private OnPlayOverListener playOverListener;

    public void setPlayOverListener(OnPlayOverListener playOverListener) {
        this.playOverListener = playOverListener;
    }

    public void doCallBack() {
        if (playOverListener != null)
            playOverListener.onPlayOver();
    }

    /**
     * Native methods, implemented in jni folder
     */
    public static native void createEngine();

    public static native void createBufferQueueAudioPlayer(int sampleRate, int samplesPerBuf);

    public static native boolean createAssetAudioPlayer(AssetManager assetManager, String filename);

    // true == PLAYING, false == PAUSED
    public static native void setPlayingAssetAudioPlayer(boolean isPlaying);

    public static native boolean createUriAudioPlayer(String uri);

    public static native void setPlayingUriAudioPlayer(boolean isPlaying);

    public static native void setLoopingUriAudioPlayer(boolean isLooping);

    public static native void setChannelMuteUriAudioPlayer(int chan, boolean mute);

    public static native void setChannelSoloUriAudioPlayer(int chan, boolean solo);

    public static native int getNumChannelsUriAudioPlayer();

    public static native void setVolumeUriAudioPlayer(int millibel);

    public static native void setMuteUriAudioPlayer(boolean mute);

    public static native void enableStereoPositionUriAudioPlayer(boolean enable);

    public static native void setStereoPositionUriAudioPlayer(int permille);

    public static native boolean enableReverb(boolean enabled);

    public static native boolean createAudioRecorder();

    public static native void startRecording();

    public static native long getDutration();

    public static native boolean setPostion(long millisecond);

    public static native long getPostion();

    /**
     * 0 stoped 1 play 2 pause -1 error
     *
     * @return above
     */
    public static native int getPlayingUriState();

    public static native void shutdown();

    /** Load jni .so on initialization */
    static {
        System.loadLibrary("native-audio-jni");
    }

}