package com.example.mytalker;


import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class Speaker implements TextToSpeech.OnInitListener {
    //=============================================語音==============================================
    private TextToSpeech tw,en;
    private static final String TAG = "SPEAKER";
    boolean mode=true;
    public Speaker(Context context){
        System.out.println(Locale.getDefault().toString());
        tw=new TextToSpeech(context,this);
        en=new TextToSpeech(context,this);
    }
    // Implements TextToSpeech.OnInitListener.
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            language();
        }

        else {
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }
    private void language(){
        float speed=(float)0.8;
        int result;
        if(!mode){
            result = en.setLanguage(Locale.US);//<<<===================================

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                System.out.println("EN ERROR");
            }
            else{
                en.setSpeechRate(speed);
                System.out.println("EN READY");
            }
        }

        else {
            mode=false;
            result = tw.setLanguage(Locale.TAIWAN);//<<<===================================
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                System.out.println("TW ERROR");
            }
            else{
                tw.setSpeechRate(speed);
                System.out.println("TW READY");
            }
        }

    }

    public void speak(String hello) {
        //tw.speak(hello,TextToSpeech.QUEUE_ADD,null);
        hello=" "+hello;
        String[] msg=new String[50];
        for(int i=0;i<50;i++)
            msg[i]="";
        int previous=0,count=0,speaker=0,current;
        char first=hello.charAt(0);
        if(first>='a' && first<='z' || first>='A' && first<='Z')
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

        for(int i=0;i<=count;i++)
        {
            System.out.println(msg[i]);
            if(speaker==0){
                tw.speak(msg[i],TextToSpeech.QUEUE_ADD,null);
                speaker++;
                speaker%=2;
            }
            else {
                en.speak(msg[i],TextToSpeech.QUEUE_ADD,null);
                speaker++;
                speaker%=2;
            }
        }
    }

    public void stop(){
        if (tw != null) {
            tw.stop();
            tw.shutdown();
        }
        if (en != null) {
            en.stop();
            en.shutdown();
        }
        tw=en=null;
    }

    public boolean isNotSpeaking(){
        return (!tw.isSpeaking() || !en.isSpeaking());
    }
}
