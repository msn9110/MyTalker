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

public class Divider {

    private Dictionary dic;
    private ArrayList<String> myWords;
    public Divider(String dicPath, ArrayList<String> results){
        if (dicPath != null)
            System.setProperty("mmseg.dic.path", dicPath);
        this.dic = Dictionary.getInstance();
        this.myWords = results;
    }

    private String segWords(String txt, String spiltSign) throws IOException {
        myWords.clear();
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

    public String spiltSentence(String args) {
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

    static public ArrayList<String> getSentences(String message){
        ArrayList<String> result = new ArrayList<>();
        final String[] checkSigns = new String[] {",", "，", ".", "。"};
        while (! message.contentEquals("")){
            int[] indexes = new int[checkSigns.length];
            for (int i = 0; i < checkSigns.length; i++){
                indexes[i] = (message.contains(checkSigns[i]) ? message.indexOf(checkSigns[i])
                                                                : Integer.MAX_VALUE);
            }
            int minIndex = 0;
            for (int i = 1; i < checkSigns.length; i++){
                if (indexes[i] < indexes[minIndex])
                    minIndex = i;
            }
            if (indexes[minIndex] == Integer.MAX_VALUE){
                result.add(message);
                message = "";
                //System.out.println(message);
            } else {
                String tmp = message.substring(0, indexes[minIndex] + 1);
                result.add(tmp);
                message = message.replace(tmp, "");
                //System.out.println(" jjj "+message);
            }
        }
        return result;
    }
}
