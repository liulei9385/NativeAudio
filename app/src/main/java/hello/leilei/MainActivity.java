package hello.leilei;

import android.Manifest;
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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobUser;
import hello.leilei.base.BaseUiLoadActivity;
import hello.leilei.base.audioplayer.BasePlayer;
import hello.leilei.base.audioplayer.FileMetaDataRepo;
import hello.leilei.base.audioplayer.IPlayerCallback;
import hello.leilei.base.audioplayer.MusicFileSearch;
import hello.leilei.base.audioplayer.NativePlayer;
import hello.leilei.base.audioplayer.PlayerLoader;
import hello.leilei.base.decoration.LinearDividerItemDecoration;
import hello.leilei.base.ui.adapter.AdapterPresenter;
import hello.leilei.base.ui.adapter.MvpRecyclerAdapter;
import hello.leilei.lyric.LyricPresenter;
import hello.leilei.model.FileMetaData;
import hello.leilei.utils.CollectionUtils;
import hello.leilei.utils.DensityUtils;
import hello.leilei.utils.FileUtils;
import hello.leilei.utils.NumberUtils;
import hello.leilei.utils.RxUiUtils;
import rx.Subscription;
import rx.observables.ConnectableObservable;
import timber.log.Timber;

/**
 * Created by liulei on 16-3-18.
 * TIME : 下午9:43
 * COMMECTS :
 */
public class MainActivity extends BaseUiLoadActivity {

    public static final String PERMISSION_RQ_AUDIO = Manifest.permission.RECORD_AUDIO;
    public static final int REQCODE_AUDIO = 0x01;
    public static final int REQCODE_READ_EXTERNAL = 0x02;
    public static final int REQCODE_AUDIO_READ_EXTERNAL = 0x03;
    AdapterPresenter<FileMetaData> adapterPresenter;
    LyricPresenter mLyricPresenter;
    BasePlayer basePlayer;
    private ActViewHolder mActViewHolder;
    private IPlayerCallback playerCallback = new IPlayerCallback() {

        @Override
        public void onPlayState(int state) {
            setPlayImageState(state);
            int selectIndex = basePlayer.getCurrentPlayIndex();
            setPlayUiWithData(adapterPresenter.getItem(selectIndex));
        }

        @Override
        public void onProgressChanged(NativePlayer.ProgressItem mProgressItem) {
            int max = mActViewHolder.mProgressBar.getMax();
            mActViewHolder.mProgressBar.setProgress((int) (mProgressItem.percent * max));
        }

        @Override
        public void onLoadResourceComplete() {

            if (adapterPresenter.getCount() > 0)
                adapterPresenter.clear();

            runOnUiThread(() -> {
                adapterPresenter.addAllItem(basePlayer.getFileMetaDatas());
                setPlayUiWithData(adapterPresenter.getItem(0));
                mActViewHolder.loadProgressbBar.setVisibility(View.GONE);
            });

        }
    };
    private Subscription seachFileSubscri;
    private int cachePlayIndex = 0;

    public static void start(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        //starter.putExtra();
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //  解决 splashAct 过度到 MainAct后的status 有偏移的问题。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

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

        basePlayer = new PlayerLoader().getPlayer(PlayerLoader.PlayerType.EXOPLAYER);
        mLyricPresenter = basePlayer.getLyricPresenter();
    }

