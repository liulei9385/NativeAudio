package hello.leilei.activity.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import hello.leilei.R;

/**
 * think
 * 2015/11/9
 * 15:31
 */
public abstract class BaseUiLoadActivity extends BaseActivity {

    @Override
    protected abstract int getLayoutResource();

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_base_uiload);
        LayoutInflater inflater = LayoutInflater.from(this);
        View rootView = ButterKnife.findById(this, R.id.baseUiLoad);
        if (layoutResID != 0)
            inflater.inflate(layoutResID, (ViewGroup) rootView, true);
    }

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