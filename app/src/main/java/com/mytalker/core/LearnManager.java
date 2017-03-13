package com.mytalker.core;

import android.content.Context;
import android.widget.Toast;

import com.utils.Check;
import com.utils.Divider;

import java.io.Serializable;
import java.util.ArrayList;

public class LearnManager implements Serializable {
    private static final long serialVersionUID = -6919461967497580385L;

    private TalkerDBManager talkerDBManager;
    private Context context;
    private static final Object lock = new Object();
    private Divider divider;
    private ArrayList<String> myWords = new ArrayList<>();

    public LearnManager(Context context, TalkerDBManager dbManager)
    {
        this.context = context;
        talkerDBManager = dbManager;
        divider = new Divider("./data", myWords);
    }

    //=======================================學習===============================================
    public void Learning(String message){

        synchronized (lock){
            try {
                final int sentenceLength = 1;

                ArrayList<String> sentences = Divider.getSentences(message);

                for (String sentence : sentences){
                    //handle sentence
                    if (sentence.length() > sentenceLength){
                        if(!talkerDBManager.updateSentence(sentence)){
                            talkerDBManager.insertSentence(sentence);
                        }
                    }
                    System.out.println("# spilt result : " + divider.spiltSentence(preProcess(sentence)));

                    //handle vocabulary
                    for(int i = 0 ; i < myWords.size() ; i++){
                        String word = myWords.get(i);
                        if(!talkerDBManager.updateVoc(word)){
                            talkerDBManager.insertVoc(word);
                        }
                    }
                    //handle relation
                    for(int i = 0 ; i < myWords.size() - 1 ; i++){
                        String current = myWords.get(i);
                        String next = myWords.get(i + 1);
                        if(!talkerDBManager.updateRelation(current, next)){
                            talkerDBManager.insertRelation(current, next);
                        }
                    }
                }

                if (sentences.size() > 1 && message.length() > sentenceLength){
                    //handle whole sentence
                    if(!talkerDBManager.updateSentence(message)){
                        talkerDBManager.insertSentence(message);
                    }
                }
            } catch (Exception e){
                Toast.makeText(context, "斷字失敗", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //==============================================================================================

    private String preProcess(String s){
        final int maxCheckLength = 2;
        String result = "";
        int length = s.length();

        //check for content whether in database
        for(int i = 0; i < length;) {

            if (Check.checkChar(s.charAt(i)) < 1){
                result += s.substring(i, i + 1);
                i++;
            } else {
                String word = null;
                for (int j = 0; j < maxCheckLength; j++){
                    int endIndex = i + j + 1;
                    word = endIndex < length ? s.substring(i, endIndex) : null;
                    if (word == null)
                        break;
                    if (talkerDBManager.isExistVoc(word)){
                        result += " " + word + " ";
                        i += (j + 1);
                        break;
                    }

                }

                if (word == null){
                    result += s.substring(i, i + 1);
                    i++;
                }
            }

        }
        return result;
    }
}
