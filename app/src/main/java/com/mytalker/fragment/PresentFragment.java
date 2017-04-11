package com.mytalker.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mytalker.R;
import com.mytalker.core.Speaker;
import com.mytalker.core.SpeakingListener;
import com.utils.Divider;
import com.utils.MyFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PresentFragment extends Fragment implements SpeakingListener, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener
{
    private final static String TAG = "## PresentFragment";
    private Context mContext;
    private View mView;
    private Speaker mSpeaker;
    private Handler mHandler = new Handler();
    private ListView fileList, functionList;
    private TextView txtDisplay;
    private ImageView imgDisplay;
    private File currentFile = Environment.getExternalStoragePublicDirectory("MyTalker");

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSpeaker.shutdown();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_presentation, container, false);
        currentFile.mkdirs();
        mSpeaker = new Speaker(mContext);
        mSpeaker.setSpeakingListener(this);
        initialize();
        return mView;
    }

    private void initialize() {
        txtDisplay = (TextView) mView.findViewById(R.id.txtDisplay);
        imgDisplay = (ImageView) mView.findViewById(R.id.imgDisplay);
        fileList = (ListView) mView.findViewById(R.id.fileList);
        functionList = (ListView) mView.findViewById(R.id.functionList);

        txtDisplay.setText("");
        setFunctionList();
        setFileList();
        functionList.setOnItemClickListener(this);
        fileList.setOnItemClickListener(this);
        functionList.setOnItemLongClickListener(this);
        fileList.setOnItemLongClickListener(this);
    }

    final String ENCODING = "更改文件編碼";
    final String CLEAR = "清除畫面";
    final String ALLPLAY = "播放全部";
    private void setFunctionList() {
        ArrayList<String> functions = new ArrayList<>(Arrays.asList("暫停/繼續", "停止", ENCODING, CLEAR, ALLPLAY));
        setList(functions, functionList);
    }

    private void setFileList() {
        setList(getListContent(currentFile), fileList);
    }

    private void setList(ArrayList<String> contents, ListView myList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, contents);
        myList.setAdapter(adapter);
    }

    final String BACK = "(上一頁)";
    private ArrayList<String> getListContent(File target) {
        ArrayList<String> myList = new ArrayList<>();
        myList.add(BACK);
        if (target.isDirectory()) {
            File[] myFiles = target.listFiles();
            ArrayList<String> dirs = new ArrayList<>();
            ArrayList<String> files = new ArrayList<>();
            for (File f : myFiles) {
                if (f.isDirectory()) {
                    dirs.add(f.getName());
                } else if (f.isFile()) {
                    Uri uri = Uri.fromFile(f);
                    String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
                    try {
                        String type = mimeType.split("/")[0];
                        List<String> types = Arrays.asList("text", "image");
                        if (types.contains(type))
                            files.add(f.getName());
                    } catch (Exception ex) {
                        Log.w(TAG, ex.getMessage());
                    }
                }
            }
            myList.addAll(dirs);
            myList.addAll(files);
        } else {
            try {
                String charset = MyFile.charset_target;
                BufferedReader myReader;
                File myFile = MyFile.getFile(target);
                FileInputStream in = new FileInputStream(myFile);
                myReader = new BufferedReader(new InputStreamReader(in, Charset.forName(charset)));
                String line;
                while ((line = myReader.readLine()) != null){
                    if (line.replaceAll("\\s", "").length() > 0)
                        myList.addAll(Divider.getSentences(line));
                    //myList.add(line);
                }
                myReader.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return myList;
    }

    @Override
    public void onPreSpeak(final String message) {
        final int font = 6000 / (message.length() + 40);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                txtDisplay.setTextSize(font);
                txtDisplay.setText(message);
            }
        });
    }

    private void playTextFile(File file) {
        try {
            File myFile = MyFile.getFile(file);
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String line;
            while ((line = myReader.readLine()) != null) {
                if(line.length() == 0)
                    continue;
                mSpeaker.addSpeak(line);
            }
            myReader.close();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
    }

    private void fileListItemClick(String select, boolean longClick) {
        Log.d(TAG, "file list");
        if (select.contentEquals(BACK)) {
            if (currentFile.equals(Environment.getExternalStorageDirectory()))
                return;
            currentFile = currentFile.getParentFile();
            setFileList();
        } else if (currentFile.isFile() && currentFile.getName().endsWith(".txt")) {
            Log.d(TAG, "In else if.");
            mSpeaker.addSpeak(select);
        } else {
            Log.d(TAG, "In else.");
            File file = new File(currentFile, select);
            if (file.isDirectory()) {
                currentFile = file;
                setFileList();
            } else if (file.isFile()) {
                Uri uri = Uri.fromFile(file);
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
                String type = mimeType.split("/")[0];
                switch (type) {
                    case "text":
                        if (longClick) {
                            playTextFile(file);
                        } else {
                            Log.d(TAG, "Long Click Called.");
                            currentFile = new File(currentFile, select);
                            Log.d(TAG, currentFile.getName());
                            setList(getListContent(currentFile), fileList);
                        }
                        break;
                    case "image":
                        Bitmap pic = BitmapFactory.decodeFile(file.getAbsolutePath());
                        imgDisplay.setImageBitmap(pic);
                        break;
                }
            }
        }
    }

    private void functionListOnItemClick(String select, boolean longClick){
        switch (select){
            case "暫停/繼續":
                mSpeaker.pause();
                break;
            case "停止":
                mSpeaker.stop();
                break;
            case ENCODING:
                MyFile.setCharset();
                Toast.makeText(mContext, MyFile.charset, Toast.LENGTH_SHORT).show();
                break;
            case CLEAR:
                if (!longClick)
                    txtDisplay.setText("");
                else {
                    txtDisplay.setText("");
                    imgDisplay.setImageResource(android.R.color.transparent);
                }
                break;
            case ALLPLAY:
                if (currentFile.getName().endsWith(".txt"))
                    playTextFile(currentFile);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String select = ((TextView) view).getText().toString();
        switch (parent.getId()) {
            case R.id.fileList:
                fileListItemClick(select, false);
                break;
            case R.id.functionList:
                functionListOnItemClick(select, false);
                break;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        String select = ((TextView) view).getText().toString();
        switch (adapterView.getId()) {
            case R.id.fileList:
                fileListItemClick(select, true);
                break;
            case R.id.functionList:
                functionListOnItemClick(select, true);
                break;
        }
        return true;
    }
}
