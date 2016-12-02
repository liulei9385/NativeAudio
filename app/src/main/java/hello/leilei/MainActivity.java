package hello.leilei;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobUser;
import hello.leilei.base.BaseUiLoadActivity;
import hello.leilei.base.audioplayer.IPlayerCallback;
import hello.leilei.base.audioplayer.NativePlayer;
import hello.leilei.base.decoration.LinearDividerItemDecoration;
import hello.leilei.base.ui.adapter.AdapterPresenter;
import hello.leilei.base.ui.adapter.MvpRecyclerAdapter;
import hello.leilei.lyric.LyricPresenter;
import hello.leilei.model.FileMetaData;
import hello.leilei.utils.CollectionUtils;
import hello.leilei.utils.DensityUtils;
import hello.leilei.utils.RxUiUtils;
import rx.Subscription;

/**
 * Created by liulei on 16-3-18.
 * TIME : 下午9:43
 * COMMECTS :
 */
public class MainActivity extends BaseUiLoadActivity {

    private ActViewHolder mActViewHolder;

    AdapterPresenter<FileMetaData> adapterPresenter;
    LyricPresenter mLyricPresenter;
    NativePlayer mNativePlayer;

    public static final String RQ_AUDIO = Manifest.permission.RECORD_AUDIO;
    private Subscription seachFileSubscri;

    class ActViewHolder {

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

        @OnClick({R.id.playNextIv, R.id.playIv, R.id.bottomRL})
        void onClick(View view) {
            final int viewId = view.getId();
            switch (viewId) {

                case R.id.playIv:

                    mNativePlayer.playMusic(0);

                    break;

                case R.id.playNextIv:

                    mNativePlayer.playerNext();

                    break;

                case R.id.bottomRL:

                    int currentIndex = mNativePlayer.getCuttentPlayIndex();
                    if (currentIndex >= 0) {
                        FileMetaData fileMetaData = mNativePlayer.getMetaData(currentIndex);
                        if (fileMetaData == null) return;
                        PlayActivity.start(MainActivity.this, fileMetaData.title,
                                fileMetaData.title + "-" + fileMetaData.album);
                    }
                    break;
            }
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        //starter.putExtra();
        context.startActivity(starter);
    }

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

        mNativePlayer = NativePlayer.getInstance();
        mLyricPresenter = mNativePlayer.getLyricPresenter();
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
                    holder.setText(R.id.playIndexTv, String.valueOf(holder.getAdapterPosition() + 1));

                });
        mActViewHolder.mRecyclerView.setAdapter(adapter);
        mActViewHolder.mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mActViewHolder.mRecyclerView.addItemDecoration(new LinearDividerItemDecoration.Builder(this)
                .margin(0, 0, DensityUtils.dp2px(MainActivity.this, 45.0f), 0)
                .build());

        adapter.setOnItemClickListener((viewGroup, view, fileMetaData, integer) -> {

            mNativePlayer.playMusic(integer);
            setPlayUiWithData(fileMetaData);

        });

        List<FileMetaData> metaDatas = mNativePlayer.getFileMetaDatas();
        mNativePlayer.addPlayerCallback(playerCallback);
        if (!CollectionUtils.isEmpty(metaDatas))
            adapterPresenter.addAllItem(mNativePlayer.getFileMetaDatas());
    }

    IPlayerCallback playerCallback = new IPlayerCallback() {
        @Override
        public void onPlayState(int state) {
            setPlayImageState(state);
        }

        @Override
        public void onProgressChanged(NativePlayer.ProgressItem mProgressItem) {
            mActViewHolder.mProgressBar.setProgress((int) (mProgressItem.percent * 100));
        }

        @Override
        public void onLoadResourceComplete() {
            if (adapterPresenter.getCount() > 0)
                adapterPresenter.clear();
            adapterPresenter.addAllItem(mNativePlayer.getFileMetaDatas());
        }
    };

    @Override
    protected void obtainData() {

        int gratId = ActivityCompat.checkSelfPermission(this, RQ_AUDIO);
        if (gratId != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{RQ_AUDIO}, 2);
        } else
            NativePlayer.getInstance().initAudio();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            gratId = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (gratId != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
            }
        }
    }

    private void setPlayUiWithData(FileMetaData metaData) {

        mActViewHolder.titleTv.setText(metaData.title);
        mActViewHolder.albumTv.setText(metaData.album);

        metaData.getBitmapObservable()
                .subscribe(bitmap -> {
                    BitmapDrawable bitmapDrawable =
                            new BitmapDrawable(getResources(), bitmap);
                    mActViewHolder.albumIv.setImageDrawable(bitmapDrawable);
                }, Throwable::printStackTrace);
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
        if (grantResults.length == 1 && requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                NativePlayer.getInstance().initAudio();
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
        seachFileSubscri = mNativePlayer.getSearchFileObserable()
                .subscribe(mp3FileList ->
                        RxUiUtils.postDelayedRxOnMain(10L, () -> {
                            int size = mp3FileList != null ? mp3FileList.size() : 0;
                            showSToast("扫描到" + size + "个文件");
                        }), Throwable::printStackTrace);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
            case R.id.media_scan:
                startToScanMusic();
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
        mNativePlayer.removePlayerCallback(playerCallback);
        NativePlayer.getInstance().shutDown();
    }

}