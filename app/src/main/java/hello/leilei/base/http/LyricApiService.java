package hello.leilei.base.http;

import hello.leilei.model.LyricRecordBean;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by liulei
 * DATE: 2016/12/1
 * TIME: 15:11
 */

public interface LyricApiService {

    @GET("lyric/{songname}")
    Observable<LyricRecordBean> getLyricRecord(@Path("songname") String song);

    @GET
    Observable<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);
}
