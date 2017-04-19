package com.speko.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.github.bassaer.chatmessageview.models.Message;
import com.github.bassaer.chatmessageview.views.ChatView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.speko.android.data.MessageLocal;
import com.speko.android.data.User;
import com.speko.android.sync.MyMessageStatusFormatter;
import com.speko.android.sync.SpekoSyncAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class ChatActivityFragment extends Fragment {


    private final String LOG_TAG = getClass().getSimpleName();
    private DatabaseReference mFirebaseDatabaseReference;
    private ChildEventListener mFirebaseListener;

    public static final String CHAT_ID = "CHAT_ID";
    public static final String FRIEND_ID = "FRIEND_ID";


    public static final int ME_CHATMESSAGE_ID = 0;
    public static final int HIM_CHATMESSAGE_ID = 1;

    private String chatId;
    private String friendId;

    private ArrayList<Message> mMessageList;

    // this ID is necessary because of Firebase and of the library for chat message models
    public static HashMap<Integer,String> mIdConvertion;


    private com.github.bassaer.chatmessageview.models.User[] mUsers = new com.github.bassaer.chatmessageview.models.User[2];
    public static final int ME_CHATMESSAGE_INDEX  = 0;
    public static final int HIM_CHATMESSAGE_INDEX = 1;
    private final int totalImagesToBeLoaded = 2;
    private int imagesLoaded;
    private EditText mInputEditText;

    public ChatActivityFragment() {
    }


    @BindView(R.id.chat_view)
    ChatView mChatView;

    @BindView(R.id.progress_bar)
    ContentLoadingProgressBar progressBar;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    public void onAttach(Context context) {

        Bundle arguments = getArguments();
        chatId = arguments.getString(ChatActivityFragment.CHAT_ID);
        friendId = arguments.getString(ChatActivityFragment.FRIEND_ID);

        imagesLoaded = 0;
        initUsers();
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "OnCreateView");
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        mInputEditText = (EditText) v.findViewById(com.github.bassaer.chatmessageview.R.id.message_edit_text);

        ButterKnife.bind(this, v);





//        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (chatId != null) {
            Log.i(LOG_TAG, "setRefreshScreen true");
            setRefreshScreen(true);
            // disabling input as we know that the room exists and the previous messages are
            // still being loaded
            Log.i(LOG_TAG,"disabling input");
            mInputEditText.setFocusable(false);

        } else {
            Log.i(LOG_TAG, "setRefreshScreen false");
            setRefreshScreen(false);
            // we allow typing as this is the first message
            Log.i(LOG_TAG,"enabling text input");
            mInputEditText.setFocusableInTouchMode(true);
        }



        setChatUI();

        //Click Send Button
        mChatView.setOnClickSendButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(chatId == null && mMessageList==null) {
                    OnCompleteListener onCompleteListener = new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            Log.i("onComplete", "Room creation completed!");
                            SpekoSyncAdapter.syncImmediatly(getContext());
                        }
                    };
                    chatId = Utility.createRoomForUsers(getActivity(),friendId, Utility.getUser(getActivity()).getId(), onCompleteListener);
                    setupFirebaseChat(chatId);

                }



                //new message
                Message message = new Message.Builder()
                        .setUser(mUsers[ME_CHATMESSAGE_INDEX])
                        .setRightMessage(true)
                        .setMessageText(mChatView.getInputText())
                        .hideIcon(true)
                        .setMessageStatusType(com.github.bassaer.chatmessageview.models.Message.MESSAGE_STATUS_ICON)
                        .build();
                if (mUsers[ME_CHATMESSAGE_INDEX].getIcon() == null) {
                    Log.d(getClass().getName(),
                            mUsers[ME_CHATMESSAGE_INDEX].getName() + "'s icon is null ");
                }
                //Set random status(Delivering, delivered, seen or fail)
                int messageStatus = new Random().nextInt(4);
                message.setStatus(messageStatus);

                //Reset edit text
                mChatView.setInputText("");






                addMessageToFirebase(message);


            }

        });




        return v;
    }



    private void setChatUI() {
        //Set UI parameters if you need
        mChatView.setRightBubbleColor(ContextCompat.getColor(getActivity(), R.color.blue500));
        mChatView.setLeftBubbleColor(ContextCompat.getColor(getActivity(), R.color.gray300));
        mChatView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.blueGray200));
        mChatView.setSendButtonColor(ContextCompat.getColor(getActivity(), R.color.lightBlue500));
        mChatView.setSendIcon(R.drawable.ic_action_send);
        mChatView.setRightMessageTextColor(Color.WHITE);
        mChatView.setLeftMessageTextColor(Color.BLACK);
        mChatView.setUsernameTextColor(ContextCompat.getColor(getActivity(), R.color.blueGray500));
        mChatView.setSendTimeTextColor(ContextCompat.getColor(getActivity(), R.color.blueGray500));
        mChatView.setDateSeparatorColor(ContextCompat.getColor(getActivity(), R.color.blueGray500));
        mChatView.setInputTextHint("new message...");
        mChatView.setMessageMarginTop(5);
        mChatView.setMessageMarginBottom(5);

        toolbar.setTitle(Utility.getOtherUserWithId(getActivity(),friendId).getName());

    }


    private void initUsers() {
        //TODO Decide if photos are cached or make this method async

        Log.i(LOG_TAG, "Init Users");
        User user = Utility.getUser(getActivity());

        User otherUser = Utility.getOtherUserWithId(getActivity(), friendId);
        //User icon
        Picasso.with(getActivity())
                .load(user.getProfilePicture())
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                        User user = Utility.getUser(getActivity());
                        Log.i(LOG_TAG, "setting Icon for user:" + user.getName());
                        Bitmap myIcon = null;
                        myIcon = bitmap;
                        final com.github.bassaer.chatmessageview.models.User me =
                                new com.github.bassaer.chatmessageview.models.User(
                                        ME_CHATMESSAGE_ID,
                                        user.getName(), myIcon);
                        if (mIdConvertion == null) {
                            mIdConvertion = new HashMap<Integer, String>();
                        }
                        mIdConvertion.put(ME_CHATMESSAGE_ID, user.getId());
                        mUsers[ME_CHATMESSAGE_INDEX] = (me);

                        imagesLoaded++;
                        if (imagesLoaded == totalImagesToBeLoaded) {
                            if(chatId!= null) {
                                setupFirebaseChat(chatId);

                            }

                        }

                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });


        int yourId = 1;
        Picasso.with(getActivity()).load(otherUser.getProfilePicture()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                User otherUser = Utility.getOtherUserWithId(getActivity(), friendId);

                Bitmap otherUserIcon = bitmap;
                Log.i(LOG_TAG, "setting Icon for user:" + otherUser.getName());
                final com.github.bassaer.chatmessageview.models.User otherChatUser =
                        new com.github.bassaer.chatmessageview.models.User(
                                HIM_CHATMESSAGE_ID,
                                otherUser.getName(), otherUserIcon);

                if (mIdConvertion == null) {
                    mIdConvertion = new HashMap<Integer, String>();
                }
                mIdConvertion.put(HIM_CHATMESSAGE_ID, otherUser.getId());
                mUsers[HIM_CHATMESSAGE_INDEX] = otherChatUser;
                imagesLoaded++;
                if (imagesLoaded == totalImagesToBeLoaded) {
                    if(chatId!= null){
                        setupFirebaseChat(chatId);

                    }


                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });


    }

    private void setRefreshScreen(boolean active) {
        if (active) {
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
        if (mFirebaseDatabaseReference != null) {
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
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);




    }



    private void detachDatabaseReadListener() {
        if (mFirebaseListener != null) {
            mFirebaseDatabaseReference.removeEventListener(mFirebaseListener);
            mFirebaseListener = null;
        }


    }


    private void addMessageToFirebase(Message m) {
        MessageLocal firebaseMessage = Utility.parseToFirebaseModel(m);
        mFirebaseDatabaseReference.push().setValue(firebaseMessage);
    }

    public void sendMessage(){

        if(chatId == null && mMessageList.size() < 1) {
            OnCompleteListener onCompleteListener = new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    Log.i("onComplete", "Room creation completed!");
                    SpekoSyncAdapter.syncImmediatly(getContext());
                }
            };
            String chatId = Utility.createRoomForUsers(getActivity(),friendId, Utility.getUser(getActivity()).getId(), onCompleteListener);
            setupFirebaseChat(chatId);
        }


    }


    private void attachDatabaseReadListener() {


        if (mFirebaseListener == null) {
            mFirebaseListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                        Log.i(LOG_TAG, "onChildAdded");
//                        Message m = dataSnapshot.getValue(Message.class);
//                        chatListAdapter.add(m);
//
//                        Log.i(LOG_TAG, "setRefreshScreen false");
                        setRefreshScreen(false);
                    Log.i(LOG_TAG,"enabling input");
                    mInputEditText.setFocusableInTouchMode(true);

                    MessageLocal messageFromFirebase = dataSnapshot.getValue(MessageLocal.class);
                    Message message = Utility.parseFromFirebaseModel(messageFromFirebase);
                    for (com.github.bassaer.chatmessageview.models.User user : mUsers) {
                        if (
                                mIdConvertion.get(message.getUser().getId()) ==
                                mIdConvertion.get(user.getId())
                                ) {
                                    Log.i(LOG_TAG, "setting the icon from  onChildAdded callback");
                                    message.getUser().setIcon(user.getIcon());
                        }
                    }

                    message.setMessageStatusType(Message.MESSAGE_STATUS_ICON_RIGHT_ONLY);
                    message.setStatusIconFormatter(new MyMessageStatusFormatter(getActivity()));
                    message.setStatusTextFormatter(new MyMessageStatusFormatter(getActivity()));
                    if (!message.isDateCell()) {
                        if (message.isRightMessage()) {
                            message.hideIcon(true);
                            Log.i(LOG_TAG, "setting Right Message");
                            mChatView.send(message);
                        } else {
                            Log.i(LOG_TAG, "setting Left Message");

                            mChatView.receive(message);

                        }


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
            }

            ;
            mFirebaseDatabaseReference.addChildEventListener(mFirebaseListener);
        }
    }
}
