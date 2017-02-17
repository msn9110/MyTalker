package com.mytalker.core;


import android.content.Context;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

import com.utils.Check;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

public class Speaker implements Serializable {
    private static final long serialVersionUID = -7060210544600464481L;
    //=============================================語音==============================================
    private TextToSpeech tw, en;
    private Deque<String> queue = new ArrayDeque<>();
    private static final String TAG = "## Speaker";
    private SpeakerQueueMonitor monitor;
    public Speaker(Context context){
        initSpeaker(context);
        monitor = new SpeakerQueueMonitor(this);
        monitor.start();
    }
    public Speaker(Context context, Handler handler, TextView display){
        initSpeaker(context);
        monitor = new SpeakerQueueMonitor(this, handler, display);
        monitor.start();
    }

    private void initSpeaker(Context context){
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

    public void addSpeak(String string){
        queue.add(string);
    }
    public void setEnable(boolean enable){
        monitor.isPaused = !enable;
    }

    public void stop(){
        queue.clear();
        stopCurrent();
    }

    @SuppressWarnings("deprecation")
    private void speak(String hello) {
        hello = " " + hello;
        for (int i = 0; i < msg.length; i++)
            msg[i] = "";
        int count = processString(hello);
        int speaker = 0;
        List<String> list = Arrays.asList(msg).subList(0, count + 1);
        for(String s : list) {
            Log.i(TAG, s);
            if(speaker == 0){
                tw.speak(s, TextToSpeech.QUEUE_ADD, null);
            } else {
                en.speak(s, TextToSpeech.QUEUE_ADD, null);
            }
            speaker++;
            speaker %= 2;
        }
    }

    private void speakSync(String hello) {
        speak(hello);
        BusySpeakerListener listener = new BusySpeakerListener(this);
        listener.start();
        try {
            listener.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            if(listener.isAlive()){
                listener.cancel();
                listener.interrupt();
            }
            Log.i(TAG, "Interrupted in Speak Sync");
        }
    }

    private String[] msg = new String[200];
    private int processString(String hello){
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

    public boolean pause(){
        if (! isNotSpeaking()){
            String tmp = queue.peek();
            queue.addFirst(tmp);
        }
        stopCurrent();
        return monitor.pause();
    }

    private void stopCurrent(){
        if (tw != null)
            tw.stop();
        if (en != null)
            en.stop();
    }

    public void shutdown(){
        stop();
        if (tw != null)
            tw.shutdown();
        if (en != null)
            en.shutdown();
        tw = en = null;
        monitor.stopMonitor();
        monitor.interrupt();
        try {
            monitor.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private boolean isNotSpeaking(){
        return (!tw.isSpeaking() || !en.isSpeaking());
    }

    private class SpeakerQueueMonitor extends Thread {
        private Speaker speaker;
        private boolean toMonitor;
        private boolean isPaused = false;
        private TextView mDisplay;
        private Handler mHandler;

        SpeakerQueueMonitor(Speaker speaker){
            this.speaker = speaker;
        }

        SpeakerQueueMonitor(Speaker speaker, Handler handler, TextView display){
            this.speaker = speaker;
            mHandler = handler;
            mDisplay = display;
        }

        @Override
        public void run() {
            toMonitor = true;
            while (toMonitor){
                if (! queue.isEmpty() && ! isPaused){
                    final String message = queue.peek();
                    if (message != null && message.length() > 0) {
                        final int font = 6000 / (message.length() + 40);
                        Log.i("## SpeakerQueueMonitor", message);
                        if (mDisplay != null)
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mDisplay.setTextSize(font);
                                    mDisplay.setText(message);
                                }
                            });
                        speaker.speakSync(message);
                        queue.poll(); // replace remove
                    }
                }
            }
        }

        private boolean pause(){
            isPaused = !isPaused;
            return isPaused;
        }

        private void stopMonitor(){
            toMonitor = false;
        }
    }

    private class BusySpeakerListener extends Thread {
        private Speaker speaker;
        private boolean toSpeak;

        BusySpeakerListener(Speaker speaker) {
            this.speaker = speaker;
        }

        @Override
        public void run() {
            try {
                toSpeak = true;
                Thread.sleep(20);
                while (!speaker.isNotSpeaking() && toSpeak) // speaker is busy now
                    Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.i("## Listener", "interrupt !");
            }
        }

        void cancel(){
            toSpeak = false;
        }
    }
}