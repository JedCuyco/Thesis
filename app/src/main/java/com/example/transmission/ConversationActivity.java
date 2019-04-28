package com.example.transmission;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConversationActivity extends AppCompatActivity {

    private static final String TAG = "ConversationActivity";

    private EditText txtMessage;
    private ListView messagesContainer;
    private Button btnSendSMS;
    private ConversationMessageAdapter adapter;
    private ArrayList<ConversationMessageItem> chatHistory;
    private ConversationMessageItem chatMessage;
    private String phoneNo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getIntent().getStringExtra("contact_name"));
        setContentView(R.layout.activity_conversation);

        btnSendSMS = (Button) findViewById(R.id.btnSendSMS);
        txtMessage = (EditText) findViewById(R.id.txtMessage);

        btnSendSMS.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //phoneNo = txtPhoneNo;
                phoneNo = getIntent().getStringExtra("destination_number");
                String message = "1;"+txtMessage.getText().toString();

                if (phoneNo.charAt(0) == '0') {
                    StringBuilder deleteZero = new StringBuilder(phoneNo);
                    deleteZero.deleteCharAt(0);
                    String withoutZero = deleteZero.toString();

                    StringBuilder setAreaCode = new StringBuilder(withoutZero);
                    setAreaCode.insert(0, "+63");
                    String newPhoneNo = setAreaCode.toString();

                    if (phoneNo.length() > 0 && message.length() > 0)
                        sendSMS(newPhoneNo, message);
                    else
                        Toast.makeText(getBaseContext(),
                                "Please enter both phone number and message.",
                                Toast.LENGTH_SHORT).show();
                } else {
                    if (phoneNo.length() > 0 && message.length() > 0)
                        sendSMS(phoneNo, message);
                    else
                        Toast.makeText(getBaseContext(),
                                "Please enter both phone number and message.",
                                Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public String reformatNumber(String phoneNum) {
        if (phoneNum.charAt(0) == '0') {
            StringBuilder deleteZero = new StringBuilder(phoneNum);
            deleteZero.deleteCharAt(0);
            String withoutZero = deleteZero.toString();

            StringBuilder setAreaCode = new StringBuilder(withoutZero);
            setAreaCode.insert(0, "+63");
            String newPhoneNo = setAreaCode.toString();
            return newPhoneNo;
        }


        return phoneNum;
    }



    public void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case android.telephony.SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case android.telephony.SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case android.telephony.SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case android.telephony.SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        android.telephony.SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    public void displayMessage(ConversationMessageItem message) {

        adapter.add(message);
        adapter.notifyDataSetChanged();
        //scroll();
    }

    private void loadChatHistory() {
        ChatHistoryDAO chatHistoryDAO = new ChatHistoryDAO(getApplicationContext());
        chatHistoryDAO.open();
        try {
            List<ChatHistoryBean> chatHistoryBeanList = chatHistoryDAO.getContactChatHistory(getIntent().getStringExtra("destination_number"));

            chatHistory = new ArrayList<>();
            for (int i = 0; i < chatHistoryBeanList.size(); i++) {
                ConversationMessageItem item = new ConversationMessageItem();
                item.setId(chatHistoryBeanList.get(i).getId());
                item.setMessage(chatHistoryBeanList.get(i).getMessage());
                item.setDate(chatHistoryBeanList.get(i).getTimestamp());
                if (chatHistoryBeanList.get(i).isSenderOrReceiver()) {
                    item.setMe(true);
                } else item.setMe(false);
                chatHistory.add(item);
            }

            for (int i = 0; i < chatHistory.size(); i++) {
                ConversationMessageItem message = chatHistory.get(i);
                displayMessage(message);
            }
        } catch (Exception e) {
            Log.d("ConversationActivity", "No chat history from this number yet");
            e.printStackTrace();
        }
        chatHistoryDAO.close();
    }

}
