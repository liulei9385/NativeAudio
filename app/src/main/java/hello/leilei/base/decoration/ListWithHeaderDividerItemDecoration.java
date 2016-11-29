package hello.leilei.base.decoration;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by liulei on 16-5-16.
 * TIME : 下午5:08
 * recyclerView　see{@link LinearLayoutManager} 自定义的分割线
 * 跟listView默认的一致
 */
public class ListWithHeaderDividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private Drawable mDivider;

    private int mOrientation;

    public ListWithHeaderDividerItemDecoration(Context context, int orientation) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
        setOrientation(orientation);
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        final Rect mRect = new Rect();
        for (int i = 1; i + 1 < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();
            mRect.set(left, top, right, bottom);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                drawCanvas(c, mRect);
            } else {
                mDivider.setBounds(mRect);
                mDivider.draw(c);
            }
        }
    }

    void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final Rect mRect = new Rect();
        final int childCount = parent.getChildCount();
        for (int i = 1; i + 1 < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDivider.getIntrinsicHeight();
            mRect.set(left, top, right, bottom);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                drawCanvas(c, mRect);
            } else {
                mDivider.setBounds(mRect);
                mDivider.draw(c);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void drawCanvas(Canvas c, Rect mRect) {
        c.save();
        c.clipRect(mRect);
        mDivider.setBounds(mRect);
        mDivider.draw(c);
        c.restore();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        } else {
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
        }
    }
}