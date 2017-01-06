package com.speko.android.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
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
    private static FirebaseAuth mFirebaseAuth;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private static String userToken;


    public SpekoSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.i(LOG_TAG, "Constructor Called");

    }

    public SpekoSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        Log.i(LOG_TAG, "Constructor Called");

    }

    public static void setUserToken(String userToken) {
        SpekoSyncAdapter.userToken = userToken;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync");

            if (userToken !=null){
                getUser(userToken);

            }else{
                Log.w(LOG_TAG, "userToken not setted!");
            }



    }

    public static void getUser(String idToken){
        // Fetch and print a list of the contributors to this library.
        FirebaseClient client = ServiceGenerator.createService(FirebaseClient.class);
        Call<User> call = client.getUser("-K_0dp55MnCf5edl2J8M", idToken);

        try {
            Log.i("SpekoSyncAdapter", "getUser: \n");
            User user = call.execute().body();
            Log.i("SpekoSyncAdapter", "Deu certo!: \n" + user.getName());

        }catch (IOException e) {
            Log.e("SpekoSyncAdapter", "Deu ruim: \n" + e.getMessage());
            // handle errors
        }

    }

    public static void initializeSyncAdapter(){

        Log.d("SpekoSyncAdapter", "initializeSyncAdapter");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();


    }
}
