package com.speko.android.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.speko.android.data.User;
import com.speko.android.retrofit.FirebaseClient;
import com.speko.android.retrofit.ServiceGenerator;

import java.io.IOException;

import retrofit2.Call;


/**
 * Created by rafaelalves on 14/12/16.
 */
public class SpekoSyncAdapter extends AbstractThreadedSyncAdapter {
    private static FirebaseDatabase mFirebaseDatabase;
    private final String LOG_TAG = this.getClass().getSimpleName();


    public SpekoSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.i(LOG_TAG, "Constructor Called");

    }

    public SpekoSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        Log.i(LOG_TAG, "Constructor Called");

    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync");

        // Create a very simple REST adapter which points the GitHub API endpoint.
        FirebaseClient client = ServiceGenerator.createService(FirebaseClient.class);

        // Fetch and print a list of the contributors to this library.
        Call<User> call = client.getUser("-K_0dp55MnCf5edl2J8M");

        try {
            User user = call.execute().body();
            Log.i(LOG_TAG, "Deu certo!: \n" + user.getName());

        } catch (IOException e) {
            Log.e(LOG_TAG, "Deu ruim: \n" + e.getMessage());
            // handle errors
        }


    }

    public static void initializeSyncAdapter(){

        Log.d("SpekoSyncAdapter", "initializeSyncAdapter");
        mFirebaseDatabase = FirebaseDatabase.getInstance();

    }
}
