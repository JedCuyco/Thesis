package com.example.transmission;

import java.sql.Timestamp;

/**
 * Created by Kielle on 7/11/2016.
 */
public class ConversationMessageItem {

    private long id;
    private boolean isMe;
    private String message;
    private Long userId;
    private Timestamp dateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean getIsme() {
        return isMe;
    }

    public void setMe(boolean isMe) {
        this.isMe = isMe;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Timestamp getDate() {
        return dateTime;
    }

    public void setDate(Timestamp dateTime) {
        this.dateTime = dateTime;
    }

}
