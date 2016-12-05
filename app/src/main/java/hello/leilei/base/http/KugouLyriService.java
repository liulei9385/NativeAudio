package hello.leilei.base.http;

import hello.leilei.model.KugouLyricPath;
import hello.leilei.model.KugouLyricRecord;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by liulei
 * DATE: 2016/12/5
 * TIME: 15:54
 */

public interface KugouLyriService {

    // notice: 2016/12/5 酷狗搜索歌词API
    //http://lyrics.kugou.com/search?ver=1&man=yes&client=pc&keyword=歌曲名&duration=歌曲总时长(毫秒)&hash=歌曲Hash值
    @GET("search?ver=1&man=no&client=android&hash=")
    Observable<KugouLyricRecord> getLryicRecord(@Query("keyword") String songname, @Query("duration") long duration);

    // notice: 2016/12/5
    //download?ver=1&client=pc&id=10515303&accesskey=3A20F6A1933DE370EBA0187297F5477D&fmt=lrc&charset=utf8 （fmt=lrc 或 krc）
    @GET("download?ver=1&client=android&fmt=lrc&charset=utf8")
    Observable<KugouLyricPath> downloadLryic(@Query("id") String id, @Query("accesskey") String accesskey);

}
