package com.mytalker.core;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.utils.Speaker;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.Queue;


public class MyDisplayManager extends AsyncTask<Void, Void, Void> {
    private static String TAG = "## MyDisplayManager";
    private TextView mDisplay;
    private Handler mHandler;
    private Context mContext;
    private Queue<String> mBuffer;
    private MyDisplay myDisplay;
    private boolean toReceive;
    public MyDisplayManager(Context context, Handler handler, TextView display){
        mContext = context;
        mHandler = handler;
        mDisplay = display;
        mBuffer = new LinkedList<>();
    }

    @Override
    protected void onPreExecute() {
        toReceive = true;
        myDisplay = new MyDisplay(mContext);
        myDisplay.start();
        Log.i(TAG, "onPreExecute");
    }

    @Override
    protected Void doInBackground(Void... voids) {
        final int PORT = 8988;
        byte[] buffer = new byte[1024];
        Log.i(TAG, "Start");
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            DatagramSocket socket = new DatagramSocket(PORT);
            while (toReceive){
                socket.receive(packet);
                String msg = new String(buffer, 0, packet.getLength(), "UTF-8");
                Log.i(TAG,"Receive : " + msg);
                mBuffer.add(msg);
            }
            socket.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    @Override
    protected void onCancelled() {
        myDisplay.cancel();
        toReceive = false;
        Log.i(TAG, "onCanceled");
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        myDisplay.cancel();
        toReceive = false;
        Log.i(TAG, "onPostExecute");
    }

    private class MyDisplay extends Thread{
        private Speaker speaker;
        private boolean toContinue;
        MyDisplay(Context context){
            speaker = new Speaker(context);
        }

        void cancel(){
            toContinue = false;
            speaker.shutdown();
        }

        @Override
        public void run() {
            toContinue = true;
            while (toContinue){
                synchronized (this){
                    if(!mBuffer.isEmpty() && speaker.isNotSpeaking()){
                        final String message = mBuffer.remove();
                        if (message.length() > 0) {
                            final int font = 6000 / (message.length() + 40);
                            Log.i(TAG, message);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mDisplay.setTextSize(font);
                                    mDisplay.setText(message);
                                    speaker.speak(message);
                                }
                            });
                        }
                    }
                }
            }
        }
    }
}
