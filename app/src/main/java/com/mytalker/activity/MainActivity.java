package com.mytalker.activity;

import android.app.Fragment;
import android.os.Bundle;
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
import android.view.MenuItem;
import android.view.View;

import com.example.mytalker.R;
import com.mytalker.fragment.BackupFragment;
import com.mytalker.fragment.DisplayFragment;
import com.mytalker.fragment.InputFragment;


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

        setFragment(R.layout.fragment_input);
    }

    private void setFragment(@LayoutRes int resId){
        Fragment f = null;
        switch (resId){
            case R.layout.fragment_input:
                f = new InputFragment();
                break;
            case R.layout.fragment_display:
                f = new DisplayFragment();
                break;
            case R.layout.fragment_backup:
                f = new BackupFragment();
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
            case R.id.nav_backup:
                setFragment(R.layout.fragment_backup);
                break;
            case R.id.nav_setting:

                break;
            case R.id.nav_help:

                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
