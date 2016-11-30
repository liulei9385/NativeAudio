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
import android.support.v4.util.TimeUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.*;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by liulei on 16-3-18.
 * TIME : 下午9:43
 * COMMECTS :
 */
public class MainActivity extends BaseUiLoadActivity {

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

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.drawerLayout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.naviView)
    NavigationView mNavigationView;

    private ActionBarDrawerToggle drawerToggle;

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

        ButterKnife.bind(this);

        //创建返回键，并实现打开关/闭监听
        Toolbar mToolbar = ButterKnife.findById(this, R.id.mtoolbar);
        drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar
                , R.string.openDrawer, R.string.closeDrawer);
        drawerToggle.syncState();
        mDrawerLayout.addDrawerListener(drawerToggle);

        mNavigationView.setNavigationItemSelectedListener(item -> {
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
        musicSeekbar.setProgress(0);
        musicSeekbar.setEnabled(false);

        mNativeAudio.setPlayOverListener(() -> {

            RxUiUtils.unsubscribe(changeProgressSubscri);
            RxUiUtils.postDelayedRxOnMain(10L, () -> showSToast("播放结束"));

        });

        adapterPresenter = new AdapterPresenter<>();
        MvpRecyclerAdapter<String> adapter = new MvpRecyclerAdapter.Builder<String>()
                .setLayoutId(R.layout.adapter_list_item)
                .setPresenter(adapterPresenter)
                .build((holder, s) -> holder.setText(android.R.id.text1, s));
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new LinearDividerItemDecoration.Builder(this).build());

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
                break;

            case R.id.play_previous:
                break;
        }
    }

    private void doPlayAction() {
        int state = NativeAudio.getPlayingUriAudioPlayer();
        //* 0 stoped 1 play 2 pause -1 error
        if (state == -1 || state == 0 || cuttentPlayIndex != selectIndex) {
            if (state == 1) {
                NativeAudio.setPlayingUriAudioPlayer(false);
                playImgView.setImageResource(R.drawable.ic_play);
            }
            selectAFileToPlay();
        } else {
            if (state == 1) {
                NativeAudio.setPlayingUriAudioPlayer(false);
                playImgView.setImageResource(R.drawable.ic_play);
            } else if (state == 2) {
                NativeAudio.setPlayingUriAudioPlayer(true);
                playImgView.setImageResource(R.drawable.ic_pause);
            }
        }
    }

    private void playMp3Music(String uri) {

        NativeAudio.setPlayingUriAudioPlayer(false);
        boolean isSuccess = NativeAudio.createUriAudioPlayer(uri);
        if (isSuccess) {
            NativeAudio.setPlayingUriAudioPlayer(true);
            musicSeekbar.setEnabled(true);

            String newUri = uri.substring(uri.lastIndexOf("/") + 1);
            infoText.setText(newUri);

            RxUiUtils.unsubscribe(changeProgressSubscri);
            changeProgressSubscri = Observable.interval(0L, 1000L, TimeUnit.MICROSECONDS)
                    .flatMap(aLong -> Observable.fromCallable((Func0<Integer>) () -> {
                        long duration = NativeAudio.getDutration();
                        long position = NativeAudio.getPostion();

                        return (int) ((double) position / duration * 10000);
                    }))//
                    .subscribe(progress -> {
                        if (progress >= 10000)
                            progress = 10000;
                        else if (progress < 0)
                            progress = 0;
                        musicSeekbar.setProgress(progress);
                    }, Throwable::printStackTrace);
        }
    }

    private void getDuratioForFile() {
        StringBuilder sb = new StringBuilder("Time:\t");
        TimeUtils.formatDuration(NativeAudio.getDutration(), sb);
        Log.e("NativeAudio", "duration##" + sb.toString());
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

    /**
     * 显示选择音乐的播放列表
     */
    private void showSeListDialog(List<String> musicList) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                musicList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View mView = super.getView(position, convertView, parent);
                TextView mText = (TextView) mView.findViewById(android.R.id.text1);
                if (mText != null) {
                    String item = getItem(position);
                    if (!TextUtils.isEmpty(item)) {
                        item = item.substring(item.lastIndexOf("/") + 1);
                        mText.setText(item);
                    }
                }
                return mView;
            }
        };
        mBuilder.setTitle("选择歌曲")//
                .setAdapter(arrayAdapter, (dialog, which) -> {
                    //执行播放音乐
                    selectIndex = which;
                    String mp3Pth = arrayAdapter.getItem(which);
                    if (!TextUtils.isEmpty(mp3Pth))
                        playMp3Music(mp3Pth);
                    else showSToast("音乐文件路径为空");
                })//
                .setNegativeButton("取消", null)//
                .create()
                .show();
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
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //req code
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //NativeAudio.setPlayingUriAudioPlayer(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NativeAudio.setPlayingUriAudioPlayer(true);
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
                break;
            case R.id.media_sel:
                //显示选择歌曲弹窗　
                if (!CollectionUtils.isEmpty(mp3FileList)) {
                    showSeListDialog(mp3FileList);
                } else
                    showSToast("未搜索到音乐文件,请添加音乐文件");
                break;
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
