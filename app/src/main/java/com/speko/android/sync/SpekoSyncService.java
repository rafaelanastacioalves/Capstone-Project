package com.speko.android.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.speko.android.Utility;

/**
 * Created by rafaelalves on 14/12/16.
 */
@SuppressWarnings("ALL")
public class SpekoSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static SpekoSyncAdapter sSpekoSyncAdapter = null;


    @Override
    public void onCreate() {

        Log.d("SpekoSyncService", "onCreate - SpekoSyncService");
        synchronized (sSyncAdapterLock) {
            if (sSpekoSyncAdapter == null) {
                sSpekoSyncAdapter = new SpekoSyncAdapter(getApplicationContext(), true);
            }
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        return sSpekoSyncAdapter.getSyncAdapterBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(SpekoSyncAdapter.ACTION_SIGNOUT)) {
            signOut();

        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void signOut() {
        clearAccount();
        Utility.deleteEverything(this);
        FirebaseAuth.getInstance().signOut();
        Utility.resetPreferences(this);


    }


    private void clearAccount() {
        sSpekoSyncAdapter.clearAccount(getApplicationContext());

    }
}
