package com.utils;


import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
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
        hello = " " + hello;
        for (int i = 0; i < msg.length; i++)
            msg[i] = "";
        int count = proccessString(hello);
        int speaker = 0;
        List<String> list = Arrays.asList(msg).subList(0, count + 1);
        for(String s : list) {
            Log.i(TAG, s);
            if(speaker==0){
                tw.speak(s, TextToSpeech.QUEUE_ADD, null);
            } else {
                en.speak(s, TextToSpeech.QUEUE_ADD, null);
            }
            speaker++;
            speaker %= 2;
        }
    }

    private String[] msg = new String[200];
    private int proccessString(String hello){
        int previous = 0, current; // 0 denotes tw
        int count = 0;
        for (int i = 0; i < hello.length(); i++)
        {
            char ch = hello.charAt(i);
            current = ((Check.check_eng(ch) ? 1 : 0));
            if (Check.check_sign(ch))
                current = previous;

            if (current != previous)
            {
                previous = current;
                count++;
            }
            msg[count] += String.valueOf(ch);
        }
        return count;
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
