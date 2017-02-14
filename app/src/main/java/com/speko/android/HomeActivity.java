package com.speko.android;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.FirebaseDatabase;
import com.speko.android.data.User;
import com.speko.android.data.generated.UsersDatabase;
import com.speko.android.sync.SpekoSyncAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

public class HomeActivity extends AppCompatActivity implements ProfileFragment.OnFragmentInteractionListener  {

    // Constants



    private static final int RC_SIGN_IN = 1;
    private static final String SELECTED_ITEM = "arg_selected_item";

    private final String LOG_TAG = getClass().getSimpleName();
    // Instance fields
    Account mAccount;
    private ContentResolver mResolver;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static UsersDatabase userDB;

    @BindView(R.id.bottom_view_layout_home_activity)
    BottomNavigationView mBottomNavigationView;
    private int mSelectedItem;
    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userDB = UsersDatabase.getInstance(getApplicationContext());
        userDB.onCreate(userDB.getReadableDatabase());

        Fabric.with(this, new Crashlytics());
        Log.d("HomeActivity", "onCreate");

        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);


        // Get the content resolver for your app
        mResolver = getContentResolver();

        mFirebaseAuth = FirebaseAuth.getInstance();

        userNotLoggedcheck();

//        setFireBaseToken();



        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            public final String LOG_TAG = getClass().getSimpleName();

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.i(LOG_TAG, "onAuthStateChanged");
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.i(LOG_TAG, "user logged in");

                    // User is signed in
                    //TODO implement this

                    setFireBaseToken();



                } else {
                    Log.i(LOG_TAG, "user logged out");

                    // User is signed out
                    //TODO implement this
                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(i);
                }
            }
        };



        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectFragment(item);
                return true;
            }
        });

        MenuItem selectedItem;
        if (savedInstanceState != null) {
            mSelectedItem = savedInstanceState.getInt(SELECTED_ITEM, 0);
            selectedItem = mBottomNavigationView.getMenu().findItem(mSelectedItem);
        } else {
            selectedItem = mBottomNavigationView.getMenu().getItem(0);
        }
        selectFragment(selectedItem);


    }


    private void selectFragment(MenuItem item) {
        Fragment frag = null;
        // init corresponding fragment
        switch (item.getItemId()) {
            case R.id.action_search:
                Log.i(LOG_TAG, "Selecting HomeActivityFragment");

                frag = new HomeActivityFragment();

                break;
            case R.id.action_profile:
                Log.i(LOG_TAG, "Selecting Profile");

                frag = new ProfileFragment();
                break;
            case R.id.action_conversations:
                Log.i(LOG_TAG, "Selecting Conversations");
                frag = ConversationsFragment.newInstance(Utility.getUser(this).getId());
                break;
        }

        // update selected item
        mSelectedItem = item.getItemId();

        // uncheck the other items.
        for (int i = 0; i< mBottomNavigationView.getMenu().size(); i++) {
            MenuItem menuItem = mBottomNavigationView.getMenu().getItem(i);
            menuItem.setChecked(menuItem.getItemId() == item.getItemId());
        }


        if (frag != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace (R.id.home_activity_fragment_container, frag, frag.getTag());
            ft.commit();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_ITEM, mSelectedItem);
        super.onSaveInstanceState(outState);
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


    @Override
    public void onFragmentInteraction(User user) {

        final FirebaseUser authUser = mFirebaseAuth.getCurrentUser();

        user.setLearningCode(user.getFluentLanguage()
                + "|"
                + user.getLearningLanguage());

        //adding more Provider User info
        user.setName(authUser.getDisplayName());
        user.setEmail(authUser.getEmail());
        user.setId(authUser.getUid());
        Utility.setUser(user,this);


        Toast.makeText(this, "ProfileUpdated!", Toast.LENGTH_SHORT).show();


    }
}
