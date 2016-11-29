package hello.leilei.ui;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * USER: liulei
 * DATE: 2015/6/8
 * TIME: 13:42
 */
public class UIhandler<T> extends Handler {

    private WeakReference<T> weakReference;

    public UIhandler(T t) {
        super(Looper.getMainLooper());
        weakReference = new WeakReference<T>(t);
    }

    public void clear() {
        if (weakReference != null)
            weakReference.clear();
    }

    @Override
    public void handleMessage(Message msg) {
        int what = msg.what;
    }

    /**
     * 删除指定what的message
     */
    public void deleteMessages() {
        this.removeCallbacksAndMessages(null);
    }

    public T getItem() {
        if (weakReference != null)
            return weakReference.get();
        else return null;
    }

}
