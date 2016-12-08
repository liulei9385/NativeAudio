package hello.leilei.base.audioplayer;

import android.support.v4.util.ArrayMap;

import java.util.List;

import hello.leilei.model.FileMetaData;
import hello.leilei.utils.CollectionUtils;

/**
 * Created by liulei
 * DATE: 2016/12/8
 * TIME: 22:41
 */

public class FileMetaDataSave {

    public static ArrayMap<String, String> cachedFileMetaDataMaps; // url//objectId
    public static List<FileMetaData> fileMetaDataList; // url//objectId

    // notice: 2016/12/8 后期可以使用你数据库 做静态存储
    public static void saveFileMetaDatas(List<FileMetaData> fileMetaDataList) {
        if (cachedFileMetaDataMaps == null)
            cachedFileMetaDataMaps = new ArrayMap<>();
        else cachedFileMetaDataMaps.clear();
        FileMetaDataSave.fileMetaDataList = fileMetaDataList;
        if (CollectionUtils.isNotEmpty(fileMetaDataList)) {
            for (FileMetaData data : fileMetaDataList) {
                cachedFileMetaDataMaps.put(data.getUri(), data.getObjectId());
            }
        }
    }

    public static String getObjectId(String uri) {
        ArrayMap<String, String> dataMaps = cachedFileMetaDataMaps;
        if (!CollectionUtils.isEmpty(dataMaps)) {
            return dataMaps.get(uri);
        }
        return null;
    }
}
