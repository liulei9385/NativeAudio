package hello.leilei.lyric;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

import hello.leilei.MainApplication;
import hello.leilei.base.http.HttpManager;
import hello.leilei.base.http.LyricApiService;
import hello.leilei.model.LyricRecordBean;
import hello.leilei.utils.FileUtils;
import hello.leilei.utils.RxUiUtils;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by liulei
 * DATE: 2016/12/1
 * TIME: 14:47
 */
public class LyricPresenter {

    private LyricApiService lyricApiService;

    public LyricPresenter() {
        this.lyricApiService = HttpManager.getInstance().getLyricApiService();
    }

    /**
     * 下载歌词文件
     */
    public void downloadLyric(final String songName, @NonNull Action1<String> filePathAction) {

        lyricApiService.getLyricRecord(songName)
                .flatMap(new Func1<LyricRecordBean, Observable<String>>() {
                    @Override
                    public Observable<String> call(LyricRecordBean lyricRecordBean) {
                        String path = lyricRecordBean.getFirstDownloadPath();
                        if (!TextUtils.isEmpty(path)) {
                            return lyricApiService.downloadFileWithDynamicUrlSync(path)
                                    .map(responseBody -> {
                                        File lyricFile = FileUtils.createSdCacheFile(MainApplication.getApp(), songName + ".lrc");
                                        BufferedSink bufferedSink = null;
                                        try {
                                            Sink sink = Okio.sink(lyricFile);
                                            bufferedSink = Okio.buffer(sink);
                                            BufferedSource source = responseBody.source();
                                            bufferedSink.writeAll(source);
                                            bufferedSink.flush();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } finally {
                                            try {
                                                if (bufferedSink != null)
                                                    bufferedSink.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        return lyricFile.getPath();
                                    });
                        }
                        return Observable.error(new IllegalArgumentException("path was null," + songName + "was valid"));
                    }
                })
                .compose(RxUiUtils.applySchedulers())
                .subscribe(filePathAction, Throwable::printStackTrace);

    }

}
