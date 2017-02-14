package com.speko.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (savedInstanceState == null) {
            String chatId = getIntent().getDataString();
            Bundle arguments = new Bundle();
            arguments.putString(ChatActivityFragment.CHAT_ID,chatId);

            ChatActivityFragment fragment = new ChatActivityFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction().
                    add(R.id.chat_fragment_container,fragment).
                    commit();


        }










    }

}
