package com.mytalker.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.mytalker.R;
import com.utils.DisplayManager;
import com.utils.Speaker;

import java.util.LinkedList;
import java.util.Queue;


public class DisplayActivity extends Activity {

    //public static final String END = "!!!@@@###";
    String TAG = "## DisplayActivity";
    TextView tvDisplay;
    Speaker speaker;
    DisplayManager displayManager;
    Queue<String> buffer = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        //initialize
        speaker = new Speaker(this);
        tvDisplay = (TextView) findViewById(R.id.txtDisplay);
        tvDisplay.setText(R.string.empty);
        displayManager = new DisplayManager(mHandler);
        Log.i(TAG, "Init done !");
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayManager.execute();
        End = false;
        display.start();
    }
    @Override
    protected void onPause(){
        super.onPause();
        End = true;
        displayManager.end();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        speaker.shutdown();
    }

    private Handler handler = new Handler();
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String message = (String) msg.obj;
            buffer.add(message);
        }
    };

    private boolean End;
    private Thread display = new Thread(){
        @Override
        public void run() {
            super.run();
            while (!End){
                synchronized (this){
                    if(!buffer.isEmpty() && speaker.isNotSpeaking()){
                        final String message = buffer.remove();
                        if (message.length() > 0) {
                            final int font = 6000 / (message.length() + 40);
                            Log.i(TAG, message);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    tvDisplay.setTextSize(font);
                                    tvDisplay.setText(message);
                                    speaker.speak(message);
                                }
                            });
                        }
                    }
                }
            }
        }
    };
}
