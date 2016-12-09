package hello.leilei.base.audioplayer;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.List;

import hello.leilei.MainApplication;
import hello.leilei.model.FileMetaData;
import hello.leilei.utils.CollectionUtils;
import hello.leilei.utils.FileUtils;
import hello.leilei.utils.GsonUtils;
import hello.leilei.utils.RxUiUtils;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import timber.log.Timber;

/**
 * Created by liulei
 * DATE: 2016/12/8
 * TIME: 22:41
 */

public class FileMetaDataSave {

    private static FileMetaDataSave save;

    public static FileMetaDataSave getInstance() {
        if (save == null) {
            synchronized (FileMetaDataSave.class) {
                if (save == null)
                    save = new FileMetaDataSave();
            }
        }
        return save;
    }

    /**
     * 程序第一次初始化
     */
    public void init() {
        if (CollectionUtils.isEmpty(fileMetaDataList)) {
            this.fileMetaDataList = getDataFromCache();
        }
    }

    public List<FileMetaData> fileMetaDataList; // url//objectId

    public List<FileMetaData> getDataFromCache() {
        Context ctx = MainApplication.getApp();
        File mFile = FileUtils.createSdCacheFile(ctx, FileMetaData.getUuid() + ".obj");
        BufferedSource bufferedSource = null;
        try {
            if (mFile == null) return null;
            bufferedSource = Okio.buffer(Okio.source(mFile));
            String jsonString = bufferedSource.readUtf8();
            return GsonUtils.parseJsonArray(FileMetaData.class, jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedSource != null)
                    bufferedSource.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    // notice: 2016/12/8 后期可以使用你数据库等等做静态存储
    public void saveFileMetaDataAndCache(List<FileMetaData> fileMetaDataList) {
        this.fileMetaDataList = fileMetaDataList;
        RxUiUtils.postDelayedOnBg(20L, () -> {
            saveObjTofile(fileMetaDataList);
            Timber.e("saveFileMetaDataAndCache success.");
        });
    }

    public void saveObjTofile(List<FileMetaData> list) {
        Context ctx = MainApplication.getApp();
        File mFile = FileUtils.createSdCacheFile(ctx, FileMetaData.getUuid() + ".obj");
        BufferedSink bufferedSink = null;
        try {
            if (mFile == null) return;
            bufferedSink = Okio.buffer(Okio.sink(mFile));
            String jsonString = GsonUtils.parseObjectToJsonString(list);
            bufferedSink.writeUtf8(jsonString);
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

}