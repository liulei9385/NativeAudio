package hello.leilei.base.ui.adapter;

import android.support.annotation.LayoutRes;
import android.view.ViewGroup;

import java.util.List;

import rx.functions.Action2;

/**
 * Created by liulei on 16-5-16.
 * TIME : 下午3:53
 * COMMECTS : 多个item的adapter
 */
public abstract class MvpMultiItemAdapter<T> extends MvpRecyclerAdapter<T> {

    public static class Builder<T> {

        @LayoutRes
        int layoutId;
        List<?> mDatas;
        AdapterPresenter<T> mPresenter;
        Action2<BaseViewHolder, Integer> createHolderAction;

        MultiItemTypeSupport<?> mMultiItemTypeSupport;

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

        public Builder<T> setMultiItemTypeSupport(MultiItemTypeSupport<T> multiItemTypeSupport) {
            mMultiItemTypeSupport = multiItemTypeSupport;
            return this;
        }

        public MvpMultiItemAdapter<T> build(Action2<BaseViewHolder, T> convertAction) {

            if (this.mPresenter == null)
                throw new IllegalArgumentException("presenter can not to null");

            if (this.mMultiItemTypeSupport == null)
                throw new IllegalArgumentException("mMultiItemTypeSupport can not to null");

            this.mPresenter.addAllItem(this.mDatas);

            MvpMultiItemAdapter<T> itemAdapter = new MvpMultiItemAdapter<T>(this.layoutId,
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

            if (this.createHolderAction != null)
                itemAdapter.setCreateHolderAction(this.createHolderAction);
            itemAdapter.setPresenter(this.mPresenter);
            //noinspection unchecked
            itemAdapter.setMultiItemTypeSupport((MultiItemTypeSupport<T>) this.mMultiItemTypeSupport);

            return itemAdapter;
        }
    }

    private MultiItemTypeSupport<T> mMultiItemTypeSupport;

    public void setMultiItemTypeSupport(MultiItemTypeSupport<T> multiItemTypeSupport) {
        mMultiItemTypeSupport = multiItemTypeSupport;
    }

    public MvpMultiItemAdapter(@LayoutRes int layoutId, List<T> mDatas) {
        super(layoutId, mDatas);
    }

    @Override
    protected int getDefItemViewType(int position) {
        if (mMultiItemTypeSupport != null) {
            int dataPos = position - getHeadViewCount();
            if (dataPos >= 0 && dataPos < mDatas.size())
                return mMultiItemTypeSupport.getItemViewType(dataPos, mDatas.get(dataPos));
        }
        return super.getItemViewType(position);
    }

    @Override
    protected BaseViewHolder onCreateDefViewHolder(ViewGroup parent, int viewType) {
        if (mMultiItemTypeSupport == null)
            return super.onCreateViewHolder(parent, viewType);
        int layoutId = mMultiItemTypeSupport.getLayoutId(viewType);
        BaseViewHolder baseViewHolder = createBaseViewHolder(parent, layoutId);
        if (createHolderAction != null)
            createHolderAction.call(baseViewHolder, viewType);
        return baseViewHolder;
    }

}
