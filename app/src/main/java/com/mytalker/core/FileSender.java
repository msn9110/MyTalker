package com.mytalker.core;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;

public class FileSender extends AsyncTask<Void, Long, Boolean> {
    private File mFile;
    private Context mContext;
    private String IP_SERVER;
    private ProgressDialog mDialog;
    private long mSize;

    public FileSender(Context context, File file, String IP) {
        mContext = context;
        mFile = file;
        IP_SERVER = IP;
        mDialog = new ProgressDialog(mContext);
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (! mFile.exists())
            return false;
        mSize = mFile.getUsableSpace();
        return null;
    }

    @Override
    protected void onProgressUpdate(Long... values) {

    }

    @Override
    protected void onPostExecute(Boolean result) {
        mDialog.dismiss();
        if (result) {
            Toast.makeText(mContext, "File Send Success !", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "File Send Failed.", Toast.LENGTH_LONG).show();
        }
    }
}
