package com.example.navigation;

import java.util.ArrayList;

public class MyList {
    public double[] items=new double[0];
    public void addItem(double item){
        double[] temp=new double[items.length+1];
        for(int i=0;i<items.length;i++){
            temp[i]=items[i];
        }
        temp[temp.length-1]=item;
        items=new double[temp.length];
        for(int i=0;i<temp.length;i++){
            items[i]=temp[i];
        }
    }
    public double average(){
        double res=0.0;
        for(int i=0;i<items.length;i++){
            res+=items[i];
        }
        res=res/items.length;
        return res;
    }
    public void removeFirst(){
        double[] temp=new double[items.length-1];
        for(int i=0;i<temp.length;i++){
            temp[i]=items[i+1];
        }
        items=new double[temp.length];
        for(int i=0;i<temp.length;i++){
            items[i]=temp[i];
        }
    }
}
