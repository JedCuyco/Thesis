package com.example.transmission;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.codinguser.android.contactpicker.ContactsPickerActivity;

import java.util.ArrayList;

public class viewRoutingTable extends AppCompatActivity {
    DatabaseHelper mDatabaseHelper;
    ArrayList<String> macAddressArr= new ArrayList<>();
    ArrayList<String> routeInfo= new ArrayList<>();
    ListView routeTable;
    ArrayAdapter<String> adapter;
    FloatingActionButton fab;

    String tempAddress, tempNextHop, tempName;
    int tempHopCount, tempBattery, tempSignal;
    boolean tempStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_routing_table);
        routeTable= (ListView)findViewById(R.id.routing_table);
        mDatabaseHelper=DatabaseHelper.getInstance(this);
        fab= findViewById(R.id.fab_route);
        actionListener();
        viewAllEntries();
    }

    public void actionListener()
    {

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), routingConfig.class);
                intent.putExtra("isEditable", false);
                startActivity(intent);

            }
        });

        routeTable.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder buildera= new AlertDialog.Builder(viewRoutingTable.this);

                StringBuilder buffer= new StringBuilder();

                Cursor res= mDatabaseHelper.dataExists(routeTable.getItemAtPosition(position).toString());
                res.moveToFirst();
                tempAddress= res.getString(0);
                tempNextHop= res.getString(1);
                tempHopCount= res.getInt(2);
                tempStatus= res.getInt(3)>0;
                tempName= res.getString(4);
                tempBattery= res.getInt(5);
                tempSignal= res.getInt(6);
                buildera.setNeutralButton("Delete Route", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabaseHelper.deleteName(tempAddress);
                        //macAddressArr.remove(position);
                        //routeInfo.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                });
                buildera.setPositiveButton("Edit",  new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        Intent rc= new Intent(getApplicationContext(), routingConfig.class);
                        rc.putExtra("mac_address", tempAddress);
                        rc.putExtra("next_hop", tempNextHop);
                        rc.putExtra("hop_count",tempHopCount);
                        rc.putExtra("status", tempStatus);
                        rc.putExtra("device_name", tempName);
                        rc.putExtra("battery", tempBattery);
                        rc.putExtra("signal", tempSignal);
                        rc.putExtra("isEditable", true);
                        startActivity(rc);
                    }
                });
                buffer.append("Neighbor: "+ res.getString(0)+"\n");
                buffer.append("Next Hop: "+ res.getString(1)+"\n");
                buffer.append("Hops: "+ res.getInt(2)+"\n");
                buffer.append("Status: "+ res.getString(3)+"\n");

                buildera.setCancelable(true);
                buildera.setTitle("Routing Table");
                buildera.setMessage(buffer.toString());
                buildera.show();
            }
        });
    }

    public void viewAllEntries()
    {

        Cursor res = mDatabaseHelper.getData();
        if(res.getCount()==0)
        {
            showRoutes("No routes.");
            return ;
        }



        StringBuffer buffer= new StringBuffer();
        while(res.moveToNext())
        {
            macAddressArr.add(res.getString(0));
            buffer.append(res.getString(0)+" : ");
            buffer.append(res.getString(1)+" : ");
            buffer.append(res.getInt(2)+" : ");
            buffer.append(res.getString(3)+" : ");
            buffer.append(res.getString(4)+" : ");
            buffer.append(res.getInt(5)+" : ");
            buffer.append(res.getInt(6)+"\n");
            routeInfo.add(buffer.toString());
            buffer.setLength(0);
        }

        showRoutes(buffer.toString());
    }

    public void showRoutes(String Message)
    {
        /*AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setNeutralButton("Delete Route", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });*/
        //builder.setCancelable(true);
        //builder.setTitle("Routing Table");
        //builder.setMessage(Message);
        //builder.show();
        adapter= new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_2,android.R.id.text1, macAddressArr){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(macAddressArr.get(position));
                text2.setText(routeInfo.get(position));
                return view;
            }
        };

        routeTable.setAdapter(adapter);
    }
}
