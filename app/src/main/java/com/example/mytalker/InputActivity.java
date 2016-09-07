package com.example.mytalker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import static android.os.StrictMode.ThreadPolicy;
import static android.os.StrictMode.setThreadPolicy;

public class InputActivity extends Activity {
    public static final String IP_SERVER = "192.168.49.1";
    public static int PORT = 8988;
    private DataOutputStream out; //for transfer
    private Socket socket;

    Speaker speaker;

    Button btn_send, btn_lv1, btn_load, btn_clear, btn_speech;
    boolean status_speech = false;
    ListView view;
    EditText editText;
    public static boolean con = false;

    //for data variable
    InputData[] Data=new InputData[1], currentData;
    int[] map=new int[1];
    //SQLiteDatabase db;
    int[][] next_id=new int[1][1];
    int current_id = 0;//0 denote main level

    DBConnection helper = new DBConnection(this);
    Learn learn;

    private Handler uihandler = new Handler();
    private ProgressDialog progressDialog = null;

    String[] list = new String[15];
    Spinner spinner;

    //=====================================oncreate===================================================
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        if (Build.VERSION.SDK_INT > 9) {
            ThreadPolicy policy = new ThreadPolicy.Builder().permitAll().build();
            setThreadPolicy(policy);
        }
        //initialize
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_lv1 = (Button) findViewById(R.id.btn_lv1);
        btn_clear = (Button) findViewById(R.id.btn_clear);
        btn_load = (Button) findViewById(R.id.btn_load);
        btn_speech = (Button) findViewById(R.id.btn_speech);
        view=(ListView)findViewById(R.id.btnView);
        editText = (EditText) findViewById(R.id.editText);
        spinner=(Spinner)findViewById(R.id.Spinner_sentence);

        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = editText.getText().toString();
                int index=editText.getSelectionStart();
                String part1=spilt(s,0,index),part2=spilt(s,index,s.length());
                s=part1+currentData[position].text+part2;
                editText.setText(s);
                editText.setSelection(part1.length()+currentData[position].text.length());
                current_id = currentData[position].id;
                setCurrentData();
            }
        });

        if (!con) {
            String text="TALK";
            btn_send.setText(text);
            btn_speech.setVisibility(View.GONE);
        }
        status_speech = !con;

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                talk();
            }
        });
        btn_speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text=(status_speech?"關閉語音":"開啟語音");
                btn_speech.setText(text);
                status_speech = !status_speech;
            }
        });

        btn_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();Update();
            }
        });
        btn_lv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current_id = 0;
                setCurrentData();
            }
        });

        setSpinner();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String _content = parent.getSelectedItem().toString();
                if(_content.length()>0){
                    editText.setText(_content);
                    editText.setSelection(_content.length());
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                FindSentence(editText.getText().toString());
                spinner.setSelection(0);
            }
        });//text change event
    }
    //===============================================================================================
    String spilt(String s,int start,int end)
    {
        String tmp="";
        for(int i=start;i<end;i++)
            tmp+=String.valueOf(s.charAt(i));
        return tmp;
    }
    //===================================onstart======================================================
    @Override
    protected void onStart() {
        super.onStart();
        btn_send.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                learn=new Learn(getApplicationContext(),helper);
                speaker=new Speaker(getApplicationContext());
                uihandler.post(new Runnable() {
                    @Override
                    public void run() {
                        btn_send.setEnabled(true);
                    }
                });
            }
        }).start();
        Update();
        if (con)
            ConnectToDisplay();
    }

    //===============================================================================================
    private void Update() {
        try {
            progressDialog = ProgressDialog.show(InputActivity.this, "請稍後", "載入資料...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    LoadData();
                    progressDialog.dismiss();
                }
            }).start();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void LoadData() {
        long stime = System.currentTimeMillis();
        SQLiteDatabase db = helper.getReadableDatabase();
        //db=SQLiteDatabase.openDatabase("/sdcard/DB/Database.db",null,SQLiteDatabase.OPEN_READWRITE);
        Cursor c = db.rawQuery("SELECT * FROM " + DBConnection.VocSchema.TABLE_NAME + " ORDER BY " + DBConnection.VocSchema.COUNT + " DESC;", null);
        int size = c.getCount();
        if (size > 0) {
            c.moveToFirst();
            Data = null; //RELEASE
            next_id = null; //RELEASE
            map=null; //RELEASE
            Data = new InputData[size];
            map = new int[size + 1];
            next_id = new int[size + 1][];
            next_id[0] = new int[size];
            for (int i = 0; i < size; i++) {
                final int id = Integer.parseInt(c.getString(c.getColumnIndex(DBConnection.VocSchema.ID)));
                next_id[0][i] = id;
                map[id] = i;
                Data[i] = new InputData(c.getString(c.getColumnIndex(DBConnection.VocSchema.CONTENT)), id);
                //==============================
                //LoadRelation(id);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LoadRelation(id);
                    }
                }).start();
                //==============================
                c.moveToNext();
            }
        }
        c.close();
        final long avg = (System.currentTimeMillis() - stime);
        uihandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(InputActivity.this, "載入時間 : " + String.valueOf(avg) + " msec", Toast.LENGTH_SHORT).show();
                current_id = 0;
                setCurrentData();
            }
        });
    }

    private void LoadRelation(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String query1 = "select " + DBConnection.RelationSchema.ID2 + " from " + DBConnection.RelationSchema.TABLE_NAME
                + " where " + DBConnection.RelationSchema.ID1 + " = '" + String.valueOf(id) + "' order by " + DBConnection.RelationSchema.COUNT + " desc;";
        Cursor c = db.rawQuery(query1, null);
        int size = c.getCount();
        next_id[id] = new int[size];
        if (size > 0) {
            c.moveToFirst();
            for (int i = 0; i < size; i++) {
                next_id[id][i] = Integer.parseInt(c.getString(0));
                c.moveToNext();
            }
        }
        c.close();
    }

    //===============================================================================================
    private void setCurrentData()
    {
        int size = next_id[current_id].length;
        if (size == 0) {
            current_id = 0;
            size = next_id[0].length;
        }
        currentData=new InputData[size];//prevent size=0
        for (int i = 0; i < size; i++ ) {
            int position = map[next_id[current_id][i]];
            currentData[i]=Data[position];
        }
        setList();
    }

    private void setList(){
        int size=currentData.length;
        String[] lists=new String[size];
        for(int i=0;i<size;i++)
            lists[i]=currentData[i].text;
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,lists);
        view.setAdapter(listAdapter);
    }

    private void setSpinner(){
        FindSentence("");
        ArrayAdapter adapter=new ArrayAdapter<>(InputActivity.this, R.layout.myspinner, list);
        adapter.setDropDownViewResource(R.layout.myspinner);
        spinner.setAdapter(adapter);
    }

    private void FindSentence (String keyword) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String query;
        if(keyword.equals("")){
            query="select content from " + DBConnection.SentenceSchema.TABLE_NAME +
                    "  ORDER BY " + DBConnection.SentenceSchema.COUNT + " desc;";
            list[0]="";
        } else {
            query="select content from " + DBConnection.SentenceSchema.TABLE_NAME +
                    " where content LIKE '%" + keyword + "%'  ORDER BY " +
                    DBConnection.SentenceSchema.COUNT + " desc;";
            list[0]=keyword;
        }
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        int SIZE = c.getCount();
        for (int i = 1; i < list.length; i++) {
            if (i > SIZE) {
                list[i] = "";
            }else {
                list[i] = c.getString(0);
                c.moveToNext();
            }
        }

        c.close();
        db.close();
    }

    private void talk() {
        //要傳送的字串
        final String message = editText.getText().toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                learn.Learning(message);
            }
        }).start();
        if (status_speech && message.length()>0)
            speaker.speak(message);
        if (con) {
            try {
                //傳送資料
                out.writeUTF(message);
                Toast.makeText(this, "成功傳送!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "傳送失敗", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void clear(){
        editText.setText("");
        current_id=0;
        setCurrentData();
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
                Log.e("terminate",e.toString());
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        speaker.stop();
        terminate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        terminate();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
