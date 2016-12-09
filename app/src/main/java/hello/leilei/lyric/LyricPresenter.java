package hello.leilei.lyric;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hello.leilei.MainApplication;
import hello.leilei.base.http.HttpManager;
import hello.leilei.base.http.NewKugouLryicService;
import hello.leilei.model.FileMetaData;
import hello.leilei.model.NewKugouLyricRecord;
import hello.leilei.utils.CollectionUtils;
import hello.leilei.utils.FileUtils;
import hello.leilei.utils.Md5Utils;
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

    private NewKugouLryicService mNewKugouLryicService;

    public LyricPresenter() {
        HttpManager httpManager = HttpManager.getInstance();
        this.mNewKugouLryicService = httpManager.getNewKugouLyricApiService();
    }

    public void downloadLyricWithKugou(String songName, @NonNull Action1<String> filePathAction) {

        String finalPath = FileUtils.getExternalCacheDir(MainApplication.getApp()).getPath()
                + File.separator + songName + ".lrc";
        if (new File(finalPath).exists()) {
            filePathAction.call(finalPath);
            Timber.d("downloadLyricWithKugou 从文件缓存获取");
            return;
        }

        this.mNewKugouLryicService.searchMusic(songName)
                .flatMap(new Func1<NewKugouLyricRecord, Observable<ResponseBody>>() {
                    @Override
                    public Observable<ResponseBody> call(NewKugouLyricRecord newKugouLyricRecord) {
                        int status = newKugouLyricRecord.status;
                        if (status == 1) {
                            List<NewKugouLyricRecord.DataBean.InfoBean> infos = newKugouLyricRecord.data.info;
                            if (CollectionUtils.isNotEmpty(infos)) {
                                NewKugouLyricRecord.DataBean.InfoBean infoBean = infos.get(0);
                                return mNewKugouLryicService.searchLyric(songName, infoBean.hash, infoBean.duration * 1000 + "");
                            }
                        }
                        return Observable.empty();
                    }
                }).map(responseBody -> doDownloadAction(responseBody, new File(finalPath)))
                .compose(RxUiUtils.applySchedulers())
                .subscribe(filePathAction, Throwable::printStackTrace);
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

    public List<FileMetaData> getMetaDataWithResolver() {
        ContentResolver contentResolver = MainApplication.getApp().getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            cursor.moveToFirst();
            List<FileMetaData> fileMetaDataList = new ArrayList<>();
            MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
            while (cursor.moveToNext()) {

                FileMetaData metaData = new FileMetaData();
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String tilte = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //歌曲的专辑名：MediaStore.Audio.Media.ALBUM
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                //歌曲的歌手名：MediaStore.Audio.Media.ARTIST
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //歌曲文件的路径：MediaStore.Audio.Media.DATA
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //歌曲的总播放时长：MediaStore.Audio.Media.DURATION
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                //歌曲文件的大小：MediaStore.Audio.Media.SIZE
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));

                metaData.author = tilte;
                metaData.duration = duration + "";
                metaData.album = album;
                metaData.artlist = artist;
                metaData.title = tilte;
                metaData.phoneid = FileMetaData.getUuid();
                metaData.setUri(url);
                fileMetaDataList.add(metaData);

                metaRetriver.setDataSource(url);
                //notice 缓存图片，并生成一个对应的uri供glide加载
                File sdCacheFile = FileUtils.createSdCacheFile(MainApplication.getApp(),
                        getArtThumbName(metaData));
                if (sdCacheFile == null || sdCacheFile.getUsableSpace() < 0L)
                    cacheArtThumbForFile(metaData, metaRetriver.getEmbeddedPicture());


            }

            cursor.close();
            return fileMetaDataList;
        }
        return null;
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
            File sdCacheFile = FileUtils.createSdCacheFile(MainApplication.getApp(),
                    getArtThumbName(metaData));
            if (sdCacheFile == null || sdCacheFile.getUsableSpace() < 0L)
                cacheArtThumbForFile(metaData, metaRetriver.getEmbeddedPicture());

            return metaData;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void cacheArtThumbForFile(FileMetaData metaData, byte[] artByte) {
        // notice: 2016/12/9 检查文件是否存在
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