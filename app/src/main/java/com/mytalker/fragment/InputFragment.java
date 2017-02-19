package com.mytalker.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.mytalker.core.LearnFile;
import com.mytalker.core.LearnManager;
import com.mytalker.core.Sender;
import com.mytalker.core.Speaker;
import com.mytalker.core.TalkerDBManager;
import com.utils.MyFile;
import com.utils.TransferMode;

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

import static android.os.StrictMode.setThreadPolicy;


public class InputFragment extends Fragment implements AdapterView.OnItemClickListener,
        View.OnClickListener, AdapterView.OnItemSelectedListener, AdapterView.OnItemLongClickListener,
        CompoundButton.OnCheckedChangeListener {
    private Context mContext;
    private View mView;
    Speaker speaker;
    Sender sender;
    TalkerDBManager talkerDBManager;
    LearnManager learnManager;
    private Handler handler = new Handler(); // thread to access ui

    private interface PrefKey{
        String immediateMode = "immediateMode";
        String speechMode = "speechMode";
        String localMode = "localMode";
        String connectMode = "connectMode";
    }
    private void setPreference(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("MyPreference", Context.MODE_PRIVATE);
        mySettings[immediateMode] = sharedPreferences.getBoolean(PrefKey.immediateMode, false);
        mySettings[speechMode] = sharedPreferences.getBoolean(PrefKey.speechMode, false);
        mySettings[localMode] = sharedPreferences.getBoolean(PrefKey.localMode, true);
        mySettings[connectMode] = sharedPreferences.getBoolean(PrefKey.connectMode, false);
        for (int i = 0; i < chkSettings.length; i++){
            chkSettings[i].setChecked(mySettings[i]);
        }
        chkSettings[localMode].setEnabled(mySettings[connectMode]);
    }
    private void savePreference(String key, boolean value){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("MyPreference", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(key, value).apply();
    }
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sender = new Sender();
        talkerDBManager = new TalkerDBManager(mContext);
        speaker = new Speaker(mContext);
        new Thread(new Runnable() {
            @Override
            public void run() {
                learnManager = new LearnManager(mContext, talkerDBManager);
            }
        }).start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        speaker.shutdown();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_input, container, false);
        initialize();
        return mView;
    }

    private static final String TAG = "## InputFragment";
    public static final int immediateMode = 0, speechMode = 1, localMode = 2, connectMode = 3;
    private Boolean[] mySettings = new Boolean[]{false, false, true, false};

    CheckBox[] chkSettings = new CheckBox[4];
    Button btnTalk;
    ListView dbList, mainList, buttonList, fileList;
    EditText editText;

    //for data variable
    private final static int mainLevel = 0;
    InputData[] data, currentData;
    int[] map; // to map vocabulary id to the position in Data array
    int[][] nextIDs; // mainLevel indicates the all vocabularies in database

    private ProgressDialog progressDialog = null;
    ArrayList<String> mySentence = new ArrayList<>();
    Spinner spinner;

    File currentDir;
    File appDir = new File(Environment.getExternalStorageDirectory(), "MyTalker");//使用者可透過此目錄下的文件隨時抽換main list的常用詞句
    final String fileEncoding = "-->更改文件編碼";
    final String BACK = "..(回上一頁)";

    private void initialize(){
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            setThreadPolicy(policy);
        }
        //variable initialize
        MyFile.mkdirs(appDir);

        int[] chkID = new int[] {R.id.chk1, R.id.chk2, R.id.chk3, R.id.chk4};
        for(int i = 0; i < chkID.length; i++){
            chkSettings[i] = (CheckBox) mView.findViewById(chkID[i]);
        }

        btnTalk = (Button) mView.findViewById(R.id.btnTalk);
        dbList = (ListView) mView.findViewById(R.id.dbList);
        mainList = (ListView) mView.findViewById(R.id.mainList);
        buttonList = (ListView) mView.findViewById(R.id.buttonList);
        fileList = (ListView) mView.findViewById(R.id.fileList);
        editText = (EditText) mView.findViewById(R.id.editText);
        spinner = (Spinner) mView.findViewById(R.id.Spinner_sentence);

        // listener set
        btnTalk.setOnClickListener(this);
        dbList.setOnItemClickListener(this);
        mainList.setOnItemClickListener(this);
        buttonList.setOnItemClickListener(this);
        fileList.setOnItemClickListener(this);
        fileList.setOnItemLongClickListener(this);
        spinner.setOnItemSelectedListener(this);
        editText.addTextChangedListener(textChange); // text change event
        for (CheckBox checkBox : chkSettings){
            checkBox.setOnCheckedChangeListener(this);
        }

        // ui content init
        setPreference();
        buttonList.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, Arrays.asList("清除", "主層", "載入資料", "暫停/繼續", "停止")));
        setMainList(new File(appDir,"words.txt"));
        fileList.setAdapter(this.createListAdapter(appDir));
        setSpinner();
        updateDBList();
    }

    //  Loading data
    private void updateDBList() {
        try {
            progressDialog = ProgressDialog.show(mContext, "請稍後", "載入資料...");
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
                            Toast.makeText(mContext, "載入時間 : " + String.valueOf(avg) + " msec", Toast.LENGTH_SHORT).show();
                            setCurrentData(mainLevel);
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void loadData(){
        Cursor cursor = talkerDBManager.getAllVoc();
        int size = cursor.getCount();
        Thread[] threads = new Thread[size];
        map = null; data = null; nextIDs = null; // release memory space
        map = new int[size + 1];
        data = new InputData[size + 1];
        nextIDs = new int[size + 1][];
        nextIDs[mainLevel] = new int[size];
        talkerDBManager.loadAllVoc(map, data, nextIDs[mainLevel], cursor);
        for (int i = 0; i < size; i++){
            int id = nextIDs[mainLevel][i];
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

    //listView adapter create
    private ListAdapter createListAdapter(File dir) {
        List<String> list = new ArrayList<>();
        boolean APPDir = dir.equals(appDir);
        currentDir = dir;
        File[] myFiles = dir.listFiles();
        List<String> dirs = new ArrayList<>();
        List<String> files = new ArrayList<>();
        list.add(fileEncoding);
        if(!APPDir){
            list.add(BACK);
        }

        for (File f : myFiles) {
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
            mView.findViewById(R.id.txt_no_data).setVisibility(View.VISIBLE);
        else
            mView.findViewById(R.id.txt_no_data).setVisibility(View.GONE);
        return new ArrayAdapter<>(mContext,android.R.layout.simple_list_item_1, list);
    }
    //======================================set function================================================
    private void setText(String text){
        String s = editText.getText().toString();
        int index = editText.getSelectionStart();
        String part1 = s.substring(0, index),part2 = s.substring(index, s.length());
        s = part1 + text + part2;
        editText.setText(s);
        editText.setSelection(part1.length() + text.length());
    }

    private void setCurrentData(int id)
    {
        int currentID = ((nextIDs[id].length == 0) ? mainLevel : id); //  ? true : false
        int size = nextIDs[currentID].length;
        currentData = new InputData[size];
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
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, lists);
        dbList.setAdapter(listAdapter);
    }

    private void setMainList(File file){
        if(file.exists()){
            ArrayList<String> myList = new ArrayList<>();
            String charset = MyFile.charset_target;
            File myFile = MyFile.getFile(file);
            try{
                FileInputStream in = new FileInputStream(myFile);
                BufferedReader myReader = new BufferedReader(new InputStreamReader(in, Charset.forName(charset)));
                String line;
                while ((line = myReader.readLine()) != null){
                    myList.add(line);
                }
                myReader.close();
                ArrayAdapter<String> listAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, myList);
                mainList.setAdapter(listAdapter);
            }catch (Exception e){
                e.printStackTrace();
            }

        } else {
            String[] words = new String[]{"不","好","要","是","對","用","有","沒"};
            ArrayAdapter<String> listAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, words);
            mainList.setAdapter(listAdapter);
        }

    }

    private void setSpinner(){
        talkerDBManager.findSentences("", mySentence);
        ArrayAdapter adapter = new ArrayAdapter<>(mContext, R.layout.myspinner, mySentence);
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
            speaker.setEnable(true);
            speaker.addSpeak(message);
        }
        if (mySettings[connectMode]) {
            sender.sendMessage(message);
        }

    }

    private void clear(){
        editText.setText("");
        setCurrentData(mainLevel);
    }

    //===============================================================================================
    //for on click listener
    private void buttonListOnItemClick(String select){
        switch (select){
            case "清除":
                clear();
                break;
            case "主層":
                setCurrentData(mainLevel);
                break;
            case "載入資料":
                clear();
                updateDBList();
                break;
            case "暫停/繼續":
                speaker.pause();
                if (mySettings[connectMode])
                    sender.remoteControl(TransferMode.IMODE_PAUSE);
                break;
            case "停止":
                speaker.stop();
                if (mySettings[connectMode])
                    sender.remoteControl(TransferMode.IMODE_STOP);
                break;
        }
    }

    private void fileListItemOnClick(String select){
        File file = new File(currentDir, select);

        switch (select){
            case BACK:
                fileList.setAdapter(createListAdapter(currentDir.getParentFile()));
                break;

            case fileEncoding:
                MyFile.setCharset();
                Toast.makeText(mContext, MyFile.charset, Toast.LENGTH_SHORT).show();
                break;

            default:
                if(file.isDirectory())
                    fileList.setAdapter(createListAdapter(file));
                else{
                    if(!mySettings[speechMode])
                        setMainList(file);
                    else {
                        final File Selection = new File(currentDir, select);
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
                setCurrentData(currentData[i].id);
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

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        String select = ((TextView) view).getText().toString();
        switch (adapterView.getId()){
            case R.id.fileList:
                if (select.endsWith(".txt")){
                    final File learningFile = new File(currentDir, select);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                    dialog.setTitle("學習文件").setMessage("確定學習此文件?").setCancelable(false).setNegativeButton("取消", null)
                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    new LearnFile(mContext, learningFile.getPath(), learnManager).execute();
                                }
                            })
                            .create();
                    dialog.show();
                    break;
                }
        }
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.chk1:
                mySettings[immediateMode] = b;
                savePreference(PrefKey.immediateMode, mySettings[immediateMode]);
                break;
            case R.id.chk2:
                mySettings[speechMode] = b;
                savePreference(PrefKey.speechMode, mySettings[speechMode]);
                break;
            case R.id.chk3:
                mySettings[localMode] = b;
                savePreference(PrefKey.localMode, mySettings[localMode]);
                break;
            case R.id.chk4:
                mySettings[connectMode] = b;
                savePreference(PrefKey.connectMode, mySettings[connectMode]);
                chkSettings[localMode].setChecked(!mySettings[connectMode]);
                chkSettings[localMode].setEnabled(mySettings[connectMode]);
                break;
        }
    }
}
