package com.utils;


public class Check {
    static public boolean check_eng(char ch){
        boolean ret=false;
        if(ch>='a' && ch<='z' || ch>='A' && ch<='Z')
            ret=true;
        return ret;
    }

    static public boolean check_sign(char ch){
        boolean ret=false;
        if(ch>=32 && ch<=64 || ch>=91 && ch<=96 || ch>=123 && ch<=126)
            ret=true;
        return ret;
    }

    static public int checkMode(String text){
        if (text.startsWith(TransferMode.MODE_TEXT)){
            return TransferMode.IMODE_TEXT;
        } else if (text.startsWith(TransferMode.MODE_PAUSE)){
            return TransferMode.IMODE_PAUSE;
        } else if (text.startsWith(TransferMode.MODE_STOP)){
            return TransferMode.IMODE_STOP;
        }

        return -1;
    }
}
