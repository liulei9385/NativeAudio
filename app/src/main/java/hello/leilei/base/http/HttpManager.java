package hello.leilei.base.http;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * Created by liulei
 * DATE: 2016/12/1
 * TIME: 14:48
 */
public class HttpManager {

    private static HttpManager mHttpManager;
    private OkHttpClient mHttpClient;
    private Retrofit lyricRetrofit;
    private Retrofit kugouRetrofit;
    private LyricApiService mLyricApiService;
    private KugouLyriService mKugouLyriService;

    public static HttpManager getInstance() {
        if (mHttpManager == null) {
            synchronized (HttpManager.class) {
                if (mHttpManager == null)
                    mHttpManager = new HttpManager();
            }
        }
        return mHttpManager;
    }

    public LyricApiService getLyricApiService() {
        if (mLyricApiService == null)
            mLyricApiService = provideGsonCachedRestAdapter().create(LyricApiService.class);
        return mLyricApiService;
    }

    public KugouLyriService getKugouLyricApiService() {
        if (mKugouLyriService == null)
            mKugouLyriService = provideKugouGsonCachedRestAdapter().create(KugouLyriService.class);
        return mKugouLyriService;
    }

    public Retrofit provideGsonCachedRestAdapter() {
        if (lyricRetrofit == null)
            lyricRetrofit = new Retrofit.Builder()
                    .baseUrl(ApiConfig.GECIME_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(getCachedOkHttpClient())
                    .build();
        return lyricRetrofit;
    }

    public Retrofit provideKugouGsonCachedRestAdapter() {
        if (kugouRetrofit == null)
            kugouRetrofit = new Retrofit.Builder()
                    .baseUrl(ApiConfig.KUGOU_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(getCachedOkHttpClient())
                    .build();
        return kugouRetrofit;
    }

    public OkHttpClient getCachedOkHttpClient() {
        if (mHttpClient == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> Timber.d(message))
                    .setLevel(HttpLoggingInterceptor.Level.BODY);
            mHttpClient = new OkHttpClient.Builder()
                    .readTimeout(10L, TimeUnit.SECONDS)
                    .connectTimeout(10L, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .build();
        }
        return mHttpClient;
    }
}
