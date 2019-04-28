package com.example.transmission;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Transmission extends MainActivity {


    String nodeAddress;
    Button btnSend;
    EditText messageInput;
    ListView conversationList;

    serverClass serverClass;
    clientClass clientClass;
    sendReceive sr;

    ConversationClass conversationClass;
   /* ArrayAdapter<String> arrayAdapter;*/



    List<String> convoMessages = new ArrayList<String>();

    public void instantiation()
    {
        btnSend= (Button)findViewById(R.id.button_chatbox_send);
        messageInput= (EditText)findViewById(R.id.edittext_chatbox);
        conversationList= (ListView) findViewById(R.id.convo_list);
        conversationClass= new ConversationClass();
        /*arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, inboxArray);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        menu.getItem(1).setVisible(true);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isBusy=true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmission_module);
        instantiation();
        setTitle(getIntent().getStringExtra("contact_name"));
        nodeAddress=getIntent().getStringExtra("destination_number");
        if(getIntent().getBooleanExtra("isInternal", false))
        {
            if(getIntent().getStringExtra("contact_name").isEmpty())
            {

            }
            else
                this.connect(getIntent().getIntExtra("position", 0));
        }

        EventListener();
        conversationList.setAdapter(arrayAdapter);


    }
    private void EventListener()
    {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.println("Hello");
                if(getIntent().getBooleanExtra("isInternal",false))
                {
                    String msg="0;"+getWFDMacAddress().toLowerCase()+";"+nodeAddress+";"+messageInput.getText().toString()+";"+ Build.MODEL;
                    System.out.println(msg);
                    sendReceive.write(msg.getBytes());
                    inboxArray.add(msg);
                }
                else
                {
                    if(mSignalStrength<0)
                    {
                        System.out.println("Hello");
                        String phoneNum = conversationClass.reformatNumber(getIntent().getStringExtra("destination_number"));
                        conversationClass.sendSMS(phoneNum, messageInput.getText().toString());
                        inboxArray.add(messageInput.getText().toString());
                    }
                    else
                    {
                        System.out.println("Helloelse");
                        String msg="1;"+getWFDMacAddress().toLowerCase()+";"+"0a:23:b2:fc:4a:b4"+";"+nodeAddress+";"+messageInput.getText().toString()+";"+ Build.MODEL;
                    }
                }

            }
        });
    }


    private void connect(int position)
    {
        System.out.println("test");
        //final WifiP2pDevice device= deviceArray[position];
        config.deviceAddress= nodeAddress;
        config.groupOwnerIntent=0;
        //status.setText(device.deviceAddress);
        nodeManager.connect(nodeChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Connected to "+ nodeAddress, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
            }
        });
    }


    WifiP2pManager.ConnectionInfoListener connectionInfoListener= new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            InetAddress groupOwnerAddress= info.groupOwnerAddress;
            //status.setText(info.groupOwnerAddress.toString());
            if(info.groupFormed && info.isGroupOwner)
            {
                Toast.makeText(getApplicationContext(), "Host", Toast.LENGTH_SHORT).show();
                serverClass= new serverClass();
                serverClass.start();
            }else if(info.groupFormed)
            {
                Toast.makeText(getApplicationContext(), "Client", Toast.LENGTH_SHORT).show();
                clientClass= new clientClass(groupOwnerAddress);
                clientClass.start();
            }
        }
    };




    /*private class sendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public sendReceive(Socket socket)
        {
            this.socket=socket;
            try{
                inputStream=this.socket.getInputStream();
                outputStream=this.socket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer=new byte[1024];
            int bytes;

            while(socket!=null)
            {
                try {

                    bytes=inputStream.read(buffer);

                    if(bytes>0)
                    {
                        //status.setText(bytes);
                        handler.obtainMessage(MESSAGE_READ, bytes, -1,buffer).sendToTarget();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes)
        {

            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/







}