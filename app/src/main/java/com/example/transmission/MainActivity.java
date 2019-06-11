package com.example.transmission;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.codinguser.android.contactpicker.ContactsPickerActivity;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_CONTACTS = 2;

    ListView list, convoList;
    FloatingActionButton fab;

    WifiManager wifi_Manager;
    WifiP2pManager nodeManager;
    WifiP2pManager.Channel nodeChannel;
    WifiP2pConfig config;

    BroadcastReceiver nodeReceiver;
    IntentFilter nodeFilter;

    List<WifiP2pDevice> peers= new ArrayList<WifiP2pDevice>();
    ArrayList<String> deviceNameArray= new ArrayList<>();
    WifiP2pDevice[] deviceArray;
    ArrayList<String>  inboxArray= new ArrayList<String>();
    ArrayList<String>  readMessagesArray= new ArrayList<String>();

    static final int MESSAGE_READ=1;

    serverClass serverClass;
    clientClass clientClass;
    sendReceive sendReceive;

    TelephonyManager mTelephonyManager;
    MyPhoneStateListener mPhoneStatelistener;
    int mSignalStrength = 0;
    int battery = 0;

    DatabaseHelper mDatabaseHelper;
    ArrayAdapter<String> arrayAdapter;

    private HashMap<String, RouteEntry> entries = new HashMap<String, RouteEntry>();

    boolean isBusy= false;

    NotificationCompat.Builder notification;

    ConversationClass conversationClass;

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            battery=level;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instatiateVariables();
        eventListener();
        onOff();
        discoverPeers();

        mPhoneStatelistener = new MyPhoneStateListener();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStatelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            mSignalStrength = signalStrength.getGsmSignalStrength();
            mSignalStrength = (2 * mSignalStrength) - 113; // -> dBm
        }
    }

    Handler handler= new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch(msg.what)
            {
                case MESSAGE_READ:
                    byte[] readbuff= (byte[]) msg.obj;
                    String tempMsg= new String(readbuff, 0, msg.arg1);
                    System.out.println(tempMsg);
                    String[] packet=tempMsg.split(";");
                    checkPacketType(packet, tempMsg);
                    inboxArray.add(tempMsg);
                    arrayAdapter.notifyDataSetChanged();
                    break;
            }
            return true;
        }
    });

    private void checkPacketType(final String [] packet, final String tempPacket)
    {
        //String test= packet[2];
        int index=0;
        //System.out.println(packet[3]+ "pa");

        if(packet[0].equals("0"))
        {
            if(packet[2].equals(getWFDMacAddress().toLowerCase()))
            {
                readMessagesArray.add(packet[4]+": "+packet[3]);
                notification.setSmallIcon(R.drawable.chat);
                notification.setTicker(packet[1]+ " sent you a message");
                notification.setWhen(System.currentTimeMillis());
                notification.setContentTitle("New Internal Message");
                notification.setContentText(packet[3]);

                Intent vma= new Intent(this, MainActivity.class);
                PendingIntent pendingIntent= PendingIntent.getActivity(this, 0, vma, PendingIntent.FLAG_UPDATE_CURRENT );
                notification.setContentIntent(pendingIntent);

                NotificationManager nm= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(2580, notification.build());
            }
            else
            {
                if(entries.containsKey(packet[2]))
                {
                    final String nextHop= entries.get(packet[2]).getNextHopMacAddress();
                    disconnect();

                    final Handler delaydiscover = new Handler();
                    delaydiscover.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 5s = 5000ms
                            discoverPeers();
                        }
                    }, 5000);

                    final Handler connect = new Handler();
                    connect.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int index=0;
                            for (int j=0; j<deviceArray.length; j++)
                            {
                                if(deviceArray[j].deviceAddress.equals(nextHop))
                                {
                                    index=j;
                                }
                            }
                            connect(deviceNameArray.get(index));

                        }
                    }, 20000);

                    final Handler relay = new Handler();
                    relay.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendReceive.write(tempPacket.getBytes());
                        }
                    }, 30000);
                }
                else
                {
                    //message sender, route is not available/obsolete.
                }

            }

        }
        else if(packet[0].equals("1"))
        {
            if(packet[2].equals(getWFDMacAddress().toLowerCase())&& mSignalStrength<0)
            {
                String phonenum= conversationClass.reformatNumber(packet[3]);
                conversationClass.sendSMS(phonenum, packet[4]);
            }
        }
        else if(packet[0].equals("2"))
        {
            System.out.println(packet[3]);
            String [] entries= packet[3].split("\\\\n");
            int no_entries= entries.length;
            for(int i=0; i<no_entries; i++)
            {
                String [] entry= entries[i].split("\\|");
                if(mDatabaseHelper.CheckData(entry[0]))
                {
                    if(mDatabaseHelper.getHopCount(entry[0])>Integer.parseInt(entry[2])+1)
                    {
                        mDatabaseHelper.updateHopCount(entry[0],Integer.parseInt(entry[2]+1));
                        mDatabaseHelper.updateNextHop(entry[0], entry[1]);
                    }

                }
                else
                {
                    //add()
                    mDatabaseHelper.addData(entry[0], entry[1],Integer.parseInt(entry[2])+1, false, Build.MODEL, 0, 0);
                }

            }
        }
        else if(packet[0].equals("3"))
        {
            Cursor res= mDatabaseHelper.getData();
            StringBuffer buffer= new StringBuffer();
            while(res.moveToNext())
            {
                buffer.append(res.getString(0)+"|");
                buffer.append(getWFDMacAddress().toLowerCase()+"|");
                buffer.append(res.getInt(2)+"|");
                buffer.append(res.getString(3)+"\n");
            }
            String msg1="2;"+getWFDMacAddress().toLowerCase()+";"+res.getCount()+";"+buffer.toString();
            System.out.println(buffer.toString());
            sendReceive.write(msg1.getBytes());
        }
        else if(packet[0].equals("4"))
        {
            if(entries.containsKey(packet[1]))
            {
                System.out.println("Hello");
                entries.get(packet[1]).setBatteryPercenteage(Integer.parseInt(packet[2]));
                entries.get(packet[1]).setPhoneSignal(Integer.parseInt(packet[3]));
            }
        }

    }

    private void onOff()
    {
        if(!wifi_Manager.isWifiEnabled())
        {
            wifi_Manager.setWifiEnabled(true);
        }
    }

    private void discoverPeers()
    {
        nodeManager.discoverPeers(nodeChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }
    private void eventListener()
    {

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ContactsPickerActivity.class);
                System.out.println("Hello");
                startActivityForResult(intent, REQUEST_CODE_PICK_CONTACTS);
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent= new Intent(view.getContext(), Transmission.class);
                Cursor res= mDatabaseHelper.dataExists(list.getItemAtPosition(position).toString());
                res.moveToNext();
                intent.putExtra("contact_name", list.getItemAtPosition(position).toString());
                intent.putExtra("destination_number", res.getString(1));
                intent.putExtra("isInternal", true);
                intent.putExtra("position", position);
                startActivity(intent);
                //connect(position);
            }
        });

    }

    private void connect(String position)
    {
        //final WifiP2pDevice device= deviceArray[position];
        final Cursor res= mDatabaseHelper.dataExists(position);
        res.moveToFirst();
        config.deviceAddress= res.getString(1);
        config.groupOwnerIntent=0;
        //status.setText(device.deviceAddress);
        nodeManager.connect(nodeChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Connected to "+ res.getString(1), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void disconnect() {
        if (nodeManager != null && nodeChannel != null) {
            nodeManager.requestGroupInfo(nodeChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && nodeManager != null && nodeChannel != null
                            && group.isGroupOwner()) {
                        nodeManager.removeGroup(nodeChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                //Toast.makeText(getApplicationContext(), "Removed", Toast.LENGTH_SHORT).show();
                                //Log.d(TAG, "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                //Log.d(TAG, "removeGroup onFailure -" + reason);
                            }
                        });
                    }

                    else if (group != null && nodeManager != null && nodeChannel != null
                            && !group.isGroupOwner()) {
                        nodeManager.removeGroup(nodeChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                //Toast.makeText(getApplicationContext(), "Removed", Toast.LENGTH_SHORT).show();
                                //Log.d(TAG, "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                //Log.d(TAG, "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }
            });
        }

        nodeManager.requestGroupInfo(nodeChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (group != null && nodeManager != null && nodeChannel != null
                        && group.isGroupOwner()) {
                    sendReceive.interrupt();
                    serverClass.interrupt();
                }

                else if (group != null && nodeManager != null && nodeChannel != null
                        && !group.isGroupOwner()) {
                    sendReceive.interrupt();
                    clientClass.interrupt();
                }
            }
        });


    }
    private void instatiateVariables()
    {
        list= (ListView) findViewById(R.id.peer_list);
        convoList= (ListView) findViewById(R.id.convo_list);
        fab =  findViewById(R.id.fab);


        wifi_Manager= (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        nodeManager= (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        nodeChannel= nodeManager.initialize(this,getMainLooper(), null);
        config= new WifiP2pConfig();
        config.groupOwnerIntent=15;
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, inboxArray);


        nodeReceiver= new WiFiDirectBroadcastReceiver(nodeManager, nodeChannel, this);
        nodeFilter= new IntentFilter();
        nodeFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        nodeFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        nodeFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        nodeFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mDatabaseHelper = new DatabaseHelper(this);

        notification= new NotificationCompat.Builder(this, "ChannelID01");
        notification.setAutoCancel(true);

        conversationClass= new ConversationClass();
    }

    WifiP2pManager.PeerListListener peerListener= new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            nodeManager.discoverPeers(nodeChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {

                }
            });

            if(!peerList.getDeviceList().equals(peers))
            {
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                //deviceNameArray= new String[peerList.getDeviceList().size()];
                deviceArray= new WifiP2pDevice[peerList.getDeviceList().size()];
                int index=0;

                for(WifiP2pDevice device: peerList.getDeviceList())
                {
                    if(mDatabaseHelper.CheckData(device.deviceAddress)==false)
                    {
                        mDatabaseHelper.addData(device.deviceAddress, device.deviceAddress, 1, false, device.deviceName, 0, 0);
                    }
                    if(!deviceNameArray.contains(device.deviceAddress))
                    {
                        deviceNameArray.add(device.deviceAddress);
                    }
                    deviceArray[index]=device;
                    index++;
                }
                Cursor res= mDatabaseHelper.getData();

                while (res.moveToNext())
                {
                    if(!deviceNameArray.contains(res.getString(0)))
                        deviceNameArray.add(res.getString(0));
                }

                ArrayAdapter<String> adapter= new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                list.setAdapter(adapter);
            }
            if(peers.size()==0)
            {
                config.groupOwnerIntent=15;
                Toast.makeText(getApplicationContext(), "No Device Found", Toast.LENGTH_SHORT).show();
            }
        }
    };

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


                /*if(!isBusy)
                {
                    //isBusy=true;
                    Intent intent= new Intent(getApplicationContext(), Transmission.class);
                    startActivity(intent);
                }
                else
                {
                    serverClass= new serverClass();
                    serverClass.start();
                }*/

            }else if(info.groupFormed)
            {
                Toast.makeText(getApplicationContext(), "Client", Toast.LENGTH_SHORT).show();
                clientClass= new clientClass(groupOwnerAddress);
                clientClass.start();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(nodeReceiver, nodeFilter);


    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(nodeReceiver);
    }

    public class serverClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try{

                if(Thread.interrupted())
                {
                    System.out.println("closed socket");
                    serverSocket.close();
                    return ;
                }



                serverSocket= new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(8888));
                socket=serverSocket.accept();
                sendReceive= new sendReceive(socket);
                sendReceive.start();
            }catch(IOException e){
                e.printStackTrace();

            }
            try
            {
                socket=serverSocket.accept();
                sendReceive= new sendReceive(socket);
                sendReceive.start();
            }catch(IOException e){
                e.printStackTrace();

            }

        }
    }

    public class sendReceive extends Thread{
        public Socket socket;
        public InputStream inputStream;
        public OutputStream outputStream;

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
                    if(Thread.interrupted())
                        return ;

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
                //System.out.println("test");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class clientClass extends Thread{
        Socket socket;
        String hostAddress;

        public clientClass(InetAddress hostAddress)
        {
            this.hostAddress=hostAddress.getHostAddress();
            this.socket=new Socket();
        }

        @Override
        public void run() {
            try {

                if(Thread.interrupted())
                {
                    socket.close();
                    return ;
                }



                //socket.setReuseAddress(true);
                socket.connect(new InetSocketAddress(hostAddress, 8888),500);
                sendReceive= new sendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Client", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        menu.getItem(1).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_nodeInfo:
                Intent i= new Intent(this, NodeInfo.class);
                Bundle extras= new Bundle();
                extras.putString("battery_percentage", Integer.toString(battery));
                extras.putString("signal_strength", Integer.toString(mSignalStrength));
                i.putExtras(extras);
                startActivity(i);
                return true;
            case R.id.action_settings:
                String nodeInfo= "4;"+getWFDMacAddress().toLowerCase()+";"+Integer.toString(battery)+";"+mSignalStrength;
                sendReceive.write(nodeInfo.getBytes());
                return true;
            case R.id.disconnect:
                disconnect();
                return true;
            case R.id.discover:
                discoverPeers();
                return true;
            case R.id.routingTable:
                Intent vrt=new Intent(this, viewRoutingTable.class);
                startActivity(vrt);
                return true;
                /*Iterator it = entries.entrySet().iterator();
                int entrysize= entries.size();
                String shared= entrysize+"";
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    RouteEntry entree= entries.get(pair.getKey());
                    shared+="="+pair.getKey()+";"+entree.getNextHopMacAddress()+";"+entree.getHopCount()+";"+entree.getPhoneSignal()+";"+entree.getBatteryPercenteage()+";"+entree.getDestinationMac();
                    *//*System.out.println(pair.getKey() + " = " + entree.getNextHopMacAddress());*//*
                    System.out.println(shared);
                    it.remove(); // avoids a ConcurrentModificationException
                }
                return true;*/
            case R.id.viewMessages:
                Intent vma= new Intent(this, viewMessages.class);
                vma.putStringArrayListExtra("inboxMessages", readMessagesArray);
                startActivity(vma);
                return true;
            case R.id.simulate:
                String msg="0;"+getWFDMacAddress().toLowerCase()+";"+"06:d6:aa:48:95:76"+";"+"this is a simulation"+";"+ Build.MODEL;
                sendReceive.write(msg.getBytes());
            case R.id.test:
                Cursor res= mDatabaseHelper.getData();
                StringBuffer buffer= new StringBuffer();
                String[] testsample;
                while(res.moveToNext())
                {
                    buffer.append(res.getString(0)+":");
                    buffer.append(res.getString(1)+":");
                    buffer.append(res.getInt(2)+":");
                    buffer.append(res.getString(3)+"\n");
                }
                String msg1="2;"+getWFDMacAddress().toLowerCase()+";"+res.getCount()+";"+buffer.toString();
                System.out.println(msg1);
                testsample=msg1.split(";");
                System.out.println(testsample[3]);
                String []test1;
                test1=testsample[3].split("////n");
                System.out.println(test1[0]);
            case R.id.rreq:
                String route_req="3;"+getWFDMacAddress().toLowerCase();
                sendReceive.write(route_req.getBytes());
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case REQUEST_CODE_PICK_CONTACTS:
                if(resultCode == Activity.RESULT_OK) {
                    String phoneNumber = (String) data.getExtras().get(ContactsPickerActivity.KEY_PHONE_NUMBER);
                    Phonenumber.PhoneNumber formattedNumber = null;
                    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                    try {
                        formattedNumber = phoneUtil.parse(phoneNumber, "PH");
                    } catch (NumberParseException e) {
                        e.printStackTrace();
                    }
                    String formatted = phoneUtil.format(formattedNumber, PhoneNumberUtil.PhoneNumberFormat.E164);

                    Toast.makeText(this, "Phone number found: " + formatted, Toast.LENGTH_SHORT).show();
                    //edit this part
                    Intent intent = new Intent(this, Transmission.class);
                    intent.putExtra("contact_name", (String) data.getExtras().get(ContactsPickerActivity.KEY_CONTACT_NAME));
                    intent.putExtra("destination_number", formatted);
                    intent.putExtra("isExternal", false);
                    startActivity(intent);
                }
        }
    }


    public String getWFDMacAddress(){
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ntwInterface : interfaces) {

                if (ntwInterface.getName().equalsIgnoreCase("p2p0")) {
                    byte[] byteMac = ntwInterface.getHardwareAddress();
                    if (byteMac==null){
                        return null;
                    }
                    StringBuilder strBuilder = new StringBuilder();
                    for (int i=0; i<byteMac.length; i++) {
                        strBuilder.append(String.format("%02X:", byteMac[i]));
                    }

                    if (strBuilder.length()>0){
                        strBuilder.deleteCharAt(strBuilder.length()-1);
                    }

                    return strBuilder.toString();
                }

            }
        } catch (Exception e) {
            //Log.d(TAG, e.getMessage());
        }
        return null;
    }

}
