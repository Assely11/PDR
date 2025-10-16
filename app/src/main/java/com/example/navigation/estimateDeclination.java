package com.example.navigation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

//估计磁偏角
public class estimateDeclination {
    private static LinkedList<DateYaw> sensorYaw = new LinkedList<DateYaw>();
    private static boolean newAcc = false;
    private static boolean newMagn = false;

    private static float[] acc;
    private static float[] magn;

    private static LinkedList<Float> yaw = new LinkedList<Float>();
    private static LinkedList<DateYaw> avaerYaw = new LinkedList<DateYaw>();//因为具体计算位置用的方向角为平滑后的姿态角

    private static LinkedList<DateLocation> locations = new LinkedList<DateLocation>();

    private static LinkedList<Double> estiDeclinations = new LinkedList<Double>();

    //传感器监听获取yaw
    public static void SensorManager(SensorManager sensorManager) {
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                switch (sensorEvent.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        newAcc = true;
                        acc = new float[]{
                                sensorEvent.values[0],
                                sensorEvent.values[1],
                                sensorEvent.values[2]
                        };
                        break;
                    case Sensor.TYPE_GRAVITY:
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        newMagn = true;
                        magn = new float[]{
                                sensorEvent.values[0],
                                sensorEvent.values[1],
                                sensorEvent.values[2]
                        };
                        break;
                }
                if (newMagn && newAcc) {
                    float[] orien = PDR.estimateOrientationByCustomer(acc, magn);
                    yaw.add(orien[0]);
                    float estimateYaw=yaw.get(yaw.size()-1);
                    if(yaw.size()==1){
                        avaerYaw.add(new DateYaw(PDR.getMillSecond(),yaw.get(0)));
                        //estimateYaw=yaw.get(0);
                    }else{
                        estimateYaw=AverYaw2(yaw.get(yaw.size()-1));
                        avaerYaw.add(new DateYaw(PDR.getMillSecond(),estimateYaw));
                    }
                    /*
                    if (yaw.size() > 1) {
                        float averYaw = PDR.AverageYaw(yaw.get(yaw.size() - 2), yaw.get(yaw.size() - 1));
                        avaerYaw.add(new DateYaw(PDR.getMillSecond(), averYaw));
                    }*/
                    if (avaerYaw.size() > 100) {
                        avaerYaw.removeFirst();
                    }//控制列表数量，提高后续计算效率
                    newMagn = false;
                    newAcc = false;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_UI);
    }
    private static float AverYaw2(float new_Yaw){
        float old_Yaw=avaerYaw.get(avaerYaw.size()-1).yaw;
        if(Math.abs(old_Yaw-new_Yaw)>5.0){
            if(old_Yaw<0){
                old_Yaw=(float) (old_Yaw+2*Math.PI);
            }
            if(new_Yaw<0){
                new_Yaw=(float) (new_Yaw+2*Math.PI);
            }
        }
        float ema1=0.95f,ema2=0.96f,ema3=0.97f,ema4=0.98f,ema5=0.99f;
        float _lastYaw_fir=old_Yaw*ema1+(1-ema1)*new_Yaw;
        float _lastYaw_sed=_lastYaw_fir*ema2+(1-ema2)*new_Yaw;
        float _lastYaw_trd=_lastYaw_sed*ema3+(1-ema3)*new_Yaw;
        float _lastYaw_for=_lastYaw_trd*ema4+(1-ema4)*new_Yaw;
        float _lastYaw_fifth=_lastYaw_for*ema5+(1-ema5)*new_Yaw;

        if(_lastYaw_fifth>Math.PI){
            _lastYaw_fifth=(float) (_lastYaw_fifth-2*Math.PI);
        }
        return _lastYaw_fifth;
    }
    //位置监听器获取检测位置变化，计算estimate——yaw，
    //根据根据参考偏差和时差，判断是否收录
    //截止惯导时刻，求收录磁偏角的均值
    public static void LocationManager(LocationManager locationManager) {
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                //个人导航一般不会持续太长时间，因此calendar无需更新
                if(MyPublic.declination==null){
                    MyPublic.declination=IgrfUtils.getNowDeclination(location.getLatitude(),location.getLongitude(),
                            MyPublic.year,MyPublic.month,MyPublic.day)*Math.PI/180.0;
                }else{
                    if(Math.abs(MyPublic.lat-location.getLatitude())>1.0||
                            Math.abs(MyPublic.lon-location.getLongitude())>1.0){
                        MyPublic.declination=IgrfUtils.getNowDeclination(location.getLatitude(),location.getLongitude(),
                                MyPublic.year,MyPublic.month,MyPublic.day)*Math.PI/180.0;
                        MyPublic.lat=location.getLatitude();
                        MyPublic.lon=location.getLongitude();
                    }
                }

                OutputXY xy = Transer.BL2XY(location.getLatitude(), location.getLongitude());

                locations.add(new DateLocation(PDR.getMillSecond(), xy.x, xy.y));
                //开始执行磁偏角估计
                if (locations.size() > 1) {
                    DateLocation oldLocation = locations.get(locations.size() - 2);
                    DateLocation newLocation = locations.get(locations.size() - 1);
                    //两位置处前后时延小于1秒
                    if (Math.abs(newLocation.time - oldLocation.time) < 1000) {
                        DateYaw oldAverYaw = null;
                        DateYaw newAverYaw = null;
                        //遍历寻找最近平滑角
                        for (int i = 0; i < avaerYaw.size(); i++) {
                            if (Math.abs(avaerYaw.get(i).time - oldLocation.time) <= 200) {
                                oldAverYaw = avaerYaw.get(i);
                            }
                            if (Math.abs(avaerYaw.get(i).time - newLocation.time) <= 200) {
                                newAverYaw = avaerYaw.get(i);
                            }
                        }
                        double averTime = (oldLocation.time + newLocation.time) / 2.0;
                        MyGnssStatus.IsUsed isUsed = null;
                        for (int i = 0; i < MyGnssStatus.isUseds.size(); i++) {
                            if (Math.abs(MyGnssStatus.isUseds.get(i).time - averTime) <= 1000) {
                                isUsed = MyGnssStatus.isUseds.get(i);
                            }
                        }
                        //判断两平滑角间差距是否满足限差10′
                        if (oldAverYaw != null && newAverYaw != null && isUsed != null) {
                            if (Math.abs(oldAverYaw.yaw-newAverYaw.yaw)<=(10.0/60.0*Math.PI/180.0)) {
                                double sensorAverYaw = (oldAverYaw.yaw + newAverYaw.yaw)/2.0;
                                if (sensorAverYaw > Math.PI) {
                                    sensorAverYaw -= 2 * Math.PI;
                                }
                                //超过15颗卫星定位才能认为有效
                                if (isUsed.count >= 25) {
                                    double dertx = newLocation.x - oldLocation.x;
                                    double derty = newLocation.y - oldLocation.y;
                                    //投影坐标系与笛卡尔坐标系的差别
                                    double gpsYaw = atan(derty,dertx);
                                    double estiDelication = gpsYaw - sensorAverYaw;
                                    double dertaYear=IgrfUtils.getDertaDay(MyPublic.year,MyPublic.month,MyPublic.day);
                                    if(Math.abs(estiDelication-MyPublic.declination)<
                                            (MyPublic.year-2020.0)*(5.0/60.0)*(Math.PI/180.0)){
                                        estiDeclinations.add(estiDelication);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(MainActivity.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10L, 0.1f, locationListener);
    }
    public static double estiDelenation(){
        double res=0.0;

        if(estiDeclinations.size()>0){
            res=estiDeclinations.get(estiDeclinations.size()-1);
        }else{
            if(MyPublic.declination!=null){
                res=MyPublic.declination;
            }
        }
        res=MyPublic.declination;
        return res;
    }
    private static double atan(double y,double x){
        double res=0;
        if(x==0){
            if(y>0){
                res=Math.PI/2.0;
            }else if(y<0){
                res=-Math.PI/2.0;
            }
        }else{
            if(y==0){
                if(x<0){
                    res=Math.PI;
                }
            }else{
                if(x>0&&y>0){
                    res=Math.atan(y/x);
                }else if(x<0&&y>0){
                    res=Math.PI-Math.abs(Math.atan(y/x));
                }else if(x>0&&y<0){
                    res=-Math.abs(Math.atan(y/x));
                }else if(x<0&&y<0){
                    res=-(Math.PI-Math.abs(Math.atan(y/x)));
                }
            }
        }
        return res;
    }
    static class DateYaw{
        public long time;
        public float yaw;
        public DateYaw(long _time,float _yaw){
            time=_time;
            yaw=_yaw;
        }
    }

    static class DateLocation{
        public long time;
        public double x;
        public double y;
        public DateLocation(long _time,double _x,double _y){
            time=_time;
            x=_x;
            y=_y;
        }
    }
}
