package com.example.navigation;

import android.graphics.Color;
import android.location.GnssStatus;
import android.view.animation.Animation;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DrawLines {
    private static int xLabelMax=24;
    //线图
    public static void drawChart(LineChart lineChart, MyList xList, MyList yList, MyList zList, String xName, String yName, String zName){
        lineChart.setTouchEnabled(false);//可触摸
        lineChart.setBackgroundColor(Color.rgb(255,255,255));//背景为白色
        lineChart.setScaleEnabled(false);//禁止缩放
        List<Entry> iX=new ArrayList<>();
        List<Entry> iY=new ArrayList<>();
        List<Entry> iZ=new ArrayList<>();
        Float maxAbs=0.0f;
        for(int i=0;i<xList.items.length;i++){
            iX.add(new Entry(i,(float) xList.items[i]));
            iY.add(new Entry(i,(float) yList.items[i]));
            iZ.add(new Entry(i,(float) zList.items[i]));

            if(Math.abs((float) xList.items[i])>maxAbs){
                maxAbs=Math.abs((float) xList.items[i]);
            }
            if(Math.abs((float) yList.items[i])>maxAbs){
                maxAbs=Math.abs((float) yList.items[i]);
            }
            if(Math.abs((float) zList.items[i])>maxAbs){
                maxAbs=Math.abs((float) zList.items[i]);
            }
        }
        maxAbs*=1.2f;

        LineDataSet dataSetX=new LineDataSet(iX,xName);
        dataSetX.setLineWidth(1);//设置线宽
        dataSetX.setColor(Color.parseColor("#A62121"));//设置线的颜色
        dataSetX.setCircleColor(Color.parseColor("#A62121"));//设置点的颜色
        dataSetX.setDrawValues(false);//不显示数据

        LineDataSet dataSetY=new LineDataSet(iY,yName);
        dataSetY.setLineWidth(1);
        dataSetY.setColor(Color.parseColor("#8BC34A"));
        dataSetY.setCircleColor(Color.parseColor("#8BC34A"));
        dataSetY.setDrawValues(false);

        LineDataSet dataSetZ=new LineDataSet(iZ,zName);
        dataSetZ.setLineWidth(1);
        dataSetZ.setColor(Color.parseColor("#FF9800"));
        dataSetZ.setCircleColor(Color.parseColor("#FF9800"));
        dataSetZ.setDrawValues(false);
        //设置x轴样式
        XAxis xAxis=lineChart.getXAxis();
        xAxis.setAxisMinimum(0.0f);
        xAxis.setAxisMaximum((float)xLabelMax);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(false);
        xAxis.enableAxisLineDashedLine(5,4,0);//横轴为虚线
        //设置y轴样式
        YAxis rightyAxis=lineChart.getAxisRight();
        rightyAxis.setAxisMinimum(-maxAbs);
        rightyAxis.setAxisMaximum(maxAbs);
        rightyAxis.enableGridDashedLine(5,4,0);
        rightyAxis.setDrawAxisLine(false);
        rightyAxis.setLabelCount(7);

        YAxis leftyAxis=lineChart.getAxisLeft();
        leftyAxis.setAxisMinimum(-maxAbs);
        leftyAxis.setAxisMaximum(maxAbs);
        leftyAxis.enableGridDashedLine(5,4,0);
        leftyAxis.setDrawAxisLine(false);
        leftyAxis.setLabelCount(7);
        //设置动画
        lineChart.animateY(1);
        //设置标题样式
        lineChart.getDescription().setTextSize(12);
        lineChart.getDescription().setTextColor(Color.parseColor("#DC4949"));
        //设置图表样式
        lineChart.setDrawMarkers(true);

        LineData lineData=new LineData();

        lineData.addDataSet(dataSetX);
        lineData.addDataSet(dataSetY);
        lineData.addDataSet(dataSetZ);
        //添加数据
        lineChart.setData(lineData);
    }
    //卫星云图
    public static void drawCombined(CombinedChart chart, LinkedList<MyGnssStatus.Satellite> satellites){
        chart.clear();
        //chart.animateY(100);
        //chart.animateX(100);
        chart.setTouchEnabled(false);
        chart.setBackgroundColor(Color.rgb(255,255,255));
        float maxValues=90.0f;//半径最大值
        XAxis xAxis=chart.getXAxis();
        xAxis.setAxisMinimum(-maxValues*1.2f);
        xAxis.setAxisMaximum(maxValues*1.2f);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(false);

        YAxis left=chart.getAxisLeft();
        left.setAxisMinimum(-maxValues*1.2f);
        left.setAxisMaximum(maxValues*1.2f);
        left.setDrawAxisLine(false);//禁用边线
        left.setDrawGridLines(false);//禁用网格线
        left.setDrawLabels(false);//不显示数据

        YAxis right=chart.getAxisRight();
        right.setAxisMinimum(-maxValues*1.2f);
        right.setAxisMaximum(maxValues*1.2f);
        right.setDrawGridLines(false);//禁用网格线
        right.setDrawAxisLine(false);//禁用边线
        right.setDrawLabels(false);//不显示数据

        chart.getDescription().setText("可用卫星云图");
        chart.getDescription().setTextSize(25);
        Legend legend=chart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setTextSize(12);

        //绘制极坐标轴
        LineData lineData=new LineData();
        lineData.setDrawValues(false);//不显示数据
        int LineWidth=1;
        //绘制直径
        List<Entry> lines0=new ArrayList<>();
        for(float i=-95;i<=95;i+=9.5){
            lines0.add(new Entry(i,0.0f));
        }
        LineDataSet set0=new LineDataSet(lines0,null);
        set0.setColor(Color.rgb(0,0,0));
        set0.setLineWidth(LineWidth);
        set0.setDrawCircles(false);

        List<Entry> lines45=new ArrayList<>();
        for(float i=(float)(-95/Math.sqrt(2));i<=(float) (95/Math.sqrt(2));i+=(float) (9.5/Math.sqrt(2))){
            lines45.add(new Entry(i,i));
        }
        LineDataSet set45=new LineDataSet(lines45,null);
        set45.setColor(Color.rgb(0,0,0));
        set45.setLineWidth(LineWidth);
        set45.setDrawCircles(false);

        List<Entry> lines135=new ArrayList<>();
        for(float i=(float)(-95/Math.sqrt(2));i<=(float) (95/Math.sqrt(2));i+=(float) (9.5/Math.sqrt(2))){
            lines135.add(new Entry(i,-i));
        }
        LineDataSet set135=new LineDataSet(lines135,null);
        set135.setLineWidth(LineWidth);
        set135.setColor(Color.rgb(0,0,0));
        set135.setDrawCircles(false);

        List<Entry> lines90=new ArrayList<>();
        for(float i=-95;i<=95;i+=9.5){
            lines90.add(new Entry(0.0f,i));
        }
        LineDataSet set90=new LineDataSet(lines90,null);
        set90.setColor(Color.rgb(0,0,0));
        set90.setLineWidth(LineWidth);
        set90.setDrawCircles(false);

        lineData.addDataSet(set0);
        lineData.addDataSet(set45);
        lineData.addDataSet(set135);
        lineData.addDataSet(set90);

        //绘制圆弧
        List<Entry> R1_30=new ArrayList<>();
        for(double ange=Math.PI;ange>=0;ange-=Math.PI/180.0){
            float x=(float) (30*Math.cos(ange));
            float y=(float) (30*Math.sin(ange));
            R1_30.add(new Entry(x,y));
        }
        LineDataSet setR1_30=new LineDataSet(R1_30,null);
        setR1_30.setColor(Color.rgb(0,0,0));
        setR1_30.setLineWidth(LineWidth);
        setR1_30.setDrawCircles(false);

        List<Entry> R2_30=new ArrayList<>();
        for(double ange=Math.PI;ange>=0;ange-=Math.PI/180.0){
            float x=(float) (30*Math.cos(ange));
            float y=(float) (-30*Math.sin(ange));
            R2_30.add(new Entry(x,y));
        }
        LineDataSet setR2_30=new LineDataSet(R2_30,null);
        setR2_30.setColor(Color.rgb(0,0,0));
        setR2_30.setLineWidth(LineWidth);
        setR2_30.setDrawCircles(false);

        List<Entry> R1_60=new ArrayList<>();
        for(double ange=Math.PI;ange>=0;ange-=Math.PI/180.0){
            float x=(float) (60*Math.cos(ange));
            float y=(float) (60*Math.sin(ange));
            R1_60.add(new Entry(x,y));
        }
        LineDataSet setR1_60=new LineDataSet(R1_60,null);
        setR1_60.setColor(Color.rgb(0,0,0));
        setR1_60.setDrawCircles(false);
        setR1_60.setLineWidth(LineWidth);

        List<Entry> R2_60=new ArrayList<>();
        for(double ange=Math.PI;ange>=0;ange-=Math.PI/180.0){
            float x=(float) (60*Math.cos(ange));
            float y=(float) (-60*Math.sin(ange));
            R2_60.add(new Entry(x,y));
        }
        LineDataSet setR2_60=new LineDataSet(R2_60,null);
        setR2_60.setLineWidth(LineWidth);
        setR2_60.setColor(Color.rgb(0,0,0));
        setR2_60.setDrawCircles(false);

        List<Entry> R1_90=new ArrayList<>();
        for(double ange=Math.PI;ange>=0;ange-=Math.PI/180.0){
            float x=(float) (90*Math.cos(ange));
            float y=(float) (90*Math.sin(ange));
            R1_90.add(new Entry(x,y));
        }
        LineDataSet setR1_90=new LineDataSet(R1_90,null);
        setR1_90.setColor(Color.rgb(0,0,0));
        setR1_90.setLineWidth(LineWidth);
        setR1_90.setDrawCircles(false);

        List<Entry> R2_90=new ArrayList<>();
        for(double ange=Math.PI;ange>=0;ange-=Math.PI/180.0){
            float x=(float) (90*Math.cos(ange));
            float y=(float) (-90*Math.sin(ange));
            R2_90.add(new Entry(x,y));
        }
        LineDataSet setR2_90=new LineDataSet(R2_90,null);
        setR2_90.setColor(Color.rgb(0,0,0));
        setR2_90.setLineWidth(LineWidth);
        setR2_90.setDrawCircles(false);

        lineData.addDataSet(setR1_30);
        lineData.addDataSet(setR2_30);
        lineData.addDataSet(setR1_60);
        lineData.addDataSet(setR2_60);
        lineData.addDataSet(setR1_90);
        lineData.addDataSet(setR2_90);
        //绘制卫星
        ScatterData scatter=new ScatterData();//绘制卫星点
        scatter.setDrawValues(false);
        int markSize=30;
        //加载数据
        List<Entry> BeiDou=new ArrayList<>();
        List<Entry> Galileo=new ArrayList<>();
        List<Entry> Glonass=new ArrayList<>();
        List<Entry> GPS=new ArrayList<>();
        List<Entry> IRnss=new ArrayList<>();
        List<Entry> Qzss=new ArrayList<>();
        List<Entry> SBAS=new ArrayList<>();
        List<Entry> Unknown=new ArrayList<>();
        for(int i=0;i<satellites.size();i++){
            float x=(float) (Math.abs(satellites.get(i).elevation)
                    *Math.sin(satellites.get(i).azimuthDegree*Math.PI/180.0));
            float y=(float) (Math.abs(satellites.get(i).elevation)
                    *Math.cos(satellites.get(i).azimuthDegree*Math.PI/180.0));
            Entry entry=new Entry(x,y);
            switch (satellites.get(i).type){
                case GnssStatus.CONSTELLATION_BEIDOU:
                    BeiDou.add(entry);
                    break;
                case GnssStatus.CONSTELLATION_GALILEO:
                    Galileo.add(entry);
                    break;
                case GnssStatus.CONSTELLATION_GLONASS:
                    Glonass.add(entry);
                    break;
                case GnssStatus.CONSTELLATION_GPS:
                    GPS.add(entry);
                    break;
                case GnssStatus.CONSTELLATION_IRNSS:
                    IRnss.add(entry);
                    break;
                case GnssStatus.CONSTELLATION_QZSS:
                    Qzss.add(entry);
                    break;
                case GnssStatus.CONSTELLATION_SBAS:
                    SBAS.add(entry);
                    break;
                case GnssStatus.CONSTELLATION_UNKNOWN:
                    Unknown.add(entry);
                    break;
            }
        }
        //设置样式
        ScatterDataSet setBeidou=new ScatterDataSet(BeiDou,"BEIDOU");
        setBeidou.setColor(Color.rgb(255,0,0));
        setBeidou.setScatterShapeSize(markSize);
        setBeidou.setScatterShape(ScatterChart.ScatterShape.CIRCLE);

        ScatterDataSet setGalileo=new ScatterDataSet(Galileo,"GALILEO");
        setGalileo.setColor(Color.rgb(255,97,0));
        setGalileo.setScatterShapeSize(markSize);
        setGalileo.setScatterShape(ScatterChart.ScatterShape.TRIANGLE);//三角

        ScatterDataSet setGlonass=new ScatterDataSet(Glonass,"GLONASS");
        setGlonass.setColor(Color.rgb(127,213,23));
        setGlonass.setScatterShapeSize(markSize);
        setGlonass.setScatterShape(ScatterChart.ScatterShape.CROSS);

        ScatterDataSet setGPS=new ScatterDataSet(GPS,"GPS");
        setGPS.setColor(Color.rgb(213,221,121));
        setGPS.setScatterShapeSize(markSize);
        setGPS.setScatterShape(ScatterChart.ScatterShape.CHEVRON_DOWN);

        ScatterDataSet setIRNSS=new ScatterDataSet(IRnss,"IRNSS");
        setIRNSS.setColor(Color.rgb(129,218,189));
        setIRNSS.setScatterShapeSize(markSize);
        setIRNSS.setScatterShape(ScatterChart.ScatterShape.CHEVRON_UP);

        ScatterDataSet setQZSS=new ScatterDataSet(Qzss,"QZSS");
        setQZSS.setColor(Color.rgb(97,214,128));
        setQZSS.setScatterShapeSize(markSize);
        setQZSS.setScatterShape(ScatterChart.ScatterShape.SQUARE);

        ScatterDataSet setSBAS=new ScatterDataSet(SBAS,"SBAS");
        setSBAS.setColor(Color.rgb(201,12,178));
        setSBAS.setScatterShapeSize(markSize);
        setSBAS.setScatterShape(ScatterChart.ScatterShape.X);

        ScatterDataSet setUnKnow=new ScatterDataSet(Unknown,"UNKNOWN");
        setUnKnow.setColor(Color.rgb(178,143,214));
        setUnKnow.setScatterShapeSize(markSize);
        setUnKnow.setScatterShape(ScatterChart.ScatterShape.CROSS);
        //添加数据
        scatter.addDataSet(setBeidou);
        scatter.addDataSet(setGalileo);
        scatter.addDataSet(setGlonass);
        scatter.addDataSet(setGPS);
        scatter.addDataSet(setIRNSS);
        scatter.addDataSet(setQZSS);
        scatter.addDataSet(setSBAS);
        scatter.addDataSet(setUnKnow);
        //组合画图
        CombinedData combinedData=new CombinedData();
        combinedData.setData(lineData);
        combinedData.setData(scatter);

        chart.setData(combinedData);
        chart.getDescription().setTextSize(12);
        chart.getDescription().setTextColor(Color.rgb(224,121,0));
    }
}
