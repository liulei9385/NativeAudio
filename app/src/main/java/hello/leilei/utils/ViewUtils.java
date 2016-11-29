package hello.leilei.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * USER: LEILEI
 * DATE: 2015/6/8
 * TIME: 12:09
 */
@SuppressWarnings("unchecked")
public class ViewUtils {

    private ViewUtils() {
    }

    public static <T extends View> T findView(Activity act, int resId) {
        return (T) act.findViewById(resId);
    }

    public static <T extends View> T findView(View view, int resId) {
        return (T) view.findViewById(resId);
    }

    /**
     * 转化dip为px
     *
     * @param context
     * @param value
     * @return
     */
    public static float dpToPx(Context context, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getScreenMetric(context));
    }

    /**
     * 获取屏幕相关的参数
     *
     * @param context
     * @return
     */
    private static DisplayMetrics getScreenMetric(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    /**
     * 获取屏幕尺寸
     *
     * @param context
     * @return
     */
    public static int[] getScreenSize(Context context) {
        DisplayMetrics metrics = getScreenMetric(context);
        return new int[]{metrics.widthPixels, metrics.heightPixels};
    }

    public static int getStatusBarHeight(Context context) {
        int statusHeight = 0;
        try {
            Resources resources = context.getResources();
            int id = resources.getIdentifier("status_bar_height", "dimen", "android");
            if (id > 0)
                statusHeight = context.getResources().getDimensionPixelSize(id);
        } catch (Exception ignored) {
        }
        return statusHeight;
    }

    /**
     * 是否运行在主线程
     *
     * @return
     */
    public static boolean isRuninMain() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    /**
     * 批量设置OnClickListener
     *
     * @param listener
     * @param views
     */
    public static void setOnClickListener(View.OnClickListener listener, View... views) {
        for (View view : views) {
            if (view != null)
                view.setOnClickListener(listener);
        }
    }

    /**
     * 如果此view已被添加到viewHireacy中去，则删除之
     *
     * @param destView
     */
    public static void fillViewHasParent(View destView) {
        ViewGroup parent = (ViewGroup) destView.getParent();
        if (parent != null)
            parent.removeView(destView);
    }

    /**
     * 计算view的高度在layout之前
     *
     * @param sourceView
     * @return
     */
    public static boolean measureView(View sourceView) {
        try {
            sourceView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        } catch (Exception id) {
            return false;
        }
        return true;
    }

    /**
     * 显示toast消息
     *
     * @param context context
     * @param text    text
     * @param isLong  islong
     */
    public static void showToast(final Context context, final CharSequence text, boolean isLong) {
        if (context == null)
            return;
        int duration = Toast.LENGTH_LONG;
        if (!isLong)
            duration = Toast.LENGTH_SHORT;

        if (isRuninMain())
            Toast.makeText(context.getApplicationContext(),
                    text, duration).show();
        else {
            final int showDuration = duration;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @SuppressWarnings("ResourceType")
                @Override
                public void run() {
                    Toast.makeText(context
                            .getApplicationContext(), text, showDuration).show();
                }
            });
        }
    }

    /**
     * 显示toast消息
     *
     * @param context context
     * @param text    text
     * @param isLong  islong
     */
    public static void showToast(final Context context, final @StringRes int textResId, final boolean isLong) {
        if (context == null ||
                textResId <= 0)
            return;
        final String text = context.getString(textResId);

        if (!isRuninMain()) {
            new Handler(Looper.getMainLooper())
                    .post(new Runnable() {
                        @Override
                        public void run() {
                            showToast(context.getApplicationContext(), text, isLong);
                        }
                    });
        } else
            showToast(context.getApplicationContext(), text, isLong);
    }

    /**
     * 当前view是否显示
     *
     * @param view
     * @return
     */
    public static boolean isViewShown(View view) {
        return view != null &&
                view.getVisibility() == View.VISIBLE;
    }

    /**
     * 关闭输入法
     *
     * @param context     上下文
     * @param requestView View.getWindowToken()
     */
    public static void hideSofoInputMethod(Context context, View requestView) {
        if (requestView == null)
            return;
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isActive()) {
                inputMethodManager.hideSoftInputFromWindow(requestView.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception ignored) {
        }
    }

}
