package com.mytalker.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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


import com.example.mytalker.R;
import com.mytalker.core.Connection;
import com.mytalker.core.InputData;
import com.mytalker.core.Learn;
import com.mytalker.core.TalkerDBManager;
import com.utils.MyFile;
import com.utils.Speaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.os.StrictMode.ThreadPolicy;
import static android.os.StrictMode.setThreadPolicy;

public class InputActivity extends AppCompatActivity {
    public static boolean con = false;

    Speaker speaker;
    Connection connection;

    Switch sw_immediate, sw_voice, sw_speech; //to control three status of app
    Button btn_send, btn_lv1, btn_load, btn_clear;
    //localVoice to control whether local Machine is enabled voice, when running in local mode, it is forced to enable
    //when immediate is true, ie can speak the text which you select in main list
    //if  speechMode is true, it will speak the whole file;otherwise, it will load file to main list
    boolean localVoice = true, immediate = false, speechMode = false;
    ListView dbList,mainList,speechList;
    EditText editText;

    //for data variable
    InputData[] data = new InputData[1], currentData;
    int[] map = new int[1];//to map vocabulary id to the position in Data array
    int[][] nextIDs = new int[1][1];//level 0 indicates the all vocabularies in database
    int currentID = 0;//0 denote main level

    TalkerDBManager talkerDBManager;
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
        MyFile.mkdirs(appDir);

        connection = new Connection();
        talkerDBManager = new TalkerDBManager(this);

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
                currentID = currentData[position].id;
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
                currentID = 0;
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
                talkerDBManager.findSentences(editText.getText().toString(), sentence);
                spinner.setSelection(0);
                System.out.println(spinner.getSelectedItem().toString());
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
                learn = new Learn(getApplicationContext(), talkerDBManager);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        btn_send.setEnabled(true);
                    }
                });
            }
        }).start();
        Update();
    }

    //===============================================================================================
    private void Update() {
        try {
            progressDialog = ProgressDialog.show(InputActivity.this, "請稍後", "載入資料...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long stime = System.currentTimeMillis();
                    loadData();
                    progressDialog.dismiss();
                    final long avg = (System.currentTimeMillis() - stime);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(InputActivity.this, "載入時間 : " + String.valueOf(avg) + " msec", Toast.LENGTH_SHORT).show();
                            currentID = 0;
                            setCurrentData();
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void loadData(){
        Cursor c1 = talkerDBManager.getAllVoc();
        int size = c1.getCount();
        Thread[] threads = new Thread[size];
        map = null; data = null; nextIDs = null; // release memory space
        map = new int[size + 1];
        data = new InputData[size + 1];
        nextIDs = new int[size + 1][];
        nextIDs[0] = new int[size];
        talkerDBManager.loadAllVoc(map, data, nextIDs[0], c1);
        for (int i = 0; i < size; i++){
            int id = nextIDs[0][i];
            Cursor c = talkerDBManager.getRelations(id);
            int count = c.getCount();
            nextIDs[id] = new int[count];
            TalkerDBManager.LoadRelations task = talkerDBManager.new LoadRelations(nextIDs[id], c);
            threads[i] = new Thread(task);
            threads[i].setName("## ID : " + id + " ( " + i + " )");
            threads[i].start();
        }
        for (int i = 0; i < size; i++){
            //System.out.println(threads[i].getName() + " loads complete ! ( " + threads[i].getId() + " )");
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
        int size = nextIDs[currentID].length;
        if (size == 0) {
            currentID = 0;
            size = nextIDs[0].length;
        }
        currentData=new InputData[size];//prevent size=0
        for (int i = 0; i < size; i++ ) {
            int position = map[nextIDs[currentID][i]];
            currentData[i]=data[position];
        }
        setList();
    }

    private void setList(){
        ArrayList<String> lists = new ArrayList<>();
        for(InputData data : currentData){
            String word = data.text;
            lists.add(word);
        }
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
        talkerDBManager.findSentences("", sentence);
        ArrayAdapter adapter=new ArrayAdapter<>(InputActivity.this, R.layout.myspinner, sentence);
        adapter.setDropDownViewResource(R.layout.myspinner);
        spinner.setAdapter(adapter);
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
            connection.send(message);
        }

    }

    private void clear(){
        editText.setText("");
        currentID = 0;
        setCurrentData();
    }

    //===============================================================================================
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
