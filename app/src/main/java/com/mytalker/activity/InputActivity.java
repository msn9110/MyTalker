package com.mytalker.activity;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.example.mytalker.R;
import com.mytalker.core.InputData;
import com.mytalker.core.LearnManager;
import com.mytalker.core.Sender;
import com.mytalker.core.TalkerDBManager;
import com.utils.MyFile;
import com.mytalker.core.Speaker;

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

public class InputActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        View.OnClickListener, AdapterView.OnItemSelectedListener{

    private static final String TAG = "InputActivity";
    public static final int immediateMode = 0, speechMode = 1, localMode = 2, connectMode = 3;
    private Boolean[] mySettings = new Boolean[]{false, false, true, false};

    Speaker speaker;
    Sender sender;

    CheckBox[] chkSettings = new CheckBox[4];
    Button btnTalk;
    ListView dbList, mainList, buttonList, fileList;
    EditText editText;

    //for data variable
    InputData[] data = new InputData[1], currentData;
    int[] map = new int[1];//to map vocabulary id to the position in Data array
    int[][] nextIDs = new int[1][1];//level 0 indicates the all vocabularies in database
    int currentID = 0;//0 denote main level

    TalkerDBManager talkerDBManager;
    LearnManager learnManager;

    private Handler handler = new Handler();//thread to access ui
    private ProgressDialog progressDialog = null;
    ArrayList<String> mySentence = new ArrayList<>();
    Spinner spinner;

    String parentPath;
    File appDir = new File(Environment.getExternalStorageDirectory(), "MyTalker");//使用者可透過此目錄下的文件隨時抽換main list的常用詞句
    final String fileEncoding = "-->更改文件編碼";
    final String BACK = "..(回上一頁)";

