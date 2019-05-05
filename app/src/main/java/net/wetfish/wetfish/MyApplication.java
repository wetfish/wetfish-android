package net.wetfish.wetfish;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;

/**
 * Created by ${Michael} on 5/3/2019.
 */

@AcraCore(reportSenderFactoryClasses = LocalCrashSenderFactory.class,
        buildConfigClass = BuildConfig.class)

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ACRA.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}
