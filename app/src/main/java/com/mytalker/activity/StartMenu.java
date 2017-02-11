package com.mytalker.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.mytalker.R;
import com.utils.MyFile;

import java.io.File;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class StartMenu extends AppCompatActivity{
    Button[] mode=new Button[4];
    private static final int REQUEST_EXTERNAL_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_menu);
        if(Build.VERSION.SDK_INT < 23){
            init();
        } else {
            int permission = ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 無權限，向使用者請求
                ActivityCompat.requestPermissions(this,
                        new String[] {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE},
                        REQUEST_EXTERNAL_STORAGE);
            }else{
                //已有權限，執行儲存程式
                init();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //取得權限，進行檔案存取
                    init();
                } else {
                    //使用者拒絕權限，停用檔案存取功能
                    this.finish();
                }
        }
    }

    private void init(){
        File buildDir = new File(Environment.getExternalStorageDirectory(), "MyTalker/Default");
        MyFile.mkdirs(buildDir);

        mode[0]=(Button)findViewById(R.id.btn_mode1);
        mode[1]=(Button)findViewById(R.id.btn_mode2);
        mode[2]=(Button)findViewById(R.id.btn_mode3);
        mode[3]=(Button)findViewById(R.id.btn_mode4);

        mode[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent =new Intent();
                intent.setClass(getApplicationContext(),InputActivity.class);
                startActivity(intent);
            }
        });

        mode[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent =new Intent();
                intent.setClass(getApplicationContext(),WiFiDirectActivity.class);
                startActivity(intent);
                StartMenu.this.finish();

            }
        });

        mode[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), DataMove.class);
                startActivity(intent);
            }
        });

        mode[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
