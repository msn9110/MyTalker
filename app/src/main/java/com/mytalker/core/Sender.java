package com.mytalker.core;


import android.util.Log;

import com.utils.NetworkManager;
import com.utils.Utils;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import com.utils.TransferMode;

public class Sender {
    private static String TAG = "## Sender";
    private InetAddress serverAddr;
    public Sender(){
        String IP = NetworkManager.getBroadcast();
        Log.i(TAG, "My broadcast IP is " + IP);
        try {
            serverAddr = InetAddress.getByName(IP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void remoteControl(int mode){
        send(makePacket(mode,""));
    }

    public void sendMessage(String msg){
        send(makePacket(TransferMode.IMODE_TEXT, msg));
    }

    private void send(String msg){
        final int port = 8988;
        try {
            byte buffer[] = Utils.getUTF8Bytes(msg);                 // 將訊息字串 msg 轉換為位元串。
            // 封裝該位元串成為封包 DatagramPacket，同時指定傳送對象。
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddr, port);
            DatagramSocket socket = new DatagramSocket();    // 建立傳送的 UDP Socket。
            socket.send(packet);                             // 傳送
            Log.i(TAG, "Packet Send !");
            socket.close();                                 // 關閉 UDP socket.
        } catch (Exception e) { e.printStackTrace(); }    // 若有錯誤產生，列印函數呼叫堆疊。
    }

    private String makePacket(int mode, String data){
        String packet = "";
        switch (mode){
            case TransferMode.IMODE_TEXT:
                packet = TransferMode.MODE_TEXT + data;
                break;
            case TransferMode.IMODE_PAUSE:
                packet = TransferMode.MODE_PAUSE;
                break;
            case TransferMode.IMODE_STOP:
                packet = TransferMode.MODE_STOP;
                break;
        }
        return packet;
    }
}
