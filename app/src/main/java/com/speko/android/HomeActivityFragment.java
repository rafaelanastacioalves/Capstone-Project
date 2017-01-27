package com.speko.android;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.speko.android.data.User;
import com.speko.android.data.UserColumns;
import com.speko.android.data.UsersProvider;
import com.speko.android.sync.SpekoSyncAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = getClass().getSimpleName();

    @BindView(R.id.fragment_button_confirm)
    AppCompatButton buttonViewAddUser;

    @BindView(R.id.fragment_edittext_add_user)
    AppCompatEditText emailInputTextView;

    @BindView(R.id.user_list)
    RecyclerView userList;

    @BindView(R.id.sync_button)
    Button sync_button;

    private static final int FRIENDS_LOADER = 1;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser authUser;
    private FriendsAdapter mAdapter;
    private ChildEventListener userListListener;
    private Query mUserQueryByEmail;


    private static final String[] USER_COLUMNS = {
            UserColumns.FIREBASE_ID,
            UserColumns.NAME,
            UserColumns.EMAIL
    };

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
        ButterKnife.bind(this,view);

        userList.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new FriendsAdapter(getActivity());

        Log.i(LOG_TAG, "setting adapter");
        userList.setAdapter(mAdapter);





        return view;
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
        if (mUserQueryByEmail != null){
            mUserQueryByEmail.removeEventListener(userListListener);
            mUserQueryByEmail = null;
        }
        super.onPause();
    }
    @OnClick(R.id.sync_button)
    public void sync(View v){
        SpekoSyncAdapter.syncImmediatly(getActivity());
//        getLoaderManager().restartLoader(FRIENDS_LOADER,null, this);
    }


    @OnClick(R.id.fragment_button_confirm)
    public void addUser(View v){

        String friendEmail = emailInputTextView.getText().toString();

        //TODO Check if user with that email exists
        userListListener = new ChildEventListener() {


            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.exists()) {
                    Log.i(LOG_TAG, "Usuário procurado existe!");
                    Toast.makeText(getActivity(), R.string.user_added, Toast.LENGTH_SHORT)
                            .show();
                    User userFriend = dataSnapshot.getValue(User.class);
                    Log.i(LOG_TAG, userFriend.getEmail() + userFriend.getName());

                    firebaseDatabase.getReference()
                            .child("friends")
                            .child(authUser.getUid())
                            // the getKey because it contains the UId of the friend
                            .child(dataSnapshot.getKey()).setValue(userFriend);
                } else {
                    Log.i(LOG_TAG, "Usuário procurado não existe!");
                    Toast.makeText(getActivity(), R.string.user_does_not_exist, Toast.LENGTH_SHORT)
                            .show();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        //TODO is it possible to get into the child after query so we can
        //use singleValueListeners instead of ChildValueListeners ?
        mUserQueryByEmail = firebaseDatabase.getReference()
                .child("users").orderByChild("email").equalTo(friendEmail);

        mUserQueryByEmail.addChildEventListener(userListListener);




    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOG_TAG,"onCreateLoader");
        return new CursorLoader(getActivity(), UsersProvider.Users.usersFrom(authUser.getUid()),
                USER_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, "onLoaderFinished with total data: " + data.getCount());
        mAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOG_TAG,"onLoaderReset");
        mAdapter.swapCursor(null);

    }

}
