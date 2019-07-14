package com.example.transmission;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class routingConfig extends AppCompatActivity {

    DatabaseHelper distress_db;
    EditText in_address, in_next, in_count, in_name, in_battery, in_signal;
    Spinner  spinner_next, spinner_stat;
    Button addNeigbor;
    ArrayList<String> db_macs = new ArrayList<>();
    ArrayList<String> status = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing_config);
        instantiation();
    }

    public void instantiation()
    {
        distress_db= DatabaseHelper.getInstance(this);

        in_address= (EditText) findViewById(R.id.input_address);
        in_name=(EditText) findViewById(R.id.input_deviceName);
        in_count= (EditText) findViewById(R.id.input_count);
        in_battery= (EditText) findViewById(R.id.input_battery);
        in_signal= (EditText) findViewById(R.id.input_signal);
        addNeigbor= (Button) findViewById(R.id.btn_add);
        spinner_next= (Spinner) findViewById(R.id.spinner_nh);
        spinner_stat= (Spinner) findViewById(R.id.spinner_status);

        addNeigbor.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(getIntent().getBooleanExtra("isEditable", true))
                {
                    System.out.println(spinner_next.getSelectedItem().toString());
                    distress_db.updateNextHop(getIntent().getStringExtra("mac_address"), spinner_next.getSelectedItem().toString());
                    distress_db.updateHopCount(getIntent().getStringExtra("mac_address"), Integer.parseInt(in_count.getText().toString()));
                    distress_db.updateBattery(getIntent().getStringExtra("mac_address"), Integer.parseInt(in_battery.getText().toString()));
                    distress_db.updateSignal(getIntent().getStringExtra("mac_address"), Integer.parseInt(in_signal.getText().toString()));
                    distress_db.updateStatus(getIntent().getStringExtra("mac_address"), spinner_stat.getSelectedItemPosition());
                    System.out.println("HELLO");
                }
            }
        });
        Cursor res=distress_db.getData();

        while(res.moveToNext())
        {
            db_macs.add(res.getString(0));
        }

        status.add("False");
        status.add("True");

        ArrayAdapter<String> spinnerAdapter= new ArrayAdapter<String>(this, R.layout.spinner_item, db_macs);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_next.setAdapter(spinnerAdapter);

        ArrayAdapter<String> spinnerAdapter2= new ArrayAdapter<String>(this, R.layout.spinner_item, status);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_stat.setAdapter(spinnerAdapter2);
        trustTheProcess();
    }

    public void trustTheProcess()
    {

        if(getIntent().getBooleanExtra("isEditable", true))
        {
            in_address.setText(getIntent().getStringExtra("mac_address"));
            in_address.setEnabled(false);
            in_name.setText(getIntent().getStringExtra("device_name"));
            in_name.setEnabled(false);

            if(db_macs.contains(getIntent().getStringExtra("next_hop")))
            {
                spinner_next.setSelection(db_macs.indexOf(getIntent().getStringExtra("next_hop")));
            }


            int temp_hc= getIntent().getIntExtra("hop_count", 0);
            in_count.setText(Integer.toString(temp_hc));

            if(getIntent().getBooleanExtra("status",false))
                spinner_stat.setSelection(0);
            else
                spinner_stat.setSelection(1);

            int temp_battery=getIntent().getIntExtra("battery", 0);
            int temp_signal=getIntent().getIntExtra("signal", 0);
            in_battery.setText(Integer.toString(temp_battery));
            in_signal.setText(Integer.toString(temp_signal));

            addNeigbor.setText("Edit");

        }

        else
        {

        }


    }
}
