package hello.leilei.base.http;

import hello.leilei.model.NewKugouLyricRecord;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by liulei
 * DATE: 2016/12/9
 * TIME: 11:10
 */

public interface NewKugouLryicService {

    //http://mobilecdn.kugou.com/api/v3/search/song?format=json&keyword=想象之中&page=1&pagesize=30

    /*根据搜索结果中的hash 搜索歌曲信息：
    http://m.kugou.com/app/i/getSongInfo.php?cmd=playInfo&hash=1d91a1a912458caa2c80ee4456c509de*/

    /*歌词搜索api：
    http://m.kugou.com/app/i/krc.php?cmd=100&keyword=许嵩 - 想象之中&hash=1D91A1A912458CAA2C80EE4456C509DE&timelength=246000&d=0.5261128980200738*/

    @GET("search/song?format=json&page=1&pagesize=10")
    Observable<NewKugouLyricRecord> searchMusic(@Query("keyword") String keyword);

    @GET("http://m.kugou.com/app/i/krc.php?cmd=100")
    Observable<ResponseBody> searchLyric(@Query("keyword") String keyword, @Query("hash") String hash, @Query("timelength") String duration);
}
