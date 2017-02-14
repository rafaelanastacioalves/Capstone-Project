package com.speko.android.data;

import java.util.HashMap;

/**
 * Created by rafaelalves on 09/02/17.
 */

public class Chat {
    private HashMap<String, User> members;
    private String chatId;

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public HashMap<String, User> getMembers() {
        return members;
    }

    public void setMembers(HashMap<String, User> members) {
        this.members = members;
    }
}
