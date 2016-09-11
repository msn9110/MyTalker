package com.example.mytalker;



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

public class MyFile {
    static public File getFile(File inputFile){
        File myDir=inputFile.getParentFile();
        String name=inputFile.getName();
        String prefix="UTF8-";
        if(name.startsWith(prefix))
            return inputFile;
        String filename=prefix+name;
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

    static public void mkdirs(File dir) {
        //判斷文件夾是否存在,如果不存在則建立文件夾
        if (!dir.exists()) {
            if(!dir.mkdirs())
                System.out.println("MakeDir : Fail");
        }
    }

    static public void moveFile(String inputPath, String outputPath, String filename) {
        copyFile(inputPath,outputPath,filename);
        deleteFiles(inputPath,filename);
    }

    static public void deleteFiles(String inputPath, String filename) {
        try {
            // delete the original file
            if(!new File(inputPath+filename).delete())
                System.out.println("Delete : Fail");
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    static public void copyFile(String inputPath, String outputPath, String filename) {

        InputStream in;
        OutputStream out;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            mkdirs(dir);

            in = new FileInputStream(inputPath + filename);
            out = new FileOutputStream(outputPath + filename);

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
}
