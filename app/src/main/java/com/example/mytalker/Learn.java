package com.example.mytalker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.Word;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class Learn {

    DBConnection helper;
    protected Dictionary dic;
    private String [] storewordspilt=new String[256];
    private int pointer_storewordspilt=0;
    Context context;
    public static final Object lock=new Object();

    public Learn(Context ctx,DBConnection dbConnection)
    {
        this.context=ctx;
        this.helper=dbConnection;
        System.setProperty("mmseg.dic.path", "./src/HelloChinese/data");
        dic = Dictionary.getInstance();
    }

    //=======================================學習===============================================
    public void Learning(String message){

        synchronized (lock){
            try {
                int sentece_lenth=1;
                //handle sentence
                if (message.length()>sentece_lenth){
                    if(!helper.update(false,message,helper.getWritableDatabase())){
                        helper.insert(false,message,helper.getWritableDatabase());
                    }
                }

                String msg=SpiltString(message, helper.getWritableDatabase());
                //System.out.println(msg);

                //handle vocabulary
                for(int i = 0 ; i < pointer_storewordspilt ; i++){
                    String word=((i==pointer_storewordspilt)?"#":storewordspilt[i]);
                    if(!helper.update(true,word,helper.getWritableDatabase())){
                        helper.insert(true,word,helper.getWritableDatabase());
                    }
                }
                //handle relation
                for(int i = 0 ; i < pointer_storewordspilt-1 ; i++){
                    int id1=helper.getVocID(storewordspilt[i],helper.getWritableDatabase());
                    int id2=helper.getVocID(storewordspilt[i+1],helper.getWritableDatabase());
                    if(!helper.update(id1,id2,helper.getWritableDatabase())){
                        helper.insert(id1,id2,helper.getWritableDatabase());
                    }

                }
                clear_storeword_spilt();
            }
            catch (Exception e){
                Toast.makeText(context, "斷字失敗", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //===============================================================================================

    //斷字系統=======================================================================================
    private void clear_storeword_spilt(){
        for(int i = 0 ; i < 256 ; i++){
            storewordspilt[i]="";
        }
        pointer_storewordspilt=0;
    }

    protected Seg getSeg() {
        return new ComplexSeg(dic);
    }

    public String segWords(String txt, String wordSpilt) throws IOException {
        Reader input = new StringReader(txt);
        StringBuilder sb = new StringBuilder();
        Seg seg = getSeg();
        MMSeg mmSeg = new MMSeg(input, seg);
        Word word;
        boolean first = true;
        while((word=mmSeg.next())!=null) {
            if(!first) {
                sb.append(wordSpilt);
            }
            String w = word.getString();
            char ch=w.charAt(0);
            if ((ch>='A' && ch<='Z') || (ch>='a' && ch<='z'))
                w+=" ";
            storewordspilt[pointer_storewordspilt]=w;
            pointer_storewordspilt++;
            sb.append(w);
            first = false;

        }
        return sb.toString();
    }

    public String SpiltString(String s,SQLiteDatabase db){
        String tmp="";
        int strlen=s.length();
        //check for content whether in database
        for(int i=0;i<strlen;) {
            boolean flag = true;
            char ch=s.charAt(i);
            String str = String.valueOf(ch);
            if(Check.check_sign(ch)){
                tmp+=" ";
                i++;
                continue;
            }

            else if (Check.check_eng(ch)){
                tmp+=String.valueOf(ch);
                i++;
                continue;
            }
            Cursor c = db.rawQuery("SELECT * FROM "+DBConnection.VocSchema.TABLE_NAME+
                    " WHERE "+DBConnection.VocSchema.CONTENT+" = '" + str + "';", null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                tmp=tmp+" "+str+" ";
                i++;
                continue;
            }
            c.close();

            if (i + 1 == strlen)
                flag = false;
            //check the vocabulary in combination of current and next character whether is in the database
            //if yes ,update weight in database, and continue loop
            if (flag) {
                str += String.valueOf(s.charAt(i + 1));
                c = db.rawQuery("SELECT * FROM "+DBConnection.VocSchema.TABLE_NAME+
                        " WHERE "+DBConnection.VocSchema.CONTENT+" = '" + str + "';", null);
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    tmp+=str+" ";
                    i += 2;
                    c.close();
                    continue;
                }
            }
            c.close();
            str = String.valueOf(s.charAt(i));
            tmp+=str;
            i++;
        }

        db.close();
        try {
            return spilt(tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public String spilt(String args) throws IOException {
        String txt;

        if(args.length() > 0) {
            txt = args;
            return segWords(txt, "|");
        }
        else
            return "";
    }
}
