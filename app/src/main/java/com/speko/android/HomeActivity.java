package com.speko.android;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.speko.android.data.generated.UsersDatabase;
import com.speko.android.sync.SpekoSyncAdapter;

import io.fabric.sdk.android.Fabric;

public class HomeActivity extends AppCompatActivity  {

    // Constants



    private static final int RC_SIGN_IN = 1;

    private final String LOG_TAG = getClass().getSimpleName();
    // Instance fields
    Account mAccount;
    private ContentResolver mResolver;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static UsersDatabase userDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userDB = UsersDatabase.getInstance(getApplicationContext());
        userDB.onCreate(userDB.getReadableDatabase());

        Fabric.with(this, new Crashlytics());
        Log.d("HomeActivity", "onCreate");

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the content resolver for your app
        mResolver = getContentResolver();

        mFirebaseAuth = FirebaseAuth.getInstance();

        userNotLoggedcheck();

        Fragment homeActivityFragment = new HomeActivityFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_activity_fragment_container, homeActivityFragment);
        transaction.commit();
//        setFireBaseToken();



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
                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(i);
                }
            }
        };






    }

    @Override
    protected void onStart() {
        Log.i(LOG_TAG, "onStart");

        super.onStart();
    }

    private void userNotLoggedcheck() {

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user == null) {
            Log.i(LOG_TAG, "User Not Logged, calling LoginActivity");
            // User is signed in

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }

        }
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
                    SpekoSyncAdapter.initializeSyncAdapter(getApplication());


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




}
