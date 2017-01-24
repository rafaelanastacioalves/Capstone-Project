package com.speko.android.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.speko.android.data.User;
import com.speko.android.data.UserColumns;
import com.speko.android.data.UsersProvider;
import com.speko.android.retrofit.AccessToken;
import com.speko.android.retrofit.FirebaseClient;
import com.speko.android.retrofit.ServiceGenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

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
                User user = getUser(userToken);
                persistUser(user);

                HashMap<String, User> userFriends = getFriends(userToken);
                persistFriends(userFriends.values().toArray(new User[userFriends.size()]));


            }else{
                Log.w(LOG_TAG, "userToken not setted!");
            }



    }

    private void persistFriends(User[] userFriends) {

        Vector<ContentValues> cVVector = new Vector<ContentValues>(userFriends.length);
        for (User user:
             userFriends) {
            ContentValues userCV = new ContentValues();
            userCV.put(UserColumns.FIREBASE_ID,user.getId());
            userCV.put(UserColumns.NAME, user.getName());
            userCV.put(UserColumns.EMAIL, user.getEmail());
            userCV.put(UserColumns.FLUENT_LANGUAGE, user.getFluentLanguage());
            userCV.put(UserColumns.FRIEND_OF, mFirebaseAuth.getCurrentUser().getUid());


            cVVector.add(userCV);
        }

        ContentValues[] cVArray = new ContentValues[cVVector.size()];

        cVVector.toArray(cVArray);
        getContext().getContentResolver().bulkInsert(UsersProvider.Users.USER_URI,cVArray);
    }

    private HashMap<String, User> getFriends(String idToken) {

        // Fetch and print a list of the contributors to this library.
        FirebaseClient client = ServiceGenerator.createService(FirebaseClient.class, new AccessToken(
                "Bearer",
                idToken)
        );
        Call<HashMap<String,User>> call = client.getUserFriends(mFirebaseAuth.getCurrentUser().getUid(), idToken);

        try {
            Log.i("SpekoSyncAdapter", "getFriends: \n");
            HashMap<String,User> user = call.execute().body();
            Log.i("SpekoSyncAdapter", "Deu certo!: \n" + user.toString());

            return user;
        }catch (IOException e) {
            Log.e("SpekoSyncAdapter", "Deu ruim: \n" + e.getMessage());
            // handle errors
        }

        return null;





    }

    private void persistUser(User user) {
        ContentValues userCV = new ContentValues();
        userCV.put(UserColumns.FIREBASE_ID,user.getId());
        userCV.put(UserColumns.NAME, user.getName());
        userCV.put(UserColumns.EMAIL, user.getEmail());
        userCV.put(UserColumns.FLUENT_LANGUAGE, user.getFluentLanguage());
        getContext().getContentResolver().insert(UsersProvider.Users.USER_URI, userCV);
    }

    public static User getUser(String idToken){
        // Fetch and print a list of the contributors to this library.
        FirebaseClient client = ServiceGenerator.createService(FirebaseClient.class, new AccessToken(
                "Bearer",
                idToken)
        );
        Call<User> call = client.getUser(mFirebaseAuth.getCurrentUser().getUid(), idToken);

        try {
            Log.i("SpekoSyncAdapter", "getUser: \n");
            User user = call.execute().body();
            Log.i("SpekoSyncAdapter", "Deu certo!: \n" + user.toString());

            return user;
        }catch (IOException e) {
            Log.e("SpekoSyncAdapter", "Deu ruim: \n" + e.getMessage());
            // handle errors
        }

        return null;




    }

    public static void initializeSyncAdapter(){

        Log.d("SpekoSyncAdapter", "initializeSyncAdapter");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();


    }
}
