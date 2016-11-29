package hello.leilei.base.ui.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hello.leilei.utils.CollectionUtils;
import rx.functions.Action2;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by liulei on 16-5-13.
 * TIME : 下午3:37
 */
public class AdapterPresenter<T> implements BasePresenter {

    public RecyclerView.Adapter mAdapter;

    public boolean isSelctEnable = false;
    public boolean isAllSelected = false; //全选

    public IAdapterViewImpl<AdapterPresenter> mView;
    //充当model的功能
    private List<T> mDatas;
    //选中状态保存
    private SparseBooleanArray mSelectedArray;

    public int getCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    public AdapterPresenter() {
        this.mView = new IAdapterViewImpl<>();
        this.mDatas = new ArrayList<>();
        this.mSelectedArray = new SparseBooleanArray();
    }

    public void setDatas(List<T> datas) {
        this.mDatas.clear();
        if (!CollectionUtils.isEmpty(datas))
            this.mDatas.addAll(datas);
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    public void setViewSelectAction(Action2<Boolean, RecyclerView.ViewHolder> selectAction) {
        mView.setViewSelectAction(holder -> selectAction.call(true, holder));
        mView.setViewUnSelectAction(holder -> selectAction.call(false, holder));
    }

    /**
     * 处理一些状态保存的问题
     *
     * @param holder
     * @param t
     */
    public void convert(BaseViewHolder holder, T t) {
        if (!isSelctEnable)
            return;
        boolean selcted = isSelcted(holder.getAdapterPosition());
        mView.setSelcted(holder, selcted);
    }

    private void initData() {
        mDatas = new ArrayList<>();
    }

    /**
     * 获取数据
     *
     * @return
     */
    public List<T> getDatas() {
        return mDatas;
    }

    public <R> List<R> getDatas(Class<R> rClazz) {
        if (getCount() == 0) return null;
        List<R> rList = new ArrayList<>();
        //noinspection Convert2streamapi
        for (Object obj : getDatas()) {
            if (obj.getClass().equals(rClazz)) {
                //noinspection unchecked
                rList.add((R) obj);
            }
        }
        return rList;
    }

    /**
     * 没做重复处理
     * 增加Item
     *
     * @param t
     */
    public void addItem(T t) {
        if (mDatas == null)
            initData();
        if (t != null) {
            mDatas.add(t);
            if (mAdapter != null)
                mAdapter.notifyDataSetChanged();
        }
    }

    public void setItem(int pos, T t) {
        if (mDatas == null)
            initData();
        if (t != null) {
            if (pos >= mDatas.size())
                mDatas.add(t);
            else
                mDatas.set(pos, t);
            if (mAdapter != null)
                mAdapter.notifyDataSetChanged();
        }
    }

    @SuppressWarnings("unchecked")
    public <R> void setItem(Func2<Object, R, Boolean> func2, R r) {
        setItem(func2, r, null);
    }

    /**
     * @param func2  比较
     * @param r      被比较对象
     * @param addFun 添加对象
     * @param <R>
     */
    @SuppressWarnings("unchecked")
    public <R> void setItem(Func2<Object, R, Boolean> func2, R r, @Nullable Func1<Object, Object> addFun) {
        // 考虑到 r 的类型可能有问题
        Object[] indexOfObject = indexOfObject(func2, r);

        if (indexOfObject == null || indexOfObject.length < 2) return;

        Object obj = indexOfObject[0];
        int index = (int) indexOfObject[1];

        if (addFun != null) {
            obj = addFun.call(obj);
        }

        if (r != null && obj != null && r.getClass().equals(obj.getClass())) {
            obj = r;
        }

        if (index >= 0)
            setItem(index, (T) obj);
        else addItem((T) obj);
    }

    /**
     * 获取当前的对象的序号
     *
     * @param func2 <Object, R, Boolean> -->> <list的item, 比较的对象, 是否相等>
     * @param r     比较的对象
     * @param <R>   入参
     * @return
     */
    public <R> int indexOf(Func2<Object, R, Boolean> func2, R r) {
        List<T> datas = getDatas();
        for (int i = 0; func2 != null && i < datas.size(); i++) {
            if (func2.call(datas.get(i), r))
                return i;
        }
        return -1;
    }

    /**
     * @param func2
     * @param r
     * @param <R>
     * @return 当前对象以及序号
     */
    private <R> Object[] indexOfObject(Func2<Object, R, Boolean> func2, R r) {
        List<T> datas = getDatas();
        Object[] returns = null;
        for (int i = 0; func2 != null && i < datas.size(); i++) {
            if (func2.call(datas.get(i), r))
                returns = new Object[]{datas.get(i), i};
        }
        if (returns == null)
            returns = new Object[]{r, -1};
        return returns;
    }

    public void addItem(T t, int position) {

        if (mDatas == null)
            initData();
        if (position < 0 || position > mDatas.size()) return;
        if (t != null) {
            mDatas.add(position, t);
            if (mAdapter != null)
                mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 更改item
     *
     * @param t
     */
    public void changeItem(T t) {
        if (mDatas == null)
            initData();
        int findKey = mDatas.indexOf(t);
        if (findKey >= 0 && findKey + 1 < mDatas.size()) {
            mDatas.set(findKey, t);
            if (mAdapter != null)
                mAdapter.notifyItemChanged(findKey);
        }

    }

    /**
     * 更改item
     *
     * @param t
     */
    public void changeItem(int pos, T t) {
        if (mDatas == null)
            initData();
        if (pos >= 0 && pos + 1 < mDatas.size()) {
            mDatas.set(pos, t);
            if (mAdapter != null)
                mAdapter.notifyItemChanged(pos);
        }
    }

    @SuppressWarnings("unchecked")
    public <R> R getItem(int pos) {
        if (mDatas == null)
            initData();
        if (pos >= 0 && pos < mDatas.size()) {
            return (R) mDatas.get(pos);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <R, A> R getItem(Func2<Object, A, Boolean> equalFun2, A a) {
        if (mDatas == null)
            initData();
        for (Object obj : mDatas) {
            if (equalFun2.call(obj, a))
                return (R) obj;
        }
        return null;
    }

    /**
     * 删除单个Item
     *
     * @param t
     */
    public void deleteItem(T t) {
        if (mDatas == null)
            initData();

        if (mAdapter == null)
            return;

        mAdapter.notifyItemRemoved(mDatas.indexOf(t));
        boolean isRemoved = mDatas.remove(t);

        if (isRemoved) {
            int pos = mDatas.indexOf(t);
            if (mSelectedArray != null)
                mSelectedArray.put(pos, false);
        }
    }

    /**
     * 根据r找出集合匹配的数据删除之,并通知ui刷新
     */
    public <R> void deleteItem(Func2<Object, R, Boolean> equalsAction, R r) {
        if (mAdapter != null && mAdapter.getItemCount() <= 0) return;
        Object findObj = null;
        for (Object obj : getDatas()) {
            if (equalsAction.call(obj, r)) {
                findObj = obj;
                break;
            }
        }
        if (findObj != null) {
            //noinspection unchecked
            deleteItem((T) findObj);
        }
    }

    /**
     * 删除单个Item
     *
     * @param pos
     */
    public void deleteItem(int pos) {
        if (mDatas == null)
            initData();
        mDatas.remove(pos);
        //用复值直接替代删除，效率更高
        mSelectedArray.put(pos, false);
        mAdapter.notifyItemRemoved(pos);
        if (pos < mDatas.size())
            mAdapter.notifyItemChanged(pos);
    }

    @SuppressWarnings("unchecked")
    public <R> void addAllItem(List<R> items) {

        if (mDatas == null)
            initData();

        if (items == null || items.size() <= 0)
            return;

        int start = mDatas != null ? mDatas.size() : 0;
        mDatas.addAll((Collection<? extends T>) items);

        if (mAdapter != null) {
            if (start == 0)
                mAdapter.notifyDataSetChanged();
            mAdapter.notifyItemRangeInserted(start, items.size());
        }
    }

    @SuppressWarnings("unchecked")
    public void addAllItemFirst(List<? extends T> items) {
        if (items == null || items.size() <= 0)
            return;
        if (mDatas == null)
            initData();
        mDatas.addAll(0, items);
        if (mAdapter != null)
            mAdapter.notifyItemRangeInserted(0, items.size());
    }

    public boolean containsItem(List<T> items) {
        if (items == null || items.size() <= 0)
            return false;
        return !CollectionUtils.isEmpty(mDatas) && mDatas.containsAll(items);
    }

    @SuppressWarnings("unchecked")
    public <R> boolean containsItem(Func2<List<Object>, R, Boolean> equalAction, R r) {
        return equalAction != null && equalAction.call((List<Object>) getDatas(), r);
    }

    public void clear() {
        if (mDatas == null || mDatas.size() == 0) return;
        mDatas.clear();
    }

    @Override
    public void start() {
        mView.setPresenter(this);
    }

    public void attachRecyclerView(RecyclerView recyclerView) {
        mView.attachView(recyclerView);
        mAdapter = recyclerView.getAdapter();
    }

    /**
     * 判断当前item是否选中
     *
     * @param pos
     * @return
     */
    public boolean isSelcted(int pos) {
        boolean selected = false;
        if (mSelectedArray != null) {
            int keyIndex = mSelectedArray.indexOfKey(pos);
            if (keyIndex >= 0)
                selected = mSelectedArray.valueAt(keyIndex);
        }
        return selected;
    }

    public void setAllSelected() {
        if (!isSelctEnable) return;
        for (int i = 0; i < getCount(); i++)
            mSelectedArray.put(i, true);
    }

    public void setUnSelect(int keepPos) {
        if (mSelectedArray != null)
            for (int i = 0; i < mSelectedArray.size(); i++) {
                int key = mSelectedArray.keyAt(i);
                if (key != keepPos) {
                    boolean sel = mSelectedArray.get(key, false);
                    if (sel) {
                        setSelcted(key, false);
                    }
                }
            }
    }

    /**
     * 设置为选中状态
     *
     * @param pos
     */
    public boolean setSelcted(int pos, boolean isSelcted) {
        if (isSelctEnable) {
            mSelectedArray.put(pos, isSelcted);
            return mView.setSelcted(pos, isSelcted);
        }
        return false;
    }

    public void detachView() {
        mView.detachView();
        if (mSelectedArray != null) {
            mSelectedArray.clear();
            mSelectedArray = null;
        }
    }

}
