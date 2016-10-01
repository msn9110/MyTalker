package com.example.mytalker;



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

public class MyFile {

    static public String charset="BIG5";
    static public String charset_target="UTF-8";
    static public String prefix="("+charset_target+")";

    static public File getFile(File inputFile){
        File myDir=new File(Environment.getExternalStorageDirectory()+"/MyTalker/"+prefix);
        mkdirs(myDir);
        String name=inputFile.getName();
        String origin_name=name;
        name=name.toUpperCase();
        if(name.contains(charset_target))
            return inputFile;
        String filename=prefix+origin_name;
        File myFile=new File(myDir,filename);
        try{

            FileInputStream in = new FileInputStream(inputFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(in, Charset.forName(charset)));
            FileOutputStream out=new FileOutputStream(new File(myDir,filename));
            BufferedWriter myWriter=new BufferedWriter(new OutputStreamWriter(out,Charset.forName(charset_target)));

            char[] buffer = new char[1024];
            int read;
            while ((read = myReader.read(buffer)) != -1) {
                myWriter.write(buffer,0,read);
            }
            myReader.close();
            // write the output file
            myWriter.flush();
            myWriter.close();
        }catch (Exception ex){
            Log.e("MyFile",ex.toString());
        }
        return myFile;
    }

    static public void mkdirs(File dir) {
        //判斷文件夾是否存在,如果不存在則建立文件夾
        if (!dir.exists()) {
            if(!dir.mkdirs())
                System.out.println("MakeDir : Fail");
        }
    }

    static public void moveFile(File source, File target) {
        copyFile(source,target);
        deleteFiles(source);
    }

    static public void deleteFiles(File file) {
        try {
            // delete the original file
            if(!file.delete())
                System.out.println("Delete : Fail");
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    static public void copyFile(File source, File target) {

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

        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    static public void log(String sentence){
        File dir=new File(Environment.getExternalStorageDirectory().getPath()+"/MyTalker/紀錄");
        mkdirs(dir);
        DateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        String filename= prefix+format.format(Calendar.getInstance().getTime())+".txt";
        File file=new File(dir,filename);
        try{
            FileOutputStream out=new FileOutputStream(file,true);
            BufferedWriter myWriter=new BufferedWriter(new OutputStreamWriter(out,Charset.forName(charset_target)));
            //myWriter.newLine();
            myWriter.write(sentence+"\n");
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
