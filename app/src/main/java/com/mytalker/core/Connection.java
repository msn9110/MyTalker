package com.mytalker.core;


import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.utils.Utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class Connection {
    int reconnect=0;
    static final int threshold=10;
    public static boolean WifiMode=true;
    public String IP_SERVER;
    public static int PORT = 8988;
    private static final UUID MY_UUID = UUID.fromString("0001101-0000-1000-8000-00805F9B34FB");
    private DataOutputStream out; //for transfer
    private Socket socket;
    private Context context;
    public Connection(Context ctx){
        this.context=ctx;
        if(WifiMode){
            String ipv4 = Utils.getIPAddress(true); // IPv4
            try {
                byte[] gw=InetAddress.getByName(ipv4).getAddress();
                gw[3]=1;
                IP_SERVER=InetAddress.getByAddress(gw).getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

    }
    //===============================connection=======================================================
    public void ConnectToDisplay() {
        if(WifiMode){
            try {
                InetAddress serverAddr = InetAddress.getByName(IP_SERVER);
                //設定Server IP位置,port
                SocketAddress sc_addr = new InetSocketAddress(serverAddr, PORT);
                socket = new Socket();
                //與Server連線，timeout時間2秒
                socket.connect(sc_addr, 2000);
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                Toast.makeText(context, "連線失敗", Toast.LENGTH_SHORT).show();
            }
        }else {
            try {
                BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();

            } catch (Exception e) {
                Toast.makeText(context, "連線失敗", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void Send(String msg){
        try {
            out.writeUTF(msg);
            reconnect=0;
            Toast.makeText(context,"成功傳送",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            if(reconnect<threshold){
                reconnect++;
                ConnectToDisplay();
                Send(msg);
            }
        }
    }

    public void terminate(boolean ConnectMode) {
        if (ConnectMode) {
            try {
                out.close();
                if(WifiMode)
                    socket.close();

            } catch (Exception e) {
                Log.e("terminate",e.toString());
            }
        }
    }
}
