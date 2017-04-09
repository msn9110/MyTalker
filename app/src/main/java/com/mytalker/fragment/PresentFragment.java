package com.mytalker.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mytalker.R;
import com.mytalker.core.Speaker;
import com.mytalker.core.SpeakingListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


public class PresentFragment extends Fragment implements SpeakingListener, AdapterView.OnItemClickListener
{
    private Context mContext;
    private View mView;
    private Speaker mSpeaker;
    private ListView fileList, functionList;
    private TextView txtDisplay;
    private File currentDir = Environment.getExternalStoragePublicDirectory("MyTalker");

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_presentation, container, false);
        currentDir.mkdirs();
        mSpeaker = new Speaker(mContext);
        mSpeaker.setSpeakingListener(this);
        initialize();
        return mView;
    }

    private void initialize() {
        txtDisplay = (TextView) mView.findViewById(R.id.txtDisplay);
        fileList = (ListView) mView.findViewById(R.id.fileList);
        functionList = (ListView) mView.findViewById(R.id.functionList);

        txtDisplay.setText("");
        setFunctionList();
        setFileList();
        fileList.setOnItemClickListener(this);
    }

    final String ENCODING = "更改文件編碼";
    private void setFunctionList() {
        ArrayList<String> functions = new ArrayList<>(Arrays.asList("暫停/繼續", "停止", ENCODING));
        setList(functions, functionList);
    }

    private void setFileList() {
        setList(getFiles(currentDir), fileList);
    }

    private void setList(ArrayList<String> contents, ListView myList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, contents);
        myList.setAdapter(adapter);
    }

    final String BACK = "(上一頁)";
    private ArrayList<String> getFiles(File dir) {
        ArrayList<String> myList = new ArrayList<>();
        myList.add(BACK);
        File[] myFiles = dir.listFiles();
        ArrayList<String> dirs = new ArrayList<>();
        ArrayList<String> files = new ArrayList<>();
        for (File f : myFiles) {
            if (f.isDirectory()) {
                dirs.add(f.getName());
            } else if (f.isFile() && f.getName().endsWith(".txt")) {
                files.add(f.getName());
            }
        }
        myList.addAll(dirs);
        myList.addAll(files);
        return myList;
    }

    @Override
    public void onPreSpeak(String message) {
        int font = 6000 / (message.length() + 40);
        txtDisplay.setTextSize(font);
        txtDisplay.setText(message);
    }

    private void fileListItemClick(String select) {
        if (select.contentEquals(BACK)) {
            if (currentDir.equals(Environment.getExternalStorageDirectory()))
                return;
            currentDir = currentDir.getParentFile();
            setFileList();
        } else {
            File file = new File(currentDir, select);
            if (file.isDirectory()) {
                currentDir = file;
                setFileList();
            } else {

            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String select = ((TextView) view).getText().toString();
        switch (parent.getId()) {
            case R.id.fileList:
                fileListItemClick(select);
                break;
            case R.id.functionList:

                break;
        }
    }
}
