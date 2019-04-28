package com.example.transmission;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by Kielle on 7/15/2016.
 */
public class ChatHistoryDAO {
    // Database fields
    private SQLiteDatabase database;
    private MySQLiteOpenHelper dbHelper;
    private String[] allColumns = {
            MySQLiteOpenHelper.COLUMN_ID,
            MySQLiteOpenHelper.COLUMN_MOBILE_NUMBER,
            MySQLiteOpenHelper.COLUMN_CONTACT_NAME,
            MySQLiteOpenHelper.COLUMN_SENDER_OR_RECEIVER,
            MySQLiteOpenHelper.COLUMN_MESSAGE,
            MySQLiteOpenHelper.COLUMN_TIMESTAMP };

    public ChatHistoryDAO(Context context) {
        dbHelper = new MySQLiteOpenHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public ChatHistoryBean createChatHistory(String mobileNumber, String contactName, String senderOrReceiver, String message, String timestamp) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteOpenHelper.COLUMN_MOBILE_NUMBER, mobileNumber);
        values.put(MySQLiteOpenHelper.COLUMN_CONTACT_NAME, contactName);
        values.put(MySQLiteOpenHelper.COLUMN_SENDER_OR_RECEIVER, senderOrReceiver);
        values.put(MySQLiteOpenHelper.COLUMN_MESSAGE, message);
        values.put(MySQLiteOpenHelper.COLUMN_TIMESTAMP, timestamp);
        long insertId = database.insert(MySQLiteOpenHelper.TABLE_CHAT_HISTORY, null,
                values);
        Cursor cursor = database.query(MySQLiteOpenHelper.TABLE_CHAT_HISTORY,
                allColumns, MySQLiteOpenHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        ChatHistoryBean newChatHistoryBean = cursorToChatHistory(cursor);
        cursor.close();
        return newChatHistoryBean;
    }

    public void deleteChatHistory(ChatHistoryBean chatHistoryBean) {
        long id = chatHistoryBean.getId();
        System.out.println("Chat history deleted with id: " + id);
        database.delete(MySQLiteOpenHelper.TABLE_CHAT_HISTORY, MySQLiteOpenHelper.COLUMN_ID
                + " = " + id, null);
    }

    public ArrayList<ChatHistoryBean> getAllChatHistory() {
        ArrayList<ChatHistoryBean> chatHistoryBeanList = new ArrayList<ChatHistoryBean>();

        Cursor cursor = database.query(MySQLiteOpenHelper.TABLE_CHAT_HISTORY,
                allColumns, null, null, null, null, null);


        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ChatHistoryBean chatHistory = cursorToChatHistory(cursor);
            chatHistoryBeanList.add(chatHistory);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return chatHistoryBeanList;
    }

    public ArrayList<ChatHistoryBean> getContactChatHistory(String mobileNumber) {
        ArrayList<ChatHistoryBean> chatHistoryBeanArrayList = new ArrayList<>();

        Cursor cursor = dbHelper.getReadableDatabase().
                rawQuery("select * from " + MySQLiteOpenHelper.TABLE_CHAT_HISTORY + " where " +
                        MySQLiteOpenHelper.COLUMN_MOBILE_NUMBER + " = ?", new String[] { mobileNumber });

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ChatHistoryBean chatHistory = cursorToChatHistory(cursor);
            chatHistoryBeanArrayList.add(chatHistory);
            cursor.moveToNext();
        }
        cursor.close();
        return chatHistoryBeanArrayList;
    }

    public ArrayList<String> getDistinctContactNumbers() {
        ArrayList<String> distinctContactArrayList = new ArrayList<>();
//        Cursor cursor = database.rawQuery("select distinct " + MySQLiteOpenHelper.COLUMN_CONTACT_NAME + " , "
//                                + MySQLiteOpenHelper.COLUMN_MOBILE_NUMBER + " from "
//                                + MySQLiteOpenHelper.TABLE_CHAT_HISTORY, null);
        String[] columns = {MySQLiteOpenHelper.COLUMN_CONTACT_NAME,
                MySQLiteOpenHelper.COLUMN_MOBILE_NUMBER};
        Cursor cursor = database.query(true,MySQLiteOpenHelper.TABLE_CHAT_HISTORY, columns, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String chatHistory = cursorToChatHistoryContactDetails(cursor).getMobileNumber();
            distinctContactArrayList.add(chatHistory);
            cursor.moveToNext();
        }
        cursor.close();
        return distinctContactArrayList;
    }

    public ArrayList<String> getDistinctContactNames() {
        ArrayList<String> distinctContactArrayList = new ArrayList<>();
//        Cursor cursor = database.rawQuery("select distinct " + MySQLiteOpenHelper.COLUMN_CONTACT_NAME + " , "
//                + MySQLiteOpenHelper.COLUMN_MOBILE_NUMBER + " from "
//                + MySQLiteOpenHelper.TABLE_CHAT_HISTORY, null);
        String[] columns = {MySQLiteOpenHelper.COLUMN_CONTACT_NAME,
                MySQLiteOpenHelper.COLUMN_MOBILE_NUMBER};
        Cursor cursor = database.query(true,MySQLiteOpenHelper.TABLE_CHAT_HISTORY, columns, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String chatHistory = cursorToChatHistoryContactDetails(cursor).getContactName();
            distinctContactArrayList.add(chatHistory);
            cursor.moveToNext();
        }
        cursor.close();
        return distinctContactArrayList;
    }


    private ChatHistoryBean cursorToChatHistory(Cursor cursor) {
        ChatHistoryBean chatHistoryBean = new ChatHistoryBean();
        chatHistoryBean.setId(cursor.getLong(0));
        chatHistoryBean.setMobileNumber(cursor.getString(1));
        chatHistoryBean.setContactName(cursor.getString(2));
        chatHistoryBean.setSenderOrReceiver(Boolean.parseBoolean(cursor.getString(3)));
        chatHistoryBean.setMessage(cursor.getString(4));
        chatHistoryBean.setTimestamp(Timestamp.valueOf(cursor.getString(5)));
        return chatHistoryBean;
    }

    private ChatHistoryBean cursorToChatHistoryContactDetails(Cursor cursor) {
        ChatHistoryBean chatHistoryBean = new ChatHistoryBean();
        chatHistoryBean.setContactName(cursor.getString(0));
        chatHistoryBean.setMobileNumber(cursor.getString(1));
        return chatHistoryBean;
    }




}
