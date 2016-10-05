package com.example.mytalker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.os.StrictMode.ThreadPolicy;
import static android.os.StrictMode.setThreadPolicy;

public class InputActivity extends Activity {
    public static final String IP_SERVER = "192.168.49.1";
    public static int PORT = 8988;
    private DataOutputStream out; //for transfer
    private Socket socket;

    Speaker speaker;

    Switch sw_immediate,sw_voice,sw_speech;//to control three status of app
    Button btn_send, btn_lv1, btn_load, btn_clear;
    boolean localVoice = false,immediate = false,speechMode = false;
    //localVoice to control whether local Machine is enabled voice, when running in local mode, it is forced to enable
    //when immediate is true, ie can speak the text which you select in main list
    //if  speechMode is true, it will speak the whole file;otherwise, it will load file to main list
    ListView dbList,mainList,speechList;
    EditText editText;
    public static boolean con = false;

    //for data variable
    InputData[] Data=new InputData[1], currentData;
    int[] map=new int[1];//to map vocabulary id to the position in Data array
    //SQLiteDatabase db;
    int[][] next_id=new int[1][1];//level 0 indicates the all vocabularies in database
    int current_id = 0;//0 denote main level

    DBConnection helper;
    Learn learn;

    private Handler handler = new Handler();//thread to access ui
    private ProgressDialog progressDialog = null;
    String[] sentence = new String[15];
    Spinner spinner;

    ArrayList<String> myList=new ArrayList<>();
    String parentPath;
    File appDir=new File(Environment.getExternalStorageDirectory(),"MyTalker");//使用者可透過此目錄下的文件隨時抽換main list的常用詞句
    final String fileEncoding="-->更改文件編碼";
    final String BACK="..(回上一頁)";
    static final String TAG="SpeechList";

    //dropbox
    final static private String APP_KEY = "admwgo2fp9n1bli";
    final static private String APP_SECRET = "x9i4k97k4ac5ilk";
    // In the class declaration section:
    private DropboxAPI<AndroidAuthSession> mDBApi;
    String dropbox="(Dropbox)";

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
        // And later in some initialization function:
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<>(session);

        MyFile.mkdirs(appDir);

        helper = new DBConnection(this);

        sw_immediate=(Switch)findViewById(R.id.sw_immediate);
        sw_voice=(Switch)findViewById(R.id.sw_voice);
        sw_speech=(Switch)findViewById(R.id.sw_speech);

        btn_send = (Button) findViewById(R.id.btn_send);
        btn_lv1 = (Button) findViewById(R.id.btn_lv1);
        btn_clear = (Button) findViewById(R.id.btn_clear);
        btn_load = (Button) findViewById(R.id.btn_load);

        dbList=(ListView)findViewById(R.id.dbList);
        mainList=(ListView)findViewById(R.id.mainList);
        speechList=(ListView)findViewById(R.id.speechList);

        editText = (EditText) findViewById(R.id.editText);
        spinner=(Spinner)findViewById(R.id.Spinner_sentence);

