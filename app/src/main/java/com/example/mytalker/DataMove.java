package com.example.mytalker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class DataMove extends Activity {
    final int REQUEST_CODE=0;
    public static final String _DBName = "Database.db";
    String _LDName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download" + "/LearnData.data";
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
    private static final String TAG = DataMove.class.getName();

    DBConnection helper= new DBConnection(this);
    Learn learn;

    private Handler handler = new Handler();
    private ProgressDialog progressDialog = null;

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
                final String mimeType = "text/plain";
                final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(mimeType);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

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
        }).start();
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
    //從File讀取data
    private boolean readFromFile() {
        boolean success=false;
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            MyFile.mkdirs(dir);
            // create the file in which we will write the contents
            File myFile = MyFile.getFile(new File(_LDName));
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String aDataRow;
            int row=0;
            while ((aDataRow = myReader.readLine()) != null) {
                learn.Learning(aDataRow);
                row++;
                System.out.println(row);
            }
            success=true;
            myReader.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
        return success;
    }

    private void LearnFromFile(){
        System.out.println(_LDName);
        progressDialog = ProgressDialog.show(DataMove.this, "請稍後", "學習中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(readFromFile())
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DataMove.this,"Success Learn",Toast.LENGTH_SHORT).show();
                        }
                    });
                else
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DataMove.this, "Fail Learn", Toast.LENGTH_SHORT).show();
                        }
                    });
                progressDialog.dismiss();
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 有選擇檔案
        if ( resultCode == RESULT_OK)
        {
            Uri uri;
            switch (requestCode){

                case REQUEST_CODE:
                    // 取得檔案的 Uri
                    uri = data.getData();
                    if( uri != null )
                    {
                        _LDName=uri.getPath();
                        LearnFromFile();
                    }
                    else
                        setTitle("無效的檔案路徑 !!");
                    break;

                case REQUEST_DBCODE:
                    // 取得檔案的 Uri
                    uri = data.getData();
                    if( uri != null )
                    {
                        String ext=".db";
                        File MyDB=new File(uri.getPath());
                        if(MyDB.getName().endsWith(ext))
                            OutToIn(MyDB,outMode);
                        else{
                            AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
                            MyAlertDialog.setTitle("選擇類型錯誤");
                            MyAlertDialog.setMessage("請選擇db檔(*.db)");
                            MyAlertDialog.show();
                        }
                    }
                    else
                        setTitle("無效的檔案路徑 !!");
                    break;

                default:
                    break;
            }
        }
        else
            setTitle("取消選擇檔案 !!");
    }
}
