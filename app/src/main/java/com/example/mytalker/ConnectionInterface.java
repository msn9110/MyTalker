package com.example.mytalker;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ConnectionInterface extends Activity{

    @Override
    protected void onCreate(Bundle savedInstantState){
        super.onCreate(savedInstantState);
        setContentView(R.layout.connect_interface);
        Button widi=(Button)findViewById(R.id.btn_widi);
        widi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setClass(getApplicationContext(),WiFiDirectActivity.class);
                startActivity(intent);
                ConnectionInterface.this.finish();
            }
        });
    }
}
