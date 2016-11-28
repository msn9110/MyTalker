package com.example.mytalker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class DataMove extends Activity {
    final int REQUEST_CODE=0;
    public static final String _DBName = "Database.db";
    String LPath=Environment.getExternalStorageDirectory().getPath()+"/MyTalker/Default/LearnData1.txt";
    final int REQUEST_DBCODE=1100;
    boolean outMode=true;//true to copy out, false to move out

    Button button_moveintoout;
    Button button_copyintoout;
    Button button_moveouttoin;
    Button button_copyouttoin;
    Button button_deletein;
    Button button_learndata;

    File out=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),_DBName);
    File in;

    DBConnection helper= new DBConnection(this);
    Learn learn;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.move_menu);

        in=getDatabasePath(_DBName);
        //System.out.println(Path_in);

        button_moveintoout = (Button)findViewById(R.id.btn_moveintoout);
        button_copyintoout = (Button)findViewById(R.id.btn_copyintoout);
        button_moveouttoin = (Button)findViewById(R.id.btn_moveouttoin);
        button_copyouttoin = (Button)findViewById(R.id.btn_copyouttoin);
        button_deletein = (Button)findViewById(R.id.btn_deletein);
        button_learndata = (Button)findViewById(R.id.btn_learndata);

        button_moveintoout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyFile.moveFile(in,out);
                Toast.makeText(DataMove.this,"Success",Toast.LENGTH_SHORT).show();
            }
        });
        button_copyintoout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyFile.copyFile(in,out);
                Toast.makeText(DataMove.this,"Success",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(DataMove.this,"Success",Toast.LENGTH_SHORT).show();
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
                learn=new Learn(getApplicationContext(),helper);
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
            Toast.makeText(DataMove.this,"Success",Toast.LENGTH_SHORT).show();
        } else{
            MyFile.moveFile(source,in);
            Toast.makeText(DataMove.this,"Success",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                                new LearnFile(DataMove.this,arg,learn).execute();
                            }
                        });
                        break;

                    case REQUEST_DBCODE:
                        String ext=".db";
                        File MyDB=new File(path);
                        if(MyDB.getName().endsWith(ext))
                            OutToIn(MyDB,outMode);
                        else{
                            AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
                            MyAlertDialog.setTitle("選擇類型錯誤");
                            MyAlertDialog.setMessage("請選擇db檔(*.db)");
                            MyAlertDialog.show();
                        }
                        break;

                    default:
                        break;
                }
            }
            else
                setTitle("無效的檔案路徑 !!");
        }
        else
            setTitle("取消選擇檔案 !!");
    }
}
