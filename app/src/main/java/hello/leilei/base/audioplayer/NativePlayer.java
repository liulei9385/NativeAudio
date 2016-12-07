package hello.leilei.base.audioplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hello.leilei.lyric.LyricPresenter;
import hello.leilei.model.FileMetaData;
import hello.leilei.nativeaudio.FileFind;
import hello.leilei.nativeaudio.NativeAudio;
import hello.leilei.utils.CollectionUtils;
import hello.leilei.utils.FileUtils;
import hello.leilei.utils.RxUiUtils;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func0;
import rx.observables.ConnectableObservable;

/**
 * Created by liulei
 * DATE: 2016/12/2
 * TIME: 11:01
 */
public class NativePlayer {

    private static NativePlayer player;

    public static final int SEEKBAR_MAX = 1000;

    public static final int STOPPED = 0;
    public static final int PLAYED = 1;
    public static final int PAUSED = 2;
    public static final int ERROR = -1;

    public static NativePlayer getInstance() {
        if (player == null) {
            synchronized (NativePlayer.class) {
                if (player == null)
                    player = new NativePlayer();
            }
        }
        return player;
    }

    private boolean isInitEngine = false;

    private List<FileMetaData> fileMetaDatas;
    private Subscription changeProgressSubscri;
    private ConnectableObservable<List<String>> searchFileObser;
    private LyricPresenter mLyricPresenter;

    private List<String> mp3FileList;

    private int cuttentPlayIndex;
    private int selectIndex;

    private List<IPlayerCallback> mPlayerCallbacks;

    public void addPlayerCallback(IPlayerCallback mPlayerCallback) {
        if (mPlayerCallbacks == null)
            this.mPlayerCallbacks = new ArrayList<>();
        this.mPlayerCallbacks.add(mPlayerCallback);
    }

    public void removePlayerCallback(IPlayerCallback mPlayerCallback) {
        if (mPlayerCallbacks != null) {
            this.mPlayerCallbacks.remove(mPlayerCallback);
        }
    }

    public List<FileMetaData> getFileMetaDatas() {
        return fileMetaDatas;
    }

    public void setFileMetaDatas(List<FileMetaData> fileMetaDatas) {
        this.fileMetaDatas = fileMetaDatas;
        if (mPlayerCallbacks != null) {
            //noinspection Convert2streamapi
            for (IPlayerCallback callback : mPlayerCallbacks)
                callback.onLoadResourceComplete();
        }
    }

    private NativePlayer() {
        mLyricPresenter = new LyricPresenter();
        NativeAudio.getInstance().addPlayOverListener(() -> {
            if (isResouceLoadComplete())
                playerNext();
        });
    }

    public void initAudio() {
        // initialize native audio system
        if (!isInitEngine) {
            NativeAudio.createEngine();
            isInitEngine = true;
        }
    }

    public boolean isResouceLoadComplete() {
        return CollectionUtils.isNotEmpty(fileMetaDatas);
    }

    public List<String> getMp3FileList() {
        return mp3FileList;
    }

    public LyricPresenter getLyricPresenter() {
        return mLyricPresenter;
    }

    private int getCount() {
        return fileMetaDatas != null ? fileMetaDatas.size() : 0;
    }

    public FileMetaData getMetaData(int index) {
        if (getCount() > 0)
            return fileMetaDatas.get(index);
        return null;
    }

    public void playerNext() {

        if (selectIndex + 1 < getCount()) {
            selectIndex++;
            playMusic(selectIndex);
        }
        // notice: 2016/12/5 循环模式
    }

    public void playPrevious() {
        if (selectIndex >= 1)
            selectIndex--;
        playMusic(selectIndex);
    }

    public void enableProgressChange(boolean isEnable) {
        if (!isEnable)
            RxUiUtils.unsubscribe(changeProgressSubscri);
        else {
            changePlayProgress();
        }
    }

    public int getCuttentPlayIndex() {
        return cuttentPlayIndex;
    }

    public void pauseCurrPlayMusic() {
        NativeAudio.setPlayingUriAudioPlayer(false);
    }