    @Override
    public void configUi() {

        adapterPresenter = new AdapterPresenter<>();
        MvpRecyclerAdapter<FileMetaData> adapter = new MvpRecyclerAdapter.Builder<FileMetaData>()
                .setLayoutId(R.layout.adapter_list_item)
                .setPresenter(adapterPresenter)
                .build((holder, fileMetaData) -> {

                    holder.setText(R.id.titleTv, fileMetaData.title);
                    holder.setText(R.id.albumTv, fileMetaData.album);

                    int pos = holder.getAdapterPosition();
                    holder.setText(R.id.playIndexTv, String.valueOf(pos + 1));

                    if (basePlayer.getCurrentPlayIndex() == pos) {
                        holder.setVisible(R.id.icPlayerOnIv, true);
                        holder.setVisible(R.id.playIndexTv, false);
                    } else {
                        holder.setVisible(R.id.icPlayerOnIv, false);
                        holder.setVisible(R.id.playIndexTv, true);
                    }

                });
        mActViewHolder.mRecyclerView.setAdapter(adapter);
        mActViewHolder.mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mActViewHolder.mRecyclerView.addItemDecoration(new LinearDividerItemDecoration.Builder(this)
                .margin(0, 0, DensityUtils.dp2px(MainActivity.this, 45.0f), 0)
                .build());

        adapter.setOnItemClickListener((viewGroup, view, fileMetaData, integer) -> {

            basePlayer.playMusic(integer);
            setPlayUiWithData(fileMetaData);
            adapter.notifyDataSetChanged();

            if (!basePlayer.isResouceLoadComplete())
                cachePlayIndex = integer;

            Timber.d("playingTitle##" + fileMetaData.title);

        });

        // notice: 2016/12/9 如果缓存没有，则从文件缓存中获取
        List<FileMetaData> metaDatas = basePlayer.getFileMetaDatas();
        basePlayer.addPlayerCallback(playerCallback);
        if (!CollectionUtils.isEmpty(metaDatas)) {
            adapterPresenter.addAllItem(basePlayer.getFileMetaDatas());
            setPlayUiWithData(metaDatas.get(0));
            mActViewHolder.loadProgressbBar.setVisibility(View.GONE);
        } else {
            List<FileMetaData> dataList = FileMetaDataRepo.getInstance().getFileMetaDataList();
            if (CollectionUtils.isNotEmpty(dataList)) {
                adapterPresenter.addAllItem(dataList);
            }
        }

        mActViewHolder.mProgressBar.setMax(NativePlayer.SEEKBAR_MAX / 2);
    }

    private boolean isPersGranted(String permiss) {
        int gratId = ActivityCompat.checkSelfPermission(this, permiss);
        return gratId == PackageManager.PERMISSION_GRANTED;
    }

    private void reqPermissIfNotGranted(String[] permission, int reqCode) {

        List<String> pers = new ArrayList<>(Arrays.asList(permission));
        Iterator<String> iterator;
        for (iterator = pers.iterator(); iterator.hasNext(); ) {
            String next = iterator.next();
            if (isPersGranted(next))
                iterator.remove();
        }

        if (pers.size() == 0)
            return;

        String[] extraPers = new String[pers.size()];
        pers.toArray(extraPers);
        if (extraPers.length == 0)
            return;

        ActivityCompat.requestPermissions(this, extraPers, reqCode);
    }

