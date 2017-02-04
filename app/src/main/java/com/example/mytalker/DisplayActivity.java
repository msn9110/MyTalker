package com.example.mytalker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.utils.DisplayManager;
import com.utils.Speaker;



public class DisplayActivity extends Activity {

    //public static final String END = "!!!@@@###";
    TextView tvDisplay;
    private  Handler handler = new Handler();
    Speaker speaker;
    DisplayManager displayManager;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        //initialize
        speaker = new Speaker(getApplicationContext());
        tvDisplay = (TextView) findViewById(R.id.txtDisplay);
        tvDisplay.setText(R.string.empty);
        displayManager = new DisplayManager(tvDisplay, speaker, handler);
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayManager.execute();
    }
    @Override
    protected void onPause(){
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        displayManager.end();
    }
}
