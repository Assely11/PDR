package com.example.navigation;

import android.location.GnssStatus;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.CombinedData;

import java.util.LinkedList;


public class MyGnssStatus {
    public static GnssStatus.Callback callback;
    public static LinkedList<Satellite> satellites;
    public static LinkedList<IsUsed> isUseds=new LinkedList<IsUsed>();
    public static void GnssListener(CombinedChart chart, TextView time){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            callback=new GnssStatus.Callback() {
                @Override
                public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                    super.onSatelliteStatusChanged(status);
                    int isUsed=0;
                    satellites=new LinkedList<Satellite>();
                    for(int i=0;i<status.getSatelliteCount();i++){
                        if(status.usedInFix(i)){
                            isUsed++;
                            int type=status.getConstellationType(i);
                            float elevation=status.getElevationDegrees(i);
                            float azimuth=status.getAzimuthDegrees(i);
                            satellites.add(new Satellite(type,elevation,azimuth));
                        }
                    }
                    isUseds.add(new IsUsed(PDR.getMillSecond(),isUsed));
                    if(isUseds.size()>50){
                        isUseds.removeFirst();
                    }//控制数量，保证运算效率
                    time.setText(PDR.getCurentTime());
                    DrawLines.drawCombined(chart,satellites);
                    //Toast.makeText(MainActivity.context,isUsed+"",Toast.LENGTH_SHORT).show();
                }
            };
        }
    }
    static class Satellite{
        public int type;
        public float elevation;
        public float azimuthDegree;
        public Satellite(int _type,float _elevation,float _azimuth){
            type=_type;
            elevation=_elevation;
            azimuthDegree=_azimuth;
        }
    }
    static class IsUsed{
        public long time;
        public int count;
        public IsUsed(long _time,int _count){
            time=_time;
            count=_count;
        }
    }
}

