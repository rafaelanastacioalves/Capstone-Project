package com.speko.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import com.github.bassaer.chatmessageview.models.Message;
import com.github.bassaer.chatmessageview.views.ChatView;
import com.github.bassaer.chatmessageview.views.MessageView;
import com.speko.android.R;

/**
 * Created by rafaelalves on 19/04/17.
 */

public class AdaptedChatView extends ChatView {

    public AdaptedChatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public AdaptedChatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }


    @Override
    public void send(Message message){
        MessageView mMessageView = (MessageView) findViewById(R.id.message_view);
        Object lastItem = mMessageView.getLastChatObject();
        if (lastItem instanceof Message) {
            if (((Message) lastItem).getUser().getId() == message.getUser().getId()) {
                //If send same person, hide username and icon.
                message.setIconVisibility(false);
                message.setUsernameVisibility(false);
            }
        }
        mMessageView.setMessage(message);
        mMessageView.scrollToEnd();

    }

    private void init(){
        Log.i("AdaptedChatView", "init");
        EditText mInputText = (EditText) findViewById(R.id.message_edit_text);

        mInputText.setMaxLines(3);
    }


}
