package com.example.navigation;

public class Transer {
    //WGS-84坐标系：全球定位系统使用，GPS、北斗等
    //GCJ-02坐标系：中国地区使用，由WGS-84偏移而来
    //BD-09坐标系：百度专用，由GCJ-02偏移而来
    private static double pi=Math.PI;
    private static double a = 6378245.0;
    private static double ee = 0.00669342162296594323;//第一偏心率
    private static double ee2=0.006738525414683;//第二偏心率
    private static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
    private static double f=1.0/298.3;

    private static boolean inChina(double lat,double lon){
        boolean res=false;
        if((lon>=72.004&&lon<=137.8347)&&(lat>=0.8293&&lat<=55.8271)){
            res=true;
        }
        return res;
    }//判断坐标点是否在国内
    private static double TransformLat(double x,double y){
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }
    private static double TransformLon(double x, double y)
    {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }

    private static GPSPoint WGS2GCJ02(double wgLat,double wgLon){
        GPSPoint point=new GPSPoint();
        if(inChina(wgLat,wgLon)){
            double dLat = TransformLat(wgLon - 105.0, wgLat - 35.0);
            double dLon = TransformLon(wgLon - 105.0, wgLat - 35.0);
            double radLat = wgLat / 180.0 * pi;
            double magic = Math.sin(radLat);
            magic = 1 - ee * magic * magic;
            double sqrtMagic = Math.sqrt(magic);
            dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
            dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
            double lat = wgLat + dLat;
            double lon = wgLon + dLon;
            point.lat=lat;
            point.lon=lon;
        }
        return point;
    }
    private static GPSPoint GCJ02ToBD09(double gg_lat, double gg_lon)
    {
        GPSPoint point = new GPSPoint();
        double x = gg_lon, y = gg_lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double bd_lon = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        point.lat = bd_lat;
        point.lon = bd_lon;
        return point;
    }

