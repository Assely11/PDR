package com.example.navigation;

//storage time
public class DataItem {
    float[] _values;
    //unit ms
    long _timeStamp;

    DataItem(float[] values,long timeStamp){
        _values=values;
        _timeStamp=timeStamp;
    }

    //@Override
    public String toString(){
        return String.format("%d,%f,%f,%f\n",_timeStamp,_values[0],_values[1],_values[2]);
    }
}

