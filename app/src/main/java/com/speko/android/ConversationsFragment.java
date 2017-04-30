package com.speko.android;

import android.annotation.SuppressLint;
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

import com.speko.android.sync.SpekoSyncAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConversationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConversationsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
        , SharedPreferences.OnSharedPreferenceChangeListener ,
                UpdateFragmentStatus{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USER_ID = "param1";
    private static final int CONVERSATIONS_LOADER = 2;
    private ConversationsListAdapter mAdapter;
    private final String LOG_TAG = getClass().getSimpleName();

    @BindView(R.id.conversations_list)
    RecyclerView conversationsList;

    @BindView(R.id.recyclerview_list_empty_textview)
    TextView emptyListTextView;

    @BindView(R.id.progress_bar)
    ContentLoadingProgressBar progressBar;

    @BindView(R.id.fragment_conversations_container)
    View fragmentConversationsContainer;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ConversationsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConversationsFragment newInstance(String param1) {
        ConversationsFragment fragment = new ConversationsFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID, param1);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onStart() {
        super.onStart();
        Log.i(LOG_TAG,"Initloader");
        getLoaderManager().initLoader(CONVERSATIONS_LOADER, null, this);

    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        updateScreenState();


    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreateView");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversations,container,false);
        ButterKnife.bind(this,view);

        conversationsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        fragmentConversationsContainer.setPadding(0,Utility.getStatusBarHeight(getActivity()),0,0);

        mAdapter = new ConversationsListAdapter(getActivity(), new ConversationsListAdapter.ConversationsAdapterOnClickHandler() {
            @Override
            public void onClick(String friendID) {
                Log.d(LOG_TAG,"onClick");
                Log.d(LOG_TAG,"the friendID is: " + friendID);
                String chatId = Utility.getFirebaseRoomIdWithUserID(friendID, getActivity());
                Log.i(LOG_TAG, "The chatID is: " + chatId );
                Intent i = new Intent(getActivity(), ChatActivity.class);
                i.putExtra(ChatActivityFragment.CHAT_ID, chatId);
                i.putExtra(ChatActivityFragment.FRIEND_ID, friendID);
                startActivity(i);
            }
        });

        Log.i(LOG_TAG, "setting adapter");
        conversationsList.setAdapter(mAdapter);

        return view;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOG_TAG,"onCreateLoader");
        setRefreshScreen(true);
        return Utility.getUserConversationsCursorLoader(getContext());
    }



    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        Log.i(LOG_TAG, "onLoaderFinished with total data: " + data.getCount());
        mAdapter.swapCursor(data);
        updateScreenState();
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.i(LOG_TAG,"onLoaderReset");
        mAdapter.swapCursor(null);
        updateScreenState();
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

    private void updateScreenState() {

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

    @SuppressLint("SwitchIntDef")
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
    public void setLoading(Boolean isLoading) {
        if (isLoading){

        }else{
            updateScreenState();
        }

    }
}
