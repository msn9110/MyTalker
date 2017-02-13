package com.mytalker.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.mytalker.R;
import com.mytalker.core.LearnFile;
import com.mytalker.core.LearnManager;
import com.mytalker.core.TalkerDBManager;
import com.utils.MyFile;

import java.io.File;

import static android.app.Activity.RESULT_OK;


public class BackupFragment extends Fragment {
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
    final int REQUEST_CODE = 0;
    public static final String _DBName = TalkerDBManager._DBName + TalkerDBManager._DBExt;
    //String LPath=Environment.getExternalStorageDirectory().getPath()+"/MyTalker/Default/LearnData1.txt";
    final int REQUEST_DBCODE = 1100;
    boolean outMode = true;//true to copy out, false to move out

    Button button_moveintoout;
    Button button_copyintoout;
    Button button_moveouttoin;
    Button button_copyouttoin;
    Button button_deletein;
    Button button_learndata;

    File out=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),_DBName);
    File in;

    TalkerDBManager talkerDBManager;
    LearnManager learnManager;

    private Handler handler = new Handler();

    private void initialize(){
        talkerDBManager = new TalkerDBManager(mContext);

        in = mContext.getDatabasePath(_DBName);
        //System.out.println(Path_in);

        button_moveintoout = (Button)mView.findViewById(R.id.btn_moveintoout);
        button_copyintoout = (Button)mView.findViewById(R.id.btn_copyintoout);
        button_moveouttoin = (Button)mView.findViewById(R.id.btn_moveouttoin);
        button_copyouttoin = (Button)mView.findViewById(R.id.btn_copyouttoin);
        button_deletein = (Button)mView.findViewById(R.id.btn_deletein);
        button_learndata = (Button)mView.findViewById(R.id.btn_learndata);

        button_moveintoout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyFile.moveFile(in,out);
                Toast.makeText(mContext,"Success",Toast.LENGTH_SHORT).show();
            }
        });
        button_copyintoout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyFile.copyFile(in,out);
                Toast.makeText(mContext,"Success",Toast.LENGTH_SHORT).show();
            }
        });
        button_moveouttoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                outMode=false;
                final String mimeType = "*/*";
                final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(mimeType);
                startActivityForResult(intent, REQUEST_DBCODE);
            }
        });
        button_copyouttoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                outMode=true;
                final String mimeType = "*/*";
                final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(mimeType);
                startActivityForResult(intent, REQUEST_DBCODE);
            }
        });
        button_deletein.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyFile.deleteFiles(in);
                Toast.makeText(mContext,"Success",Toast.LENGTH_SHORT).show();
            }
        });
        button_learndata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new LearnFile(DataMove.this,LPath,learn).execute();
                final String mimeType = "text/plain";
                final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(mimeType);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
//thread無法直接access ui
        button_learndata.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                learnManager = new LearnManager(mContext, talkerDBManager);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        button_learndata.setEnabled(true);
                    }
                });
            }
        }).start();//學習模組初始化
    }

    private void OutToIn(File source,boolean mode){
        if(mode){
            MyFile.copyFile(source,in);
            Toast.makeText(mContext,"Success",Toast.LENGTH_SHORT).show();
        } else{
            MyFile.moveFile(source,in);
            Toast.makeText(mContext,"Success",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 有選擇檔案
        if ( resultCode == RESULT_OK)
        {
            // 取得檔案的 Uri
            Uri uri= data.getData();
            String path;
            if( uri != null )
            {
                path=uri.getPath();
                if (path.startsWith("/file")){
                    path=path.replaceFirst("/file","");
                }

                switch (requestCode){

                    case REQUEST_CODE:
                        final String arg=path;
                        System.out.println(path);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                new LearnFile(mContext, arg, learnManager).execute();
                            }
                        });
                        break;

                    case REQUEST_DBCODE:
                        String ext= TalkerDBManager._DBExt;
                        File MyDB=new File(path);
                        if(MyDB.getName().endsWith(ext))
                            OutToIn(MyDB,outMode);
                        else{
                            AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(mContext);
                            MyAlertDialog.setTitle("選擇類型錯誤");
                            MyAlertDialog.setMessage("請選擇db檔(*" + TalkerDBManager._DBExt + ")");
                            MyAlertDialog.show();
                        }
                        break;

                    default:
                        break;
                }
            }
        }
    }
}
