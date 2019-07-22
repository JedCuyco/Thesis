package com.example.transmission;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager nodeManager;
    private WifiP2pManager.Channel nodeChannel;
    private MainActivity nodeActivity;


    public WiFiDirectBroadcastReceiver(WifiP2pManager nodeManager, WifiP2pManager.Channel nodeChannel, MainActivity nodeActivity)
    {
        this.nodeManager=nodeManager;
        this.nodeChannel=nodeChannel;
        this.nodeActivity=nodeActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action= intent.getAction();

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action))
        {
            System.out.println("STATE CHANGED ACTION");
            int state= intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1 );

            if(state==WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            {
                Toast.makeText(context, "WIFI IS ON", Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(context, "WIFI IS OFF", Toast.LENGTH_SHORT).show();
            }
        }else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action))
        {
            //System.out.println("PEERS CHANGED");

            if (nodeManager!=null)
            {
                nodeManager.requestPeers(nodeChannel, nodeActivity.peerListener);
            }
        }
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {
            System.out.println("CONNECTION CHANGED");
            if(nodeManager==null)
            {
                return;
            }
            NetworkInfo networkInfo=intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo.isConnected())
            {
                nodeManager.requestConnectionInfo(nodeChannel, nodeActivity.connectionInfoListener);
            }

            else
            {
                //
            }
        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action))
        {
            System.out.println("THIS DEVICE CHANGED");
        }
        //else if


    }
}
