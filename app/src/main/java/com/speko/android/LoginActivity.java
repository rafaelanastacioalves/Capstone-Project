package com.speko.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.speko.android.data.UserComplete;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity implements ProfileFragment.OnFragmentInteractionListener {

    private static final int RC_SIGN_IN = 123;
    private final String LOG_TAG = getClass().getSimpleName();
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private ValueEventListener userEventListener;

    @BindView(R.id.progress_bar)
    ContentLoadingProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Fabric.with(this, new Crashlytics());
        Log.d(LOG_TAG,"onCreate");

        firebaseDatabase = FirebaseDatabase.getInstance();
        ButterKnife.bind(this);
        Log.i(LOG_TAG, "Setting Loading true");
        setLoading(true);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in

                newUserProcedure();

        } else {
            // not signed in

            callFirebaseLogin();
        }
    }

    private void setLoading(boolean active) {
        if(active){
            progressBar.show();
        }else {
            progressBar.hide();
        }
    }

    private void callFirebaseLogin() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.AppTheme_NoActionBar)
                        .setLogo(R.drawable.ic_speko_complete_full_screen)
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
                Log.w(LOG_TAG, "Result OK");

                newUserProcedure();


                return;
            }

            // Sign in canceled
            if (resultCode == RESULT_CANCELED) {
                //TODO Implement
//                showSnackbar(R.string.sign_in_cancelled);
                Log.w(LOG_TAG, "Result Cancelled");
                setResult(RESULT_CANCELED);

                finish();
            }



            // User is not signed in. Maybe just wait for the user to press
            // "sign in" again, or show a message.

            Log.w(LOG_TAG, "Não deu certo login: \n" +
                    "Result Code: " + resultCode);
        }
    }

    private void newUserProcedure() {
        final FirebaseUser authUser = auth.getCurrentUser();
        @SuppressWarnings("ConstantConditions") String uid = authUser.getUid();

        Log.d(LOG_TAG,"Querying possible reference to the user in database with uid: " + uid);

                userEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //if user doesn't exist

                        Log.d(LOG_TAG, "Snapshot: " + dataSnapshot.toString());
                        if (!dataSnapshot.exists() ){

                            Log.i(LOG_TAG, "Setting Loading false");
                            setLoading(false);


                            Log.d(LOG_TAG, "There is no user. Should create in database");

                            Bundle fragmentArguments = new Bundle();
                            fragmentArguments.putBoolean(
                                    ProfileFragment.BUNDLE_ARGUMENT_FIRST_TIME_ENABLED,true);
                            fragmentArguments.putBoolean(
                                    ProfileFragment.BUNDLE_ARGUMENT_IS_SYNCABLE,false);


                            Fragment newUserFragment = new ProfileFragment();
                            newUserFragment.setArguments(fragmentArguments);
                            FragmentTransaction transaction = getSupportFragmentManager().
                                    beginTransaction();
                            transaction.replace(R.id.login_fragment_container, newUserFragment);
                            transaction.commit();



                        }else{
                            // user exists

                            Log.i(LOG_TAG, "Setting Loading false");
                            setLoading(false);

                            Log.i(LOG_TAG, "There is the user!: " + dataSnapshot + "\n" +
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

                firebaseDatabase.getReference()
                        .child(getString(R.string.firebase_database_node_users))
                        .child(authUser.getUid())
                        .addListenerForSingleValueEvent(userEventListener);

        }

    @Override
    protected void onPause() {
        Log.i(LOG_TAG, "onPause");
        if(userEventListener != null){
            firebaseDatabase.getReference().child("users").removeEventListener(userEventListener);
        }
        setLoading(true);
        super.onPause();
    }

    @Override
    public void completeSignup(UserComplete userComplete) {
        Log.i(LOG_TAG,"completeSignup");
        //TODO Implement interaction with Activity

        final FirebaseUser authUser = auth.getCurrentUser();

        userComplete.setLearningCode(userComplete.getFluentLanguage()
                + "|"
                + userComplete.getLearningLanguage());

        //adding more Provider User info
        //noinspection ConstantConditions
        userComplete.setEmail(authUser.getEmail());
        userComplete.setId(authUser.getUid());

        OnCompleteListener onCompleteListener = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                final Context context = getApplicationContext();
                Toast.makeText(getBaseContext(),"Signed Up Successfully!",Toast.LENGTH_SHORT).show();

                setResult(RESULT_OK);
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
            }
        };
        Utility.setUserIntoFirebase(userComplete,this, onCompleteListener);
        setLoading(true);


    }
}
