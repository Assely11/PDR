package com.example.navigation;

public class Point2f {
    public float x;
    public float y;
    public long time;
    public Point2f(long _time,float _x,float _y){
        time=_time;
        x=_x;
        y=_y;
    }
    public String toString(){
        return String.format("%d,%f,%f\n",time,x,y);
    }
}
