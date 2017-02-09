package com.mytalker.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.mytalker.R;
import com.mytalker.core.MyDisplayManager;
import com.utils.Speaker;


public class DisplayActivity extends AppCompatActivity {

    //public static final String END = "!!!@@@###";
    String TAG = "## DisplayActivity";
    TextView tvDisplay;
    MyDisplayManager displayManager;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        //initialize
        tvDisplay = (TextView) findViewById(R.id.txtDisplay);
        tvDisplay.setText(R.string.empty);
        Log.i(TAG, "Init done !");
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayManager = new MyDisplayManager(getApplicationContext(), handler, tvDisplay);
        displayManager.start();
    }
    @Override
    protected void onPause(){
        super.onPause();
        displayManager.cancel();
        try {
            displayManager.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}