    public void playMusic(int selectIndex) {
        this.selectIndex = selectIndex;
        int state = NativeAudio.getPlayingUriState();
        //* 0 stoped 1 play 2 pause -1 error
        if (state == ERROR || state == STOPPED || cuttentPlayIndex != selectIndex) {

            int cout = getCount();
            if (selectIndex == -1 && cout > 0) {
                //默认选择第一首歌
                selectIndex = 0;
            }

            if (selectIndex < 0 || selectIndex > cout) {
                setPlayImageState(state);
                return;
            }

            if (state == PLAYED) {
                NativeAudio.setPlayingUriAudioPlayer(false);
            }
            playMp3File();
        } else {
            if (state == PLAYED) {
                NativeAudio.setPlayingUriAudioPlayer(false);
                setPlayImageState(PAUSED);
            } else if (state == PAUSED) {
                NativeAudio.setPlayingUriAudioPlayer(true);
                setPlayImageState(PLAYED);
            }
        }
    }

    private void playMp3File() {
        int count = getCount();
        if (count <= 0) {
            //showSToast("没有文件，请添加。。");
            return;
        }

        if (selectIndex >= 0 && selectIndex < count) {

            FileMetaData metaData = getMetaData(selectIndex);
            if (metaData == null || TextUtils.isEmpty(metaData.getUri())) {
                //showSToast("歌曲文件错误");
                return;
            }
            playMp3Music(metaData.getUri());
            cuttentPlayIndex = selectIndex;
        }
    }

    public void playMp3Music(String uri) {
        // 在后台线程中创建相关资源
        Observable.fromCallable((Func0<Boolean>) () -> {
            NativeAudio.setPlayingUriAudioPlayer(false);
            return NativeAudio.createUriAudioPlayer(uri);
        }).compose(RxUiUtils.applySchedulers())
                .subscribe(isSuccess -> {

                    NativeAudio.setPlayingUriAudioPlayer(true);
                    setPlayImageState(PLAYED);
                    changePlayProgress();

                }, Throwable::printStackTrace);
    }

    public static class ProgressItem {
        public long duration;
        public long position;
        public float percent;

        public ProgressItem(long duration, long position, float percent) {
            this.duration = duration;
            this.position = position;
            this.percent = percent;
        }
    }

    private void changePlayProgress() {
        RxUiUtils.unsubscribe(changeProgressSubscri);
        changeProgressSubscri = Observable.interval(0L, 500L, TimeUnit.MICROSECONDS)
                .flatMap(aLong -> Observable.fromCallable((Func0<ProgressItem>) () -> {
                    long duration = NativeAudio.getDutration();
                    long position = NativeAudio.getPostion();
                    float percent = position / (float) duration;
                    return new ProgressItem(duration, position, percent);
                }))//
                .compose(RxUiUtils.applySchedulers())
                .subscribe(progressItem -> {

                    if (mPlayerCallbacks != null) {
                        //noinspection Convert2streamapi
                        for (IPlayerCallback callback : mPlayerCallbacks)
                            callback.onProgressChanged(progressItem);
                    }

                }, Throwable::printStackTrace);
    }

    public ConnectableObservable<List<String>> getSearchFileObserable() {
        if (searchFileObser == null)
            searchFileObser = Observable.fromCallable((Func0<List<String>>) () -> {
                File sdDir = FileUtils.getExternalSdDir();
                List<String> mp3FileList = null;
                if (sdDir != null)
                    mp3FileList = FileFind.getMp3FileFromPath(sdDir.getPath());
                return mp3FileList;
            })//
                    .compose(RxUiUtils.applySchedulers())
                    .publish();
        return searchFileObser;
    }

    public void startToSearchMp3File() {

        getSearchFileObserable()
                .flatMap(mp3FileList -> {
                    this.mp3FileList = mp3FileList;
                    if (CollectionUtils.isNotEmpty(mp3FileList))
                        return mLyricPresenter.getMetaDataAction(mp3FileList);
                    return Observable.error(new IllegalArgumentException("list was null"));
                })
                .subscribe(datas -> {

                    NativePlayer.getInstance().setFileMetaDatas(datas);

                }, Throwable::printStackTrace);

        searchFileObser.connect();
        searchFileObser.refCount();
    }

    public void shutDown() {
        isInitEngine = false;
        NativeAudio.shutdown();
    }

    private void setPlayImageState(int state) {
        if (mPlayerCallbacks != null) {
            //noinspection Convert2streamapi
            for (IPlayerCallback callback : mPlayerCallbacks)
                callback.onPlayState(state);
        }
    }

    void exoPlayMp3(Context context, Uri localFileUri) {
        Handler handler = new Handler();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(handler, null);
        SimpleExoPlayer exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector, null);
        ExtractorMediaSource mediaSource = new ExtractorMediaSource(localFileUri,
                new FileDataSourceFactory(), Mp3Extractor.FACTORY, handler, null);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
    }

}
