package com.example.mytalker;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;



public class DisplayActivity extends Activity {

    public static final String END="!!!@@@###";
    public static final int PORT = 8988;
    public boolean terminal=true;
    public static boolean WifiMode=true;
    String[] buf=new String[100];//queue for playing tts
    int count=0;
    TextView tvDisplay,tvStatus;
    private  Handler handler=new Handler();
    private ServerSocket serverSocket=null;
    private BluetoothServerSocket BTSocket=null;
    Speaker speaker;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        //initialize
        speaker=new Speaker(getApplicationContext());
        for(int i=0;i<100;i++)
            buf[i]="";
        tvDisplay=(TextView)findViewById(R.id.textView);
        tvStatus=(TextView)findViewById(R.id.textView3);
        tvDisplay.setText(R.string.empty);
    }


    @Override
    protected void onStart(){
        super.onStart();
        new Thread(socket_server).start();
    }

    ReentrantLock lock=new ReentrantLock();
    int current=0;
    boolean EndDisplay=false;
    String message="";
    Thread display=new Thread(new Runnable() {
        @Override
        public void run() {

            while (!EndDisplay){

                if(count>0 && speaker.isNotSpeaking()){
                    message=buf[current];
                    System.out.println("CURRENT : "+current);
                    System.out.println("COUNT : "+count);
                    handler.post(new Runnable() {
                        public void run() {
                            if(message.length()>0 && speaker.isNotSpeaking()) {
                                lock.lock();
                                int font=6000/(message.length()+40);
                                tvDisplay.setTextSize((float)font);
                                tvDisplay.setText(message);
                                speaker.speak(message);
                                message=buf[current]="";
                                current=(current+1) % buf.length;
                                count--;
                                lock.unlock();
                            }
                        }
                    });
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });
    private Runnable socket_server = new Runnable(){
        public void run(){

            try{
                DataInputStream inputStream;
                if(WifiMode){
                    //建立serverSocket
                    serverSocket = new ServerSocket(PORT);
                    //接收連線
                    handler.post(new Runnable() {
                        public void run() {
                            tvStatus.setText(R.string.listening);
                        }
                    });
                    Socket client = serverSocket.accept();
                    handler.post(new Runnable() {
                        public void run() {
                            tvStatus.setText(R.string.connected);
                        }
                    });
                    inputStream=new DataInputStream(client.getInputStream());
                } else {
                    BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
                    //建立serverSocket
                    BTSocket=adapter.listenUsingRfcommWithServiceRecord("MyDisplay",UUID.fromString("0001101-0000-1000-8000-00805F9B34FB"));
                    //接收連線
                    handler.post(new Runnable() {
                        public void run() {
                            tvStatus.setText(R.string.listening);
                        }
                    });
                    BluetoothSocket client = BTSocket.accept();
                    handler.post(new Runnable() {
                        public void run() {
                            tvStatus.setText(R.string.connected);
                        }
                    });
                    inputStream=new DataInputStream(client.getInputStream());
                }

                receive(inputStream);
            }catch(IOException e) {
                final String err="建立socket失敗";
                handler.post(new Runnable() {
                    public void run() {
                        tvStatus.setText(err);
                    }
                });
                EndDisplay=true;
                terminal=false;
                DisplayActivity.this.finish();
            }
        }
    };

    private void receive(DataInputStream in){
        try {
            int i=0;
            display.start();
            //接收資料
            String line;
            do{
                line = in.readUTF();
                if(line.length()>0){
                    System.out.println("COUNT : "+count);
                    buf[i]=line;
                    System.out.println("Receive "+i+" : "+buf[i]);
                    i=(i+1) % buf.length;
                    lock.lock();
                    count++;
                    lock.unlock();
                }
            }while (!line.equals(END));
            terminal=true;
        } catch (Exception e) {
            handler.post(new Runnable() {
                public void run() {
                    tvStatus.setText("傳送失敗");
                }
            });
            //in.close();
            EndDisplay=true;
            terminal=false;
            DisplayActivity.this.finish();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        speaker.shutdown();
        this.finish();
    }

    @Override
    protected void onPause(){
        super.onPause();
        try {
            if (WifiMode)
                serverSocket.close();
            else
                BTSocket.close();
        }catch (IOException e){
            Log.d(WiFiDirectActivity.TAG,e.toString());
        }
    }
}
