package com.speko.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {
    public static final String CHAT_ID = "CHAT_ID";
    public static final String FRIEND_ID = "FRIEND_ID";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (savedInstanceState == null) {

            String chatId = getIntent().getStringExtra(CHAT_ID);
            String friendId = getIntent().getStringExtra(FRIEND_ID);
            Bundle arguments = new Bundle();
            arguments.putString(CHAT_ID,chatId);
            arguments.putString(FRIEND_ID, friendId);

            ChatActivityFragment fragment = new ChatActivityFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction().
                    add(R.id.chat_fragment_container,fragment).
                    commit();


        }










    }

}