    public static GPSPoint WGS2BD09(double lat,double lon){
        GPSPoint point=new GPSPoint();
        point=WGS2GCJ02(lat,lon);
        point=GCJ02ToBD09(point.lat, point.lon);
        if(!inChina(lat,lon)){
            point=new GPSPoint(lat,lon);
        }//不在国内，按GPS输出
        return point;
    }
    //1.5°带高斯投影正反算
    public static OutputXY BL2XY(double B,double L){
        double _B=B*pi/180.0;
        double _L=L*pi/180.0;
        double n=Math.floor((L+0.75)/1.5);
        double L0=1.5*n;
        double l=(L-L0)*3600.0;//经差，单位是秒
        double pro=3600.0*180.0/pi;
        double _pro=1.0/pro;//求p的倒数
        double b=a-a*f;
        double e=(a*a-b*b)/(a*a);//计算第一偏心率
        double m_e=(a*a-b*b)/(b*b);//计算第二偏心率
        double c=a*a/b;
        double V=Math.sqrt(1+m_e*Math.cos(_B)*Math.cos(_B));
        double N=c/V;//计算卯酉圈半径
        double ita2=m_e*Math.cos(_B)*Math.cos(_B);
        double t=Math.tan(_B);
        double m0=a*(1-e);
        double m2=3.0*e*m0/2.0;
        double m4=5.0*e*m2/4.0;
        double m6=7.0*e*m4/6.0;
        double m8=9.0*e*m6/8.0;

        double n0=a;
        double n2=1.0*e*n0/2.0;
        double n4=3.0*e*n2/4.0;
        double n6=5.0*e*n4/6.0;
        double n8=7.0*e*n6/8.0;

        double a0=m0+m2/2.0+3*m4/8.0+5*m6/16.0+35*m8/128.0;
        double a2=m2/2.0+m4/2.0+15*m6/32.0+7*m8/16.0;
        double a4=m4/8.0+3*m6/16.0+7*m8/32.0;
        double a6=m6/32.0+m8/16.0;
        double a8=m8/128.0;

        double X=a0*_B-a2/2.0*Math.sin(2*_B)+a4/4.0*Math.sin(4*_B)-a6/6.0*Math.sin(6*_B)+a8/8.0*Math.sin(8*_B);
        double x=X+N*Math.sin(_B)*Math.cos(_B)/2.0*Math.pow(l*_pro,2)+
                N*Math.sin(_B)*Math.pow(Math.cos(_B),3)*(5-t*t+9*ita2+4*ita2*ita2)*Math.pow(l*_pro,4)/24.0
                +N*Math.sin(_B)*Math.pow(Math.cos(_B),5)*(61-58*t*t+Math.pow(t,4))*Math.pow(l*_pro,6)/720.0;
        double y=N*Math.cos(_B)*l*_pro+N*Math.pow(Math.cos(_B),3)*(1-t*t+ita2)*Math.pow(l*_pro,3)/6.0
                +N*Math.pow(Math.cos(_B),5)*(5-18*t*t+Math.pow(t,4)+14*ita2-58*ita2*t*t)*Math.pow(l*_pro,5)/120.0;

        OutputXY xy=new OutputXY();
        xy.x=x;xy.y=y;xy.n=n;
        return xy;
    }
    private static double CalBf(double x){
        double pro=3600.0*180.0/pi;
        double b=a-a*f;
        double e=(a*a-b*b)/(a*a);//计算第一偏心率
        double m_e=(a*a-b*b)/(b*b);//计算第二偏心率
        double m0=a*(1-e);
        double m2=3.0*e*m0/2.0;
        double m4=5.0*e*m2/4.0;
        double m6=7.0*e*m4/6.0;
        double m8=9.0*e*m6/8.0;

        double a0=m0+m2/2.0+3*m4/8.0+5*m6/16.0+35*m8/128.0;
        double a2=m2/2.0+m4/2.0+15*m6/32.0+7*m8/16.0;
        double a4=m4/8.0+3*m6/16.0+7*m8/32.0;
        double a6=m6/32.0+m8/16.0;
        double a8=m8/128.0;

        double B0=x/a0;
        double B1=1;
        for(int i=0;i<100;i++){
            double F=-a2*Math.sin(2*B0)/2.0+a4*Math.sin(4*B0)/4.0-a6*Math.sin(6*B0)/6.0;
            B1=(x-F)/a0;
            if(Math.abs(B1-B0)<0.001/pro){
                break;
            }else{
                B0=B1;
            }
        }
        return B0;
    }
    public static GPSPoint XY2BL(double x,double y,double n){
        double b=a-a*f;
        double e=(a*a-b*b)/(a*a);//计算第一偏心率
        double m_e=(a*a-b*b)/(b*b);//计算第二偏心率

        double Bf=CalBf(x);
        double tf=Math.tan(Bf);
        double Mf=a*(1-e)/Math.pow(Math.sqrt(1-e*Math.sin(Bf)*Math.sin(Bf)),3);
        double Nf=a/Math.sqrt(1-e*Math.sin(Bf)*Math.sin(Bf));
        double ita2f=m_e*Math.cos(Bf)*Math.cos(Bf);

        double B=Bf-tf*y*y/(2*Mf*Nf)+tf*(5+3*tf*tf+ita2f-9*ita2f*tf*tf)
                *Math.pow(y,4)/(24*Mf*Math.pow(Nf, 3))-tf*(61+90*tf*tf+45*Math.pow(tf,4))
                *Math.pow(y,6)/(720*Mf*Math.pow(Nf,5));
        double l=y/(Nf*Math.cos(Bf))-(1+2*tf*tf+ita2f)*Math.pow(y,3)/(6*Math.pow(Nf,3)
                *Math.cos(Bf))+(5+28*tf*tf+24*Math.pow(tf,4))*Math.pow(y,5)/
                (120*Math.pow(Nf,5)*Math.cos(Bf));
        double L=l*180.0/pi+1.5*n;
        B=B*180.0/pi;
        GPSPoint point=new GPSPoint();
        point.lat=B;
        point.lon=L;
        return point;
    }
    //BaiduToWGS84
    private static double transformLon(double x,double y){
        double PI = 3.14159265358979324;
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }
    private static double transformLat(double x,double y){
        double PI = 3.14159265358979324;
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }
    public static GPSPoint baiduToWgs84(double bdLat,double bdLon){
        double PI = 3.14159265358979324;
        //x_pi = 3.14159265358979324 * 3000.0 / 180.0;
        double x = bdLon - 0.0065;
        double y = bdLat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double gcjLon = z * Math.cos(theta);
        double gcjLat = z * Math.sin(theta);
        a = 6378245.0;
        ee = 0.00669342162296594323;
        double dLat = transformLat(gcjLon - 105.0, gcjLat - 35.0);
        double dLon = transformLon(gcjLon - 105.0, gcjLat - 35.0);
        double radLat = gcjLat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * PI);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);
        dLat = gcjLat - dLat;
        dLon = gcjLon - dLon;
        GPSPoint res=new GPSPoint(dLat,dLon);
        return res;
    }
}

