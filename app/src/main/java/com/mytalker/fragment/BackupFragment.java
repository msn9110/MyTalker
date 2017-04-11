package com.mytalker.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mytalker.R;
import com.mytalker.core.LearnFile;
import com.mytalker.core.LearnManager;
import com.mytalker.core.TalkerDBManager;
import com.utils.MyFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;



public class BackupFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener{
    private Context mContext;
    private View mView;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_backup, container, false);
        initialize();
        return mView;
    }
    //===============================================================================================
    private final String Ext = TalkerDBManager._DBExt;
    public static final String _DBName = TalkerDBManager._DBName + TalkerDBManager._DBExt;

    Button localBkup, cloudBkup, delData;
    ListView fileList;

    File in;
    File currentDir;
    ArrayList<String> dirFiles = new ArrayList<>();

    TalkerDBManager talkerDBManager;
    LearnManager learnManager;

    //private Handler handler = new Handler();

    private void initialize(){
        talkerDBManager = new TalkerDBManager(mContext);
        new Thread(new Runnable() {
            @Override
            public void run() {
                learnManager = new LearnManager(mContext, talkerDBManager);
            }
        }).start();//學習模組初始化
        currentDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        in = mContext.getDatabasePath(_DBName);

        localBkup = (Button) mView.findViewById(R.id.localBkup);
        cloudBkup = (Button) mView.findViewById(R.id.cloudBkup);
        delData = (Button) mView.findViewById(R.id.delData);
        fileList = (ListView) mView.findViewById(R.id.fileList);

        localBkup.setOnClickListener(this);
        cloudBkup.setOnClickListener(this);
        delData.setOnClickListener(this);
        fileList.setOnItemClickListener(this);
        fileList.setOnItemLongClickListener(this);

        fileList.setAdapter(createAdapter());
    }

    final String BACK = "(上一頁)";
    private void setDirFiles(){
        dirFiles.clear();
        ArrayList<String> myDirs = new ArrayList<>(), myFiles = new ArrayList<>();
        myDirs.add(BACK);
        File[] files = currentDir.listFiles();
        String[] exts = new String[]{Ext, ".txt", "TXT"};
        for (File f : files){
            if (f.isFile()){
                String filename = f.getName();
                for (String ext : exts){
                    if (filename.endsWith(ext)){
                        myFiles.add(filename);
                        break;
                    }
                }
            } else if(f.isDirectory()){
                myDirs.add(f.getName());
            }
        }
        dirFiles.addAll(myDirs);
        dirFiles.addAll(myFiles);
    }

    private ArrayAdapter<String> createAdapter(){
        setDirFiles();
        return new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, dirFiles);
    }

    private void reloadFileList(){
        fileList.setAdapter(createAdapter());
    }

    private int checkExtension(String name){
        name = name.toLowerCase();
        String[] exts = new String[]{"txt", Ext};
        for (int i = 0; i < exts.length; i++){
            if (name.endsWith(exts[i])){
                return i;
            }
        }
        return -1;
    }

    private void fileListItemOnClick(String select){
        if (select.contentEquals(BACK)) {
            if (currentDir.equals(Environment.getExternalStorageDirectory()))
                return;
            currentDir = currentDir.getParentFile();
            reloadFileList();
        }
        File file = new File(currentDir, select);
        if(file.isDirectory()){
            currentDir = file;
            reloadFileList();
            return;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        switch (checkExtension(select)){
            case 0:
                final File learningFile = new File(currentDir, select);
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
            case 1:
                final File out = new File(currentDir, select);
                dialog.setTitle("複製詞句庫").setMessage("確定要取代目前所使用的詞句庫?").setCancelable(false).setNegativeButton("取消", null)
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (MyFile.copyFile(out, in)){
                                    reloadFileList();
                                    Toast.makeText(mContext, "成功複製!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .create();
                dialog.show();
                break;
        }
    }
    @Override
    public void onClick(View view) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        String filename = df.format(new Date()) + Ext;
        boolean isSuccess = false;
        switch (view.getId()){
            case R.id.localBkup:
                File out = new File(currentDir, filename);
                isSuccess = MyFile.copyFile(in, out);
                reloadFileList();
                break;
            case R.id.cloudBkup:

                break;
            case R.id.delData:
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setTitle("刪除詞句庫").setMessage("確定刪除現在使用中的詞句庫?").setCancelable(false).setNegativeButton("取消", null)
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (in.delete()){
                                    Toast.makeText(mContext, "成功刪除!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .create();
                dialog.show();
                return;
        }
        String msg = (isSuccess ? "Success !" : "Failed;");
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String select = ((TextView) view).getText().toString();
        switch (adapterView.getId()){
            case R.id.fileList:
                fileListItemOnClick(select);
                break;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        String select = ((TextView) view).getText().toString();
        switch (adapterView.getId()){
            case R.id.fileList:
                if(! select.equals("..")){
                    final File file = new File(currentDir, select);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                    dialog.setTitle("刪除文件").setMessage("確定刪除此文件?").setCancelable(false).setNegativeButton("取消", null)
                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (file.delete()){
                                        reloadFileList();
                                        Toast.makeText(mContext, "成功刪除!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .create();
                    dialog.show();
                }
                break;
        }
        return true;
    }
}
