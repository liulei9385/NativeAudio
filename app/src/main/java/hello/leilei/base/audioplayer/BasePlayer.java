package hello.leilei.base.audioplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hello.leilei.lyric.LyricPresenter;
import hello.leilei.model.FileMetaData;
import hello.leilei.utils.CollectionUtils;
import hello.leilei.utils.RxUiUtils;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func0;

/**
 * Created by liulei
 * DATE: 2016/12/8
 * TIME: 16:34
 */
public abstract class BasePlayer {

    public static final int STOPPED = 0;
    public static final int PLAYED = 1;
    public static final int PAUSED = 2;
    public static final int ERROR = -1;
    protected LyricPresenter mLyricPresenter;
    private int currentPlayIndex = -1;
    protected List<IPlayerCallback> mPlayerCallbacks;
    private List<FileMetaData> fileMetaDatas;
    private Subscription changeProgressSubscri;

    public BasePlayer() {
        mLyricPresenter = new LyricPresenter();
    }

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

    /**
     * 是否要进行播放
     *
     * @return
     */
    public boolean shouldPlay(int index) {
        return currentPlayIndex != index;
    }

    public LyricPresenter getLyricPresenter() {
        return mLyricPresenter;
    }

    public List<FileMetaData> getFileMetaDatas() {
        return fileMetaDatas;
    }

    public void setFileMetaDatas(List<FileMetaData> fileMetaDatas) {
        this.fileMetaDatas = fileMetaDatas;
        if (!CollectionUtils.isEmpty(fileMetaDatas)) {
            if (currentPlayIndex == -1)
                currentPlayIndex = 0;
        }
        if (mPlayerCallbacks != null) {
            //noinspection Convert2streamapi
            for (IPlayerCallback callback : mPlayerCallbacks)
                callback.onLoadResourceComplete();
        }
    }

    public final int getCount() {
        return fileMetaDatas != null ? fileMetaDatas.size() : 0;
    }

    public FileMetaData getMetaData(int index) {
        if (getCount() > 0)
            return fileMetaDatas.get(index);
        return null;
    }

    public int getCurrentPlayIndex() {
        return currentPlayIndex;
    }

    public void setCurrentPlayIndex(int currentPlayIndex) {
        this.currentPlayIndex = currentPlayIndex;
    }

    public boolean isResouceLoadComplete() {
        return CollectionUtils.isNotEmpty(getFileMetaDatas());
    }

    protected void setPlayImageStateCallBack(int state) {
        if (mPlayerCallbacks != null) {
            //noinspection Convert2streamapi
            for (IPlayerCallback callback : mPlayerCallbacks)
                callback.onPlayState(state);
        }
    }

    public abstract void playMusic(int selectIndex);

    public abstract void playNext();

    public abstract void playPrevious();

    public abstract void release();

    public void enableProgressChange(boolean isEnable) {
        if (!isEnable)
            RxUiUtils.unsubscribe(changeProgressSubscri);
        else {
            changePlayProgress();
        }
    }

    public abstract long getDuration();

    public abstract long getPostion();

    public abstract void setPosition(long millisecond);

    public void changePlayProgress() {
        RxUiUtils.unsubscribe(changeProgressSubscri);
        changeProgressSubscri = Observable.interval(0L, 1000L, TimeUnit.MILLISECONDS)
                .flatMap(aLong -> Observable.fromCallable((Func0<ProgressItem>) () -> {
                    long duration = getDuration();
                    long position = getPostion();
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


}