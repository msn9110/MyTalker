package com.utils;


import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.Word;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

public class Divider {

    private Dictionary dic;
    public Divider(String dicPath){
        if (dicPath != null)
            System.setProperty("mmseg.dic.path", dicPath);
        this.dic = Dictionary.getInstance();
    }

    private String segWords(String txt, String spiltSign, ArrayList<String> myWords) throws IOException {
        Reader input = new StringReader(txt);
        StringBuilder sb = new StringBuilder();
        Seg seg = new ComplexSeg(dic);
        MMSeg mmSeg = new MMSeg(input, seg);
        Word word;
        while((word = mmSeg.next()) != null) {
            String w = word.getString();
            if (Check.check_eng(w.charAt(0)))
                w += " ";
            myWords.add(w);
            w += spiltSign;
            sb.append(w);
        }
        return sb.toString().substring(0, sb.length() - 1);
    }

    public String spiltSentence(String args, ArrayList<String> result) {
        String txt;
        if(args.length() > 0) {
            txt = args;
            try {
                return segWords(txt, "|", result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return args;
    }

    static public ArrayList<String> getSentences(String message){
        ArrayList<String> result = new ArrayList<>();
        //message = message.replaceAll("(\\W\\s\\W)", "$1,$3");
        String regex = "(?<=[,.，。])";

        String[] temp = message.split(regex);
        result.addAll(Arrays.asList(temp));
        return result;
    }
}
