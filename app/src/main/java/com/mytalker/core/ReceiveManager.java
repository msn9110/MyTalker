package com.mytalker.core;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.utils.Check;
import com.utils.TransferMode;

import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class ReceiveManager extends Thread implements SpeakingListener {
    private static String TAG = "## ReceiveManager";

    private TextView mDisplay;
    private Handler mHandler;
    private Context mContext;
    private Speaker mSpeaker;

    private boolean toReceive;
    private DatagramSocket socket;

    public ReceiveManager(Context context, Handler handler, TextView display){
        mContext = context;
        mHandler = handler;
        mDisplay = display;
        Log.d(TAG, "Created !");
    }

    private void onPreExecute() {
        toReceive = true;
        mSpeaker = new Speaker(mContext);
        mSpeaker.setSpeakingListener(this);
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
                switch (Check.checkMode(msg)){
                    case TransferMode.IMODE_TEXT:
                        msg = msg.replaceFirst(TransferMode.MODE_TEXT, "");
                        Log.i(TAG,"Receive : " + msg);
                        mSpeaker.setEnable(true);
                        mSpeaker.addSpeak(msg);
                        break;
                    case TransferMode.IMODE_PAUSE:
                        mSpeaker.pause();
                        break;
                    case TransferMode.IMODE_STOP:
                        mSpeaker.stop();
                        break;
                }
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

    @Override
    public void onPreSpeak(final String message) {
        final int font = 6000 / (message.length() + 40);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDisplay.setTextSize(font);
                mDisplay.setText(message);
            }
        });
    }
}
