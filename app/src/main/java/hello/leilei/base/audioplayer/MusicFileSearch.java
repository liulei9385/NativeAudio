package hello.leilei.base.audioplayer;

import java.io.File;
import java.util.List;

import hello.leilei.lyric.LyricPresenter;
import hello.leilei.nativeaudio.FileFind;
import hello.leilei.utils.CollectionUtils;
import hello.leilei.utils.FileUtils;
import hello.leilei.utils.RxUiUtils;
import rx.Observable;
import rx.functions.Func0;
import rx.observables.ConnectableObservable;

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

        getSearchFileObserable()
                .flatMap(mp3FileList -> {
                    this.mp3FileList = mp3FileList;
                    if (CollectionUtils.isNotEmpty(mp3FileList))
                        return mLyricPresenter.getMetaDataAction(mp3FileList);
                    return Observable.error(new IllegalArgumentException("list was null"));
                })
                .subscribe(datas -> {

                    new PlayerLoader().getPlayer(PlayerLoader.PlayerType.EXOPLAYER)
                            .setFileMetaDatas(datas);

                }, Throwable::printStackTrace);

        searchFileObser.connect();
        searchFileObser.refCount();
    }

}
