package hello.leilei;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobUser;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ImageViewTarget;
import hello.leilei.utils.RxUiUtils;

/**
 * Created by liulei on 2016/11/29.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView splashIv = ButterKnife.findById(this, R.id.splashImgV);
        String imgPath = "http://sjbz.fd.zol-img.com.cn/t_s1080x1920c/g5/M00/00/01/ChMkJlfJUOeIYW8AAAaciAofGP8AAU9awOl8VUABpyg483.jpg";
        Glide.with(this)
                .load(Uri.parse(imgPath))
                .centerCrop()
                .into(new ImageViewTarget<GlideDrawable>(splashIv) {

                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        super.onResourceReady(resource, glideAnimation);
                        RxUiUtils.postDelayedOnBg(1200L, () -> gotoMain());
                    }

                    @Override
                    protected void setResource(GlideDrawable resource) {
                        splashIv.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        gotoMain();
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