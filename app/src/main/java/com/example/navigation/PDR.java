package com.example.navigation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import androidx.annotation.NonNull;
import androidx.transition.Transition;
import com.adam.gpsstatus.GpsStatusProxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TimeZone;

public class PDR {
    private static int _HMAWinSize=10;
    private static float _model_param_a=0.371f;
    private static float _model_param_b=0.227f;
    private static float _EMA_param_fir=0.7f;
    private static float _EMA_param_sed=0.8f;
    private static float _EMA_param_trd=0.9f;//姿态角平滑
    public static float _model_param_c=1.13f;//需要根据实际情况修正
    public static float _model_param_height=1.75f;//模型身高需要set

    private static float _stepFrequency;
    private static float _stepLength;
    private static LinkedList<DataItem> _processSequence=new LinkedList<DataItem>();

    public static boolean _isStatic=true;
    private static long _lastTimeStamp=0;

    private static boolean new_Acc=false;
    private static boolean new_Magn=false;
    private static float[] acc;
    private static float[] magn;
    private static LinkedList<Float> DertaYaw=new LinkedList<Float>();

    public static LinkedList<Float> avaerYaws=new LinkedList<Float>();

    public static LinkedList<Float> weightMoveAverage(LinkedList<Float> data, int winSize) {
        LinkedList<Float> result = new LinkedList<>();
        for (int i = 0; i != winSize - 1; ++i) {
            result.add(data.get(i));
        }
        for (int i = winSize - 1; i != data.size(); ++i) {
            float val = 0.0f;
            for (int j = i - winSize + 1; j != i + 1; ++j) {
                val += (winSize + j - i) * data.get(j);
            }
            val /= (double) winSize * (winSize + 1) / 2;
            result.add(val);
        }
        return result;
    }

    public static float[] phone2world(float[] vector, float roll, float pitch, float yaw) {

        float x = vector[0];
        float y = vector[1];
        float z = vector[2];

        float x_after_roll = (float) (x * Math.cos(roll) + z * Math.sin(roll));
        float z_after_roll = (float) (x * (-Math.sin(roll)) + z * Math.cos(roll));

        float y_after_pitch = (float) (y * Math.cos(pitch) + z_after_roll * Math.sin(pitch));
        float z_after_pitch = (float) (y * (-Math.sin(pitch)) + z_after_roll * Math.cos(pitch));

        float x_after_yaw = (float) (x_after_roll * Math.cos(yaw) + y_after_pitch * Math.sin(yaw));
        float y_after_yaw = (float) (x_after_roll * (-Math.sin(yaw)) + y_after_pitch * Math.cos(yaw));

        return new float[]{x_after_yaw, y_after_yaw, z_after_pitch};
    }

    public static float[] estimateOrientationBySystem(MyList acc_x,MyList acc_y,MyList acc_z,MyList magn_x,MyList magn_y,MyList magn_z){
        float[] rotationMatrix=new float[9];
        float[] thisAcc=new float[]{
                (float) acc_x.items[acc_x.items.length-1],
                (float) acc_y.items[acc_y.items.length-1],
                (float) acc_z.items[acc_z.items.length-1]
        };
        float[] thisMagne=new float[]{
                (float) magn_x.items[magn_x.items.length-1],
                (float) magn_y.items[magn_y.items.length-1],
                (float) magn_z.items[magn_z.items.length-1]
        };
        SensorManager.getRotationMatrix(rotationMatrix,null,
                thisAcc,thisMagne);

        float[] orientationAngles=new float[3];
        SensorManager.getOrientation(rotationMatrix,orientationAngles);
        return orientationAngles;
    }
    public static float[] estimateOrientationByCustomer(float[] acc,float[] magn) {
        float ax = acc[0];
        float ay = acc[1];
        float az = acc[2];

        float totalAcceleration = (float) Math.sqrt(ax * ax + ay * ay + az * az);
        float thetaRoll = (float) Math.atan2(-ax, az);
        float thetaPitch = (float) Math.asin(-ay / totalAcceleration);

        float[] vec = phone2world(magn, thetaRoll, thetaPitch, 0.0f);
        float thetaYaw = (float) Math.atan2(-vec[0], vec[1]);
        return new float[]{thetaYaw, thetaPitch, thetaRoll};
    }
    public static String getCurentTime(){
        //TimeZone timeZone=TimeZone.getTimeZone("UTM+8:00");
        Calendar calendar=Calendar.getInstance();
        //calendar.setTimeZone(timeZone);
        int year=calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH)+1;
        int day=calendar.get(Calendar.DAY_OF_MONTH);
        int hour=calendar.get(Calendar.HOUR_OF_DAY);
        int min=calendar.get(Calendar.MINUTE);
        int second=calendar.get(Calendar.SECOND);
        int millsecond=calendar.get(Calendar.MILLISECOND);
        double seconds=second+millsecond/1000.0;

