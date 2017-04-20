package com.speko.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.FirebaseDatabase;
import com.speko.android.sync.SpekoSyncAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    static final int COL_USER_ID = 0;
    static final int COL_USER_NAME = 1;
    static final int COL_EMAIL = 2;
    private static final int FRIENDS_LOADER = 1;
    private final String LOG_TAG = getClass().getSimpleName();
    @BindView(R.id.user_list)
    RecyclerView userList;

    @BindView(R.id.progress_bar)
    ContentLoadingProgressBar progressBar;
    @BindView(R.id.recyclerview_list_empty_textview)
    TextView emptyListTextView;

    @BindView(R.id.fragment_home_container)
    View fragmentHomeContainer;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser authUser;
    private FriendsListAdapter mAdapter;
    private ChildEventListener userListListener;


    public HomeActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        fragmentHomeContainer.setPadding(0,Utility.getStatusBarHeight(getActivity()),0,0);

        userList.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new FriendsListAdapter(getActivity(), new FriendsListAdapter.FriendsAdapterOnClickHandler() {

            @Override
            public void onClick(String friendUserID) {
                Log.d(LOG_TAG, "onClick");
                Log.d(LOG_TAG, "the friendID is: " + friendUserID);

                //TODO should not allow click while onClickSync adapter is updating.
                //TODO This framgnet should confirm if syncAdapter is updating somehow!
                String chatId = Utility.getFirebaseRoomIdWithUserID(friendUserID, getActivity());
                Log.i(LOG_TAG, "The chatId is: " + chatId);
                Intent i = new Intent(getActivity(), ChatActivity.class);
                i.putExtra(ChatActivityFragment.CHAT_ID, chatId);
                i.putExtra(ChatActivityFragment.FRIEND_ID, friendUserID);
                startActivity(i);
            }
        });

        Log.i(LOG_TAG, "setting adapter");
        userList.setAdapter(mAdapter);


        return view;
    }


    private void setRefreshScreen(Boolean active) {
        //TODO Implement
        Log.i(LOG_TAG, "setRefresh: " + active.toString());
        if (active) {
            progressBar.show();
            //if onClickSync active, disable list clicking
            mAdapter.setViewItensClickable(false);


        } else {
            progressBar.hide();
            //if onClickSync NOT active, enable list clicking

            // If is connected to the internet
            if (Utility.getIsConnectedStatus(getActivity())) {
                mAdapter.setViewItensClickable(true);
            }

        }


    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        updateScreenState();

    }

    @Override
    public void onStart() {
        Log.i(LOG_TAG, "onStart");

        // we putted here because until onActivityCreated, the activity hasn' decided to
        // put the user to login Ativity when necessary
        firebaseDatabase = FirebaseDatabase.getInstance();
        authUser = FirebaseAuth.getInstance().getCurrentUser();

        Log.i(LOG_TAG, "Initloader");
        getLoaderManager().initLoader(FRIENDS_LOADER, null, this);


        super.onStart();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }




    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOG_TAG, "onCreateLoader");
        return Utility.getUserFriendsCursorLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, "onLoaderFinished with total data: " + data.getCount());
        mAdapter.swapCursor(data);
        updateEmptyView();
        updateScreenState();
    }

    private void updateEmptyView() {
        Log.i(LOG_TAG, "updateEmptyView");

        if (mAdapter.getItemCount() == 0) {
            @SpekoSyncAdapter.LocationStatus int status = Utility.getSyncStatus(getActivity());
            String message = getString(R.string.no_friend_to_show);
            switch (status) {
                //TODO: preencher com as mensagens
                case SpekoSyncAdapter.SYNC_STATUS_SERVER_DOWN:
                    Log.i(LOG_TAG, "updateEmptyView: Sync Statys Server Down");
                    message = getString(R.string.sync_status_message_server_down);
                    Log.i(LOG_TAG, "updateEmptyView: Message Server Down");
                    break;
                case SpekoSyncAdapter.SYNC_STATUS_SERVER_ERROR:
                    Log.i(LOG_TAG, "updateEmptyView: Server Error");
                    message = getString(R.string.sync_status_message_server_error);
                    break;
                default:
                    if (!Utility.isNetworkAvailable(getActivity())) {
                        Log.i(LOG_TAG, "updateEmptyView: No Network");
                        message = getString(R.string.empty_conversations_list_no_network);
                    }
            }

            if (emptyListTextView != null) {
                emptyListTextView.setText(message);
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOG_TAG, "onLoaderReset");
        mAdapter.swapCursor(null);

    }

    public void updateScreenState() {
        updateEmptyView();
        if (SpekoSyncAdapter.isSyncActive(getContext())) {
            Log.i(LOG_TAG, "Sync is active");
            setRefreshScreen(true);
        } else {
            Log.i(LOG_TAG, "Sync is NOT active");
            setRefreshScreen(false);

            //in case we are offline
            if (!Utility.isNetworkAvailable(getActivity())) {
                //we keep not allowing click
                mAdapter.setViewItensClickable(false);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(LOG_TAG, "Shared Preferences changed: ");
        if (key.equals(getString(R.string.shared_preference_sync_status_key))) {
            Log.d(LOG_TAG, "Case onClickSync-status");
            updateScreenState();
        }

        if (key.equals(getString(R.string.shared_preference_active_connectivity_status_key))) {
            Log.d(LOG_TAG, "Case connectivity");
            updateScreenState();

        }
    }



}
