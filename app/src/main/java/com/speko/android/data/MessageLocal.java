package com.speko.android.data;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by rafaelalves on 06/02/17.
 */

@IgnoreExtraProperties
public class MessageLocal {

    public MessageLocal(){

    }

    /**
     * Firebase ID
     **/


    private String id;
    /**
     * Sender information
     */
    private String name;

    /**
     * Whether sender username is shown or not
     */
    private boolean mUsernameVisibility = true;
    /**
     * If true, there is the icon space but invisible.
     */
    private boolean mIconVisibility = true;
    /**
     * If true, there is no icon space.
     */
    private boolean mHideIcon = false;

    /**
     * Whether the message is shown right side or not.
     */
    private boolean isRightMessage;

    /**
     * Message content text
     */
    private String mMessageText;

    /**
     * The time message that was created
     */
    private String mCreatedAt;

    /**
     * Whether cell of list view is date separator text or not.
     */
    private boolean isDateCell;


    /**
     * Message status
     * You can use to know the message status such as fail, delivered, seen.. etc.
     */
    private int mStatus;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean ismUsernameVisibility() {
        return mUsernameVisibility;
    }

    public void setmUsernameVisibility(boolean mUsernameVisibility) {
        this.mUsernameVisibility = mUsernameVisibility;
    }

    public boolean ismIconVisibility() {
        return mIconVisibility;
    }

    public void setmIconVisibility(boolean mIconVisibility) {
        this.mIconVisibility = mIconVisibility;
    }

    public boolean ismHideIcon() {
        return mHideIcon;
    }

    public void setmHideIcon(boolean mHideIcon) {
        this.mHideIcon = mHideIcon;
    }

    public boolean isRightMessage() {
        return isRightMessage;
    }

    public void setRightMessage(boolean rightMessage) {
        isRightMessage = rightMessage;
    }

    public String getmMessageText() {
        return mMessageText;
    }

    public void setmMessageText(String mMessageText) {
        this.mMessageText = mMessageText;
    }

    public String getmCreatedAt() {
        return mCreatedAt;
    }

    public void setmCreatedAt(String mCreatedAt) {
        this.mCreatedAt = mCreatedAt;
    }

    public boolean isDateCell() {
        return isDateCell;
    }

    public void setDateCell(boolean dateCell) {
        isDateCell = dateCell;
    }

    public int getmStatus() {
        return mStatus;
    }

    public void setmStatus(int mStatus) {
        this.mStatus = mStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
