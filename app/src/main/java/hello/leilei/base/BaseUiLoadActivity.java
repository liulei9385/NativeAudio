package hello.leilei.base;

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
        prepareData();
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

    public void prepareData() {
    }

    public void initView() {
    }

}