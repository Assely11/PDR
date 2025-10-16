package com.example.navigation;

import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

/*
* IGRF模型求磁偏角
* */
public class IgrfUtils {
    static class G{
        public int n;
        public int m;
        public double data;
        public G(int _n,int _m,double _data){
            n=_n;
            m=_m;
            data=_data;
        }
        public String toString(){
            return String.format("(%d,%d)%.4f",n,m,data);
        }
    }
    static class H{
        public int n;
        public int m;
        public double data;
        public H(int _n,int _m,double _data){
            n=_n;
            m=_m;
            data=_data;
        }
        public String toString(){
            return String.format("(%d,%d)%.4f", n,m,data);
        }
    }
    private  static LinkedList<G[]> Gs=new LinkedList<G[]>();
    private  static LinkedList<H[]> Hs=new LinkedList<H[]>();
    private  static LinkedList<Double[]> Pnms=new LinkedList<Double[]>();
    private  static LinkedList<Double[]> dPnms=new LinkedList<Double[]>();
    public  static double speed=-(4.9671/60.0)*(Math.PI/180.0);

    //读取模型参数
    public static void readGH(InputStream inputG,InputStream inputH){

        try{
            BufferedReader bufferG=new BufferedReader(new InputStreamReader(inputG));
            BufferedReader bufferH=new BufferedReader(new InputStreamReader(inputH));

            Gs=new LinkedList<G[]>();
            Hs=new LinkedList<H[]>();
            try{
                String line;
                for(int i=1;i<=13;i++){
                    G[] Gtemp=new G[i+1];
                    for(int j=0;j<=i;j++){
                        line=bufferG.readLine();
                        String[] temp=line.split(",");

                        Integer n=Integer.parseInt(temp[0]);
                        Integer m=Integer.parseInt(temp[1]);
                        Double data=Double.parseDouble(temp[2]);
                        Gtemp[j]=new G(n,m,data);
                    }
                    Gs.add(Gtemp);
                }
                for(int i=1;i<=13;i++){
                    H[] Htemp=new H[i];
                    for(int j=1;j<=i;j++){
                        line=bufferH.readLine();
                        String[] temp=line.split(",");

                        Integer n=Integer.parseInt(temp[0]);
                        Integer m=Integer.parseInt(temp[1]);
                        Double data=Double.parseDouble(temp[2]);
                        Htemp[j-1]=new H(n,m,data);
                    }
                    Hs.add(Htemp);
                }
            }catch(Exception e){}
            bufferH.close();
            bufferG.close();
        }catch(Exception e){
            Toast.makeText(MainActivity.context,e.toString(),Toast.LENGTH_SHORT).show();
        }
    }
    private static double deg2rad(double d){
        return d*(Math.PI/180.0);
    }
    private static double rad2deg(double r){
        return r*(180.0/Math.PI);
    }
    //计算施密特正交勒让德
    private static void getPnm(double theta,int N){
        theta=deg2rad(theta);
        Pnms=new LinkedList<Double[]>();
        Double[] temp=new Double[]{1.0};
        Pnms.add(temp);

        double u=Math.sin(theta);double t=Math.cos(theta);
        temp=new Double[]{t,u};
        Pnms.add(temp);

        for(int n=2;n<=N;n++){
            temp=new Double[n+1];
            for(int m=0;m<n-1;m++){
                if(m==0){
                    temp[m]=((2.0*n-1.0)/(n*1.0))*t*Pnms.get(n-1)[0]-((n*1.0-1.0)/(n*1.0))*Pnms.get(n-2)[0];
                }else{
                    double a=(2.0*n-1.0)/Math.sqrt(n*n-m*m);
                    double up=(n+m-1)*(n-m-1)*1.0;
                    double down=(n*n-m*m)*1.0;
                    double b=Math.sqrt(up/down);

                    temp[m]=a*t*Pnms.get(n-1)[m]-b*Pnms.get(n-2)[m];
                }
            }

            temp[n-1]=t*Math.sqrt(2.0*(n-1.0)+1.0)*Pnms.get(n-1)[n-1];

            temp[n]=u*Math.sqrt((2.0*n-1)/(2.0*n))*Pnms.get(n-1)[n-1];

            Pnms.add(temp);
        }
    }
    //计算施密特正交勒让德导数
    private static void getDPnms(double theta,int N){
        getPnm(theta,N);

        dPnms=new LinkedList<Double[]>();
        theta=deg2rad(theta);

        Double[] temp=new Double[]{0.0};
        dPnms.add(temp);

        double u=Math.sin(theta);double t=Math.cos(theta);

        temp=new Double[]{-u,t};
        dPnms.add(temp);

        for(int n=2;n<=N;n++){
            temp=new Double[n+1];
            for(int m=0;m<n-1;m++){
                if(m==0){
                    temp[m]=((2.0*n-1)/(n*1.0))*t*dPnms.get(n-1)[0]-u*((2.0*n-1)/(n*1.0))*Pnms.get(n-1)[0]-((n*1.0-1)/(n*1.0))*dPnms.get(n-2)[0];
                }else{
                    double a=(2.0*n-1.0)/Math.sqrt(n*n-m*m);
                    double up=(n+m-1)*(n-m-1)*1.0;
                    double down=(n*n-m*m)*1.0;
                    double b=Math.sqrt(up/down);

                    temp[m]=a*t*dPnms.get(n-1)[m]-b*dPnms.get(n-2)[m]-a*u*Pnms.get(n-1)[m];
                }
            }

            temp[n-1]=t*Math.sqrt(2.0*(n-1)+1.0)*dPnms.get(n-1)[n-1]-u*Math.sqrt(2.0*(n-1)+1.0)*Pnms.get(n-1)[n-1];

            temp[n]=u*Math.sqrt((2.0*n-1)/(2.0*n))*dPnms.get(n-1)[n-1]+t*Math.sqrt((2.0*n-1)/(2.0*n))*Pnms.get(n-1)[n-1];

            dPnms.add(temp);
        }
    }
    //经纬度转地固直角坐标系
    private  static Double[] BL2XYZ(double B,double L){
        double H=0;
        double _b=deg2rad(B);//转弧度
        double _l=deg2rad(L);//转弧度

        double a=6378137;
        double f=1.0/298.2572236;
        double b=a-a*f;

        double e2=(a*a-b*b)/(a*a);//第一偏心率
        double _e2=(a*a-b*b)/(b*b);//第二偏心率

        double N=a/Math.sqrt(1-e2*Math.sin(_b)*Math.sin(_b));

        double x=N*Math.cos(_b);
        double y=-b*b*x/(a*a*Math.tan(_b+Math.PI/2.0));

        double X=N*Math.cos(_b)*Math.cos(_l);
        double Y=N*Math.cos(_b)*Math.sin(_l);
        double Z=N*(1-e2)*Math.sin(_b);

        return new Double[]{X,Y,Z};
    }
    //地固直角坐标系转rtl坐标系
    private  static Double[] XYZ2rtl(double X,double Y,double Z){
        double r=Math.sqrt(X*X+Y*Y+Z*Z);
        double theta=Math.atan2(Math.sqrt(X*X+Y*Y), Z);
        double l=Math.atan2(Y, X);

        return new Double[]{r,theta,l};
    }

