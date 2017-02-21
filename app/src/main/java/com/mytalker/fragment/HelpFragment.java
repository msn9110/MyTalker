package com.mytalker.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mytalker.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class HelpFragment extends Fragment implements AdapterView.OnItemClickListener{
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
        mView = inflater.inflate(R.layout.fragment_help, container, false);
        initialize();
        return mView;
    }

    private ListView helpList;
    private TextView tvFile, tvContent;
    private void initialize(){
        helpList = (ListView) mView.findViewById(R.id.helpList);
        tvFile = (TextView) mView.findViewById(R.id.txtFileName);
        tvContent = (TextView) mView.findViewById(R.id.readme);

        helpList.setOnItemClickListener(this);
        setHelpList();
    }

    private void setHelpList(){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, new String[]{"代言人", "顯示器", "資料備份"});
        helpList.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String select = ((TextView) view).getText().toString();
        tvFile.setText(select);
        try {
            InputStream in = mContext.getAssets().open("help/" + select + ".txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String content = "", line;
            while ((line = reader.readLine()) != null)
                content += line + "\n";
            reader.close();
            tvContent.setText(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
