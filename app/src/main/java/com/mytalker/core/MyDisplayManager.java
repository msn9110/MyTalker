package com.mytalker.core;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.Queue;


public class MyDisplayManager extends Thread {
    private static String TAG = "## MyDisplayManager";

    private TextView mDisplay;
    private Handler mHandler;
    private Context mContext;
    private Speaker mSpeaker;

    private boolean toReceive;
    private DatagramSocket socket;

    public MyDisplayManager(Context context, Handler handler, TextView display){
        mContext = context;
        mHandler = handler;
        mDisplay = display;
        Log.d(TAG, "Created !");
    }

    private void onPreExecute() {
        toReceive = true;
        mSpeaker = new Speaker(mContext, mHandler, mDisplay);
        Log.i(TAG, "onPreExecute");
    }

    @Override
    public void run() {
        onPreExecute();
        final int PORT = 8988;
        byte[] buffer = new byte[1024];
        Log.i(TAG, "Start");
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket = new DatagramSocket(PORT);
            while (toReceive){
                socket.receive(packet);
                String msg = new String(buffer, 0, packet.getLength(), "UTF-8");
                Log.i(TAG,"Receive : " + msg);
                mSpeaker.addSpeak(msg);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void onCancel() {
        mSpeaker.shutdown();
        toReceive = false;
        socket.close();
        Log.i(TAG, "onCancel");
    }

    public void cancel(){
        onCancel();
    }
}
