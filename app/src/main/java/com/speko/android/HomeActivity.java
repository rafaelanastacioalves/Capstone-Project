package com.speko.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.speko.android.data.UserComplete;
import com.speko.android.data.generated.UsersDatabase;
import com.speko.android.sync.SpekoSyncAdapter;
import com.speko.android.sync.SpekoSyncService;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

/**
 * Should not retrieve user infor here. As it could be still loading database and it
 * could be returned null. Here we just need id info so use Firebase framework instead.
 */
public class HomeActivity extends AppCompatActivity implements ProfileFragment.OnFragmentInteractionListener, SharedPreferences.OnSharedPreferenceChangeListener {

    // Constants


    private static final int RC_SIGN_IN = 1;
    private static final String SELECTED_ITEM = "arg_selected_item";

    private final String LOG_TAG = getClass().getSimpleName();
    // Instance fields
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private int mSelectedItem;


    // TODO: Maybe refactor and put it apart because of repeated code in chat activity
    private final BroadcastReceiver connectivityChangeReceiver = new BroadcastReceiver() {
        private final String LOG_TAG = "BroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "Intent received in HomeActivity");
            if (intent.getAction().equals(CONNECTIVITY_ACTION)) {
                if (!Utility.isNetworkAvailable(context)) {
                    showSnackBar(true);
                    setActiveConnectivityStatus(context, false);

                } else {
                    showSnackBar(false);
                    setActiveConnectivityStatus(context, true);

                }
            }
            if (intent.getAction().equals(SpekoSyncAdapter.ACTION_DATA_UPDATED)) {
                Log.i(LOG_TAG, "Action data Updated");
                ((UpdateFragmentStatus) currentFragment).setLoading(false);
            }
        }

