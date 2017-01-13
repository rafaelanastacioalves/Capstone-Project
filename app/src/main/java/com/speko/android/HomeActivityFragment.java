package com.speko.android;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.speko.android.data.User;

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
    ListView userList;

    private static final int FRIENDS_LOADER = 1;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser authUser;
    private FirebaseListAdapter<User> mAdapter;
    private DatabaseReference ref;
    private ChildEventListener userListListener;
    private Query mUserQueryByEmail;

    public HomeActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this,view);




        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(FRIENDS_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {

        // we putted here because until onActivityCreated, the activity hasn' decided to
        // put the user to login Ativity when necessary

        firebaseDatabase = FirebaseDatabase.getInstance();

        authUser = FirebaseAuth.getInstance().getCurrentUser();

        ref = FirebaseDatabase.getInstance().getReference()
                .child("friends")
                .child(authUser.getUid());
        mAdapter = new FirebaseListAdapter<User>(getActivity(), User.class, android.R.layout.two_line_list_item, ref) {
            @Override
            protected void populateView(View view, User user, int position) {
                ((TextView)view.findViewById(android.R.id.text1)).setText(user.getName());
                ((TextView)view.findViewById(android.R.id.text2)).setText(user.getEmail());

            }
        };

        Log.i(LOG_TAG, "setting adapter");
        userList.setAdapter(mAdapter);

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

    @OnClick(R.id.fragment_button_confirm)
    public void addUser(View v){

        String friendEmail = emailInputTextView.getText().toString();

        //TODO Check if user with that email exists
        userListListener = new ChildEventListener() {


            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.exists()) {
                    Log.i(LOG_TAG, "Usuário procurado existe! Tem " +
                            dataSnapshot.getChildrenCount() + " filhos \n" +
                            "E seu ID é " + dataSnapshot.getKey());
                    Toast.makeText(getActivity(), "Usuário procurado existe! : \n", Toast.LENGTH_SHORT)
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
                    Toast.makeText(getActivity(), "Usuário procurado NÃO existe!", Toast.LENGTH_SHORT)
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
        mUserQueryByEmail = firebaseDatabase.getReference()
                .child("users").orderByChild("email").equalTo(friendEmail);

        mUserQueryByEmail.addChildEventListener(userListListener);




    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
