package com.speko.android.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.speko.android.data.UserEntity;


/**
 * Created by rafaelalves on 14/12/16.
 */
public class SpekoSyncAdapter extends AbstractThreadedSyncAdapter {
    private static FirebaseDatabase mFirebaseDatabase;
    private static DatabaseReference mMessagesDatabaseReference;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private ChildEventListener mChildEventListener;


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
        addUser();
        attachDatabaseReadListener();
    }

    private void addUser() {
        UserEntity userEntity = new UserEntity("Rafael");
        mMessagesDatabaseReference.push().setValue(userEntity);
    }

    private void attachDatabaseReadListener() {
        Log.d(LOG_TAG, "attachDatabaseReadListener");

        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                private String LOG_TAG = getClass().getSimpleName();
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d(LOG_TAG, "onChildAdded");

                    UserEntity userEntity = dataSnapshot.getValue(UserEntity.class);
                }


                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                public void onCancelled(DatabaseError databaseError) {}
            };
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    public static void initializeSyncAdapter(){

        Log.d("SpekoSyncAdapter", "initializeSyncAdapter");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("users");


    }
}
