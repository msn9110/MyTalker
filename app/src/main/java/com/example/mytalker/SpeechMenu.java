package com.example.mytalker;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SpeechMenu extends Activity implements AdapterView.OnItemClickListener {

    static final String TAG="SpeechMenu";
    private String parentPath;
    ListView lv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_menu);

        lv = (ListView) this.findViewById(R.id.list_dir);
        lv.setAdapter(this.createListAdapter());
        lv.setOnItemClickListener(this);
    }
    private ListAdapter createListAdapter() {
        List<String> list = new ArrayList<>();
        File sdDir = Environment.getExternalStorageDirectory();
        File Dir = new File(sdDir, "MySpeaker/");
        this.parentPath = Dir.getPath();
        Log.d(TAG, "根目錄：" + this.parentPath);
        File[] files = Dir.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                continue;
            }
            list.add(f.getName());
        }
        return new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, list);
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        File Selected=new File(this.parentPath,((TextView) view).getText().toString());
        SpeechMode.path=Selected.getName();
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), SpeechMode.class);
        startActivity(intent);
    }
}
