package com.mytalker.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.mytalker.R;
import com.mytalker.core.LearnManager;
import com.mytalker.core.Speaker;
import com.mytalker.core.TalkerDBManager;
import com.mytalker.fragment.FragmentInput;


public class MainActivity extends AppCompatActivity {

    TalkerDBManager talkerDBManager;
    LearnManager learnManager;
    Speaker speaker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Runnable initTask = new Runnable() {
            @Override
            public void run() {
                talkerDBManager = new TalkerDBManager(getApplicationContext());
                //learnManager = new LearnManager(getApplicationContext(), talkerDBManager);
                //speaker = new Speaker(getApplicationContext());
            }
        };

        new Thread(initTask).start();
        setFragment(R.layout.fragment_input);
    }

    private void setFragment(@LayoutRes int resId){
        Fragment f = null;
        switch (resId){
            case R.layout.fragment_input:
                f = new FragmentInput();
                break;
        }

        if(f != null){
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, f);
            transaction.commit();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //speaker.shutdown();
    }
}
