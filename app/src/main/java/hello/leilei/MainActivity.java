package hello.leilei;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobUser;
import hello.leilei.base.BaseUiLoadActivity;
import hello.leilei.base.decoration.LinearDividerItemDecoration;
import hello.leilei.base.ui.adapter.AdapterPresenter;
import hello.leilei.base.ui.adapter.MvpRecyclerAdapter;
import hello.leilei.nativeaudio.FileFind;
import hello.leilei.nativeaudio.NativeAudio;
import hello.leilei.utils.CollectionUtils;
import hello.leilei.utils.FileUtils;
import hello.leilei.utils.RxUiUtils;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func0;
import rx.observables.ConnectableObservable;
import timber.log.Timber;

/**
 * Created by liulei on 16-3-18.
 * TIME : 下午9:43
 * COMMECTS :
 */
public class MainActivity extends BaseUiLoadActivity {

    public static final int STOPPED = 0;
    public static final int PLAYED = 1;
    public static final int PAUSED = 2;
    public static final int ERROR = -1;

    public static final int SEEKBAR_MAX = 1000;
    private ActViewHolder mActViewHolder;

    class ActViewHolder {
        @BindView(R.id.media_play)
        ImageView playImgView;
        @BindView(R.id.play_next)
        ImageView nextImgView;
        @BindView(R.id.play_previous)
        ImageView previousImgView;

        @BindView(R.id.musicSeekbar)
        SeekBar musicSeekbar;
        @BindView(R.id.infoText)
        TextView infoText;

        @BindView(R.id.playDurationTv)
        TextView playDurationTv;
        @BindView(R.id.maxDurationTv)
        TextView maxDurationTv;

        @BindView(R.id.recyclerView)
        RecyclerView mRecyclerView;

        @BindView(R.id.drawerLayout)
        DrawerLayout mDrawerLayout;
        @BindView(R.id.naviView)
        NavigationView mNavigationView;
    }

    public static final String RQ_AUDIO = Manifest.permission.RECORD_AUDIO;
    //局部变量数据
    private List<String> mp3FileList;//文件列表
    private int selectIndex = -1;
    private int cuttentPlayIndex = -1;
    //native-audio
    private NativeAudio mNativeAudio;

    private ConnectableObservable<Void> searchFileObser;
    private Subscription seachFileSubscri;
    private Subscription changeProgressSubscri;

    private AdapterPresenter<String> adapterPresenter;

    public static void start(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        //starter.putExtra();
        context.startActivity(starter);
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        ButterKnife.bind(mActViewHolder = new ActViewHolder(), this);

        //创建返回键，并实现打开关/闭监听
        Toolbar mToolbar = ButterKnife.findById(this, R.id.mtoolbar);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this,
                mActViewHolder.mDrawerLayout, mToolbar
                , R.string.openDrawer, R.string.closeDrawer);
        drawerToggle.syncState();
        mActViewHolder.mDrawerLayout.addDrawerListener(drawerToggle);

        mActViewHolder.mNavigationView.setNavigationItemSelectedListener(item -> {
            final int temId = item.getItemId();
            switch (temId) {
                case R.id.logout:
                    BmobUser.logOut();
                    break;
            }
            return false;
        });

