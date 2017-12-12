package hello.leilei.base.audioplayer;

import android.annotation.SuppressLint;
import android.net.Uri;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.flac.FlacExtractor;
import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor;
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer2.extractor.wav.WavExtractor;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;

import java.io.File;

import hello.leilei.MainApplication;
import hello.leilei.base.listener.SimpleEventListener;
import hello.leilei.model.FileMetaData;
import hello.leilei.utils.RxUiUtils;
import timber.log.Timber;

/**
 * Created by liulei
 * DATE: 2016/12/7
 * TIME: 10:33
 * 使用上Exoplayer
 */

public class AudioPlayer extends BasePlayer {

    private DefaultTrackSelector trackSelector;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private static AudioPlayer player;
    private SimpleExoPlayer exoPlayer;
    private boolean isInit = false;

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

        LoadControl loadControl = new DefaultLoadControl();
        TrackSelection.Factory adaptiveTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);

        DefaultRenderersFactory defaultRenderersFactory = new DefaultRenderersFactory(MainApplication.getApp(), null,
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);

        exoPlayer = ExoPlayerFactory.newSimpleInstance(defaultRenderersFactory,
                trackSelector, loadControl);
        exoPlayer.addListener(new SimpleEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    playNext();
                }
            }
        });

        isInit = true;
    }

    public DefaultTrackSelector getTrackSelector() {
        return trackSelector;
    }

    @SuppressLint("BinaryOperationInTimber")
    private void exoPlayMp3(String fileUri) {

        Uri localFileUri = Uri.fromFile(new File(fileUri));
        ExtractorMediaSource mediaSource = new ExtractorMediaSource(localFileUri,
                new FileDataSourceFactory(), () -> new Extractor[]{
                new Mp3Extractor(), new WavExtractor(), new Mp4Extractor(), new FlacExtractor()
        }, null, null);

        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
        setPlayerState(Player.STATE_READY);
        changePlayProgress();
        RxUiUtils.postDelayedOnBg(1000L, () -> Timber.d("fileUrl##" + fileUri + "###duration=" +
                exoPlayer.getDuration()));
    }

    @Override
    public void playMusic(int selectIndex) {

        if (!isInit)
            initPlayer();

        if (selectIndex < 0 || selectIndex >= getCount()) return;

        if (shouldPlay(selectIndex)) {
            setCurrentPlayIndex(selectIndex);
            exoPlayer.setPlayWhenReady(false);
            FileMetaData metaData = getMetaData(selectIndex);
            exoPlayMp3(metaData.getUri());
            return;
        }

        int state = exoPlayer.getPlaybackState();
        switch (state) {
            case Player.STATE_READY:
                boolean isReady = exoPlayer.getPlayWhenReady();
                exoPlayer.setPlayWhenReady(!isReady);
                setPlayerState(state);
                break;
            case Player.STATE_ENDED:
                playNext();
                break;
            case Player.STATE_IDLE:
                FileMetaData metaData = getMetaData(selectIndex);
                exoPlayMp3(metaData.getUri());
                break;
        }
    }

    private void setPlayerState(int state) {
        switch (state) {
            case Player.STATE_IDLE:
                setPlayImageStateCallBack(ERROR);
                break;
            case Player.STATE_READY:
                setPlayImageStateCallBack(exoPlayer.getPlayWhenReady() ? PLAYED : PAUSED);
                break;
        }
    }

    // 播放下一首
    @Override
    public void playNext() {
        playMusic(getCurrentPlayIndex() + 1);
    }

    // 播放上一首
    @Override
    public void playPrevious() {
        playMusic(getCurrentPlayIndex() - 1);
    }

    public void release() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.release();
            exoPlayer = null;
            isInit = false;
        }
    }

    @Override
    public long getDuration() {
        if (exoPlayer == null)
            initPlayer();
        return exoPlayer.getDuration();
    }

    @Override
    public long getPostion() {
        if (exoPlayer == null)
            initPlayer();
        return exoPlayer.getCurrentPosition();
    }

    @Override
    public void setPosition(long millisecond) {
        if (exoPlayer == null)
            initPlayer();
        exoPlayer.seekTo(millisecond);
    }

}