package com.speko.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;



public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private final String LOG_TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Tela de Login - on create");
//        Fabric.with(this, new Crashlytics());
//
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        if (auth.getCurrentUser() != null) {
//            // already signed in
//            startActivity(new Intent(this, HomeActivity.class));
//
//
//        } else {
//            // not signed in
//            startActivityForResult(
//                    AuthUI.getInstance()
//                            .createSignInIntentBuilder()
//                            .setIsSmartLockEnabled(!BuildConfig.DEBUG)
//                            .setProviders(Arrays.asList(
//                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
//                                    new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
//                                    .build(),
//                    RC_SIGN_IN);
//        }
    }






}
