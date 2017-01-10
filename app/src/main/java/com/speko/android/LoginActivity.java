package com.speko.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ui.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity implements FillNewUserDataFragment.OnFragmentInteractionListener {

    private static final int RC_SIGN_IN = 123;
    private final String LOG_TAG = getClass().getSimpleName();
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private ValueEventListener userEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Fabric.with(this, new Crashlytics());
        Log.d(LOG_TAG,"onCreate");
        firebaseDatabase = FirebaseDatabase.getInstance();

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in

                newUserProcedure();

        } else {
            // not signed in
            callFirebaseLogin();
        }
    }

    private void callFirebaseLogin() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                        .setProviders(Arrays.asList(
                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                                .build(),
                RC_SIGN_IN);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // user is signed in!
                newUserProcedure();


                return;
            }

            // Sign in canceled
            if (resultCode == RESULT_CANCELED) {
                //TODO Implement
//                showSnackbar(R.string.sign_in_cancelled);

                setResult(RESULT_CANCELED);

                finish();
            }

            // No network
            if (resultCode == ResultCodes.RESULT_NO_NETWORK) {
                //TODO Implement
//                showSnackbar(R.string.no_internet_connection);
                return;
            }

            // User is not signed in. Maybe just wait for the user to press
            // "sign in" again, or show a message.
        }
    }

    private void newUserProcedure() {
        final FirebaseUser authUser = auth.getCurrentUser();
        String uid = authUser.getUid();
        Log.d(LOG_TAG,"Querying possible reference to the user in database with uid: " + uid);

                userEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //if user doesn't exist

                        Log.d(LOG_TAG, "Snapshot: " + dataSnapshot.toString());
                        if (!dataSnapshot.hasChild(authUser.getUid()) ){


                            FirebaseUser authUser = auth.getCurrentUser();
                            Log.d(LOG_TAG, "There is no user. Should create in database");

                            Fragment newUserFragment = new FillNewUserDataFragment();
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.login_fragment_container, newUserFragment);
                            transaction.commit();


//
//                            firebaseDatabase
//                                    .getReference()
//                                    .child(getString(R.string.firebase_database_node_users))
//                                    .child(authUser.getUid())
//                                    .setValue(new User(authUser.getDisplayName()));

                        }else{
                            // user exists
                            DataSnapshot childSnapshot = dataSnapshot.child(authUser.getUid());
                            Log.d(LOG_TAG, "There is the user!: " + childSnapshot + "\n" +
                                    "should go to main activity");
                            setResult(RESULT_OK);
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            finish();


                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };

                firebaseDatabase.getReference().child(getString(R.string.firebase_database_node_users)).addValueEventListener(userEventListener);

        }

    @Override
    protected void onPause() {
        if(userEventListener != null){
            firebaseDatabase.getReference().child("users").removeEventListener(userEventListener);
        }
        super.onPause();
    }

    @Override
    public void onFragmentInteraction(Context context) {
        //TODO Implement interaction with Activity
    }
}
