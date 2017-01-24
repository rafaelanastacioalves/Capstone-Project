package com.speko.android.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by rafaelalves on 14/12/16.
 */
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
}
