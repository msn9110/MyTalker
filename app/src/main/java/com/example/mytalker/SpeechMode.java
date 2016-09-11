package com.example.mytalker;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import static android.os.StrictMode.setThreadPolicy;


public class SpeechMode extends ListActivity implements AdapterView.OnItemClickListener {
    public static String path="Default";
    private String parentPath;
    File Selection;
    private DataOutputStream out; //for transfer
    public static boolean con = false;
    public static final String IP_SERVER = "192.168.49.1";
    public static int PORT = 8988;
    private Socket socket;
    static final String TAG="SpeechMode";
    TextView mydir,empty;
    Handler handler=new Handler();
    ProgressDialog dialog;
    Speaker speaker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_list);
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            setThreadPolicy(policy);
        }

        mydir=(TextView)findViewById(R.id.mydir);
        empty=(TextView)findViewById(R.id.nofile);

        this.setListAdapter(this.createListAdapter());
        ListView lv = (ListView) this.findViewById(android.R.id.list);
        lv.setOnItemClickListener(this);
    }

    private ListAdapter createListAdapter() {
        List<String> list = new ArrayList<>();
        File sdDir = Environment.getExternalStorageDirectory();
        File Dir = new File(sdDir, "MySpeaker/"+path);
        mydir.setText(Dir.getPath());
        this.parentPath = Dir.getPath();
        Log.d(TAG, "根目錄：" + this.parentPath);
        File[] files = Dir.listFiles();
        if(files.length==0)
            empty.setVisibility(View.VISIBLE);
        for (File f : files) {
            if (f.isDirectory()) {
                continue;
            }
            list.add(f.getName());
            Log.d(TAG, "加入檔案：" + f.getName());
        }
        return new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, list);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Selection=new File(this.parentPath,((TextView) view).getText().toString());
        new Thread(SpeakFile).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        dialog=ProgressDialog.show(SpeechMode.this,"請稍後","正在載入語音模組");
        new Thread(new Runnable() {
            @Override
            public void run() {
                speaker=new Speaker(getApplicationContext());
                try {
                    Thread.sleep(2000);
                }catch (Exception e){
                    Log.e(TAG,e.toString());
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
            }
        }).start();
        if (con)
            ConnectToDisplay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speaker.stop();
        terminate();
    }

    private void ConnectToDisplay() {
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
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "連線失敗", Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

    private void terminate() {
        if (con) {
            try {
                out.close();
                socket.close();
            } catch (Exception e) {
                Log.e("Terminate",e.toString());
            }
        }
    }

    //從File讀取data
    private Runnable SpeakFile = new Runnable() {
        @Override
        public void run() {
            Looper.prepare();
            try {
                File myFile = MyFile.getFile(Selection);
                FileInputStream fIn = new FileInputStream(myFile);
                BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                String aDataRow;
                while ((aDataRow = myReader.readLine()) != null) {
                    if(aDataRow.length()==0)
                        continue;
                    if (con) {
                        try {
                            //傳送資料
                            out.writeUTF(aDataRow);
                            Toast.makeText(getApplicationContext(), "成功傳送!", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "傳送失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        speaker.speak(aDataRow);
                    }

                }
                myReader.close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e(TAG, "Can not read file: " + e.toString());
            }
            Looper.loop();
        }
    };

}
