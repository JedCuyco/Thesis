package com.example.transmission;

import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class viewRoutingTable extends AppCompatActivity {
    DatabaseHelper mDatabaseHelper;
    ArrayList<String> macAddressArr= new ArrayList<>();
    ListView routeTable;


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
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder buildera= new AlertDialog.Builder(viewRoutingTable.this);
                buildera.setNeutralButton("Delete Route", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                //System.out.println("position "+ position);
                StringBuilder buffer= new StringBuilder();
                Cursor res= mDatabaseHelper.dataExists(routeTable.getItemAtPosition(position).toString());
                res.moveToFirst();
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
            buffer.append("Neighbor: "+ res.getString(0)+"\n");
            buffer.append("Next Hop: "+ res.getString(1)+"\n");
            buffer.append("Hops: "+ res.getInt(2)+"\n");
            buffer.append("Status: "+ res.getString(3)+"\n");

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
        ArrayAdapter<String> adapter= new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, macAddressArr);
        routeTable.setAdapter(adapter);
    }
}
