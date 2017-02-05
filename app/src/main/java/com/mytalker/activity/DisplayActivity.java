package com.mytalker.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.mytalker.R;
import com.utils.DisplayManager;
import com.utils.Speaker;



public class DisplayActivity extends AppCompatActivity {

    //public static final String END = "!!!@@@###";
    TextView tvDisplay;
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
        displayManager = new DisplayManager(handler);
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
        speaker.shutdown();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            synchronized (this){
                if(speaker.isNotSpeaking()){
                    String message = (String) msg.obj;
                    if(message.length() > 0){
                        int font = 6000 / (message.length() + 40);
                        tvDisplay.setTextSize(font);
                        tvDisplay.setText(message);
                        speaker.speak(message);
                    }
                }
            }
        }
    };
}
