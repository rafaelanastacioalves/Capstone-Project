package com.speko.android.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by rafaelalves on 19/12/16.
 */
@SuppressWarnings("ALL")
public class SpekoAuthenticatorService extends Service {
    private SpekoAuthenticator mAuthenticator;


    @Override
    public void onCreate() {
        Log.d("AuthenticatorService", "onCreate");
        mAuthenticator = new SpekoAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("AuthenticatorService", "onBind");
        return mAuthenticator.getIBinder();
    }
}
