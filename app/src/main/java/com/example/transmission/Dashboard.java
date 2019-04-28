package com.example.transmission;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Dashboard extends AppCompatActivity {
    private Button buttonSendMessage, buttonSendManet, buttonRouting, buttonSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        buttonSendMessage = (Button) findViewById(R.id.buttonSendMessage);
        buttonSendManet= (Button) findViewById(R.id.buttonSendMANET);
        buttonRouting= (Button) findViewById(R.id.buttonRouting);
        buttonSettings=(Button) findViewById(R.id.buttonSettings);

        buttonSendManet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSendMessage();
            }
        });
    }

    public void openSendMessage(){
        Intent intent= new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
