
/*
 * Copyright (c) 2016. Hefei Royalstar Electronic Appliance Group Co., Ltd. All rights reserved.
 */

package hello.leilei.activity.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import hello.leilei.R;

public abstract class BaseActivity extends AppCompatActivity {

    /* activity 状态的简单判断 */
    protected boolean isPaused = false;
    protected boolean isResumed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        Toolbar mToolbar = findView(R.id.mtoolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
    }

    protected abstract int getLayoutResource();

    @SuppressWarnings("unchecked")
    protected <T extends View> T findView(int resId) {
        return (T) this.findViewById(resId);
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T findView(View view, int resId) {
        return (T) view.findViewById(resId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
        isPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
        isPaused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mToast != null)
            mToast.cancel();
    }

    private Toast mToast;

    protected void showSToast(String msg) {
        if (mToast == null)
            mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        else mToast.setText(msg);
        mToast.show();
    }

    protected void showLToast(String msg) {
        if (mToast == null)
            mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        else mToast.setText(msg);
        mToast.show();
    }

}