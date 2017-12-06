package hello.leilei.base.audioplayer;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hello.leilei.lyric.LyricPresenter;
import hello.leilei.model.FileMetaData;
import hello.leilei.nativeaudio.FileFind;
import hello.leilei.utils.CollectionUtils;
import hello.leilei.utils.FileUtils;
import hello.leilei.utils.RxUiUtils;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by liulei
 * DATE: 2016/12/8
 * TIME: 22:23
 */

public class MusicFileSearch {

    private static MusicFileSearch search;

    public static MusicFileSearch getInstace() {
        if (search == null) {
            synchronized (MusicFileSearch.class) {
                if (search == null)
                    search = new MusicFileSearch();
            }
        }
        return search;
    }

    private MusicFileSearch() {
        mLyricPresenter = new LyricPresenter();
    }

    private ConnectableObservable<List<String>> searchFileObser;
    private List<String> mp3FileList;
    private LyricPresenter mLyricPresenter;

    public List<String> getMp3FileList() {
        return mp3FileList;
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

        FileMetaDataRepo metaDataRepo = FileMetaDataRepo.getInstance();

        if (!metaDataRepo.isInited()) {
            // 延时等待1s
            Observable.timer(1, TimeUnit.SECONDS)
                    .subscribe((ojb) -> {
                        if (!metaDataRepo.isInited())
                            return;
                        startToSearchMp3FileInternal(false);

                    }, RxUiUtils.onErrorDefault());
        } else {
            startToSearchMp3FileInternal(false);
        }
    }

    public void forceToSearchMp3File() {
        startToSearchMp3FileInternal(true);
    }

    /**
     * @param force 是否强制刷新列表
     */
    private void startToSearchMp3FileInternal(boolean force) {

        // 扫描磁盘文件
        final Observable<List<FileMetaData>> listObservable = getSearchFileObserable()
                .flatMap(mp3FileList -> {
                    this.mp3FileList = mp3FileList;
                    if (CollectionUtils.isNotEmpty(mp3FileList))
                        return mLyricPresenter.getMetaDataAction(mp3FileList);
                    return Observable.error(new IllegalArgumentException("list was null"));
                });

        // 获取FileMetaData(请求逻辑哦)
        Observable<List<FileMetaData>> observable = Observable.fromCallable((Func0<List<FileMetaData>>) ()
                -> mLyricPresenter.getMetaDataWithResolver())
                .flatMap(new Func1<List<FileMetaData>, Observable<List<FileMetaData>>>() {
                    @Override
                    public Observable<List<FileMetaData>> call(List<FileMetaData> fileMetaDatas) {
                        if (CollectionUtils.isEmpty(fileMetaDatas)) {
                            Timber.d("search 文件获取");
                            return listObservable.subscribeOn(Schedulers.computation());
                        }
                        Timber.d("ContentResolver 获取");
                        return Observable.just(fileMetaDatas);
                    }
                });

        Observable.just(null)
                .flatMap((ojb) -> {
                    FileMetaDataRepo metaDataRepo = FileMetaDataRepo.getInstance();
                    if (force || CollectionUtils.isEmpty(metaDataRepo.getFileMetaDataList()))
                        return observable.subscribeOn(Schedulers.io());
                    else {
                        new PlayerLoader().getPlayer(PlayerLoader.PlayerType.EXOPLAYER)
                                .setFileMetaDatas(metaDataRepo.getFileMetaDataList());
                    }
                    return Observable.empty();
                }).subscribe(fileMetaList -> {

            if (CollectionUtils.isNotEmpty(fileMetaList)) {
                new PlayerLoader().getPlayer(PlayerLoader.PlayerType.EXOPLAYER)
                        .setFileMetaDatas(fileMetaList);
                FileMetaDataRepo.getInstance().saveFileMetaDataAndCache(fileMetaList);
            }

        }, Throwable::printStackTrace);

        searchFileObser.connect();
        searchFileObser.refCount();
    }

}
