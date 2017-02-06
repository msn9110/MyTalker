package com.utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.Deque;


public class DisplayManager extends AsyncTask <Void, Void, Void> {

    private String TAG = "## DisplayManager";
    private Handler mHandler;
    private boolean ToReceive;

    public DisplayManager(Handler handler){
        mHandler = handler;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ToReceive = true;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        final int PORT = 8988;
        byte[] buffer = new byte[1024];
        Log.i(TAG, "Start");
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            DatagramSocket socket = new DatagramSocket(PORT);
            while (ToReceive){
                socket.receive(packet);
                String msg = new String(buffer, 0, packet.getLength(), "UTF-8");
                Log.i(TAG,"Receive : " + msg);
                Message message =new Message();
                message.obj = msg;
                mHandler.sendMessage(message);
            }
            socket.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.i(TAG, "End");
    }

    public void end(){
        ToReceive = false;
    }
}
