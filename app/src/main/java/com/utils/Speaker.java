package com.utils;


import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class Speaker {
    //=============================================語音==============================================
    private TextToSpeech tw, en;
    private static final String TAG = "## Speaker";
    public Speaker(Context context){

        tw = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                setLanguage(status, tw, Locale.TAIWAN, "TW");
            }
        });

        en = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                setLanguage(status, en, Locale.ENGLISH, "EN");
            }
        });
    }

    private void setLanguage(int status, TextToSpeech tts, Locale locale, String lang){
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            int result;
            result = tts.setLanguage(locale);//<<<===================================
            if (result == TextToSpeech.LANG_MISSING_DATA) { ///|| result == TextToSpeech.LANG_NOT_SUPPORTED
                Log.e(TAG, lang);
            }
            else{
                float speed = (float) 0.75;
                tts.setSpeechRate(speed);
                Log.i(TAG, lang);
            }
        } else {
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }

    public void speak(String hello) {
        hello=" "+hello;
        String[] msg=new String[50];
        for(int i=0;i<50;i++)
            msg[i]="";
        int previous=0,count=0,speaker=0,current;
        char first=hello.charAt(0);
        if(Check.check_eng(first))
            speaker=1;
        for(int i=0;i<hello.length();i++)
        {
            current=0;
            char ch1=hello.charAt(i);
            if(Check.check_eng(ch1))
                current=1;
            else if(Check.check_sign(ch1))
                current=previous;

            if(current!=previous)
            {
                previous=current;
                count++;
            }
            msg[count]+=String.valueOf(ch1);
        }

        for(int i=0;i<=count; i++) {
            Log.i(TAG, msg[i]);
            if(speaker==0){
                tw.speak(msg[i],TextToSpeech.QUEUE_ADD,null);
            } else {
                en.speak(msg[i],TextToSpeech.QUEUE_ADD,null);
            }
            speaker++;
            speaker%=2;
        }
    }

    public void stop(){
        if (tw != null)
            tw.stop();
        if (en != null)
            en.stop();
    }

    public void shutdown(){

        if (tw != null){
            tw.stop();
            tw.shutdown();
        }
        if (en != null){
            en.stop();
            en.shutdown();
        }
        tw=en=null;
    }
    public boolean isNotSpeaking(){
        return (!tw.isSpeaking() || !en.isSpeaking());
    }
}
