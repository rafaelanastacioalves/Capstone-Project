package com.speko.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class ChatActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String CHAT_ID = "CHAT_ID";
    private static final String FRIEND_ID = "FRIEND_ID";
    private final String LOG_TAG = getClass().getSimpleName();
    private Snackbar connectivitySnackBar;
    private SharedPreferences sp;

    @BindView(R.id.chat_activity_linear_layout)
    LinearLayout mLinearLayout;

    // TODO: Maybe refactor and put it apart because of repeated code in chat activity
    private final BroadcastReceiver connectivityChangeReceiver = new BroadcastReceiver() {
        private final String LOG_TAG = "BroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "Intent received in ChatActivity");
            if (intent.getAction().equals(CONNECTIVITY_ACTION)) {
                if (!Utility.isNetworkAvailable(context)) {
                    setActiveConnectivityStatus(context, false);

                } else {
                    setActiveConnectivityStatus(context, true);

                }
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


    private void showSnackBar(Boolean show) {
        if (show) {
            connectivitySnackBar.show();
        } else {
            connectivitySnackBar.dismiss();
        }


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (savedInstanceState == null) {

            String chatId = getIntent().getStringExtra(CHAT_ID);
            String friendId = getIntent().getStringExtra(FRIEND_ID);
            Bundle arguments = new Bundle();
            arguments.putString(CHAT_ID, chatId);
            arguments.putString(FRIEND_ID, friendId);

            ChatActivityFragment fragment = new ChatActivityFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction().
                    add(R.id.chat_fragment_container, fragment).
                    commit();


        }


    }

    @Override
    protected void onResume() {

        Log.i(LOG_TAG, "onResume ");

        //this coordinator layout is inside the fragment so we need to wait for its creation
        ButterKnife.bind(this);
        connectivitySnackBar = Snackbar.make(mLinearLayout,
                R.string.connectivity_error, Snackbar.LENGTH_INDEFINITE);


        sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(CONNECTIVITY_ACTION);
        registerReceiver(connectivityChangeReceiver, filter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(LOG_TAG, "Shared Preferences changed: ");


        if (key.equals(getString(R.string.shared_preference_active_connectivity_status_key))) {
            Log.i(LOG_TAG, "Case connectivity");

            updateScreenState();
        }
    }

    private void updateScreenState() {

        //in case we are offline
        if (!Utility.isNetworkAvailable(this)) {
            //we keep not allowing click
            showSnackBar(true);
        }else {
            showSnackBar(false);
        }
    }
}

