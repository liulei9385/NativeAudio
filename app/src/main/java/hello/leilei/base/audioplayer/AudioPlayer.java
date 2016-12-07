package hello.leilei.base.audioplayer;

import android.net.Uri;
import android.os.Handler;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor;
import com.google.android.exoplayer2.extractor.wav.WavExtractor;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;

import hello.leilei.MainApplication;

/**
 * Created by liulei
 * DATE: 2016/12/7
 * TIME: 10:33
 * 使用上Exoplayer
 */

public class AudioPlayer {

    private static AudioPlayer player;
    private ExoPlayer exoPlayer;

    public static AudioPlayer getInstance() {
        if (player == null) {
            synchronized (AudioPlayer.class) {
                if (player == null) {
                    player = new AudioPlayer();
                    player.initPlayer();
                }
            }
        }
        return player;
    }

    private void initPlayer() {
        Handler eventHanlder = new Handler();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(eventHanlder);
        if (exoPlayer != null) {
            exoPlayer.release();
        }
        exoPlayer = ExoPlayerFactory.newSimpleInstance(MainApplication.getApp(),
                trackSelector, new DefaultLoadControl());

    }

    public void exoPlayMp3(Uri localFileUri) {
        ExtractorMediaSource mediaSource = new ExtractorMediaSource(localFileUri,
                new FileDataSourceFactory(), () -> new Extractor[]{
                new Mp3Extractor(), new WavExtractor()
        }, null, null);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
    }

}
