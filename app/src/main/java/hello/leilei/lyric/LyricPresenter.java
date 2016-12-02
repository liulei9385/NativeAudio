package hello.leilei.lyric;

import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hello.leilei.MainApplication;
import hello.leilei.base.http.HttpManager;
import hello.leilei.base.http.LyricApiService;
import hello.leilei.model.FileMetaData;
import hello.leilei.model.LyricRecordBean;
import hello.leilei.utils.CollectionUtils;
import hello.leilei.utils.FileUtils;
import hello.leilei.utils.RxUiUtils;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import timber.log.Timber;

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

        // 先查看文件是否有内容呀
        File lyricFile = FileUtils.createSdCacheFile(MainApplication.getApp(), songName + ".lrc");
        try {

            FileInputStream fileInputStream = new FileInputStream(lyricFile);
            int available = fileInputStream.available();
            fileInputStream.close();
            if (available > 0L) {
                filePathAction.call(lyricFile.getPath());
                return;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        lyricApiService.getLyricRecord(songName)
                .flatMap(downloadLyricFile(songName, lyricFile))
                .compose(RxUiUtils.applySchedulers())
                .subscribe(filePathAction, Throwable::printStackTrace);

    }

    private Func1<LyricRecordBean, Observable<String>> downloadLyricFile(String songName, File lyricFile) {
        return lyricRecordBean -> {
            String path = lyricRecordBean.getFirstDownloadPath();
            if (!TextUtils.isEmpty(path)) {

                return lyricApiService.downloadFileWithDynamicUrlSync(path)
                        .map(responseBody -> doDownloadAction(responseBody, lyricFile));
            }
            return Observable.error(new IllegalArgumentException("path was null," + songName + "was valid"));
        };
    }

    @NonNull
    private String doDownloadAction(ResponseBody responseBody, File lyricFile) {

        String filePath = lyricFile.getPath();

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
        return filePath;
    }

    public Observable<List<FileMetaData>> getMetaDataAction(List<String> fileUris) {
        return Observable.fromCallable((Func0<List<FileMetaData>>) () -> getMeteData(fileUris))
                .compose(RxUiUtils.applySchedulers());
    }

    private MediaMetadataRetriever metaRetriver;

    private List<FileMetaData> getMeteData(List<String> fileUris) {

        if (CollectionUtils.isEmpty(fileUris)) return null;

        Timber.d("begin getMeteDatas ");

        try {
            if (metaRetriver == null)
                metaRetriver = new MediaMetadataRetriever();

            List<FileMetaData> fileMetaDatas = new ArrayList<>();
            //noinspection Convert2streamapi
            for (String uri : fileUris) {
                FileMetaData metaData = getMetaDataForFile(metaRetriver, uri);
                if (metaData != null)
                    fileMetaDatas.add(metaData);
            }

            if (metaRetriver != null) {
                metaRetriver.release();
                metaRetriver = null;
            }

            return fileMetaDatas;

        } finally {
            Timber.d("finish getMeteDatas ");
        }
    }

    private FileMetaData getMetaDataForFile(MediaMetadataRetriever metaRetriver, String fileUri) {

        try {

            metaRetriver.setDataSource(fileUri);

            FileMetaData metaData = new FileMetaData();

            metaData.uri = fileUri;

            metaData.art = metaRetriver.getEmbeddedPicture();
            metaData.author = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
            metaData.duration = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            metaData.album = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            metaData.artlist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            metaData.title = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

            return metaData;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

}