    @Override
    protected void obtainData() {

        if (isPersGranted(PERMISSION_RQ_AUDIO)) {
            if (basePlayer instanceof NativePlayer) {
                NativePlayer.getInstance().initAudio();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            String[] permission = new String[]{PERMISSION_RQ_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE};
            reqPermissIfNotGranted(permission, REQCODE_AUDIO_READ_EXTERNAL);
        } else {
            reqPermissIfNotGranted(new String[]{PERMISSION_RQ_AUDIO}, REQCODE_AUDIO);
        }
    }

    private void setPlayUiWithData(FileMetaData metaData) {

        mActViewHolder.titleTv.setText(metaData.title);
        mActViewHolder.albumTv.setText(metaData.album);

        String url = mLyricPresenter.getArtThumbUrl(metaData);
        if (FileUtils.getFileSize(url) > 0) {
            Glide.with(this)
                    .fromFile()
                    .load(new File(url))
                    .into(mActViewHolder.albumIv);
        }
    }

    void setPlayImageState(int state) {
        if (state == NativePlayer.STOPPED || state == NativePlayer.PAUSED || state == NativePlayer.ERROR)
            mActViewHolder.playIv.setImageResource(R.drawable.playbar_btn_play);
        else if (state == NativePlayer.PLAYED)
            mActViewHolder.playIv.setImageResource(R.drawable.playbar_btn_pause);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean searchMp3File = false;

        if (requestCode == REQCODE_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                if (basePlayer instanceof NativePlayer)
                    NativePlayer.getInstance().initAudio();
        } else if (requestCode == REQCODE_AUDIO_READ_EXTERNAL) {

            for (int i = 0; i < permissions.length; i++) {
                if (TextUtils.equals(Manifest.permission.READ_EXTERNAL_STORAGE, permissions[i])
                        && grantResults[i] == PackageManager.PERMISSION_GRANTED)
                    searchMp3File = true;
            }

        } else if (requestCode == REQCODE_READ_EXTERNAL) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                searchMp3File = true;

        }

        if (searchMp3File && basePlayer.getCount() <= 0) {

            mActViewHolder.loadProgressbBar.setVisibility(View.VISIBLE);
            MusicFileSearch.getInstace().startToSearchMp3File();
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
        ConnectableObservable<List<String>> fileObserable = MusicFileSearch.getInstace()
                .getSearchFileObserable();
        seachFileSubscri = fileObserable.subscribe(mp3FileList ->
                RxUiUtils.postDelayedRxOnMain(10L, () -> {
                    int size = mp3FileList != null ? mp3FileList.size() : 0;
                    showSToast("扫描到" + size + "个文件");
                }), Throwable::printStackTrace);
        fileObserable.connect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
            case R.id.media_scan:
                startToScanMusic();
                return true;
            case R.id.refresh_list:
                mActViewHolder.loadProgressbBar.setVisibility(View.VISIBLE);
                MusicFileSearch.getInstace().forceToSearchMp3File();
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
        basePlayer.removePlayerCallback(playerCallback);
        basePlayer.enableProgressChange(false);
        basePlayer.release();
    }

    @SuppressWarnings("WeakerAccess")
    public class ActViewHolder {

        @BindView(R.id.playNextIv)
        ImageView playNextIv;
        @BindView(R.id.playIv)
        ImageView playIv;

        @BindView(R.id.titleTv)
        TextView titleTv;
        @BindView(R.id.albumTv)
        TextView albumTv;
        @BindView(R.id.albumIv)
        ImageView albumIv;

        @BindView(R.id.recyclerView)
        RecyclerView mRecyclerView;

        @BindView(R.id.drawerLayout)
        DrawerLayout mDrawerLayout;
        @BindView(R.id.naviView)
        NavigationView mNavigationView;

        @BindView(R.id.progressV)
        ProgressBar mProgressBar;

        @BindView(R.id.loadProgressbV)
        ProgressBar loadProgressbBar;

        @OnClick({R.id.playNextIv, R.id.playIv, R.id.bottomRL})
        void onClick(View view) {
            final int viewId = view.getId();

            int playIndex = basePlayer.getCurrentPlayIndex();
            boolean loadComplete = basePlayer.isResouceLoadComplete();
            FileMetaData metaData = null;

            switch (viewId) {

                case R.id.playIv:

                    basePlayer.playMusic(playIndex);
                    break;

                case R.id.playNextIv:

                    basePlayer.playNext();

                    break;

                case R.id.bottomRL:

                    if (playIndex >= 0 && loadComplete)
                        metaData = basePlayer.getMetaData(playIndex);
                    if (!loadComplete)
                        metaData = adapterPresenter.getItem(cachePlayIndex);

                    if (metaData == null) return;
                    long parseLong = NumberUtils.safeParseLong(metaData.duration, 0L);
                    PlayActivity.start(MainActivity.this, metaData.title, parseLong,
                            metaData.title + "-" + metaData.album);

                    break;
            }
        }
    }

}