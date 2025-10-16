package com.example.navigation;

import android.location.LocationManager;

import java.util.Calendar;

import static androidx.core.content.ContextCompat.getSystemService;

public class MyPublic {
    public static Double lat=null;
    public static Double lon=null;
    public static int year;
    public static int month;
    public static int day;
    public static Double declination=null;//形式为弧度
    public static void calender(){
        Calendar calendar=Calendar.getInstance();

        MyPublic.year=calendar.get(Calendar.YEAR);
        MyPublic.month=calendar.get(Calendar.MONTH)+1;
        MyPublic.day=calendar.get(Calendar.DAY_OF_MONTH);
    }
}
