package hello.leilei.base.ui.adapter;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

import rx.functions.Action2;

/**
 * Created by liulei on 16-5-16.
 * TIME : 上午9:25
 */
public abstract class MvpRecyclerAdapter<T> extends BaseRecyclerAdapter<T> {

    /**
     * integer : viewtype
     */
    Action2<BaseViewHolder, Integer> createHolderAction;

    public void setCreateHolderAction(Action2<BaseViewHolder, Integer> createHolderAction) {
        this.createHolderAction = createHolderAction;
    }

    /**
     * 使用建造者模式建立Adapter
     */
    public static class Builder<T> {

        @LayoutRes
        int layoutId;
        List<?> mDatas;
        AdapterPresenter<T> mPresenter;
        Action2<BaseViewHolder, Integer> createHolderAction;

        public Builder() {
        }

        public Builder<T> setLayoutId(int layoutId) {
            this.layoutId = layoutId;
            return this;
        }

        public Builder<T> setDatas(List<T> mDatas) {
            this.mDatas = mDatas;
            return this;
        }

        public Builder<T> setPresenter(AdapterPresenter<T> presenter) {
            this.mPresenter = presenter;
            return this;
        }

        public Builder<T> setCreateHolderAction(Action2<BaseViewHolder, Integer> createHolderAction) {
            this.createHolderAction = createHolderAction;
            return this;
        }

        public MvpRecyclerAdapter<T> build(Action2<BaseViewHolder, T> convertAction) {

            if (this.layoutId <= 0)
                throw new IllegalArgumentException("layoutId can not to null");

            if (this.mPresenter == null)
                throw new IllegalArgumentException("presenter can not to null");

            this.mPresenter.addAllItem(this.mDatas);

            //noinspection unchecked
            MvpRecyclerAdapter<T> adapter = new MvpRecyclerAdapter<T>(
                    this.layoutId,
                    this.mPresenter.getDatas()) {
                @Override
                public void convert(BaseViewHolder holder, T t) {

                    //利用mPresenter处理一些状态保存的问题
                    if (mPresenter != null)
                        mPresenter.convert(holder, t);

                    if (convertAction != null)
                        convertAction.call(holder, t);
                }
            };

            //noinspection unchecked
            adapter.setPresenter(this.mPresenter);

            if (this.createHolderAction != null)
                adapter.setCreateHolderAction(this.createHolderAction);

            return adapter;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mPresenter.start();
        mPresenter.attachRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mPresenter.detachView();
    }

    @Override
    protected BaseViewHolder onCreateDefViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder viewHolder = super.onCreateDefViewHolder(parent, viewType);
        if (viewHolder != null && createHolderAction != null) {
            createHolderAction.call(viewHolder, viewType);
        }
        return viewHolder;
    }

    private AdapterPresenter<T> mPresenter;

    public AdapterPresenter<T> getPresenter() {
        return mPresenter;
    }

    public void setPresenter(AdapterPresenter<T> presenter) {
        mPresenter = presenter;
    }

    public MvpRecyclerAdapter(@LayoutRes int layoutId, List<T> mDatas) {
        super(layoutId, mDatas);
    }

    @Override
    public void convert(BaseViewHolder holder, T t) {
        //利用mPresenter处理一些状态保存的问题
        if (mPresenter != null)
            mPresenter.convert(holder, t);
    }
}
