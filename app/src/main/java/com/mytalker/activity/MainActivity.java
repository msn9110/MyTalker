package com.mytalker.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.mytalker.R;
import com.mytalker.fragment.BackupFragment;
import com.mytalker.fragment.DisplayFragment;
import com.mytalker.fragment.HelpFragment;
import com.mytalker.fragment.InputFragment;
import com.mytalker.fragment.PresentFragment;
import com.mytalker.fragment.SettingFragment;
import com.utils.MyFile;
import com.utils.WifiAdmin;
import com.utils.WifiApControl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    NavigationView view;
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        view = (NavigationView) findViewById(R.id.navigation_view);
        view.setNavigationItemSelectedListener(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle( this, drawerLayout, toolbar, R.string.openDrawer , R.string.closeDrawer){
            @Override
            public void onDrawerClosed(View drawerView) {
                super .onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super .onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        defaultDataCopy();
        setFragment(R.layout.fragment_input);
    }

    private void setFragment(@LayoutRes int resId){
        Fragment f = null;
        int nOrientation = getRequestedOrientation();
        switch (resId){
            case R.layout.fragment_input:
                f = new InputFragment();
                break;
            case R.layout.fragment_display:

                f = new DisplayFragment();
                break;
            case R.layout.fragment_presentation:
                f = new PresentFragment();
                break;
            case R.layout.fragment_backup:
                f = new BackupFragment();
                break;
            case R.layout.fragment_setting:
                f = new SettingFragment();
                break;
            case R.layout.fragment_help:
                f = new HelpFragment();
                break;
        }

        if(f != null){
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, f);
            transaction.commit();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.nav_input:
                setFragment(R.layout.fragment_input);
                break;
            case R.id.nav_display:
                setFragment(R.layout.fragment_display);
                break;
            case R.id.nav_presentation:
                setFragment(R.layout.fragment_presentation);
                break;
            case R.id.nav_backup:
                setFragment(R.layout.fragment_backup);
                break;
            case R.id.nav_setting:
                setFragment(R.layout.fragment_setting);
                break;
            case R.id.nav_help:
                setFragment(R.layout.fragment_help);
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_title_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_wifi:
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                break;
            case R.id.menu_openAP:
                WifiApControl wifiApControl = WifiApControl.createApControl(this);
                if (wifiApControl != null)
                    wifiApControl.openAP(!wifiApControl.isWifiApEnabled());
                break;
            case R.id.menu_connectAP:
                WifiAdmin wifiAdmin = new WifiAdmin(this);
                wifiAdmin.openWifi();
                wifiAdmin.addNetwork();
                break;
        }
        return true;
    }

    private void defaultDataCopy() {
        String filename = "words.txt";
        File appDir = Environment.getExternalStoragePublicDirectory("MyTalker");
        File file = new File(appDir, filename);
        try {
            InputStream inputStream = getAssets().open(filename);
            MyFile.copyFile(inputStream, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
