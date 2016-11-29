package hello.leilei.base.ui.adapter;

import android.support.v7.widget.RecyclerView;


import hello.leilei.base.BaseView;
import rx.functions.Action1;

/**
 * Created by liulei on 16-5-13.
 * TIME : 下午4:16
 * RecyclerView的adapter的view层
 */
public class IAdapterViewImpl<T> implements BaseView<AdapterPresenter> {

    private AdapterPresenter mAdapterPresenter;
    private RecyclerView mView;

    private Action1<RecyclerView.ViewHolder> viewSelectAction;
    private Action1<RecyclerView.ViewHolder> viewUnSelectAction;

    public void setViewSelectAction(Action1<RecyclerView.ViewHolder> viewSelectAction) {
        this.viewSelectAction = viewSelectAction;
    }

    public void setViewUnSelectAction(Action1<RecyclerView.ViewHolder> viewUnSelectAction) {
        this.viewUnSelectAction = viewUnSelectAction;
    }

    @Override
    public void setPresenter(AdapterPresenter presenter) {
        mAdapterPresenter = presenter;
    }

    /**
     * @param pos
     * @param isSelcted
     * @return viewholder为null则返回false
     */
    public boolean setSelcted(int pos, boolean isSelcted) {
        final BaseViewHolder viewHolder = (BaseViewHolder)
                mView.findViewHolderForAdapterPosition(pos);
        //如果当前pos不在屏幕显示范围内，则viewHolder为空
        if (viewHolder != null) {

            if (viewSelectAction != null && isSelcted) {
                viewSelectAction.call(viewHolder);
            }

            if (viewUnSelectAction != null && !isSelcted) {
                viewUnSelectAction.call(viewHolder);
            }
            return true;
        }
        return false;
    }

    public void setSelcted(BaseViewHolder viewHolder, boolean isSelcted) {
        //如果当前pos不在屏幕显示范围内，则viewHolder为空
        if (viewHolder != null) {

            if (viewSelectAction != null && isSelcted) {
                viewSelectAction.call(viewHolder);
            }

            if (viewUnSelectAction != null && !isSelcted) {
                viewUnSelectAction.call(viewHolder);
            }
        }
    }

    public void attachView(RecyclerView mView) {
        this.mView = mView;
    }

    public void detachView() {
        mView = null;
    }

}
