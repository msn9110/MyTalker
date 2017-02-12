package com.mytalker.core;

import android.content.Context;
import android.widget.Toast;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.Word;
import com.utils.Check;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class LearnManager {

    private TalkerDBManager talkerDBManager;
    private Dictionary dic;
    private String [] storewordspilt=new String[256];
    private int pointer_storewordspilt=0;
    private Context context;
    private static final Object lock=new Object();

    public LearnManager(Context ctx, TalkerDBManager dbManager)
    {
        this.context = ctx;
        talkerDBManager = dbManager;
        System.setProperty("mmseg.dic.path", "./src/HelloChinese/data");
        dic = Dictionary.getInstance();
    }

    //=======================================學習===============================================
    public void Learning(String message){

        synchronized (lock){
            try {
                int sentece_lenth=1;
                //handle sentence
                if (message.length() > sentece_lenth){
                    if(!talkerDBManager.updateSentence(message)){
                        talkerDBManager.insertSentence(message);
                    }
                }

                System.out.println("# " + spilt(preProcess(message)));

                //handle vocabulary
                for(int i = 0 ; i < pointer_storewordspilt ; i++){
                    String word=storewordspilt[i];//((i==pointer_storewordspilt)?"#":storewordspilt[i]);
                    if(!talkerDBManager.updateVoc(word)){
                        talkerDBManager.insertVoc(word);
                    }
                }
                //handle relation
                for(int i = 0 ; i < pointer_storewordspilt-1 ; i++){
                    if(!talkerDBManager.updateRelation(storewordspilt[i], storewordspilt[i+1])){
                        talkerDBManager.insertRelation(storewordspilt[i], storewordspilt[i+1]);
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

    private Seg getSeg() {
        return new ComplexSeg(dic);
    }

    private String segWords(String txt, String wordSpilt) throws IOException {
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

    private String spilt(String args) {
        String txt;
        if(args.length() > 0) {
            txt = args;
            try {
                return segWords(txt, "|");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return args;
    }

    private String preProcess(String s){
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
            if (talkerDBManager.isExistVoc(str)) {
                tmp=tmp+" "+str+" ";
                i++;
                continue;
            }

            if (i + 1 == strlen)
                flag = false;
            //check the vocabulary in combination of current and next character whether is in the database
            //if yes ,update weight in database, and continue loop
            if (flag) {
                str += String.valueOf(s.charAt(i + 1));
                if (talkerDBManager.isExistVoc(str)) {
                    tmp=tmp+" "+str+" ";
                    i += 2;
                    continue;
                }
            }
            str = String.valueOf(s.charAt(i));
            tmp+=str;
            i++;
        }
        return tmp;
    }
}
