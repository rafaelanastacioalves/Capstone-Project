package com.speko.android;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.speko.android.data.Message;
import com.speko.android.data.User;
import com.speko.android.sync.SpekoSyncAdapter;

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
    private ChatListAdapter chatListAdapter;

    public static final String CHAT_ID = "CHAT_ID";
    public static final String FRIEND_ID = "FRIEND_ID";

    private String chatId;
    private String friendId;

    public ChatActivityFragment() {
    }

    @BindView(R.id.chat_recycler_view)
    RecyclerView chatRecyclerView;

    @BindView(R.id.chat_text_input)
    EditText chatTextInput;

    @BindView(R.id.chat_send_button)
    Button chatSendButton;

    @BindView(R.id.progress_bar)
    ContentLoadingProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        ButterKnife.bind(this,v);

        Bundle arguments = getArguments();
        chatId = arguments.getString(ChatActivityFragment.CHAT_ID);
        friendId = arguments.getString(ChatActivityFragment.FRIEND_ID);




        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if(chatId != null){
            setupFirebaseChat(chatId);
            Log.i(LOG_TAG, "setRefreshScreen true");
            setRefreshScreen(true);
        }
        setRefreshScreen(false);



        return v;
    }

    private void setRefreshScreen(boolean active) {
        if(active){
            progressBar.show();
        }else {
            progressBar.hide();
        }
    }

    private void setupFirebaseChat(String chatId) {
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("chats")
                .child(chatId)
                .child("messages");

        // if it is null, chatId is not set and we need to set it properly and attach call
        // the following method again
        if (mFirebaseDatabaseReference != null){
            attachDatabaseReadListener();

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        detachDatabaseReadListener();



    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbar = (Toolbar)activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        setupChatListAdapter();


    }

    private void setupChatListAdapter() {
        if(chatListAdapter == null){
            chatListAdapter = new ChatListAdapter(getActivity());
            chatRecyclerView.setAdapter(chatListAdapter);

        }
    }

    private void detachDatabaseReadListener() {
        if (mFirebaseListener != null) {
            mFirebaseDatabaseReference.removeEventListener(mFirebaseListener);
            mFirebaseListener = null;
        }


    }

    @OnClick(R.id.chat_send_button)
    public void SendMessage(View v){

        if(chatId == null && chatListAdapter.getItemCount() < 1) {
            OnCompleteListener onCompleteListener = new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    Log.i("onComplete", "Room creation completed!");
                    SpekoSyncAdapter.syncImmediatly(getActivity());
                }
            };
            String chatId = Utility.createRoomForUsers(getActivity(),friendId, Utility.getUser(getActivity()).getId(), onCompleteListener);
            setupFirebaseChat(chatId);
            setupChatListAdapter();
        }

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
                        chatListAdapter.add(m);
                        setRefreshScreen(false);
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
