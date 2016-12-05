package hello.leilei.utils;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by liulei
 * DATE: 2016/12/5
 * TIME: 8:52
 */

public class StatusBarUtils {

    /**
     * 隐藏系统顶部标题栏
     */
    public static void hideStatusBar(Activity act) {

        /*WindowManager.LayoutParams attrs = baseAct.getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        baseAct.getWindow().setAttributes(attrs);
        baseAct.getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);*/

        Window window = act.getWindow();

        if (Build.VERSION.SDK_INT < 16) { //ye olde method
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else { // Jellybean and up, new hotness

            View decorView = window.getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(uiOptions);
            // Remember that you should never show the action bar if the
            // status bar is hidden, so hide that too if necessary.
            ActionBar actionBar = act.getActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }
    }

    public static void showStatusBar(Activity act) {
        Window window = act.getWindow();
        if (Build.VERSION.SDK_INT < 16) { //ye olde method
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else { // Jellybean and up, new hotness
            View decorView = window.getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            decorView.setSystemUiVisibility(uiOptions);

            ActionBar actionBar = act.getActionBar();
            if (actionBar != null)
                actionBar.show();

        }
    }
}
