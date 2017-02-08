package com.speko.android.data;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by rafaelalves on 06/02/17.
 */

@IgnoreExtraProperties
public class Message {
    private String name;
    private String senderId;
    private String text;

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
