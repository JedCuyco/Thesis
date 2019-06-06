package com.example.transmission;

import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class viewRoutingTable extends AppCompatActivity {
    DatabaseHelper mDatabaseHelper;
    ArrayList<String> macAddressArr= new ArrayList<>();
    ArrayList<String> routeInfo= new ArrayList<>();
    ListView routeTable;
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_routing_table);
        routeTable= (ListView)findViewById(R.id.routing_table);
        mDatabaseHelper=new DatabaseHelper(this);
        actionListener();
        viewAllEntries();
    }

    public void actionListener()
    {


        routeTable.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder buildera= new AlertDialog.Builder(viewRoutingTable.this);
                StringBuilder buffer= new StringBuilder();
                Cursor res= mDatabaseHelper.dataExists(routeTable.getItemAtPosition(position).toString());
                res.moveToFirst();
                final String tempAddress= res.getString(0);
                buildera.setNeutralButton("Delete Route", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabaseHelper.deleteName(tempAddress);
                        macAddressArr.remove(position);
                        routeInfo.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                });
                //System.out.println("position "+ position);

                //System.out.println(res.getString(0)+ "this res");
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
        System.out.println("hello");
        Cursor res = mDatabaseHelper.getData();
        if(res.getCount()==0)
        {
            showRoutes("No routes.");
            System.out.println("hello");
            return ;
        }



        StringBuffer buffer= new StringBuffer();
        while(res.moveToNext())
        {
            System.out.println(res.getString(0));
            macAddressArr.add(res.getString(0));
            buffer.append(res.getString(0)+" : ");
            buffer.append(res.getString(1)+" : ");
            buffer.append(res.getInt(2)+" : ");
            buffer.append(res.getString(3)+"\n");
            routeInfo.add(buffer.toString());
            buffer.setLength(0);
        }

        showRoutes(buffer.toString());
        System.out.println(buffer.toString());
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
