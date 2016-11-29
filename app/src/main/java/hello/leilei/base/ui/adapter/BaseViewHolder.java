package hello.leilei.base.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import java.nio.charset.CharsetDecoder;

import butterknife.ButterKnife;

/**
 * Created by liulei on 16-5-13.
 * TIME : 上午10:55
 * COMMECTS : recyclerView通用viewHolder
 * 封装了一些常用设置操作，比如设置字体，文字颜色，图片等等
 */
public class BaseViewHolder extends RecyclerView.ViewHolder {

    private SparseArray<View> mViews;

    public BaseViewHolder(View itemView) {
        super(itemView);
        mViews = new SparseArray<>();
    }

    /**
     * 通过viewId获取控件
     *
     * @param viewId
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = ButterKnife.findById(itemView, viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 设置TextView的值
     */
    public BaseViewHolder setText(int viewId, CharSequence text) {
        TextView tv = getView(viewId);
        if (tv != null)
            tv.setText(text);
        return this;
    }

    /**
     * 设置TextView的值
     */
    public BaseViewHolder setText(int viewId, int textResId) {
        TextView tv = getView(viewId);
        if (tv != null)
            tv.setText(textResId);
        return this;
    }

    /**
     * 设置TextView的hint值
     */
    public BaseViewHolder setHint(@IdRes int viewId, @StringRes int textResId) {
        TextView tv = getView(viewId);
        if (tv != null)
            tv.setHint(textResId);
        return this;
    }

    public BaseViewHolder setMinimumHeight(int viewId, int height) {
        View v = getView(viewId);
        if (v != null) {
            v.setMinimumHeight(height);
        }
        return this;
    }


    public BaseViewHolder setImageResource(int viewId, int resId) {
        ImageView view = getView(viewId);
        if (view != null)
            view.setImageResource(resId);
        return this;
    }

    public BaseViewHolder setImageBitmap(int viewId, Bitmap bitmap) {
        ImageView view = getView(viewId);
        view.setImageBitmap(bitmap);
        return this;
    }

    public BaseViewHolder setImageDrawable(int viewId, Drawable drawable) {
        ImageView view = getView(viewId);
        view.setImageDrawable(drawable);
        return this;
    }

    public BaseViewHolder setBackgroundColor(int viewId, int color) {
        View view = getView(viewId);
        view.setBackgroundColor(color);
        return this;
    }

    public BaseViewHolder setBackgroundRes(int viewId, int backgroundRes) {
        View view = getView(viewId);
        view.setBackgroundResource(backgroundRes);
        return this;
    }

    public BaseViewHolder setTextColor(int viewId, int textColor) {
        TextView view = getView(viewId);
        view.setTextColor(textColor);
        return this;
    }

    public BaseViewHolder setTextColorRes(int viewId, @ColorRes int textColorRes) {
        TextView view = getView(viewId);
        Context mContext = itemView.getContext();
        int color = ContextCompat.getColor(mContext, textColorRes);
        view.setTextColor(color);
        return this;
    }

    @SuppressLint("NewApi")
    public BaseViewHolder setAlpha(int viewId, float value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getView(viewId).setAlpha(value);
        } else {
            // Pre-honeycomb hack to set Alpha value
            AlphaAnimation alpha = new AlphaAnimation(value, value);
            alpha.setDuration(0);
            alpha.setFillAfter(true);
            getView(viewId).startAnimation(alpha);
        }
        return this;
    }

    public BaseViewHolder setVisible(int viewId, boolean visible) {
        View view = getView(viewId);
        if (view != null)
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }

    /**
     * @param viewId
     * @param visible 1 VISIBLE  2 INVISIBLE 3 GONE
     * @return
     */
    public BaseViewHolder setVisible(int viewId, int visible) {
        View view = getView(viewId);
        if (view != null) {
            switch (visible) {
                case 1:
                    view.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    view.setVisibility(View.INVISIBLE);
                    break;
                case 3:
                    view.setVisibility(View.GONE);
                    break;
            }
        }

        return this;
    }

    public BaseViewHolder linkify(int viewId) {
        TextView view = getView(viewId);
        Linkify.addLinks(view, Linkify.ALL);
        return this;
    }

    public BaseViewHolder setTypeface(Typeface typeface, int... viewIds) {
        for (int viewId : viewIds) {
            TextView view = getView(viewId);
            view.setTypeface(typeface);
            view.setPaintFlags(view.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        }
        return this;
    }

    public BaseViewHolder setProgress(int viewId, int progress) {
        ProgressBar view = getView(viewId);
        view.setProgress(progress);
        return this;
    }

    public BaseViewHolder setProgress(int viewId, int progress, int max) {
        ProgressBar view = getView(viewId);
        view.setMax(max);
        view.setProgress(progress);
        return this;
    }

    public BaseViewHolder setMax(int viewId, int max) {
        ProgressBar view = getView(viewId);
        view.setMax(max);
        return this;
    }

    public BaseViewHolder setRating(int viewId, float rating) {
        RatingBar view = getView(viewId);
        view.setRating(rating);
        return this;
    }

    public BaseViewHolder setRating(int viewId, float rating, int max) {
        RatingBar view = getView(viewId);
        view.setMax(max);
        view.setRating(rating);
        return this;
    }

    public BaseViewHolder setTag(int viewId, Object tag) {
        View view = getView(viewId);
        view.setTag(tag);
        return this;
    }

    public BaseViewHolder setTag(int viewId, int key, Object tag) {
        View view = getView(viewId);
        view.setTag(key, tag);
        return this;
    }

    public BaseViewHolder setChecked(int viewId, boolean checked) {
        Checkable view = (Checkable) getView(viewId);
        view.setChecked(checked);
        return this;
    }

    public BaseViewHolder setTag(Object tag) {
        itemView.setTag(tag);
        return this;
    }

    public BaseViewHolder setOnClickListener(View.OnClickListener listener) {
        itemView.setClickable(true);
        itemView.setOnClickListener(listener);
        return this;
    }

    /**
     * 关于事件的
     */
    public BaseViewHolder setOnClickListener(int viewId,
                                             View.OnClickListener listener) {
        View view = getView(viewId);
        if (view != null)
            view.setOnClickListener(listener);
        return this;
    }

    public BaseViewHolder setOnCheckedChangeListener(int viewId,
                                                     CompoundButton.OnCheckedChangeListener listener) {
        View view = getView(viewId);
        if (view != null && view instanceof CheckBox)
            ((CheckBox) view).setOnCheckedChangeListener(listener);
        return this;
    }

    public BaseViewHolder setOnTouchListener(int viewId,
                                             View.OnTouchListener listener) {
        View view = getView(viewId);
        view.setOnTouchListener(listener);
        return this;
    }

    public BaseViewHolder setOnLongClickListener(int viewId,
                                                 View.OnLongClickListener listener) {
        View view = getView(viewId);
        view.setOnLongClickListener(listener);
        return this;
    }

    public CharSequence getText(int viewId) {
        View view = getView(viewId);
        if (view instanceof TextView)
            return ((TextView) view).getText();
        return null;
    }

}
