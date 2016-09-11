package com.example.mytalker;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import java.io.File;

public class WelcomePage extends Activity{
    Button goto_startmenu;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);
        goto_startmenu=(Button)findViewById(R.id.btn_goto_start_menu);
        File sdDir = Environment.getExternalStorageDirectory();
        File buildDir = new File(sdDir, "MySpeaker/Default");
        isExist(buildDir);

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

    public void isExist(File dir) {
        //判斷文件夾是否存在,如果不存在則建立文件夾
        if (!dir.exists()) {
            if(!dir.mkdirs())
                System.out.println("MakeDir : Fail");
        }
    }
}
