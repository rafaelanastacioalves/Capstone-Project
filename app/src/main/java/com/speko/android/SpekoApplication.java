package com.speko.android;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import io.fabric.sdk.android.Fabric;

/**
 * Created by rafaelalves on 04/02/17.
 */

@SuppressWarnings("ALL")
public class SpekoApplication extends android.app.Application {

    @Override
    public void onCreate() {


        //supposing its first usage is here. Must be first use!
        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
        super.onCreate();
        Fabric.with(this, new Crashlytics());
    }
}
