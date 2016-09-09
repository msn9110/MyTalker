package com.example.mytalker;


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
}