        DecimalFormat df=new DecimalFormat("00");
        DecimalFormat df_second=new DecimalFormat("00.000");
        return String.format("%d-%s-%s %s:%s:%s"
                ,year,df.format(month),df.format(day)
                ,df.format(hour),df.format(min),df_second.format(seconds));
    }
    public static Long getMillSecond(){
        long millseconds=Calendar.getInstance().getTimeInMillis();
        return millseconds;
    }
    /*
    *
    * */
    //估计位置，主要函数
    public static GPSPoint estimatePosition(float yaw,DataItem acceleration,GPSPoint oldPoint) {
        _processSequence.add(acceleration);
        GPSPoint newPoint=new GPSPoint();
        if (estimateStepFrequency()) {
            estimateStepLength();
            //磁偏角改正
            double delenation=estimateDeclination.estiDelenation();
            //DecimalFormat ft = new DecimalFormat("0.000");
            /*
            File file=new File("sdcard/documents","333.txt");
            try{
                if(!file.exists()){
                    file.createNewFile();
                }
                if(file.exists()){
                    try{
                        FileOutputStream fileOutputStream=new FileOutputStream(file,true);
                        fileOutputStream.write(String.format("%.5f",delenation*180.0/Math.PI).getBytes());
                        fileOutputStream.close();
                    }catch (Exception e){}
                }
            }catch (Exception e){}*/
            double derty = _stepLength * Math.sin(yaw+delenation);
            double dertx = _stepLength * Math.cos(yaw+delenation);

            OutputXY outputXY=Transer.BL2XY(oldPoint.lat, oldPoint.lon);
            double new_x= outputXY.x+dertx;
            double new_y=outputXY.y+derty;
            newPoint=Transer.XY2BL(new_x,new_y,outputXY.n);
            //positionList.add(new Point2f(getMillSecond(),position.x, position.y));
            //需要调整返回x，y的变化值

            /*
            if (!_isStatic) {
                this.drawPositionChart();
            }

            this._tv_XPosition.setText(ft.format(this._position._x));
            this._tv_YPosition.setText(ft.format(this._position._y));

            this._tv_step_frequency.setText(ft.format(this._stepFrequency));
            this._tv_step_length.setText(ft.format(this._stepLength));*/
        }else{
            newPoint=new GPSPoint(oldPoint.lat, oldPoint.lon);
        }
        return newPoint;
    }
    private static void estimateStepLength() {
        if (_isStatic) {
            // the state is static
            _stepLength = 0.0f;
            _stepFrequency = 0.0f;
        } else {
            _stepLength = (float) ((0.7f + _model_param_a * (_model_param_height - 1.75) + _model_param_b * (_stepFrequency - 1.79) * _model_param_height / 1.75) * _model_param_c);
        }
    }

    private static Boolean estimateStepFrequency() {
        // length limit

        if (_processSequence.size() < 3 * _HMAWinSize) {
            return false;
        }

        // compute the total acceleration
        LinkedList<Float> totalAcceleration = new LinkedList<>();
        for (DataItem item : _processSequence) {
            totalAcceleration.add((float) Math.sqrt(item._values[0] * item._values[0] +
                    item._values[1] * item._values[1] +
                    item._values[2] * item._values[2]));
        }

        // compute the hull moving average
        LinkedList<Float> wma_T_2 = weightMoveAverage(totalAcceleration, _HMAWinSize/2);
        LinkedList<Float> wma_T = weightMoveAverage(totalAcceleration, _HMAWinSize);
        LinkedList<Float> newSeq = new LinkedList<>();
        for (int i = 0; i != totalAcceleration.size(); ++i) {
            newSeq.add(wma_T_2.get(i) * 2 - wma_T.get(i));
        }
        LinkedList<Float> hma = weightMoveAverage(newSeq, (int) Math.sqrt(_HMAWinSize));

        // statistic the average step frequency
        for (int i = hma.size() - 2; i != 0; --i) {
            // if is a peak
            if (hma.get(i - 1) < hma.get(i) && hma.get(i) > hma.get(i + 1)) {
                long curTimeStamp = _processSequence.get(i)._timeStamp;

                if (_lastTimeStamp == 0L) {
                    // first find the peak
                    _lastTimeStamp = curTimeStamp;
                    return false;
                } else if (_lastTimeStamp == curTimeStamp) {
                    // the same peak
                    _processSequence.remove(0);
                    return false;
                } else {
                    // a new peak
                    if (hma.get(i) - 9.8f > 1.2f) {
                        _stepFrequency = 1.0f / ((float) ((curTimeStamp - _lastTimeStamp) / 1000.0));
                        _isStatic = false;
                    } else {
                        // the state is static
                        _isStatic = true;
                    }
                    _lastTimeStamp = curTimeStamp;
                    _processSequence.remove(0);
                    return true;
                }
            }
        }
        return false;
    }
    public static float AverageYaw(float old_Yaw,float new_Yaw){//前后两次均为未平滑的角
        if(Math.abs(old_Yaw-new_Yaw)>5.0){
            if(old_Yaw<0) {
                old_Yaw += 2 * Math.PI;
            }else if(new_Yaw<0){
                new_Yaw+=2*Math.PI;
            }
        }
        float _lastYaw_fir=old_Yaw;
        float _lastYaw_sed=old_Yaw;
        float _lastYaw_trd=old_Yaw;
        float _lastYaw_for=old_Yaw;
        float _lastYaw_fifth=old_Yaw;

        float ema1=0.5f;
        float ema2=0.6f;
        float ema3=0.7f;
        float ema4=0.8f;
        float ema5=0.9f;
        _lastYaw_fir=_lastYaw_fir*ema1+(1-ema1)*new_Yaw;
        _lastYaw_sed=_lastYaw_sed*ema2+(1-ema2)*_lastYaw_fir;
        _lastYaw_trd=_lastYaw_trd*ema3+(1-ema3)*_lastYaw_sed;
        _lastYaw_for=_lastYaw_for*ema4+(1-ema4)*_lastYaw_trd;
        _lastYaw_fifth=_lastYaw_fifth*ema5+(1-ema5)*_lastYaw_for;

        if(_lastYaw_fifth>Math.PI){
            _lastYaw_fifth=(float) (_lastYaw_fifth-2*Math.PI);
        }
        return _lastYaw_fifth;
    }
    public static float AverYaw2(float new_Yaw){
        float old_Yaw=avaerYaws.get(avaerYaws.size()-1);
        if(Math.abs(old_Yaw-new_Yaw)>5.0){
            if(old_Yaw<0){
                old_Yaw=(float) (old_Yaw+2*Math.PI);
            }
            if(new_Yaw<0){
                new_Yaw=(float) (new_Yaw+2*Math.PI);
            }
        }
        float ema1=0.87f,ema2=0.88f,ema3=0.89f,ema4=0.90f,ema5=0.91f;
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
    static class trdAverYaw{
        public double _lastYaw1;
        public double _lastYaw2;
        public double _lastYaw3;
        public double getAver(double new_Yaw,double old_Yaw){
            if(Math.abs(_lastYaw3-new_Yaw)>5.0){
                _lastYaw1=PDR.AverageYaw((float) (old_Yaw),(float) (new_Yaw));
                _lastYaw2=PDR.AverageYaw((float) (old_Yaw),(float) (new_Yaw));
                _lastYaw3=PDR.AverageYaw((float) (old_Yaw),(float) (new_Yaw));
            }else{
                double enma1=0.7;
                double enma2=0.8;
                double enma3=0.9;

                _lastYaw1=_lastYaw1*enma1+(1-enma1)*new_Yaw;
                _lastYaw2=_lastYaw2*enma2+(1-enma2)*_lastYaw1;
                _lastYaw3=_lastYaw3*enma3+(1-enma3)*_lastYaw2;

                if(_lastYaw1>Math.PI){
                    _lastYaw1-=2*Math.PI;
                }
                if(_lastYaw2>Math.PI){
                    _lastYaw2-=2*Math.PI;
                }
                if(_lastYaw3>Math.PI){
                    _lastYaw3-=2*Math.PI;
                }
            }
            return _lastYaw3;
        }
    }
    /*
    public static Boolean Adjust(GPSPoint point1,GPSPoint point2){
        OutputXY xy1= Transer.BL2XY(point1.lat,point1.lon);
        OutputXY xy2= Transer.BL2XY(point2.lat, point2.lon);
        MyGnssStatus.IsUsed isUsed=null;
        long time=getMillSecond();
        for(int i=0;i<MyGnssStatus.isUseds.size();i++){
            if(Math.abs(MyGnssStatus.isUseds.get(i).time-time)<1000){
                isUsed=MyGnssStatus.isUseds.get(i);
            }
        }
        if(isUsed!=null){
            if(isUsed.count>=15){
                if(xy1.n==xy2.n){
                    double s=Math.sqrt((xy1.x-xy2.x)*(xy1.x-xy2.x)+(xy1.y-xy2.y)*(xy1.y-xy2.y));
                    if(s>30.0){
                        return true;
                    }
                }
            }
        }

        return false;
        //if(MyGnssStatus.isUsd)
    }*/
}