        mNativeAudio = NativeAudio.getInstance();
    }

    @Override
    public void configUi() {

        //for seekbar
        mActViewHolder.musicSeekbar.setProgress(0);
        mActViewHolder.musicSeekbar.setMax(SEEKBAR_MAX);
        mActViewHolder.musicSeekbar.setEnabled(false);


        mActViewHolder.musicSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            boolean fromUser;
            int sysProgress;
            int userProgress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.fromUser = fromUser;
                if (this.fromUser)
                    this.userProgress = progress;
                else this.sysProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!fromUser) return;
                RxUiUtils.postDelayedOnBg(20L, () -> {

                    RxUiUtils.unsubscribe(changeProgressSubscri);

                    long dutration = NativeAudio.getDutration();
                    if (dutration <= 0) return;

                    long milisecond = (long) (dutration * (userProgress / (float) seekBar.getMax()));
                    boolean success = NativeAudio.setPostion(milisecond);
                    if (success) {
                        Timber.d("NativeAudio.setPostion " + milisecond + "success");
                    }
                    changePlayProgress();

                });
            }
        });

        mNativeAudio.setPlayOverListener(() -> {
            RxUiUtils.unsubscribe(changeProgressSubscri);
            RxUiUtils.postDelayedRxOnMain(10L, () -> showSToast("播放结束"));
        });

        adapterPresenter = new AdapterPresenter<>();
        MvpRecyclerAdapter<String> adapter = new MvpRecyclerAdapter.Builder<String>()
                .setLayoutId(R.layout.adapter_list_item)
                .setPresenter(adapterPresenter)
                .build((holder, s) -> holder.setText(android.R.id.text1, s));
        mActViewHolder.mRecyclerView.setAdapter(adapter);
        mActViewHolder.mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mActViewHolder.mRecyclerView.addItemDecoration(new LinearDividerItemDecoration.Builder(this).build());

        adapter.setOnItemClickListener((viewGroup, view, s, integer) -> {
            selectIndex = integer;
            doPlayAction();
        });

        getSearchFileObserable();
        searchFileObser.subscribe(aVoid -> adapterPresenter.addAllItem(mp3FileList),
                Throwable::printStackTrace);

        searchFileObser.connect();
        searchFileObser.refCount();

    }

    @Override
    protected void obtainData() {

        int gratId = ActivityCompat.checkSelfPermission(this, RQ_AUDIO);
        if (gratId != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{RQ_AUDIO}, 2);
        } else
            initAudio();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            gratId = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (gratId != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
            }
        }
    }

    @OnClick({R.id.media_play, R.id.play_next, R.id.play_previous})
    void onClick(View view) {
        final int viewId = view.getId();
        switch (viewId) {

            case R.id.media_play:

                doPlayAction();

                break;

            case R.id.play_next:

                if (selectIndex + 1 == adapterPresenter.getCount()) {
                    selectIndex = 0;
                    doPlayAction();
                } else {
                    selectIndex += 1;
                    doPlayAction();
                }

                break;

            case R.id.play_previous:
                if (selectIndex == 0)
                    doPlayAction();
                else {
                    selectIndex -= 1;
                    doPlayAction();
                }
                break;
        }
    }

    void setPlayImageState(int state) {
        if (state == STOPPED || state == PAUSED || state == ERROR)
            mActViewHolder.playImgView.setImageResource(R.drawable.ic_play);
        else if (state == PLAYED)
            mActViewHolder.playImgView.setImageResource(R.drawable.ic_pause);
    }

    private void doPlayAction() {
        int state = NativeAudio.getPlayingUriState();
        //* 0 stoped 1 play 2 pause -1 error
        if (state == ERROR || state == STOPPED || cuttentPlayIndex != selectIndex) {

            if (selectIndex < 0 || selectIndex > adapterPresenter.getCount()) {
                setPlayImageState(state);
                return;
            }

            if (state == PLAYED) {
                NativeAudio.setPlayingUriAudioPlayer(false);
            }
            selectAFileToPlay();
        } else {
            if (state == PLAYED) {
                NativeAudio.setPlayingUriAudioPlayer(false);
                setPlayImageState(PAUSED);
            } else if (state == PAUSED) {
                NativeAudio.setPlayingUriAudioPlayer(true);
                setPlayImageState(PLAYED);
            }
        }
    }

    private void playMp3Music(String uri) {
        // 在后台线程中创建相关资源
        Observable.fromCallable((Func0<Boolean>) () -> {
            NativeAudio.setPlayingUriAudioPlayer(false);
            return NativeAudio.createUriAudioPlayer(uri);
        }).compose(RxUiUtils.applySchedulers())
                .subscribe(isSuccess -> {

                    NativeAudio.setPlayingUriAudioPlayer(true);

                    mActViewHolder.musicSeekbar.setEnabled(true);
                    setPlayImageState(PLAYED);

                    String newUri = uri.substring(uri.lastIndexOf("/") + 1);
                    mActViewHolder.infoText.setText(newUri);

                    changePlayProgress();
                }, Throwable::printStackTrace);
    }

    private void changePlayProgress() {
        RxUiUtils.unsubscribe(changeProgressSubscri);
        changeProgressSubscri = Observable.interval(0L, 1000L, TimeUnit.MICROSECONDS)
                .flatMap(aLong -> Observable.fromCallable((Func0<Object[]>) () -> {
                    long duration = NativeAudio.getDutration();
                    long position = NativeAudio.getPostion();
                    int progress = (int) ((double) position / duration * SEEKBAR_MAX);
                    return new Object[]{duration, position, progress};
                }))//
                .compose(RxUiUtils.applySchedulers())
                .subscribe(objs -> {

                    int progress = (int) objs[2];

                    if (progress >= SEEKBAR_MAX)
                        progress = SEEKBAR_MAX;
                    else if (progress < 0)
                        progress = 0;
                    mActViewHolder.musicSeekbar.setProgress(progress);

                    setDurationForView((long) objs[0], (long) objs[1]);

                }, Throwable::printStackTrace);
    }

    private SimpleDateFormat format = new SimpleDateFormat("mm:ss", Locale.getDefault());
    private Date date = new Date();

    private void setDurationForView(long duration, long postion) {
        date.setTime(duration);
        mActViewHolder.maxDurationTv.setText(format.format(date));
        date.setTime(postion);
        mActViewHolder.playDurationTv.setText(format.format(date));
    }

    private void selectAFileToPlay() {
        if (!CollectionUtils.isEmpty(mp3FileList)) {
            if (selectIndex >= 0 && selectIndex < mp3FileList.size()) {
                String mp3Path = mp3FileList.get(selectIndex);
                playMp3Music(mp3Path);
                cuttentPlayIndex = selectIndex;
                return;
            }
        }
        showSToast("请选择要播放的音乐文件");
    }

    private void initAudio() {
        // initialize native audio system
        NativeAudio.createEngine();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 1 && requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initAudio();
            }
        } else if (grantResults.length == 1 && requestCode == 3) {
            //noinspection StatementWithEmptyBody
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //req code
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.media_select, menu);
        return true;
    }

    private void startToScanMusic() {
        //不用每次搜索,浪费资源
        RxUiUtils.unsubscribe(seachFileSubscri);
        seachFileSubscri = searchFileObser.subscribe(a -> RxUiUtils.postDelayedRxOnMain(10L, () -> {
            int size = mp3FileList != null ? mp3FileList.size() : 0;
            showSToast("扫描到" + size + "个文件");
        }), Throwable::printStackTrace);
    }

    private void getSearchFileObserable() {
        searchFileObser = Observable.fromCallable((Func0<Void>) () -> {
            File sdDir = FileUtils.getExternalSdDir(MainActivity.this);
            if (sdDir != null)
                mp3FileList = FileFind.getMp3FileFromPath(sdDir.getPath());
            return null;
        })//
                .compose(RxUiUtils.applySchedulers())
                .publish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
            case R.id.media_scan:
                startToScanMusic();
                searchFileObser.connect();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main_drawer;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NativeAudio.shutdown();
    }
}