    private static Double[] BL2rtl(double B,double L){
        Double[] XYZ=BL2XYZ(B, L);
        Double[] rtl=XYZ2rtl(XYZ[0], XYZ[1], XYZ[2]);
        return rtl;
    }
    //求当前位置磁偏角
    private  static double getD(double B,double L){
        Double[] rtl=BL2rtl(B, L);
        double r=rtl[0];
        double theta=rtl[1];//弧度
        double lamda=rtl[2];//弧度

        double a=6378137;
        getDPnms(rad2deg(theta),13);

        double x=0;
        for(int i=1;i<=13;i++){
            double sum=0;
            for(int j=0;j<=i;j++){
                if(j>0){
                    sum+=(Gs.get(i-1)[j].data*Math.cos(j*lamda)+Hs.get(i-1)[j-1].data*Math.sin(j*lamda))*dPnms.get(i)[j];
                }else{
                    sum+=(Gs.get(i-1)[j].data*Math.cos(j*lamda))*dPnms.get(i)[j];
                }
            }
            x+=sum*Math.pow(a/r,i+2);
        }

        double y=0;
        for(int i=1;i<=13;i++){
            double sum=0;
            for(int j=0;j<=i;j++){
                if(j>0){
                    double temp=Gs.get(i-1)[j].data*Math.sin(j*lamda)-Hs.get(i-1)[j-1].data*Math.cos(j*lamda);
                    sum+=(j/Math.sin(theta))*temp*Pnms.get(i)[j];
                }else{
                    double temp=Gs.get(i-1)[j].data*Math.sin(j*lamda);
                    sum+=(j/Math.sin(theta))*temp*Pnms.get(i)[j];
                }
            }
            y+=sum*Math.pow(a/r,i+2);
        }
        return Math.atan2(y,x);
    }
    public static double getDertaDay(int year,int month,int day){
        Boolean leapyear=false;
        if(((year%4==0)&&(year%100!=0))||((year%100==0)&&(year%400==0))){
            leapyear=true;
        }
        int[] days=new int[]{0,31,59,90,120,151,181,212,243,273,304,334};

        double totalDays=day+days[month-1];
        if(leapyear&&month>2){
            totalDays+=1.0;
        }
        double decimalYear=year+totalDays/365.0;
        if(leapyear){
            decimalYear=year+totalDays/366.0;
        }
        double dertData=decimalYear-2020.0-1.0/366.0;
        return dertData;
    }
    //对当前位置磁偏角进行近似时间改正
    public static double getNowDeclination(double B,double L,int year,int month,int day){
        double decl2020=getD(B,L);
        double dertYear=year-2020.0;
        return (decl2020+speed*dertYear);
    }//返回单位为rad
}

