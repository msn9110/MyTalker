package com.utils;


import java.util.ArrayList;

public class Divider {

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
