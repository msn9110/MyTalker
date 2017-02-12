package com.mytalker.core;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.utils.MyFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LearnFile extends AsyncTask<Void,Integer,Integer> {

    private static final String TAG = "## LearnFile";
    private Context mContext;
    private ProgressDialog mDialog;

    private int mFileLen;
    private LearnManager mLearn;
    private ArrayList<String> Data;

    public LearnFile(Context context,
                     final String path, LearnManager learn){
        mContext = context.getApplicationContext();
        mLearn = learn;
        Data = new ArrayList<>();
        mFileLen = readFromFile(path);
        mDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        mDialog.setTitle("文件學習");
        mDialog.setMessage("學習中.....");
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setMax(mFileLen);
        mDialog.show();
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        int count=0;
        publishProgress(0);
        for(int i=0;i<mFileLen;i++){
            String line=Data.get(i);
            int index=i+1;
            publishProgress(index);
            try{
                mLearn.Learning(line);
                count++;
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("Fail "+index+" : "+line);
            }
        }
        return count;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        mDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        mDialog.dismiss();
        float success=(float)result/mFileLen;
        if (success>=0.98) {
            Toast.makeText(mContext,"學習成功!!! "+result,Toast.LENGTH_SHORT).show();
            //System.out.println(result+'/'+mFileLen);
        } else {
            Toast.makeText(mContext,"學習失敗 : 成功率 : "+result+" / "+mFileLen,Toast.LENGTH_LONG).show();
            //System.out.println(result+'/'+mFileLen);
        }
    }

    private int readFromFile(String path) {
        int count=0;
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            MyFile.mkdirs(dir);
            // create the file in which we will write the contents
            File myFile = MyFile.getFile(new File(path));
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String aDataRow;
            while ((aDataRow = myReader.readLine()) != null) {
                Data.add(aDataRow);
                count++;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
        return count;
    }
}