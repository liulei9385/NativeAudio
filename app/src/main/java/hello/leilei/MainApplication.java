package hello.leilei;

import android.app.Application;
import timber.log.Timber;

/**
 * Created by liulei on 2016/11/29.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } /*else {
            Timber.plant(new CrashReportingTree());
        }*/
    }
}
