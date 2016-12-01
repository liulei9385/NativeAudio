package hello.leilei;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import hello.leilei.base.BaseUiLoadActivity;
import hello.leilei.lyric.LyricPresenter;
import hello.leilei.lyric.LyricView;
import hello.leilei.nativeaudio.NativeAudio;
import hello.leilei.utils.RxUiUtils;
import rx.Observable;
import rx.Subscription;

/**
 * Created by liulei
 * DATE: 2016/12/1
 * TIME: 14:44
 */
public class PlayActivity extends BaseUiLoadActivity {

    @BindView(R.id.lyricV)
    LyricView lyricV;

    private static final String KEY_SONNAME = "songName";
    private static final String KEY_SHOWNAME = "showName";

    private String songName;
    private String title;
    LyricPresenter mLyricPresenter;
    NativeAudio.OnPlayOverListener onPlayOverListener;

    public static void start(Context context, String songName, String title) {
        Intent starter = new Intent(context, PlayActivity.class);
        starter.putExtra(KEY_SONNAME, songName);
        starter.putExtra(KEY_SHOWNAME, title);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLyricPresenter = new LyricPresenter();
        ButterKnife.bind(this);
        onPlayOverListener = () -> RxUiUtils.unsubscribe(updateLyricSubscri);
        NativeAudio.getInstance().addPlayOverListener(onPlayOverListener);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_play;
    }

    @Override
    protected void configUi() {
        if (!TextUtils.isEmpty(title))
            setToolbarTitle(title);
    }

    @Override
    public void prepareData(Intent mIntent) {
        songName = mIntent.getStringExtra(KEY_SONNAME);
        title = mIntent.getStringExtra(KEY_SHOWNAME);
    }

    Subscription updateLyricSubscri;

    @Override
    protected void obtainData() {

        mLyricPresenter.downloadLyric(songName, filePath -> {
            lyricV.initLyricFile(new File(filePath));
            updateLyricSubscri = Observable.interval(20L, 120L, TimeUnit.MILLISECONDS)
                    .compose(RxUiUtils.applySchedulers())
                    .subscribe(aLong -> {

                        long duration = NativeAudio.getDutration();
                        long position = NativeAudio.getPostion();
                        lyricV.updateLyrics(position, duration);

                    }, Throwable::printStackTrace);

        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxUiUtils.unsubscribe(updateLyricSubscri);
        if (onPlayOverListener != null)
            NativeAudio.getInstance().removePlayOverListener(onPlayOverListener);
    }
}