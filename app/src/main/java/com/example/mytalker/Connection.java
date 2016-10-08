package com.example.mytalker;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class Connection {
    public String IP_SERVER;
    public static int PORT = 8988;
    private DataOutputStream out; //for transfer
    private Socket socket;
    private Context context;
    public Connection(Context ctx){
        this.context=ctx;
        String ipv4 = Utils.getIPAddress(true); // IPv4
        try {
            byte[] gw=InetAddress.getByName(ipv4).getAddress();
            gw[3]=1;
            IP_SERVER=InetAddress.getByAddress(gw).getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    //===============================connection=======================================================
    public boolean ConnectToDisplay() {
        try {
            InetAddress serverAddr;
            SocketAddress sc_add;            //設定Server IP位置
            serverAddr = InetAddress.getByName(IP_SERVER);
            //設定port
            sc_add = new InetSocketAddress(serverAddr, PORT);
            socket = new Socket();
            //與Server連線，timeout時間2秒
            socket.connect(sc_add, 2000);
            out = new DataOutputStream(socket.getOutputStream());
            return true;
        } catch (IOException e) {
            Toast.makeText(context, "連線失敗", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void Send(String msg){
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void terminate(boolean ConnectMode) {
        if (ConnectMode) {
            try {
                out.close();
                socket.close();
            } catch (Exception e) {
                Log.e("terminate",e.toString());
            }
        }
    }
}
