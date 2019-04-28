package com.example.transmission;


import java.sql.Timestamp;

/**
 * Created by Kielle on 7/15/2016.
 */
public class ChatHistoryBean {
    private long id;
    private String mobileNumber;
    private String contactName;
    private boolean isSenderOrReceiver;
    private String message;
    private Timestamp timestamp;

    public ChatHistoryBean() {}

    public ChatHistoryBean(String mobileNumber, boolean isSenderOrReceiver, String message, Timestamp timestamp) {
        this.mobileNumber = mobileNumber;
        this.isSenderOrReceiver = isSenderOrReceiver;
        this.message = message;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSenderOrReceiver() {
        return isSenderOrReceiver;
    }

    public void setSenderOrReceiver(boolean senderOrReceiver) {
        isSenderOrReceiver = senderOrReceiver;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
}
