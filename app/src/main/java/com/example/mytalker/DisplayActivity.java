package com.example.mytalker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;



public class DisplayActivity extends Activity {

    public static final String END="!!!@@@###";
    public static final int PORT = 8988;
    public boolean terminal=true;
    String[] buf=new String[100];//queue for playing tts
    int count=0;
    TextView tvDisplay,tvStatus;
    private  Handler handler;
    private ServerSocket serverSocket=null;
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
    protected void onDestroy(){
        super.onDestroy();
        speaker.shutdown();
        this.finish();
    }

    @Override
    protected void onPause(){
        super.onPause();
        try {
            serverSocket.close();
        }catch (IOException e){
            Log.d(WiFiDirectActivity.TAG,e.toString());
        }
    }
    int current=0;
    @Override
    protected void onStart(){
        super.onStart();
        handler=new Handler();

        //建立Thread
        Thread server = new Thread(socket_server);
        //啟動Thread
        server.start();
    }
    boolean end=false;
    ReentrantLock lock=new ReentrantLock();
    String message="";
    Thread display=new Thread(new Runnable() {
        @Override
        public void run() {

            while (!end){

                if(count>0 && speaker.isNotSpeaking()){
                    message=buf[current];
                    System.out.println("CURRENT : "+current);
                    System.out.println("COUNT : "+count);
                    handler.post(new Runnable() {
                        public void run() {
                            int size=message.length();
                            float font=120;
                            if(size>30)
                                font=100;
                            else if(size>80)
                                font=75;
                            else if(size>120)
                                font=50;
                            if(message.length()>0 && speaker.isNotSpeaking()) {
                                lock.lock();
                                tvDisplay.setTextSize(font);
                                tvDisplay.setText(message);
                                speaker.speak(message);
                                message="";
                                buf[current]="";
                                current++;
                                current%=100;
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
                DataInputStream in = new DataInputStream(client.getInputStream());
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
                            i++;
                            i%=100;
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
                    in.close();
                    end=true;
                    terminal=false;
                    DisplayActivity.this.finish();
                }
            }catch(IOException e) {
                final String err="建立socket失敗";
                handler.post(new Runnable() {
                    public void run() {
                        tvStatus.setText(err);
                    }
                });
                end=true;
                terminal=false;
                DisplayActivity.this.finish();
            }
        }
    };
}
