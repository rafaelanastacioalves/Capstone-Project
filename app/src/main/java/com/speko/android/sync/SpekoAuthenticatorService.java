package com.speko.android.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by rafaelalves on 19/12/16.
 */
public class SpekoAuthenticatorService extends Service {
    private SpekoAuthenticator mAuthenticator;


    @Override
    public void onCreate() {
        mAuthenticator = new SpekoAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
