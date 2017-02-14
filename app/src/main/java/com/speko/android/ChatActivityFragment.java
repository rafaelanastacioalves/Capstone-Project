package com.speko.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.speko.android.data.Message;
import com.speko.android.data.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class ChatActivityFragment extends Fragment {

    private final String LOG_TAG = getClass().getSimpleName();
    private DatabaseReference mFirebaseDatabaseReference;
    private ChildEventListener mFirebaseListener;
    private MessagesListAdapter messagesListAdapter;

    public ChatActivityFragment() {
    }

    @BindView(R.id.chat_recycler_view)
    RecyclerView chatRecyclerView;

    @BindView(R.id.chat_text_input)
    EditText chatTextInput;

    @BindView(R.id.chat_send_button)
    Button chatSendButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        ButterKnife.bind(this,v);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        mFirebaseDatabaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("chats")
                .child("ID_1")
                .child("messages");


        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        detachDatabaseReadListener();



    }

    @Override
    public void onResume() {
        super.onResume();

        if(messagesListAdapter == null){
            messagesListAdapter = new MessagesListAdapter(getActivity());
            chatRecyclerView.setAdapter(messagesListAdapter);

        }
        attachDatabaseReadListener();
    }

    private void detachDatabaseReadListener() {
        if (mFirebaseListener != null) {
            mFirebaseDatabaseReference.removeEventListener(mFirebaseListener);
            mFirebaseListener = null;
        }
    }

    @OnClick(R.id.chat_send_button)
    public void SendMessage(View v){

        User user = Utility.getUser(getActivity());
        Message newMessage = new Message();
        newMessage.setText(String.valueOf(chatTextInput.getText()));
        newMessage.setName(user.getName());
        newMessage.setSenderId(user.getId());

        addMessageToFirebase(newMessage);

        chatTextInput.setText("");
    }

    private void addMessageToFirebase(Message m) {
        mFirebaseDatabaseReference.push().setValue(m);
    }


    private void attachDatabaseReadListener() {


        if (mFirebaseListener == null) {
                mFirebaseListener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.i(LOG_TAG, "onChildAdded");
                        Message m = dataSnapshot.getValue(Message.class);
                        messagesListAdapter.add(m);
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
            mFirebaseDatabaseReference.addChildEventListener(mFirebaseListener);
        }
    }
}
