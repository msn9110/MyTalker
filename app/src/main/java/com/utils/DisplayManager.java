package com.utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.Deque;


public class DisplayManager extends AsyncTask <Void, Void, Void> {

    private TextView mDisplay;
    private Speaker mSpeaker;
    private Deque<String> mBuffer;
    private Handler mHandler;
    private boolean ToReceive = true;
    private Looper mLooper;
    private MyDisplay myDisplay;

    public DisplayManager(TextView display, Speaker speaker, Handler handler){
        mDisplay = display;
        mSpeaker = speaker;
        mHandler = handler;
        mBuffer = new ArrayDeque<>();
        myDisplay = new MyDisplay();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        final int PORT = 8988;
        byte[] buffer = new byte[1024];
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            DatagramSocket socket = new DatagramSocket(PORT);
            myDisplay.run();
            while (ToReceive){
                socket.receive(packet);
                String msg = new String(buffer, 0, packet.getLength(), "UTF-8");
                mBuffer.addLast(msg);
            }
            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void end(){
        ToReceive = false;
        if (mLooper != null){
            mSpeaker.shutdown();
            mLooper.quit();
            mLooper = null;
        }
    }

    private class MyDisplay extends Thread{
        @Override
        public void run() {
            Looper.prepare();
            mLooper = Looper.myLooper();
            if(!mBuffer.isEmpty() && mSpeaker.isNotSpeaking()){
                final String msg = mBuffer.removeFirst();
                if(msg.length() > 0){
                    final int font = 6000 / (msg.length() + 40);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mDisplay.setTextSize(font);
                            mDisplay.setText(msg);
                            mSpeaker.speak(msg);
                        }
                    });
                }
            }
            Looper.loop();
        }
    }
}
