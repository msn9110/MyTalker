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
import java.io.Serializable;
import java.io.StringReader;

public class LearnManager implements Serializable {
    private static final long serialVersionUID = -6919461967497580385L;

    private TalkerDBManager talkerDBManager;
    private Dictionary dic;
    private String [] spiltWords = new String[256];
    private int pSpiltWords = 0;
    private Context context;
    private static final Object lock = new Object();

    public LearnManager(Context context, TalkerDBManager dbManager)
    {
        this.context = context;
        talkerDBManager = dbManager;
        System.setProperty("mmseg.dic.path", "./data");
        dic = Dictionary.getInstance();
    }

    //=======================================學習===============================================
    public void Learning(String message){

        synchronized (lock){
            try {
                int sentenceLength = 1;
                //handle sentence
                if (message.length() > sentenceLength){
                    if(!talkerDBManager.updateSentence(message)){
                        talkerDBManager.insertSentence(message);
                    }
                }

                System.out.println("# spilt result : " + spilt(preProcess(message)));

                //handle vocabulary
                for(int i = 0 ; i < pSpiltWords ; i++){
                    String word = spiltWords[i];
                    if(!talkerDBManager.updateVoc(word)){
                        talkerDBManager.insertVoc(word);
                    }
                }
                //handle relation
                for(int i = 0 ; i < pSpiltWords - 1 ; i++){
                    if(!talkerDBManager.updateRelation(spiltWords[i], spiltWords[i+1])){
                        talkerDBManager.insertRelation(spiltWords[i], spiltWords[i+1]);
                    }
                }
            }
            catch (Exception e){
                Toast.makeText(context, "斷字失敗", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //===============================================================================================

    //==========================================斷字系統=============================================
    private String segWords(String txt, String spiltSign) throws IOException {
        pSpiltWords = 0;
        Reader input = new StringReader(txt);
        StringBuilder sb = new StringBuilder();
        Seg seg = new ComplexSeg(dic);
        MMSeg mmSeg = new MMSeg(input, seg);
        Word word;
        while((word = mmSeg.next()) != null) {
            String w = word.getString();
            if (Check.check_eng(w.charAt(0)))
                w += " ";
            spiltWords[pSpiltWords++] = w;
            w += spiltSign;
            sb.append(w);
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
        String tmp = "";
        int strlen = s.length();
        //check for content whether in database
        for(int i = 0; i < strlen;) {
            boolean flag = true;
            char ch = s.charAt(i);
            String str = String.valueOf(ch);
            if(Check.check_sign(ch)){
                tmp += " ";
                i++;
                continue;
            }

            else if (Check.check_eng(ch)){
                tmp += String.valueOf(ch);
                i++;
                continue;
            }
            if (talkerDBManager.isExistVoc(str)) {
                tmp = tmp + " " + str + " ";
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
                    tmp = tmp + " " + str + " ";
                    i += 2;
                    continue;
                }
            }
            str = String.valueOf(s.charAt(i));
            tmp += str;
            i++;
        }
        return tmp;
    }
}
