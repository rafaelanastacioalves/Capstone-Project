package com.speko.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.speko.android.sync.SpekoSyncAdapter;

public class HomeActivity extends AppCompatActivity  {

    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.speko.android.data";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "android.speko.com";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    // Instance fields
    Account mAccount;
    private ContentResolver mResolver;

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
        mAccount = CreateSyncAccount(this);
        SpekoSyncAdapter.initializeSyncAdapter();


//        Log.d("HomeActibvity", "requestSync");
//
//        Bundle b = new Bundle();
//        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
//        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
//        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
//        ContentResolver.requestSync(mAccount,AUTHORITY,b);

//        TableObserver observer = new TableObserver(false);

//        Log.d("HomeActibvity", "ContentObserver ");
//        Log.d("HomeActibvity", "NotifyChange ");
//
//        mResolver.registerContentObserver(UsersProvider.URI, true, observer);
//        mResolver.notifyChange(UsersProvider.URI, null, true);


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

    public class TableObserver extends ContentObserver {


        private boolean selfChange;

        public TableObserver(Handler handler) {
            super(handler);
        }

        public TableObserver(boolean b) {
            super(null);
            this.selfChange = b;
        }

        /*
                                 * Define a method that's called when data in the
                                 * observed content provider changes.
                                 * This method signature is provided for compatibility with
                                 * older platforms.
                                 */
        @Override
        public void onChange(boolean selfChange) {
            Log.d("HomeActibvity", "onChange(selfChange)");
            /*
             * Invoke the method signature available as of
             * Android platform version 4.1, with a null URI.
             */
            onChange(selfChange, null);
        }

        /*
         * Define a method that's called when data in the
         * observed content provider changes.
         */
        @Override
        public void onChange(boolean selfChange, Uri changeUri) {
            /*
             * Ask the framework to run your sync adapter.
             * To maintain backward compatibility, assume that
             * changeUri is null.
             */
            Log.d("HomeActibvity", "onChange(selfChange, changeUri)");

            ContentResolver.requestSync(mAccount, AUTHORITY, null);
        }

    }
}
