package hello.leilei.utils;

import android.support.annotation.NonNull;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

/**
 * Created by liulei on 2016/11/29.
 */
public class RxUiUtils {
    /**
     * 延时一定的时间
     *
     * @param mAction0
     * @param delayMilises
     */
    public static void postDelayedOnBg(long delayMilises, Action0 mAction0) {
        Observable.just(null)
                .delay(delayMilises, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(obj -> {
                    if (mAction0 != null) mAction0.call();
                }, Throwable::printStackTrace);
    }

    /**
     * 延时一定的时间
     *
     * @param mAction0
     * @param delayMilises
     */
    public static Subscription postDelayedRxOnMain(long delayMilises, @NonNull Action0 mAction0) {
        return Observable.empty()
                .delay(delayMilises, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(mAction0)
                .subscribe(obj -> {
                }, Throwable::printStackTrace);
    }


    public static void unsubscribe(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }

    public static <T> Observable.Transformer<T, T> applySchedulers() {
        return tObservable -> tObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