    //=====================================oncreate===================================================
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_input);
        if (Build.VERSION.SDK_INT > 9) {
            ThreadPolicy policy = new ThreadPolicy.Builder().permitAll().build();
            setThreadPolicy(policy);
        }
        //variable initialize
        MyFile.mkdirs(appDir);

        sender = new Sender();
        talkerDBManager = new TalkerDBManager(this);

        int[] chkID = new int[] {R.id.chk1, R.id.chk2, R.id.chk3, R.id.chk4};
        for(int i = 0; i < chkID.length; i++){
            chkSettings[i] = (CheckBox) findViewById(chkID[i]);
        }

        btnTalk = (Button) findViewById(R.id.btnTalk);
        dbList = (ListView) findViewById(R.id.dbList);
        mainList = (ListView) findViewById(R.id.mainList);
        buttonList = (ListView) findViewById(R.id.buttonList);
        fileList = (ListView) findViewById(R.id.fileList);
        editText = (EditText) findViewById(R.id.editText);
        spinner = (Spinner) findViewById(R.id.Spinner_sentence);

        // listener set
        btnTalk.setOnClickListener(this);
        dbList.setOnItemClickListener(this);
        mainList.setOnItemClickListener(this);
        buttonList.setOnItemClickListener(this);
        fileList.setOnItemClickListener(this);
        spinner.setOnItemSelectedListener(this);
        editText.addTextChangedListener(textChange); // text change event
        chkSettings[connectMode].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mySettings[connectMode] = b;
                if (!mySettings[connectMode]){
                    chkSettings[localMode].setChecked(true);
                    chkSettings[localMode].setEnabled(false);
                } else {
                    chkSettings[localMode].setChecked(false);
                    chkSettings[localMode].setEnabled(true);
                }
            }
        });
        for (int i = 0; i < chkSettings.length - 1; i++){
            final int j = i;
            chkSettings[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mySettings[j] = b;
                }
            });
        }

        // ui content init
        buttonList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList("清除", "主層", "載入資料")));

        if (!mySettings[connectMode]){
            chkSettings[localMode].setChecked(true);
            chkSettings[localMode].setEnabled(false);
        }
        mySettings[localMode] = !mySettings[connectMode];
        for (int i = 0; i < chkSettings.length; i++){
            chkSettings[i].setChecked(mySettings[i]);
        }

        setMainList(new File(appDir,"words.txt"));
        fileList.setAdapter(this.createListAdapter(appDir));
        setSpinner();

        btnTalk.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                speaker = new Speaker(getApplicationContext());
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
                        btnTalk.setEnabled(false);
                    }
                });
                learnManager = new LearnManager(getApplicationContext(), talkerDBManager);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        btnTalk.setEnabled(true);
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
        boolean APPDir = dir.equals(appDir);
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

        if(dirs.size() + files.size() == 0)
            findViewById(R.id.txt_no_data).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.txt_no_data).setVisibility(View.GONE);
        return new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, list);
    }
    //===============================================================================================
    private void setText(String text){
        String s = editText.getText().toString();
        int index = editText.getSelectionStart();
        String part1 = s.substring(0, index),part2 = s.substring(index, s.length());
        s = part1 + text + part2;
        editText.setText(s);
        editText.setSelection(part1.length() + text.length());
    }

    private void setCurrentData()
    {
        int size = nextIDs[currentID].length;
        if (size == 0) {
            currentID = 0;
            size = nextIDs[0].length;
        }
        currentData = new InputData[size];//prevent size=0
        for (int i = 0; i < size; i++ ) {
            int position = map[nextIDs[currentID][i]];
            currentData[i] = data[position];
        }
        setDBList();
    }

    private void setDBList(){
        ArrayList<String> lists = new ArrayList<>();
        for(InputData data : currentData){
            String word = data.text;
            lists.add(word);
        }
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,lists);
        dbList.setAdapter(listAdapter);
    }

    private void setMainList(File file){
        ArrayList<String> myList = new ArrayList<>();
        myList.clear();
        String charset = MyFile.charset_target;
        if(file.exists()){
            File myFile = MyFile.getFile(file);
            try{
                FileInputStream in = new FileInputStream(myFile);
                BufferedReader myReader = new BufferedReader(new InputStreamReader(in, Charset.forName(charset)));
                String line;
                while ((line = myReader.readLine()) != null){
                    myList.add(line);
                }
                myReader.close();
                ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, myList);
                mainList.setAdapter(listAdapter);
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        else {
            String[] words = new String[]{"不","好","要","是","對","用","有","沒"};
            myList.addAll(Arrays.asList(words));
            ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, myList);
            mainList.setAdapter(listAdapter);
        }

    }

    private void setSpinner(){
        talkerDBManager.findSentences("", mySentence);
        ArrayAdapter adapter = new ArrayAdapter<>(InputActivity.this, R.layout.myspinner, mySentence);
        adapter.setDropDownViewResource(R.layout.myspinner);
        spinner.setAdapter(adapter);
    }

    private void talk(final String message, boolean learning) {
        //要傳送的字串
        MyFile.log(message);
        if(learning)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    learnManager.Learning(message);
                }
            }).start();
        if (mySettings[localMode] && message.length() > 0){
            //speaker.stop();
            speaker.speak(message);
        }
        if (mySettings[connectMode]) {
            sender.send(message);
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
    //for on click listener
    private void buttonListOnItemClick(String select){
        switch (select){
            case "清除":
                clear();
                break;
            case "主層":
                currentID = 0;
                setCurrentData();
                break;
            case "載入資料":
                clear();
                Update();
                break;
        }
    }

    private void fileListItemOnClick(String select){
        File file = new File(parentPath, select);

        switch (select){
            case BACK:
                fileList.setAdapter(createListAdapter(new File(parentPath).getParentFile()));
                break;

            case fileEncoding:
                MyFile.setCharset();
                Toast.makeText(InputActivity.this, MyFile.charset, Toast.LENGTH_SHORT).show();
                break;

            default:
                if(file.isDirectory())
                    fileList.setAdapter(createListAdapter(file));
                else{
                    if(!mySettings[speechMode])
                        setMainList(file);
                    else {
                        final File Selection = new File(parentPath,select);
                        try {
                            File myFile = MyFile.getFile(Selection);
                            FileInputStream fIn = new FileInputStream(myFile);
                            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                            String line;
                            while ((line = myReader.readLine()) != null) {
                                if(line.length() == 0)
                                    continue;
                                talk(line, false);
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

    //on click listener
    @Override
    public void onItemClick(AdapterView<?> a, View view, int i, long l) {
        String select = ((TextView) view).getText().toString();
        switch (a.getId()){
            case R.id.mainList:
                if(mySettings[immediateMode])
                    talk(select, false);
                else
                    setText(select);
                break;
            case R.id.dbList:
                setText(currentData[i].text);
                currentID = currentData[i].id;
                setCurrentData();
                break;
            case R.id.buttonList:
                buttonListOnItemClick(select);
                break;
            case R.id.fileList:
                fileListItemOnClick(select);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnTalk:
                talk(editText.getText().toString(), true);
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> a, View view, int i, long l) {
        String select = a.getSelectedItem().toString();
        switch (a.getId()){
            case R.id.Spinner_sentence:
                if(select.length() > 0 && i > 0){
                    editText.setText(select);
                    editText.setSelection(select.length());
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private TextWatcher textChange = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            talkerDBManager.findSentences(editText.getText().toString(), mySentence);
            spinner.setSelection(0);
        }
    };
}