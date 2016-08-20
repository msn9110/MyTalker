package com.example.mytalker;


public class InputData {
    public String text="";
    public int id=0;
    public InputData(){

    }

    public InputData(String s){
        setText(s);
    }

    public InputData(String s,int sid){
        text=s;
        id=sid;
    }

    public void setText(String s){
        text=s;
    }
}
