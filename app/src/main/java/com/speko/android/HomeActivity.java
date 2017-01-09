package com.speko.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.speko.android.sync.SpekoSyncAdapter;

public class HomeActivity extends AppCompatActivity  {

    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.speko.android.data";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "android.speko.com";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    private static final int RC_SIGN_IN = 1;
    // Instance fields
    Account mAccount;
    private ContentResolver mResolver;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("HomeActibvity", "onCreate");

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Get the content resolver for your app
        mResolver = getContentResolver();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFirebaseAuth = FirebaseAuth.getInstance();
        setFireBaseToken();



        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            public final String LOG_TAG = getClass().getSimpleName();

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.i(LOG_TAG, "onAuthStateChanged");
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    //TODO implement this
//                    onSignedInInitialize(user.getDisplayName());

                    setFireBaseToken();



                } else {
                    // User is signed out
                    //TODO implement this
//                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(
                                            AuthUI.FACEBOOK_PROVIDER,
                                            AuthUI.GOOGLE_PROVIDER)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };






    }

    private void setFireBaseToken() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        user.getToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            public final String LOG_TAG = getClass().getSimpleName();

            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    String userToken = task.getResult().getToken();
                    Log.i(LOG_TAG, "O token Deu certo! \n");
                    Log.i(LOG_TAG, "O ID do usuário é: \n" + mFirebaseAuth.getCurrentUser().getUid());
                    SpekoSyncAdapter.setUserToken(userToken);
                    mAccount = CreateSyncAccount(getApplicationContext());
                    SpekoSyncAdapter.initializeSyncAdapter();


                } else {
                    Log.e(LOG_TAG, task.getException().getMessage());
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }

    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(newAccount, AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(newAccount, AUTHORITY, true);


        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            Log.w("HomeActivity", "Deu ruim com o Account");
        }

        return newAccount;

    }


}
