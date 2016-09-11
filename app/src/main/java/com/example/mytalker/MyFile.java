package com.example.mytalker;


import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class MyFile {
    static public File getFile(File inputFile){
        File myDir=new File(Environment.getExternalStorageDirectory(),"MyTalker");
        if(!myDir.exists()){
            boolean success=myDir.mkdirs();
            if(!success)
                System.out.println("Make Dir : Fail");
        }
        String filename="UTF8-"+inputFile.getName();
        File myFile=new File(myDir,filename);
        if (!myFile.exists()){
            try{

                FileInputStream in = new FileInputStream(inputFile);
                BufferedReader myReader = new BufferedReader(new InputStreamReader(in, Charset.forName("BIG5")));
                FileOutputStream out=new FileOutputStream(myDir.getPath()+"/"+filename);
                BufferedWriter myWriter=new BufferedWriter(new OutputStreamWriter(out,Charset.forName("UTF-8")));

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

        }
        return myFile;
    }
}
