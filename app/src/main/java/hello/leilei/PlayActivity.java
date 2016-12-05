package hello.leilei;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hello.leilei.base.BaseUiLoadActivity;
import hello.leilei.base.audioplayer.IPlayerCallback;
import hello.leilei.base.audioplayer.NativePlayer;
import hello.leilei.base.listener.SimpleSeekbarChangeListener;
import hello.leilei.lyric.LyricPresenter;
import hello.leilei.lyric.LyricView;
import hello.leilei.nativeaudio.NativeAudio;
import hello.leilei.utils.RxUiUtils;
import rx.Observable;
import rx.Subscription;
import timber.log.Timber;

/**
 * Created by liulei
 * DATE: 2016/12/1
 * TIME: 14:44
 */
public class PlayActivity extends BaseUiLoadActivity {

    private static final String KEY_SONNAME = "songName";
    private static final String KEY_SHOWNAME = "showName";
    private static final String KEY_DURATION = "duration";

    @BindView(R.id.lyricV)
    LyricView lyricV;
    @BindView(R.id.media_play)
    ImageView playImgView;
    @BindView(R.id.play_next)
    ImageView nextImgView;
    @BindView(R.id.play_previous)
    ImageView previousImgView;

    @BindView(R.id.musicSeekbar)
    SeekBar musicSeekbar;

    @BindView(R.id.playDurationTv)
    TextView playDurationTv;
    @BindView(R.id.maxDurationTv)
    TextView maxDurationTv;

    private String songName;
    private String title;
    private long duration = 0L;
    LyricPresenter mLyricPresenter;
    NativeAudio.OnPlayOverListener onPlayOverListener;

    NativePlayer mNativePlayer;

    public static void start(Context context, String songName, long duration, String title) {
        Intent starter = new Intent(context, PlayActivity.class);
        starter.putExtra(KEY_SONNAME, songName);
        starter.putExtra(KEY_SHOWNAME, title);
        starter.putExtra(KEY_DURATION, duration);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLyricPresenter = new LyricPresenter();
        ButterKnife.bind(this);
        onPlayOverListener = () -> RxUiUtils.unsubscribe(updateLyricSubscri);
        NativeAudio.getInstance().addPlayOverListener(onPlayOverListener);
        mNativePlayer = NativePlayer.getInstance();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_play;
    }

    @Override
    protected void configUi() {

        if (!TextUtils.isEmpty(title))
            setToolbarTitle(title);

        musicSeekbar.setProgress(0);
        musicSeekbar.setMax(NativePlayer.SEEKBAR_MAX);
        musicSeekbar.setEnabled(false);

        mNativePlayer.addPlayerCallback(callback);

        musicSeekbar.setOnSeekBarChangeListener(
                new SimpleSeekbarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (!fromUser) return;
                        RxUiUtils.postDelayedOnBg(20L, () -> {

                            mNativePlayer.enableProgressChange(false);

                            long dutration = NativeAudio.getDutration();
                            if (dutration <= 0) return;

                            long milisecond = (long) (dutration * (userProgress / (float) seekBar.getMax()));
                            boolean success = NativeAudio.setPostion(milisecond);
                            if (success) {
                                Timber.d("NativeAudio.setPostion " + milisecond + "success");
                            }
                            mNativePlayer.enableProgressChange(true);

                        });
                    }
                });
    }

    IPlayerCallback callback = new IPlayerCallback() {
        @Override
        public void onPlayState(int state) {

            if (state == NativePlayer.STOPPED || state == NativePlayer.PAUSED || state == NativePlayer.ERROR)
                playImgView.setImageResource(R.drawable.playbar_btn_play);
            else if (state == NativePlayer.PLAYED)
                playImgView.setImageResource(R.drawable.playbar_btn_pause);

        }

        @Override
        public void onProgressChanged(NativePlayer.ProgressItem progressItem) {
            musicSeekbar.setProgress((int) (progressItem.percent * NativePlayer.SEEKBAR_MAX));
            setDurationForView(progressItem.duration, progressItem.position);
        }

        @Override
        public void onLoadResourceComplete() {
        }
    };

    @Override
    public void prepareData(Intent mIntent) {
        songName = mIntent.getStringExtra(KEY_SONNAME);
        title = mIntent.getStringExtra(KEY_SHOWNAME);
        duration = mIntent.getLongExtra(KEY_DURATION, duration);
    }

    @SuppressWarnings("unused")
    @OnClick({R.id.media_play, R.id.play_next, R.id.play_previous})
    void onClick(View view) {
        final int viewId = view.getId();
        switch (viewId) {

            case R.id.media_play:
                mNativePlayer.playMusic(mNativePlayer.getCuttentPlayIndex());
                break;

            case R.id.play_next:
                mNativePlayer.playerNext();
                break;

            case R.id.play_previous:
                mNativePlayer.playPrevious();
                break;
        }
    }

    Subscription updateLyricSubscri;

    @Override
    protected void obtainData() {
        mLyricPresenter.downloadLyricWithKugou(songName, duration, filePath -> {
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

    private SimpleDateFormat format = new SimpleDateFormat("mm:ss", Locale.getDefault());
    private Date date = new Date();

    private void setDurationForView(long duration, long postion) {
        date.setTime(duration);
        maxDurationTv.setText(format.format(date));
        date.setTime(postion);
        playDurationTv.setText(format.format(date));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxUiUtils.unsubscribe(updateLyricSubscri);
        mNativePlayer.removePlayerCallback(callback);
        if (onPlayOverListener != null)
            NativeAudio.getInstance().removePlayOverListener(onPlayOverListener);
    }

}