package hello.leilei.base.ui.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import hello.leilei.R;
import hello.leilei.utils.CollectionUtils;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Action4;
import rx.functions.Func4;

/**
 * Created by liulei on 16-5-12.
 * TIME : 下午5:33
 * COMMECTS : RecyclerView通用Adapter封装
 */
public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> {

    public static final String TAG = "BaseRecyclerAdapter";

    private boolean mNextLoadEnable = false;
    private boolean mLoadingMoreEnable = false;
    //此flag表示是否需要刷新数据源
    private boolean notifyAdapterLazy = false;
    private boolean mEmptyEnable = false;
    private int pageSize = -1;

    protected List<T> mDatas;

    public List<T> getDatas() {
        return mDatas;
    }

    public BaseRecyclerAdapter setEmptyEnable(boolean emptyEnable) {
        mEmptyEnable = emptyEnable;
        return this;
    }

    public boolean isEmptyEnable() {
        return mEmptyEnable;
    }

    public void setDatas(List<? extends T> datas) {

        if (datas == null) {
            mDatas = null;
            notifyDataSetChanged();
            return;
        }

        if (mDatas != null && mDatas.size() > 0)
            mDatas.clear();

        if (!CollectionUtils.isEmpty(datas)) {
            if (mDatas == null)
                mDatas = new ArrayList<>();
            mDatas.addAll(datas);
        }

        notifyDataSetChanged();
    }

    public boolean addItem(T data) {
        if (data == null) {
            return false;
        }

        if (mDatas == null) {
            mDatas = new ArrayList<>();
        }

        return mDatas.add(data);
    }

    protected int mLayoutResId;

    protected static final int HEADER_VIEW = 0x00000111;
    protected static final int LOADING_VIEW = 0x00000222;
    protected static final int FOOTER_VIEW = 0x00000333;
    protected static final int EMPTY_VIEW = 0x00000555;

    Action4<ViewGroup, View, T, Integer> itemClickListener;
    Func4<ViewGroup, View, T, Integer, Boolean> itemLongClickListener;
    Action0 mRequestLoadMoreListener, emptyClickListener;

    /**
     * itemView　viewholder中的itemView
     */
    View mContentView;

    /**
     * 头部，脚,空view，加载中view
     */
    View mHeaderView;
    View mFooterView;
    View mEmptyView;
    View mLoadingView;

    /**
     * adapter　所依赖的view
     */
    public RecyclerView mRecyclerView;
    RecyclerView.OnScrollListener onScrollListener;

    int recyclerState;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.mRecyclerView = recyclerView;

        onScrollListener =
                new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        recyclerState = newState;
                        //recyclerview处于静止状态
                        if (recyclerState == RecyclerView.SCROLL_STATE_IDLE) {

                            if (notifyAdapterLazy
                                    && mRequestLoadMoreListener != null && mNextLoadEnable) {
                                notifyAdapterLazy = false;
                                notifyDataSetChanged();
                                return;
                            }

                            if (mLoadingMoreEnable) {
                                if (mRequestLoadMoreListener != null)
                                    mRequestLoadMoreListener.call();
                            }
                        }
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                    }
                };
        this.mRecyclerView.addOnScrollListener(onScrollListener);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.onScrollListener = null;
        this.onScrollListener = null;
        this.mRecyclerView = null;
    }

    public BaseRecyclerAdapter(int layoutResId, List<T> mDatas) {
        //数据源交由AdapterPresenter管理
        this.mDatas = mDatas;
        this.mLayoutResId = layoutResId;
    }

    public BaseRecyclerAdapter(View contentView, List<T> mDatas) {
        this(0, mDatas);
        this.mContentView = contentView;
    }

    public void setOnLoadMoreListener(Action0 requestLoadMoreListener) {
        this.mRequestLoadMoreListener = requestLoadMoreListener;
    }

    public void setOnItemClickListener(Action4<ViewGroup, View, T, Integer> clickListener) {
        this.itemClickListener = clickListener;
    }

    public void setEmptyViewClickListener(Action0 emptyClickListener) {
        this.emptyClickListener = emptyClickListener;
    }

    public void setOnItemLongClickListener(Func4<ViewGroup, View, T, Integer, Boolean> longClickListener) {
        this.itemLongClickListener = longClickListener;
    }

    public int getHeadViewCount() {
        return mHeaderView != null ? 1 : 0;
    }

    public int getFooterViewCount() {
        return mFooterView != null ? 1 : 0;
    }

    public int getEmptyViewCount() {
        return mEmptyView != null ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        //TODO 重要
        //手动添加1个容纳加载更多的item-viewholder
        int contentSize = isLoadMore() ? 1 : 0;
        contentSize += mDatas != null ? mDatas.size() : 0;
        contentSize += getHeadViewCount() + getFooterViewCount();

        if (contentSize > 0 && mEmptyEnable) {
            mEmptyEnable = false;
        } else if (contentSize == 0) {
            mEmptyEnable = true;
        }

        //mEmptyEnable = false;
        if (contentSize == 0 && mEmptyEnable) {
            //mEmptyEnable = true;
            contentSize += getEmptyViewCount();
        }
        return contentSize;
    }

    public int getActualCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    public T getItemAtPostion(int position) {

        if (getItemCount() > 0) {
            return mDatas.get(position);
        }
        return null;
    }

    /**
     * 点击或者长按监听器的开关
     */
    protected boolean isEnabled(int viewType) {
        return true;
    }

    /**
     * 为新生成的viewHolder设置单击监听器，在{@link #isEnabled(int)}为true的情况下
     */
    protected void setListener(BaseViewHolder viewHolder) {
        if (!isEnabled(viewHolder.getItemViewType())) return;
        //click

        final View.OnClickListener listener = view -> {
            if (itemClickListener != null) {

                int pos = viewHolder.getAdapterPosition();
                if (pos == -1) return;

                if (mHeaderView != null) {
                    pos--;
                }

                if (pos >= 0 && pos < getItemCount()) {
                    itemClickListener.call(null,
                            viewHolder.itemView, getItemAtPostion(pos), pos);
                }


            }
        };

        viewHolder.itemView.setOnClickListener(listener);
        //long click
        viewHolder.itemView.setOnLongClickListener(v -> {
            int pos = viewHolder.getAdapterPosition();
            if (pos == -1) return false;

            if (mHeaderView != null) {
                pos--;
            }
            if (itemLongClickListener != null) {
                if (pos >= 0 && pos < getItemCount())
                    return itemLongClickListener.call(null,
                            viewHolder.itemView, getItemAtPostion(pos), pos);
            }

            return false;
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (mHeaderView != null && position == 0) {
            return HEADER_VIEW;
        } else if (mEmptyView != null && getItemCount() == 1
                && getActualCount() == 0 && mEmptyEnable) {
            return EMPTY_VIEW;
        } else if (position == mDatas.size() + getHeadViewCount()) {
            if (mNextLoadEnable)
                return LOADING_VIEW;
            else
                return FOOTER_VIEW;
        }
        return getDefItemViewType(position);
    }

    protected int getDefItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        BaseViewHolder baseViewHolder;
        switch (viewType) {
            case LOADING_VIEW:
                baseViewHolder = getLoadingView(parent);
                break;
            case HEADER_VIEW: {
                baseViewHolder = new BaseViewHolder(mHeaderView);
            }
            break;
            case EMPTY_VIEW: {
                baseViewHolder = new BaseViewHolder(mEmptyView);
            }
            break;
            case FOOTER_VIEW:
                baseViewHolder = new BaseViewHolder(mFooterView);
                break;
            default:
                baseViewHolder = onCreateDefViewHolder(parent, viewType);
        }
        return baseViewHolder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {

        switch (holder.getItemViewType()) {
            case 0:
                int dataPos = holder.getLayoutPosition() - getHeadViewCount();
                if (dataPos >= 0 && dataPos < mDatas.size()) {
                    convert(holder, mDatas.get(dataPos));
                }

                setListener(holder);
                break;
            case LOADING_VIEW:
                addLoadMore(holder);
                break;
            case HEADER_VIEW:
                if (headViewShowAction != null)
                    headViewShowAction.call(holder);
                break;
            case EMPTY_VIEW:
                if (holder.itemView != null) {
                    if (emptyClickListener != null) {
                        holder.itemView.setOnClickListener(view -> emptyClickListener.call());
                    }

                    if (emptyViewBindAction != null) {
                        emptyViewBindAction.call(holder.itemView);
                    }
                }
                break;
            case FOOTER_VIEW:
                break;
            default:
                convert(holder, mDatas.get(holder.getLayoutPosition() - getHeadViewCount()));
                setListener(holder);
                break;
        }
    }

    private Action1<View> emptyViewBindAction;

    /**
     * 绑定空白view
     */
    public void bindEmptyViewHolder(Action1<View> action1) {
        emptyViewBindAction = action1;
    }

    /**
     * 绑定数据
     *
     * @param t 数据item
     */
    public abstract void convert(BaseViewHolder holder, T t);

    private BaseViewHolder getLoadingView(ViewGroup parent) {
        if (mLoadingView == null) {
            return createBaseViewHolder(parent, R.layout.def_loading);
        }
        return new BaseViewHolder(mLoadingView);
    }

    protected BaseViewHolder onCreateDefViewHolder(ViewGroup parent, int viewType) {
        return createBaseViewHolder(parent, mLayoutResId);
    }

    protected BaseViewHolder createBaseViewHolder(ViewGroup parent, int layoutResId) {
        if (mContentView == null) {
            View mView = getItemView(layoutResId, parent);
            return new BaseViewHolder(mView);
        }
        return new BaseViewHolder(mContentView);
    }

    protected View getItemView(int layoutResId, ViewGroup parent) {
        return layoutResId > 0 ? LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false) : null;
    }

    public void setIsLoadMoreEnabled(boolean enable) {
        mNextLoadEnable = enable;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    private boolean isLoadMore() {
        return mNextLoadEnable && pageSize > 0
                && mRequestLoadMoreListener != null && mDatas.size() >= pageSize;
    }

    private void addLoadMore(RecyclerView.ViewHolder holder) {
        if (isLoadMore()) {
            mLoadingMoreEnable = true;
            if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams)
                        holder.itemView.getLayoutParams();
                params.setFullSpan(true);
            }
        }
    }

    /**
     * 如果recyclerview处于滑动状态,强行更新数据会报错
     */
    public boolean canNotifySafely() {
        return (mRecyclerView != null && !mRecyclerView.isComputingLayout());
    }

    public void notifyDataChangedAfterLoadMore(boolean isNextLoad) {
        mNextLoadEnable = isNextLoad;
        mLoadingMoreEnable = false;
        //TODO 此处notifydatasetChanged不能用
        if (canNotifySafely()) {
            notifyDataSetChanged();
        } else
            notifyAdapterLazy = true;
    }

    public void notifyDataChangedAfterLoadMore(List<T> data, boolean isNextLoad) {
        mDatas.addAll(data);
        notifyDataChangedAfterLoadMore(isNextLoad);
    }

    public void addHeaderView(@NonNull View header) {
        this.mHeaderView = header;
        this.notifyDataSetChanged();
    }

    public void addFooterView(@NonNull View footer) {
        mNextLoadEnable = false;
        this.mFooterView = footer;
        this.notifyDataSetChanged();
    }

    /**
     * Sets the view to show if the adapter is empty
     */
    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
    }

    public void setEmptyView(@Nullable Context context, @LayoutRes int emptyResId) {
        if (emptyResId > 0) {
            mEmptyView = LayoutInflater.from(context).inflate(emptyResId, null, false);
        }
    }

    public void setEmptyView(@Nullable Context context,
                             @LayoutRes int emptyResId, Action1<View> bindAction) {
        setEmptyView(context, emptyResId);
        if (bindAction != null)
            bindEmptyViewHolder(bindAction);
    }

    public View getEmptyView() {
        return mEmptyView;
    }

    public boolean toggleEmptyView(boolean isShown) {
        if (mEmptyView != null) {
            mEmptyView.setVisibility(isShown ? View.VISIBLE : View.GONE);
            return true;
        }
        return false;
    }

    public boolean toggleHeadView(Action1<BaseViewHolder> holderSetAction) {
        if (mHeaderView != null) {
            if (holderSetAction != null) {
                BaseViewHolder holder = (BaseViewHolder) mRecyclerView
                        .findContainingViewHolder(mHeaderView);
                if (holder != null) {
                    holderSetAction.call(holder);
                }
            }
            return true;
        }
        return false;
    }

    private Action1<BaseViewHolder> headViewShowAction;

    public void setHeadViewShowAction(Action1<BaseViewHolder> headViewShowAction) {
        this.headViewShowAction = headViewShowAction;
    }
}