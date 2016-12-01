package hello.leilei.base;

import android.content.Intent;
import android.os.Bundle;

/**
 * think
 * 2015/11/9
 * 15:31
 */
public abstract class BaseUiLoadActivity extends BaseActivity {

    @Override
    protected abstract int getLayoutResource();

    private boolean isLoad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareData(getIntent());
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isLoad) {
            configUi();
            obtainData();
            isLoad = true;
        }
    }

    protected abstract void configUi();

    protected abstract void obtainData();

    public void prepareData(Intent mIntent) {
    }

    public void initView() {
    }

}