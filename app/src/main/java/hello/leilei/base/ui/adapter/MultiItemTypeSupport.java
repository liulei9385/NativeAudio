package hello.leilei.base.ui.adapter;

import android.support.annotation.LayoutRes;

/**
 * Created by liulei on 16-5-13.
 * TIME : 下午2:52
 * COMMECTS : item类型可选的adapter封装接口
 */
public interface MultiItemTypeSupport<T> {
    @LayoutRes
    int getLayoutId(int itemType);

    int getItemViewType(int position, T t);
}
