package com.example.mytalker;

import android.app.Activity;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class DataMove extends Activity {
    final int REQUEST_CODE=0;
    public static final String _DBName = "Database.db";
    public static String _LDName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download" + "/LearnData.data";

    Button button_moveintoout;
    Button button_copyintoout;
    Button button_moveouttoin;
    Button button_copyouttoin;
    Button button_deletein;
    Button button_learndata;

    String Path_out = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/";
    String Path_in;
    private static final String TAG = DataMove.class.getName();

    DBConnection helper= new DBConnection(this);
    Learn learn;

    private Handler handler = new Handler();
    private ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.move_menu);

        Path_in=getDatabasePath(_DBName).getParent()+"/";
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
                moveFile(Path_in,Path_out,_DBName);
                Toast.makeText(DataMove.this,"Success",Toast.LENGTH_SHORT).show();
            }
        });
        button_copyintoout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyFile(Path_in,Path_out,_DBName);
                Toast.makeText(DataMove.this,"Success",Toast.LENGTH_SHORT).show();
            }
        });
        button_moveouttoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveFile(Path_out,Path_in,_DBName);
                Toast.makeText(DataMove.this,"Success",Toast.LENGTH_SHORT).show();
            }
        });
        button_copyouttoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyFile(Path_out, Path_in,_DBName);
                Toast.makeText(DataMove.this,"Success",Toast.LENGTH_SHORT).show();
            }
        });
        button_deletein.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFiles(Path_in,_DBName);
                Toast.makeText(DataMove.this,"Success",Toast.LENGTH_SHORT).show();
            }
        });
        button_learndata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String mimeType = "*/*";
                final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(mimeType);
                //startActivityForResult(intent, REQUEST_CODE);

                LearnFromFile();
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
    private void moveFile(String inputPath, String outputPath, String filename) {
        copyFile(inputPath,outputPath,filename);
        deleteFiles(inputPath,filename);
    }

    private void deleteFiles(String inputPath, String filename) {
        try {
            // delete the original file
            boolean success=new File(inputPath+filename).delete();
            System.out.println(success);
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    private void copyFile(String inputPath, String outputPath, String filename) {

        InputStream in;
        OutputStream out;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                boolean success = dir.mkdir();
                System.out.println("Make Dir "+success);
            }


            in = new FileInputStream(inputPath + filename);
            out = new FileOutputStream(outputPath + filename);


            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            // write the output file
            out.flush();
            out.close();

        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }
    //從File讀取data
    private boolean readFromFile() {
        boolean success=false;
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            boolean success2=path.mkdir();
            System.out.println(success2);
            // create the file in which we will write the contents
            File myFile = new File(_LDName);
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
        }
        catch (FileNotFoundException e) {
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
        System.out.println("6666666666666666666666666666");
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("777777777777777777777777");
        // 有選擇檔案
        if ( resultCode == RESULT_OK  && requestCode == REQUEST_CODE)
        {
            System.out.println("8888888888888888888888888");
            // 取得檔案的 Uri
            Uri uri = data.getData();
            if( uri != null )
            {
                _LDName=uri.getPath();
                System.out.println(_LDName);
                //learnfromfile();
            }
            else
            {
                System.out.println("9999999999999999999999");
                setTitle("無效的檔案路徑 !!");
            }
        }
        else
        {
            System.out.println("0000000000000000000000000000");
            setTitle("取消選擇檔案 !!");
            //learnfromfile();
        }
    }

}
