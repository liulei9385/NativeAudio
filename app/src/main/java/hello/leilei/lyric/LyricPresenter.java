package hello.leilei.lyric;

import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Base64;

import com.google.android.exoplayer2.SimpleExoPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListListener;
import es.dmoral.prefs.Prefs;
import hello.leilei.MainActivity;
import hello.leilei.MainApplication;
import hello.leilei.base.audioplayer.AudioPlayer;
import hello.leilei.base.audioplayer.FileMetaDataSave;
import hello.leilei.base.http.HttpManager;
import hello.leilei.base.http.KugouLyriService;
import hello.leilei.base.http.LyricApiService;
import hello.leilei.model.FileMetaData;
import hello.leilei.model.KugouLyricPath;
import hello.leilei.model.KugouLyricRecord;
import hello.leilei.model.LyricRecordBean;
import hello.leilei.utils.CollectionUtils;
import hello.leilei.utils.FileUtils;
import hello.leilei.utils.Md5Utils;
import hello.leilei.utils.OkioUtils;
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
    private KugouLyriService mKugouLyriService;

    public LyricPresenter() {
        this.lyricApiService = HttpManager.getInstance().getLyricApiService();
        this.mKugouLyriService = HttpManager.getInstance().getKugouLyricApiService();
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

    public void downloadLyricWithKugou(final String songName, long duration, @NonNull Action1<String> filePathAction) {
        // 先查看文件是否有内容呀
        File lyricFile = FileUtils.createSdCacheFile(MainApplication.getApp(), songName + ".lrc");
        mKugouLyriService.getLryicRecord(songName, duration)
                .flatMap(new Func1<KugouLyricRecord, Observable<KugouLyricPath>>() {
                    @Override
                    public Observable<KugouLyricPath> call(KugouLyricRecord kugouLyricRecord) {
                        List<KugouLyricRecord.CandidatesBean> candidates = kugouLyricRecord.candidates;
                        if (CollectionUtils.isNotEmpty(candidates)) {
                            KugouLyricRecord.CandidatesBean candidatesBean = candidates.get(0);
                            return mKugouLyriService.downloadLryic(candidatesBean.id, candidatesBean.accesskey);
                        }
                        return null;
                    }
                })
                .flatMap(kugouLyricPath -> {
                    if (kugouLyricPath != null) {
                        String conent = kugouLyricPath.content;
                        byte[] lyricBytes = Base64.decode(conent.getBytes(Charset.defaultCharset()), Base64.NO_WRAP);
                        OkioUtils.writeByteToFile(lyricFile, lyricBytes);
                        return Observable.just(lyricFile.getPath());
                    }
                    return null;
                })
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
                .map(fileMetaDatas -> {
                    // notice: 2016/12/5 插入数据到bmob云中..
                    /*long metaDataTime = Prefs.with(MainApplication.getApp()).readLong("pushFileMetaDataTime", 0L);
                    if (System.currentTimeMillis() - metaDataTime >= 30 * 60 * 1000) {*/
                    Timber.d("inset bach fileMata to Bmob ....");

                    boolean update = false;
                    if (CollectionUtils.isNotEmpty(fileMetaDatas)) {
                        for (FileMetaData metaData : fileMetaDatas) {
                            String obejctId = FileMetaDataSave.getObjectId(metaData.getUri());
                            if (!TextUtils.isEmpty(obejctId)) {
                                metaData.setObjectId(obejctId);
                                update = true;
                            }
                        }
                    }

                    BmobBatch bmobBatch = new BmobBatch();
                    if (!update)
                        bmobBatch.insertBatch(new ArrayList<>(fileMetaDatas));
                    else
                        bmobBatch.updateBatch(new ArrayList<>(fileMetaDatas));
                    final boolean _update = update;
                    bmobBatch.doBatch(new QueryListListener<BatchResult>() {
                        @Override
                        public void done(List<BatchResult> list, BmobException e) {
                            if (CollectionUtils.isNotEmpty(list)) {
                                Timber.d("FileMetaData\t" + list.size() + "个数据批量" + (_update ? "更新" : "新增") + "成功");
                                Prefs.with(MainApplication.getApp())
                                        .writeLong("pushFileMetaDataTime", System.currentTimeMillis());
                            }
                            if (e != null)
                                Timber.e(e.getMessage(), e);
                        }
                    });
                    //}
                    return fileMetaDatas;
                })
                .compose(RxUiUtils.applySchedulers());
    }

    private List<FileMetaData> getMeteData(List<String> fileUris) {

        if (CollectionUtils.isEmpty(fileUris)) return null;

        Timber.d("begin getMeteDatas ");

        try {

            MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();

            List<FileMetaData> fileMetaDatas = new ArrayList<>();
            //noinspection Convert2streamapi
            for (String uri : fileUris) {
                if (TextUtils.isEmpty(uri) || !uri.endsWith(".mp3")) continue;
                FileMetaData metaData = getMetaDataForFile(metaRetriver, uri);
                if (metaData != null)
                    fileMetaDatas.add(metaData);
            }

            metaRetriver.release();

            return fileMetaDatas;

        } finally {
            Timber.d("finish getMeteDatas ");
        }
    }

    private FileMetaData getMetaDataForFile(MediaMetadataRetriever metaRetriver, String fileUri) {

        try {

            metaRetriver.setDataSource(fileUri);

            FileMetaData metaData = new FileMetaData();

            metaData.setUri(fileUri);

            metaData.author = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
            metaData.duration = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            metaData.album = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            metaData.artlist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            metaData.title = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            metaData.phoneid = FileMetaData.getUuid();

            //notice 缓存图片，并生成一个对应的uri供glide加载
            cacheArtThumbForFile(metaData, metaRetriver.getEmbeddedPicture());

            return metaData;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void cacheArtThumbForFile(FileMetaData metaData, byte[] artByte) {
        if (artByte == null) return;
        File sdCacheFile = FileUtils.createSdCacheFile(MainApplication.getApp(),
                getArtThumbName(metaData));
        if (sdCacheFile == null) return;
        BufferedSink bufferedSink = null;
        try {
            bufferedSink = Okio.buffer(Okio.sink(sdCacheFile));
            bufferedSink.write(artByte);
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
    }

    private String getArtThumbName(FileMetaData metaData) {
        String url = metaData.title + File.separator + metaData.album + "_thumb.jpg";
        return "thumbCache" + File.separator + Md5Utils.md5(url);
    }

    public String getArtThumbUrl(FileMetaData metaData) {
        return FileUtils.getExternalCacheDir(MainApplication.getApp())
                + File.separator + getArtThumbName(metaData);
    }

}