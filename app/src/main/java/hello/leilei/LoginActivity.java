package hello.leilei;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import hello.leilei.base.BaseUiLoadActivity;
import okhttp3.*;
import timber.log.Timber;

import java.io.IOException;

/**
 * Created by liulei on 2016/11/30.
 */
public class LoginActivity extends BaseUiLoadActivity {

    @BindView(R.id.userNameEt)
    EditText userNameEt;
    @BindView(R.id.pwdEt)
    EditText pwdEt;
    @BindView(R.id.submitV)
    Button submitBtn;

    public static void start(Context context) {
        Intent starter = new Intent(context, LoginActivity.class);
        //starter.putExtra();
        context.startActivity(starter);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_login;
    }

    @Override
    protected void configUi() {
        ButterKnife.bind(this);
    }

    @Override
    protected void obtainData() {
    }

    @OnClick({R.id.submitV})
    void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.submitV:

                startLogin();

                break;
        }
    }

    /**
     * 登录逻辑
     */
    private void startLogin() {

        if (BmobUser.getCurrentUser() != null) {
            showSToast("已经登录");
            return;
        }

        String uName = userNameEt.getText().toString();
        String pwd = pwdEt.getText().toString();

        BmobUser.loginByAccount(uName, pwd, new LogInListener<BmobUser>() {
            @Override
            public void done(BmobUser user, BmobException e) {
                if (e == null) {
                    showSToast("登录成功:");
                    Timber.d("登录成功");
                    MainActivity.start(LoginActivity.this);
                    //通过BmobUser user = BmobUser.getCurrentUser()获取登录成功后的本地用户信息
                    //如果是自定义用户对象MyUser，可通过MyUser user = BmobUser.getCurrentUser(MyUser.class)获取自定义用户信息
                } else {
                    showSToast(e.getMessage());
                    Timber.e(e);
                }
            }
        });
    }

}