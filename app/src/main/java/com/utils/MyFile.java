package com.utils;



import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MyFile {


    static public void mkdirs(File dir) {
        //判斷文件夾是否存在,如果不存在則建立文件夾
        if (!dir.exists()) {
            if(!dir.mkdirs())
                System.out.println("MakeDir : Fail");
        }
    }

    static public boolean moveFile(File source, File target) {
        return copyFile(source, target) && source.delete();
    }

    static public boolean copyFile(File source, File target) {

        InputStream in;
        OutputStream out;
        try {

            //create output directory if it doesn't exist
            File dir = new File (target.getParent());
            mkdirs(dir);

            in = new FileInputStream(source.getPath());
            out = new FileOutputStream(target.getPath());

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            // write the output file
            out.flush();
            out.close();
            return true;

        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
        return false;
    }

    static public boolean copyFile(InputStream in, File target) {

        OutputStream out;
        try {

            //create output directory if it doesn't exist
            File dir = new File (target.getParent());
            mkdirs(dir);

            out = new FileOutputStream(target.getPath());

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            // write the output file
            out.flush();
            out.close();
            return true;

        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
        return false;
    }

    static public void log(String sentence){
        File dir=new File(Environment.getExternalStorageDirectory().getPath() + "/MyTalker/紀錄");
        mkdirs(dir);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);
        String filename = format.format(Calendar.getInstance().getTime()) + ".txt";
        File file = new File(dir, filename);
        try{
            FileOutputStream out = new FileOutputStream(file, true);
            BufferedWriter myWriter = new BufferedWriter(new OutputStreamWriter(out, CharsetDetector.detect(file)));
            //myWriter.newLine();
            myWriter.write(sentence + "\n");
            myWriter.flush();
            myWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    //no construct
    private MyFile(){

    }
}
