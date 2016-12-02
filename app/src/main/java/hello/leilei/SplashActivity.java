package hello.leilei;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import hello.leilei.base.audioplayer.NativePlayer;
import hello.leilei.model.SplashImgBean;
import hello.leilei.utils.CollectionUtils;
import hello.leilei.utils.RxUiUtils;
import timber.log.Timber;

/**
 * Created by liulei
 * DATE: 2016/11/29
 * TIME: 14:33
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        NativePlayer.getInstance()
                .startToSearchMp3File();

        ImageView splashIv = ButterKnife.findById(this, R.id.splashImgV);

        BmobQuery<SplashImgBean> imgQuery = new BmobQuery<>();
        imgQuery.findObjects(new FindListener<SplashImgBean>() {
            @Override
            public void done(List<SplashImgBean> list, BmobException e) {
                if (CollectionUtils.isNotEmpty(list)) {
                    int count = list.size();
                    int index = new Random().nextInt(count);
                    BmobFile imgData = list.get(index).getImgData();

                    Glide.with(SplashActivity.this)
                            .load(imgData.getFileUrl())
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model,
                                                           Target<GlideDrawable> target, boolean isFirstResource) {
                                    gotoMain();
                                    return true;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource,
                                                               String model,
                                                               Target<GlideDrawable> target,
                                                               boolean isFromMemoryCache, boolean isFirstResource) {

                                    RxUiUtils.postDelayedOnBg(2000L, SplashActivity.this::gotoMain);

                                    return false;
                                }
                            })
                            .into(splashIv);

                } else {
                    Timber.e(e);
                    gotoMain();
                }
            }
        });
    }

    private void gotoMain() {
        BmobUser currentUser = BmobUser.getCurrentUser();
        if (currentUser == null) {
            LoginActivity.start(this);
        } else {
            MainActivity.start(SplashActivity.this);
        }
        overridePendingTransition(R.anim.ui_slide_in_right, R.anim.ui_slide_out_left);
        finish();
    }

}