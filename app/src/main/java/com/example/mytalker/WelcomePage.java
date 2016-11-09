package com.example.mytalker;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import java.io.File;
import android.support.v4.app.ActivityCompat;
import static android.Manifest.permission.*;

public class WelcomePage extends Activity{
    private static final int REQUEST_EXTERNAL_STORAGE = 0;
    Button goto_startmenu;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);
        goto_startmenu=(Button)findViewById(R.id.btn_goto_start_menu);
        goto_startmenu.setEnabled(false);
        int permission = ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 無權限，向使用者請求
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE
            );
        }else{
             //已有權限，執行儲存程式
            File buildDir = new File(Environment.getExternalStorageDirectory(), "MyTalker/Default");
            MyFile.mkdirs(buildDir);
            goto_startmenu.setEnabled(true);
        }

        goto_startmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), StartMenu.class);
                startActivity(intent);
                WelcomePage.this.finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //取得權限，進行檔案存取
                    File buildDir = new File(Environment.getExternalStorageDirectory(), "MyTalker/Default");
                    MyFile.mkdirs(buildDir);
                    goto_startmenu.setEnabled(true);
                } else {
                    //使用者拒絕權限，停用檔案存取功能
                    this.finish();
                }
        }
    }
}
