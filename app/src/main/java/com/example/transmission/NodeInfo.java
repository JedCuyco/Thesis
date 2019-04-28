package com.example.transmission;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class NodeInfo extends AppCompatActivity {

    TextView uid;
    TextView batt;
    TextView signal;
    TextView gateway;
    WifiManager wifi_Manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_info);
        instantiateVariables();
        setValues();
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

    private void instantiateVariables()
    {
        uid= (TextView) findViewById(R.id.user_ID);
        batt= (TextView) findViewById(R.id.batteryPercentage);
        signal= (TextView) findViewById(R.id.signalStrength);
        gateway= (TextView) findViewById(R.id.gatewayNode);
        wifi_Manager= (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void setValues()
    {
        Bundle extras = getIntent().getExtras();
        String mbatt = extras.getString("battery_percentage");
        String msignal = extras.getString("signal_strength");
        WifiInfo info= wifi_Manager.getConnectionInfo();
        /*uid.setText(info.getMacAddress());*/
        uid.setText(getWFDMacAddress().toLowerCase());
        batt.setText(mbatt+ "%");
        signal.setText(msignal+ "dbm");
        gateway.setText("N/A");

    }
}
