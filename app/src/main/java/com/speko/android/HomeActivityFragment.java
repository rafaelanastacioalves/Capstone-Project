package com.speko.android;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.FirebaseDatabase;
import com.speko.android.sync.SpekoSyncAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = getClass().getSimpleName();


    @BindView(R.id.user_list)
    RecyclerView userList;

    @BindView(R.id.log_out)
    Button logOut;

    @BindView(R.id.sync_button)
    Button sync_button;

    @BindView(R.id.progress_bar)
    ContentLoadingProgressBar progressBar;



    private static final int FRIENDS_LOADER = 1;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser authUser;
    private FriendsListAdapter mAdapter;
    private ChildEventListener userListListener;


    static final int COL_USER_ID = 0;
    static final int COL_USER_NAME = 1;
    static final int COL_EMAIL = 2;

    public HomeActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        userList.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new FriendsListAdapter(getActivity(), new FriendsListAdapter.FriendsAdapterOnClickHandler() {

            @Override
            public void onClick(String friendUserID) {
                Log.d(LOG_TAG, "onClick");
                Log.d(LOG_TAG, "the friendID is: " + friendUserID);

                //TODO should not allow click while sync adapter is updating.
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
            //if sync active, disable list clicking
            mAdapter.setViewItensClickable(false);


        } else {
            progressBar.hide();
            //if sync NOT active, enable list clicking
            mAdapter.setViewItensClickable(true);

        }


    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
        if (SpekoSyncAdapter.isSyncActive(getContext())){
            Log.i(LOG_TAG, "Sync is active");
            setRefreshScreen(true);
        }else{
            Log.i(LOG_TAG, "Sync is NOT active");
        }

    }

    @Override
    public void onStart() {
        Log.i(LOG_TAG, "onStart");

        // we putted here because until onActivityCreated, the activity hasn' decided to
        // put the user to login Ativity when necessary
        firebaseDatabase = FirebaseDatabase.getInstance();
        authUser = FirebaseAuth.getInstance().getCurrentUser();

        Log.i(LOG_TAG,"Initloader");
        getLoaderManager().initLoader(FRIENDS_LOADER, null, this);



        super.onStart();
    }

    @Override
    public void onPause() {

        super.onPause();
    }
    @OnClick(R.id.sync_button)
    public void sync(View v){
        SpekoSyncAdapter.syncImmediatly(getActivity());
        setRefreshScreen(true);
//        getLoaderManager().restartLoader(FRIENDS_LOADER,null, this);
    }


    @OnClick(R.id.log_out)
    public void logOut(View v){

        Utility.deleteEverything(getContext());
        FirebaseAuth.getInstance().signOut();
    }






    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOG_TAG,"onCreateLoader");
        return Utility.getUserFriendsCursorLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, "onLoaderFinished with total data: " + data.getCount());
        mAdapter.swapCursor(data);

        if(!SpekoSyncAdapter.isSyncActive(getActivity())){
            setRefreshScreen(false);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOG_TAG,"onLoaderReset");
        mAdapter.swapCursor(null);

    }

}
