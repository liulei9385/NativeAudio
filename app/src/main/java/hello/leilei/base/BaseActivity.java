
/*
 * Copyright (c) 2016. Hefei Royalstar Electronic Appliance Group Co., Ltd. All rights reserved.
 */

package hello.leilei.base;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import butterknife.ButterKnife;
import hello.leilei.R;

public abstract class BaseActivity extends AppCompatActivity {

    /* activity 状态的简单判断 */
    protected boolean isPaused = false;
    protected boolean isResumed = false;

    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        mToolbar = ButterKnife.findById(this, R.id.mtoolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
    }

    protected abstract int getLayoutResource();

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

    public void setToolbarTitle(CharSequence title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        } else if (mToolbar != null) {
            mToolbar.setTitle(title);
        } else setTitle(title);
    }

    private Toast mToast;

    protected void showSToast(String msg) {
        if (mToast == null)
            mToast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        else mToast.setText(msg);
        mToast.show();
    }

    protected void showLToast(String msg) {
        if (mToast == null)
            mToast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
        else mToast.setText(msg);
        mToast.show();
    }

}