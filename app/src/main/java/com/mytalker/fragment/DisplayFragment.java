package com.mytalker.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mytalker.R;
import com.mytalker.core.ReceiveManager;


public class DisplayFragment extends Fragment {
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
        mView = inflater.inflate(R.layout.fragment_display, container, false);
        initialize();
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        manager = new ReceiveManager(mContext, handler, tvDisplay);
        manager.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.cancel();
        try {
            manager.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //public static final String END = "!!!@@@###";
    String TAG = "## DisplayFragment";
    TextView tvDisplay;
    ReceiveManager manager;
    private Handler handler = new Handler();

    private void initialize(){
        //initialize
        tvDisplay = (TextView) mView.findViewById(R.id.txtDisplay);
        tvDisplay.setText(R.string.empty);
        Log.i(TAG, "Init done !");
    }
}