        public void setActiveConnectivityStatus(Context c, boolean connectivityStatus) {
            Log.i(LOG_TAG, "Set ConnectivityStatus: " + connectivityStatus);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
            SharedPreferences.Editor spe = sp.edit();
            spe.putBoolean(c.getString(R.string.shared_preference_active_connectivity_status_key), connectivityStatus);
            spe.commit();
        }
    };


    @BindView(R.id.bottom_view_layout_home_activity)
    BottomNavigationView mBottomNavigationView;

    @BindView(R.id.home_activity_coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    private Snackbar connectivitySnackBar;
    private Fragment currentFragment;
    private ValueEventListener mValueEventListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mChatRoomsReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDB();

        Fabric.with(this, new Crashlytics());
        Log.d("HomeActivity", "onCreate");

        attachView(savedInstanceState);

        setupFirebase();


        if (!isUserLogged()) {
            callLoginActivity();
            return;
        }

        userNotCreatedCheck();


    }

    private void setUpBottomNavigationView(Bundle savedInstanceState) {
        mBottomNavigationView.setContentDescription(this.getString(
                R.string.bottom_navigation_view_content_description)
        );

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
            Log.i(LOG_TAG, "previous selected item is: " + selectedItem.toString());
        } else {
            selectedItem = mBottomNavigationView.getMenu().getItem(0);
            Log.i(LOG_TAG, "first time selecting item: " + selectedItem.toString());

        }
        selectFragment(selectedItem);
    }

    private void setupFirebase() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();


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

                    attachFirebaseChatRoomsListener();
                } else {
                    Log.i(LOG_TAG, "user logged out");

                    // User is signed out
                    //TODO implement this
                    callLoginActivity();
                }
            }
        };


    }

    private void attachFirebaseChatRoomsListener() {
        if (mFirebaseAuth.getCurrentUser() != null) {
            mChatRoomsReference = mFirebaseDatabase.getReference()
                    .child("users")
                    .child(mFirebaseAuth.getCurrentUser().getUid())
                    .child("chats");
            if (mValueEventListener == null) {
                mValueEventListener = new ValueEventListener() {


                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(LOG_TAG, "New Room Created!");

                        if (!SpekoSyncAdapter.isSyncActive(getApplicationContext())) {
                            SpekoSyncAdapter.syncImmediatly(getApplicationContext());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(LOG_TAG, databaseError.getDetails());
                    }

                };
                mChatRoomsReference.addValueEventListener(mValueEventListener);
            }
        }


    }

    private void attachView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        connectivitySnackBar = Snackbar.make(mCoordinatorLayout,
                R.string.connectivity_error, Snackbar.LENGTH_INDEFINITE);

        setUpBottomNavigationView(savedInstanceState);

    }

    private void setupDB() {
        UsersDatabase userDB;
        userDB = UsersDatabase.getInstance(getApplicationContext());
        userDB.onCreate(userDB.getReadableDatabase());
    }



    private void callLoginActivity() {
        Log.w(LOG_TAG, "Calling Login Activity");
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
        finish();
    }


    private void showSnackBar(Boolean show) {
        if (show) {
            connectivitySnackBar.show();
        } else {
            connectivitySnackBar.dismiss();
        }


    }


    private void selectFragment(MenuItem item) {
        // init corresponding fragment
        switch (item.getItemId()) {
            case R.id.action_search:
                Log.i(LOG_TAG, "Selecting HomeActivityFragment");

                currentFragment = new HomeActivityFragment();

                break;
            case R.id.action_profile:
                Log.i(LOG_TAG, "Selecting Profile");

                currentFragment = new ProfileFragment();
                Bundle args = new Bundle();
                args.putBoolean(ProfileFragment.BUNDLE_ARGUMENT_IS_SYNCABLE, true);
                args.putBoolean(ProfileFragment.BUNDLE_ARGUMENT_FIRST_TIME_ENABLED, false);
                currentFragment.setArguments(args);
                break;
            case R.id.action_conversations:
                Log.i(LOG_TAG, "Selecting Conversations");
                //noinspection ConstantConditions
                currentFragment = ConversationsFragment.newInstance(mFirebaseAuth
                        .getCurrentUser().getUid());
                break;
        }

        // update selected item
        mSelectedItem = item.getItemId();

        // uncheck the other items.
        for (int i = 0; i < mBottomNavigationView.getMenu().size(); i++) {
            MenuItem menuItem = mBottomNavigationView.getMenu().getItem(i);
            if (menuItem.getItemId() == mSelectedItem) {
                Log.i(LOG_TAG, "Checked menu is: " + String.valueOf(menuItem.getItemId()));
                menuItem.setEnabled(true);

            }
        }


        if (currentFragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.home_activity_fragment_container, currentFragment, currentFragment.getTag());
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
        userNotCreatedCheck();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CONNECTIVITY_ACTION);
        filter.addAction(SpekoSyncAdapter.ACTION_DATA_UPDATED);
        registerReceiver(connectivityChangeReceiver, filter);

        if (!SpekoSyncAdapter.isSyncActive(this)) {
            SpekoSyncAdapter.syncImmediatly(this);
        }

        attachFirebaseChatRoomsListener();

        Log.i(LOG_TAG, "onStart");
        super.onStart();
    }

    private boolean isUserLogged() {

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user == null) {
            Log.i(LOG_TAG, "User Not Logged, calling LoginActivity");
            // User is signed in

            return false;
        }
        return true;


    }

    private void userNotCreatedCheck() {
        Log.i(LOG_TAG, "userNotCreatedCheck");
        UserComplete userComplete = Utility.getUser(this);
        if (userComplete == null
                || userComplete.getId() == null
                || userComplete.getId().isEmpty()
                || !SpekoSyncAdapter.hasUserTokenSetted()) {

            setFireBaseToken();
            // this should always be priority
            localUserSyncCheck();
        }


    }

    private void localUserSyncCheck() {
        if (Utility.getLastSyncStatus(this) == SpekoSyncAdapter.SYNC_STATUS_LOCAL_USER_INVALID) {
            Log.w(LOG_TAG, "signing out");
            signOut();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(LOG_TAG, "onActivityResult");
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

    @Override
    protected void onStop() {
        super.onStop();

        detachFirebaseChatRoomsListener();
    }

    private void setFireBaseToken() {
        Log.i(LOG_TAG, "setFireBaseToken");
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        //noinspection ConstantConditions
        user.getIdToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            public final String LOG_TAG = getClass().getSimpleName();

            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    String userToken = task.getResult().getToken();
                    Log.i(LOG_TAG, "O token Deu certo! \n");
                    SpekoSyncAdapter.setUserToken(userToken);
                    SpekoSyncAdapter.initializeSyncAdapter(getApplication());
                    SpekoSyncAdapter.syncImmediatly(getApplicationContext());


                } else {
                    //noinspection ConstantConditions,ThrowableResultOfMethodCallIgnored
                    Log.e(LOG_TAG, task.getException().getMessage());
                    callLoginActivity();

                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);

    }

    @Override
    protected void onPause() {

        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        //noinspection ConstantConditions
        if (connectivityChangeReceiver != null) {
            try {
                unregisterReceiver(connectivityChangeReceiver);
            } catch (Exception e) {
                Log.w(LOG_TAG, e.getMessage());
            }
        }

        detachFirebaseChatRoomsListener();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);

        Log.i(LOG_TAG, "onPause()");
        super.onPause();
    }

    private void detachFirebaseChatRoomsListener() {
        if (mValueEventListener != null) {
            mChatRoomsReference.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }


    @Override
    public void completeSignup(UserComplete userComplete) {

        final FirebaseUser authUser = mFirebaseAuth.getCurrentUser();

        userComplete.setLearningCode(userComplete.getFluentLanguage()
                + "|"
                + userComplete.getLearningLanguage());

        //adding more Provider User info
        //noinspection ConstantConditions
        userComplete.setName(authUser.getDisplayName());
        userComplete.setEmail(authUser.getEmail());
        userComplete.setId(authUser.getUid());
        Utility.setUserIntoFirebase(userComplete, this, null);


        Toast.makeText(this, "ProfileUpdated!", Toast.LENGTH_SHORT).show();


    }

    @Override
    public void signOut() {
        Intent i = new Intent(this, SpekoSyncService.class);
        i.setAction(SpekoSyncAdapter.ACTION_SIGNOUT);
        startService(i);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(LOG_TAG, "Shared Preferences changed: " + key);

        if (key.equals(getString(R.string.shared_preference_sync_status_key))) {
            Log.i(LOG_TAG, "onSharedPreferenceChanged");

            //checking if somehow syncying main user to local storage was ok
            localUserSyncCheck();
        }

    }


}
    interface UpdateFragmentStatus {
        @SuppressWarnings("SameParameterValue")
        void setLoading(Boolean isLoading);
    }

