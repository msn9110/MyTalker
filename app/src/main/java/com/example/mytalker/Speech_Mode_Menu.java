package com.example.mytalker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Speech_Mode_Menu extends Activity {
    Button[] mode=new Button[5];

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_menu);

        mode[0]=(Button)findViewById(R.id.btn_speech_main);
        mode[1]=(Button)findViewById(R.id.btn_speech_sub1);
        mode[2]=(Button)findViewById(R.id.btn_speech_sub2);
        mode[3]=(Button)findViewById(R.id.btn_speech_sub3);
        mode[4]=(Button)findViewById(R.id.btn_speech_sub4);


        mode[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeechMode.path="Main";
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SpeechMode.class);
                startActivity(intent);
            }
        });

        mode[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeechMode.path="Sub1";
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SpeechMode.class);
                startActivity(intent);
            }
        });

        mode[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeechMode.path="Sub2";
                Intent intent =new Intent();
                intent.setClass(getApplicationContext(), SpeechMode.class);
                startActivity(intent);
            }
        });

        mode[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeechMode.path="Sub3";
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SpeechMode.class);
                startActivity(intent);
            }
        });

        mode[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeechMode.path="Sub4";
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SpeechMode.class);
                startActivity(intent);
            }
        });
    }
}
