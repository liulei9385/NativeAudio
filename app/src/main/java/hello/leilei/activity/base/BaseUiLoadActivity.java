package hello.leilei.activity.base;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hello.leilei.R;
import hello.leilei.utils.ViewUtils;

/**
 * think
 * 2015/11/9
 * 15:31
 */
public abstract class BaseUiLoadActivity extends BaseActivity {

    @Override
    protected abstract int getLayoutResource();

    public class BaseUiLoadClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            onViewClicked(v.getId(), v);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_base_uiload);
        LayoutInflater inflater = LayoutInflater.from(this);
        View rootView = findView(R.id.baseUiLoad);
        if (layoutResID != 0)
            inflater.inflate(layoutResID, (ViewGroup) rootView, true);
    }

    protected abstract void onViewClicked(int id, View v);

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

    private View.OnClickListener clickListener;

    private View.OnClickListener getClickLisgener() {
        if (clickListener == null)
            clickListener = new BaseUiLoadClickListener();
        return clickListener;
    }

    /**
     * 批量绑定单击监听器
     *
     * @param views
     */
    protected void setClickListenrForViews(View... views) {
        ViewUtils.setOnClickListener(getClickLisgener(), views);
    }

    /**
     * 批量绑定单击监听器
     *
     * @param ids
     */
    protected void setClickListenrForViews(@IdRes Integer... ids) {
        for (Integer inte : ids) {
            if (inte > 0) {
                View view = findView(inte);
                if (view != null)
                    view.setOnClickListener(getClickLisgener());
            }
        }
    }

}