package hello.leilei.base.audioplayer;

import android.text.TextUtils;

import hello.leilei.lyric.LyricPresenter;
import hello.leilei.model.FileMetaData;
import hello.leilei.nativeaudio.NativeAudio;
import hello.leilei.utils.RxUiUtils;
import rx.Observable;
import rx.functions.Func0;

/**
 * Created by liulei
 * DATE: 2016/12/2
 * TIME: 11:01
 */
public class NativePlayer extends BasePlayer {

    private static NativePlayer player;

    public static final int SEEKBAR_MAX = 1000;

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

    private NativePlayer() {
        mLyricPresenter = new LyricPresenter();
        NativeAudio.getInstance().addPlayOverListener(() -> {
            if (isResouceLoadComplete())
                playNext();
        });
    }

    public void initAudio() {
        // initialize native audio system
        if (!isInitEngine) {
            NativeAudio.createEngine();
            isInitEngine = true;
        }
    }

    @Override
    public long getDuration() {
        return NativePlayer.getInstance().getDuration();
    }

    @Override
    public long getPostion() {
        return NativePlayer.getInstance().getPostion();
    }

    @Override
    public void setPosition(long millisecond) {
        NativeAudio.setPostion(millisecond);
    }

    @Override
    public void playNext() {

        if (selectIndex + 1 < getCount()) {
            selectIndex++;
            playMusic(selectIndex);
        }
        // notice: 2016/12/5 循环模式
    }

    @Override
    public void playPrevious() {
        if (selectIndex >= 1)
            selectIndex--;
        playMusic(selectIndex);
    }

    public int getCuttentPlayIndex() {
        return currentPlayIndex;
    }

    public void pauseCurrPlayMusic() {
        NativeAudio.setPlayingUriAudioPlayer(false);
    }

    public void playMusic(int selectIndex) {
        this.selectIndex = selectIndex;
        int state = NativeAudio.getPlayingUriState();
        //* 0 stoped 1 play 2 pause -1 error
        if (state == ERROR || state == STOPPED || currentPlayIndex != selectIndex) {

            int cout = getCount();
            if (selectIndex == -1 && cout > 0) {
                //默认选择第一首歌
                selectIndex = 0;
            }

            if (selectIndex < 0 || selectIndex > cout) {
                setPlayImageStateCallBack(state);
                return;
            }

            if (state == PLAYED) {
                NativeAudio.setPlayingUriAudioPlayer(false);
            }
            playMp3File();
        } else {
            if (state == PLAYED) {
                NativeAudio.setPlayingUriAudioPlayer(false);
                setPlayImageStateCallBack(PAUSED);
            } else if (state == PAUSED) {
                NativeAudio.setPlayingUriAudioPlayer(true);
                setPlayImageStateCallBack(PLAYED);
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
            currentPlayIndex = selectIndex;
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
                    setPlayImageStateCallBack(PLAYED);
                    changePlayProgress();

                }, Throwable::printStackTrace);
    }

    @Override
    public void release() {
        isInitEngine = false;
        NativeAudio.shutdown();
    }

}
