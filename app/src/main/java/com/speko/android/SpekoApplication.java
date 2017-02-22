package com.speko.android;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

/**
 * Created by rafaelalves on 04/02/17.
 */

public class SpekoApplication extends android.app.Application {

    @Override
    public void onCreate() {


        //supposing its first usage is here. Must be first use!
        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
        super.onCreate();
    }
}