        dbList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setText(currentData[position].text);
                current_id = currentData[position].id;
                setCurrentData();
            }
        });

        mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String msg=myList.get(position);
                if(immediate)
                    talk(msg,false);
                else
                    setText(msg);
            }
        });

        speechList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String select=((TextView) view).getText().toString();
                File file=new File(parentPath,select);

                switch (select){
                    case BACK:
                        speechList.setAdapter(createListAdapter(new File(parentPath).getParentFile()));
                        break;

                    case fileEncoding:
                        MyFile.setCharset();
                        Toast.makeText(InputActivity.this,MyFile.charset,Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        if(file.isDirectory())
                            speechList.setAdapter(createListAdapter(file));
                        else{
                            if(!speechMode)
                                setMainList(file);
                            else {
                                final File Selection=new File(parentPath,select);
                                try {
                                    File myFile = MyFile.getFile(Selection);
                                    FileInputStream fIn = new FileInputStream(myFile);
                                    BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                                    String line;
                                    while ((line = myReader.readLine()) != null) {
                                        if(line.length()==0)
                                            continue;
                                        talk(line,false);
                                    }
                                    myReader.close();
                                } catch (FileNotFoundException e) {
                                    Log.e(TAG, "File not found: " + e.toString());
                                } catch (IOException e) {
                                    Log.e(TAG, "Can not read file: " + e.toString());
                                }
                            }
                        }
                        break;
                }
            }
        });

        if (!con) {
            String text="TALK";
            btn_send.setText(text);
            sw_voice.setChecked(true);
            sw_voice.setEnabled(false);
        }
        localVoice = !con;
        sw_voice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                localVoice=b;
            }
        });
        sw_immediate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                immediate=b;
            }
        });
        sw_speech.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                speechMode=b;
            }
        });

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                talk(editText.getText().toString(),true);
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

        setMainList(new File(appDir,"words.txt"));
        speechList.setAdapter(this.createListAdapter(appDir));
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

        btn_send.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                speaker=new Speaker(getApplicationContext());
                try {
                    Thread.sleep(1250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    //===============================================================================================
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_input_activity, menu);
        return true;
    }
    //===================================onstart======================================================
    @Override
    protected void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        btn_send.setEnabled(false);
                    }
                });
                learn=new Learn(getApplicationContext(),helper);
                handler.post(new Runnable() {
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

            final long avg = (System.currentTimeMillis() - stime);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(InputActivity.this, "載入時間 : " + String.valueOf(avg) + " msec", Toast.LENGTH_SHORT).show();
                    current_id = 0;
                    setCurrentData();
                }
            });
        }
        c.close();
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
    private ListAdapter createListAdapter(File dir) {
        List<String> list = new ArrayList<>();
        boolean APPDir=dir.equals(appDir);
        this.parentPath = dir.getPath();
        File[] myfiles = dir.listFiles();
        List<String> dirs = new ArrayList<>();
        List<String> files = new ArrayList<>();
        list.add(fileEncoding);
        if(!APPDir){
            list.add(BACK);
        }else {
            list.add(dropbox);
        }

        for (File f : myfiles) {
            if(MyFile.prefix.equals(f.getName()))
                continue;
            if(f.isDirectory())
                dirs.add(f.getName());
            else
                files.add(f.getName());
        }
        list.addAll(dirs);
        list.addAll(files);

        if(dirs.size() + files.size()==0)
            findViewById(R.id.txt_no_data).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.txt_no_data).setVisibility(View.GONE);
        return new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, list);
    }
    //===============================================================================================
    private void setText(String text){
        String s = editText.getText().toString();
        int index=editText.getSelectionStart();
        String part1=s.substring(0,index),part2=s.substring(index,s.length());
        s=part1+text+part2;
        editText.setText(s);
        editText.setSelection(part1.length()+text.length());
    }

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
        dbList.setAdapter(listAdapter);
    }

    private void setMainList(File file){
        myList.clear();
        String charset=MyFile.charset_target;
        if(file.exists()){
            File myFile=MyFile.getFile(file);
            try{
                FileInputStream in = new FileInputStream(myFile);
                BufferedReader myReader = new BufferedReader(new InputStreamReader(in, Charset.forName(charset)));
                String line;
                while ((line=myReader.readLine())!=null){
                    myList.add(line);
                }
                myReader.close();
                ArrayAdapter<String> listAdapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,myList);
                mainList.setAdapter(listAdapter);
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        else {
            String[] words=new String[]{"不","好","要","是","對","用","有","沒"};
            myList.addAll(Arrays.asList(words));
            ArrayAdapter<String> listAdapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,myList);
            mainList.setAdapter(listAdapter);
        }

    }

    private void setSpinner(){
        FindSentence("");
        ArrayAdapter adapter=new ArrayAdapter<>(InputActivity.this, R.layout.myspinner, sentence);
        adapter.setDropDownViewResource(R.layout.myspinner);
        spinner.setAdapter(adapter);
    }

    private void FindSentence (String keyword) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String query;
        if(keyword.equals("")){
            query="select content from " + DBConnection.SentenceSchema.TABLE_NAME +
                    "  ORDER BY " + DBConnection.SentenceSchema.COUNT + " desc;";
            sentence[0]="";
        } else {
            query="select content from " + DBConnection.SentenceSchema.TABLE_NAME +
                    " where content LIKE '%" + keyword + "%'  ORDER BY " +
                    DBConnection.SentenceSchema.COUNT + " desc;";
            sentence[0]=keyword;
        }
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        int SIZE = c.getCount();
        for (int i = 1; i < sentence.length; i++) {
            if (i > SIZE) {
                sentence[i] = "";
            }else {
                sentence[i] = c.getString(0);
                c.moveToNext();
            }
        }

        c.close();
        db.close();
    }

    private void talk(final String message,boolean learning) {
        //要傳送的字串
        MyFile.log(message);
        if(learning)
            new Thread(new Runnable() {
            @Override
            public void run() {
                learn.Learning(message);
            }
        }).start();
        if (localVoice && message.length()>0){
            //speaker.stop();
            speaker.speak(message);
        }
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

    //===============================connection=======================================================
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
    //===============================================================================================

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.dropbox_auth:
                mDBApi.getSession().startOAuth2Authentication(InputActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                //String accessToken = mDBApi.getSession().getOAuth2AccessToken();
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speaker.shutdown();
    }
}
