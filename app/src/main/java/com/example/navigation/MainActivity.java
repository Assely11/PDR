package com.example.navigation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.*;
import android.os.Build;
import android.os.PowerManager;
import android.text.InputFilter;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.adam.gpsstatus.GpsStatusProxy;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.tabs.TabLayout;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private BaiduMapOptions options;
    private MapView mapView;
    private BaiduMap baiduMap;
    private List<Overlay> baiduPoint = null;
    private List<Overlay> baiduLines = null;
    private List<LatLng> linesPoints = null;
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private SensorEventListener listener;
    private LocationListener locationListener;

    private FrameLayout frame1, frame2, frame3, frame4;
    private CheckBox drawLinesButton;
    private RadioButton radioGPS, radioNet, radiolib, radioGPSInternet, radioGPSMy, radioInternetMy;
    private LinearLayout scrollView;
    private TextView acc_x, acc_y, acc_z;//加速度
    private TextView gyr_x, gyr_y, gyr_z;//陀螺仪
    private TextView magn_x, magn_y, magn_z;//地磁
    private TextView orien_x, orien_y, orien_z;//姿态角
    private TextView orien_x1, orien_y1, orien_z1;

    private LineChart acc_chart;//加速度线图
    private LineChart gyr_chart;//陀螺仪线图
    private LineChart magn_chart;//地磁线图
    private LineChart orien_chart;//姿态角线图
    private LineChart orien_chart1;

    private TextView acc_time, gyr_time, magn_time, orien_time, orien_time1;

    private int xLabelMax = 24;

    //加速度
    private MyList acc_xList = new MyList();
    private MyList acc_yList = new MyList();
    private MyList acc_zList = new MyList();
    //陀螺仪
    private MyList gyr_xList = new MyList();
    private MyList gyr_yList = new MyList();
    private MyList gyr_zList = new MyList();
    //磁场强度
    private MyList magn_xList = new MyList();
    private MyList magn_yList = new MyList();
    private MyList magn_zList = new MyList();
    //姿态角
    private MyList orgin_xList = new MyList();
    private MyList orgin_yList = new MyList();
    private MyList orgin_zList = new MyList();

    private MyList orien_xList1 = new MyList();
    private MyList orien_yList1 = new MyList();
    private MyList orien_zList1 = new MyList();

    private TextView startButton, stopButton, cancelButton, saveButton;

    private Drawable myButton1, myButton2;

    private boolean check_acc = false;
    private boolean check_magn = false;

    private LinkedList<DataItem> total_acc, total_gyr, total_magn, total_orien, total_orien1;

    private SensorEventListener PDR_listener;
    private SensorManager PDR_sensorManager;

    private MyList PDR_acc_x, PDR_acc_y, PDR_acc_z;
    private MyList PDR_magn_x, PDR_magn_y, PDR_magn_z;

    private LinkedList<Float> yaw;
    private LinkedList<GPSPoint> PDR_locations;//直接缓冲到该地址,数据必须为WGS84坐标系统


    private boolean new_PDR_acc = false;
    private boolean new_PDR_magn = false;

    private DataItem acceleration;

    static private String savepath;
    static private float[] custom_acc = new float[3];
    static private float[] custom_magn = new float[3];
    public static Context context;

    private CombinedChart satelliteChart;
    private TextView time_Chart;

    //private boolean isChangedLocation = false;
    private LinkedList<KF.YawItem> yawItems = new LinkedList<>();
    private KF kf;

    public static EditText textHeigt;
    private EditText textC;

    private static Float setHeght = PDR._model_param_height;

    private static Long lastTime = null;

    private PDR.trdAverYaw trdAverYaw = new PDR.trdAverYaw();

    private float yawLogo=0.0f;

    private TextView postShow;

    private Long PostRecordTime=PDR.getMillSecond();//姿态暂留时间记录

    private SensorManager postManager;//姿态检测管理服务
    private SensorEventListener postListener;//
    private boolean new_Post_acc=false;
    private boolean new_Post_magn=false;
    private float[] post_Acc=new float[3];
    private float[] post_Magn=new float[3];

    static final String[] permission = new String[]{
            Manifest.permission.INTERNET,//网络权限
            Manifest.permission.ACCESS_NETWORK_STATE,//网络状态
            Manifest.permission.READ_EXTERNAL_STORAGE,//读
            Manifest.permission.WRITE_EXTERNAL_STORAGE,//写
            Manifest.permission.ACCESS_COARSE_LOCATION,//初略定位
            Manifest.permission.ACCESS_FINE_LOCATION,//精密定位
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
    };

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SDKInitializer.initialize(this.getApplication());
        getPermission();
        context = this;//静态实例化，

        frame1 = new FrameLayout(this);
        frame2 = new FrameLayout(this);
        frame3 = new FrameLayout(this);
        drawLinesButton = new CheckBox(this);
        linesPoints = new ArrayList<LatLng>();
        scrollView = new LinearLayout(this);
        listener = getSensorListener(SENSOR_SERVICE);
        locationListener = getLocationListener();

        frame1 = (FrameLayout) findViewById(R.id.MyPage1);
        frame2 = (FrameLayout) findViewById(R.id.MyPage2);
        frame3 = (FrameLayout) findViewById(R.id.MyPage3);
        frame4 = (FrameLayout) findViewById(R.id.MyPage4);
        drawLinesButton = (CheckBox) findViewById(R.id.drawLines);
        radioGPS = (RadioButton) findViewById(R.id.checkGPS);
        radioNet = (RadioButton) findViewById(R.id.checkInternet);
        radiolib = (RadioButton) findViewById(R.id.checkLiberty);
        radioGPSInternet = (RadioButton) findViewById(R.id.checkGPSInternet);
        radioGPSMy = (RadioButton) findViewById(R.id.checkGPSMy);
        radioInternetMy = (RadioButton) findViewById(R.id.checkInternetMy);
        scrollView = (LinearLayout) findViewById(R.id.scrollView);
        acc_x = (TextView) findViewById(R.id.acc_x);
        acc_y = (TextView) findViewById(R.id.acc_y);
        acc_z = (TextView) findViewById(R.id.acc_z);
        gyr_x = (TextView) findViewById(R.id.gyr_x);
        gyr_y = (TextView) findViewById(R.id.gyr_y);
        gyr_z = (TextView) findViewById(R.id.gyr_z);
        magn_x = (TextView) findViewById(R.id.magn_x);
        magn_y = (TextView) findViewById(R.id.magn_y);
        magn_z = (TextView) findViewById(R.id.magn_z);
        orien_x = (TextView) findViewById(R.id.orien_x);
        orien_y = (TextView) findViewById(R.id.orien_y);
        orien_z = (TextView) findViewById(R.id.orien_z);
        orien_x1 = (TextView) findViewById(R.id.myorien_x);
        orien_y1 = (TextView) findViewById(R.id.myorien_y);
        orien_z1 = (TextView) findViewById(R.id.myorien_z);

        acc_time = (TextView) findViewById(R.id.acc_time);
        gyr_time = (TextView) findViewById(R.id.gyr_time);
        magn_time = (TextView) findViewById(R.id.magn_time);
        orien_time = (TextView) findViewById(R.id.orien_time);
        orien_time1 = (TextView) findViewById(R.id.myorien_time);

        acc_chart = (LineChart) findViewById(R.id.drawAcc);
        gyr_chart = (LineChart) findViewById(R.id.drawGyr);
        magn_chart = (LineChart) findViewById(R.id.drawMgn);
        orien_chart = (LineChart) findViewById(R.id.drawOrien);
        orien_chart1 = (LineChart) findViewById(R.id.mydrawOrien);

        startButton = (TextView) findViewById(R.id.startButton);
        stopButton = (TextView) findViewById(R.id.stopButton);
        cancelButton = (TextView) findViewById(R.id.cancelButton);
        saveButton = (TextView) findViewById(R.id.saveButton);

        myButton1 = getResources().getDrawable(R.drawable.mybutton);
        myButton2 = getResources().getDrawable(R.drawable.mybutton2);

        satelliteChart = (CombinedChart) findViewById(R.id.SatelliteChart);
        time_Chart = (TextView) findViewById(R.id.time_chart);

        textHeigt = (EditText) findViewById(R.id.heightEdit);
        textC = (EditText) findViewById(R.id.cEdit);

        total_acc = new LinkedList<DataItem>();
        total_gyr = new LinkedList<DataItem>();
        total_magn = new LinkedList<DataItem>();
        total_orien = new LinkedList<DataItem>();
        total_orien1 = new LinkedList<DataItem>();

        PDR_acc_x = new MyList();
        PDR_acc_y = new MyList();
        PDR_acc_z = new MyList();
        PDR_magn_x = new MyList();
        PDR_magn_y = new MyList();
        PDR_magn_z = new MyList();
        yaw = new LinkedList<Float>();
        PDR_listener = createPDRListener();
        PDR_locations = new LinkedList<GPSPoint>();

        //开启post检测
        postListener=createPostListener();
        Post_registSensor();

        startButton.setText("开始");


        acc_chart.getDescription().setText("加速度(M/S^2)");
        gyr_chart.getDescription().setText("陀螺仪(°/s)");
        magn_chart.getDescription().setText("磁场强度(UT)");
        orien_chart.getDescription().setText("姿态角(°)");
        orien_chart1.getDescription().setText("姿态角(°)");

        frame1.setVisibility(View.VISIBLE);
        frame2.setVisibility(View.GONE);
        frame3.setVisibility(View.GONE);
        frame4.setVisibility(View.GONE);


        options = new BaiduMapOptions();
        options.mapType(BaiduMap.MAP_TYPE_NORMAL);
        options.zoomControlsPosition(new Point(0, 0));
        options.scaleControlPosition(new Point(0, 310));

        mapView = new MapView(this, options);
        frame1.addView(mapView);

        baiduMap = mapView.getMap();
        MapStatus init_mapstaus = new MapStatus.Builder().zoom(30).build();
        MapStatusUpdate init_mapUpdate = MapStatusUpdateFactory.newMapStatus(init_mapstaus);
        baiduMap.setMapStatus(init_mapUpdate);

        postShow=this.findViewById(R.id.post);
        //PDR_registSensor();
        init_map();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabsLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        try {
                            page1Selected();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        try {
                            page2Selected();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        try {
                            page3Selected();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 3:
                        try {
                            page4Selected();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        Toast.makeText(this, "长按可更新当前位置", Toast.LENGTH_SHORT).show();
        BaiduMap.OnMapLongClickListener longClickListener = new BaiduMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //地图映射
                positionMap(latLng.latitude, latLng.longitude);
                //此处需要将百度坐标系转至WGS84坐标系
                GPSPoint gpsPoint = Transer.baiduToWgs84(latLng.latitude, latLng.longitude);
                PDR_locations.add(gpsPoint);
                //if (radioGPSMy.isChecked() || radioInternetMy.isChecked()) {
                //  PDR_locations.add(new GPSPoint(latLng.latitude, latLng.longitude));
                //isChangedLocation = true;
                //}
            }
        };
        baiduMap.setOnMapLongClickListener(longClickListener);

        startGnssStaus();//开启GNSS状态服务
        LocationClient locationClient = new LocationClient(this);
        locationClient.start();//开启位置监督
        //开启磁偏角监督
        LocationManager locationManager_delenation = (LocationManager) getSystemService(LOCATION_SERVICE);
        SensorManager sensorManager_delenation = (SensorManager) getSystemService(SENSOR_SERVICE);
        estimateDeclination.LocationManager(locationManager_delenation);
        estimateDeclination.SensorManager(sensorManager_delenation);
        //关于模型参数

        File file = new File(getFilesDir(), ".height.txt");
        if (!file.exists()) {
            EditText heigtEdit = new EditText(this);
            heigtEdit.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
            heigtEdit.setGravity(Gravity.RIGHT);
            heigtEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
            heigtEdit.setTextSize(20);
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setView(heigtEdit);
            alert.setTitle("提示⚠️");
            alert.setMessage("输入身高(单位：厘米)");
            alert.setCancelable(false);

            alert.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (heigtEdit.getText().length() > 0) {
                        setHeght = new Float(heigtEdit.getText().toString());
                        PDR._model_param_height = setHeght / 100.0f;
                        MainActivity.textHeigt.setText(PDR._model_param_height * 100 + "");
                        try {
                            if (!file.getParentFile().exists()) {
                                file.getParentFile().mkdir();
                            }
                            file.createNewFile();
                            if (file.exists()) {
                                FileOutputStream fileWriter = new FileOutputStream(file, false);
                                fileWriter.write(String.format("%f", setHeght).getBytes());
                                fileWriter.close();
                                Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        PDR._model_param_height = 1.80f;
                        MainActivity.textHeigt.setText(PDR._model_param_height * 100 + "");
                        try {
                            if (!file.getParentFile().exists()) {
                                file.getParentFile().mkdir();
                            }
                            file.createNewFile();
                            if (file.exists()) {
                                FileOutputStream fileWriter = new FileOutputStream(file, false);
                                fileWriter.write(String.format("%f", 180.0f).getBytes());
                                fileWriter.close();
                                //Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                                Toast.makeText(MainActivity.this, "默认身高为180cm", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            alert.show();
        } else {
            try {
                InputStream inputStream = new FileInputStream(file);
                if (inputStream != null) {
                    InputStreamReader reader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String line = bufferedReader.readLine();
                    inputStream.close();
                    //Toast.makeText(MainActivity.this,line,Toast.LENGTH_SHORT).show();
                    PDR._model_param_height = new Float(line);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        textHeigt.setText(PDR._model_param_height * 100 + "");
        textC.setText(PDR._model_param_c + "");
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常亮

        try {
            InputStream inputG = getAssets().open("g.csv");
            InputStream inputH = getAssets().open("h.csv");
            IgrfUtils.readGH(inputG, inputH);
            MyPublic.calender();
            if (MyPublic.lat != null && MyPublic.lon != null) {
                MyPublic.declination = IgrfUtils.getNowDeclination(MyPublic.lat, MyPublic.lon,
                        MyPublic.year, MyPublic.month, MyPublic.day);
                //Toast.makeText(this,String.format("%f",MyPublic.declination*180.0/Math.PI),Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /*private void auto_Location() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this,permission,1);
            //return;
        }
        Location location;
        if(PDR_locations.size()>1){
            long currentTime=PDR.getMillSecond();
            MyGnssStatus.IsUsed isUsed=MyGnssStatus.isUseds.get(MyGnssStatus.isUseds.size()-1);
            if(Math.abs(isUsed.time-currentTime)<1000&&isUsed.count>20){
                try{
                    //PDR_sensorManager.unregisterListener();
                }catch (Exception e){}
                location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                upToNewLocation(location.getLatitude(),location.getLongitude());
                PDR_locations.add(new GPSPoint(location.getLatitude(),location.getLongitude()));
            }else{
                if(PDR_locations.size()>0){
                    PDR_registSensor();
                }else{
                    location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(location!=null){
                        upToNewLocation(location.getLatitude(),location.getLongitude());
                        PDR_locations.add(new GPSPoint(location.getLatitude(),location.getLongitude()));
                    }else{
                        Toast.makeText(this,"请连接网络！",Toast.LENGTH_SHORT);
                    }
                }
            }
        }
    }*/

    //////////////////////////////////////////////////////////////////Page1
    //获取初始化地图数据(网络）
    private void init_map() {
        //服务对象监听
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            String[] permission = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            ActivityCompat.requestPermissions(this, permission, 1);
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            MyPublic.lat = location.getLatitude();
            MyPublic.lon = location.getLongitude();
            PDR_locations.add(new GPSPoint(location.getLatitude(), location.getLongitude()));
            GPSPoint gpsPoint = Transer.WGS2BD09(location.getLatitude(), location.getLongitude());
            positionMap(gpsPoint.lat, gpsPoint.lon);
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                MyPublic.lat = location.getLatitude();
                MyPublic.lon = location.getLongitude();
                PDR_locations.add(new GPSPoint(location.getLatitude(), location.getLongitude()));
                GPSPoint gpsPoint = Transer.WGS2BD09(location.getLatitude(), location.getLongitude());
                positionMap(gpsPoint.lat, gpsPoint.lon);
            } else {
                Toast.makeText(this, "获取位置信息失败", Toast.LENGTH_SHORT).show();
            }
        }
        PDR_registSensor();
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10L, (float) 0.01, locationListener);
    }

    private Criteria createCriteria() {
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);//高精度
        c.setAltitudeRequired(true);//包含高度信息
        c.setBearingRequired(true);//包含方位信息
        c.setSpeedRequired(true);//get Speed
        c.setCostAllowed(true);//allow paid
        c.setPowerRequirement(Criteria.POWER_HIGH);//high power
        return c;
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permission, 1);
        }
    }

    //地图定位显示
    private void positionMap(double latitude, double longitude) {
        if (baiduPoint != null) {
            baiduMap.removeOverLays(baiduPoint);
        }
        //将地图聚焦于当前位置
        LatLng center = new LatLng(latitude, longitude);//定义中心点坐标
        MapStatus mapStatus = new MapStatus.Builder()
                .target(center)
                .zoom(baiduMap.getMapStatus().zoom)
                .build();//定义地图状态
        MapStatusUpdate mapStatusUpdate
                = MapStatusUpdateFactory.newMapStatus(mapStatus);//便于描述地图状态变化
        baiduMap.setMapStatus(mapStatusUpdate);
        //中心位置绘制点
        BitmapDescriptor bitmap = BitmapDescriptorFactory.
                fromResource(R.drawable.arrow);
        OverlayOptions overlayOptions = new MarkerOptions()
                .position(center)
                .icon(bitmap)
                .flat(true)
                .scaleX(0.1f)
                .scaleY(0.1f).rotate(yawLogo);
        baiduPoint = new ArrayList<Overlay>();
        Overlay item = baiduMap.addOverlay(overlayOptions);
        baiduPoint.add(item);
    }

    //输入坐标为WGS84坐标
    private void upToNewLocation(double lat, double lon) {
        GPSPoint point = Transer.WGS2BD09(lat, lon);//坐标转换
        positionMap(point.lat, point.lon);
        //路径绘制
        if (drawLinesButton.isChecked()) {
            try {
                updataLines(lat, lon);
            } catch (Exception e) {
                //Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    //地图绘制轨迹线
    private void drawLinew(List<LatLng> latLngs) {
        if (baiduLines != null) {
            baiduMap.removeOverLays(baiduLines);
        }
        OverlayOptions overlayOptions = new PolylineOptions()
                .width(5)
                .color(0xAAFF0000)
                .points(latLngs);
        baiduLines = new ArrayList<Overlay>();
        Overlay polyLine = baiduMap.addOverlay(overlayOptions);
        baiduLines.add(polyLine);
    }

    //设置轨迹线绘制更新函数
    private void updataLines(double lat, double lon) {
        //注意，需要坐标转换
        GPSPoint thisPoint = Transer.WGS2BD09(lat, lon);
        LatLng latLng = new LatLng(thisPoint.lat, thisPoint.lon);
        linesPoints.add(latLng);
        drawLinew(linesPoints);
    }

    private LocationListener getLocationListener() {
        LocationListener thisListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (location != null) {
                    upToNewLocation(location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(MainActivity.this, "获取位置失败", Toast.LENGTH_SHORT).show();
                }
            }
        };
        return thisListener;
    }
    //////////////////////////////////////////////////////////////////////////////Page2

    private void registSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                , SensorManager.SENSOR_DELAY_UI);
    }

    private void PDR_registSensor() {
        PDR_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        PDR_sensorManager.registerListener(PDR_listener, PDR_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
        PDR_sensorManager.registerListener(PDR_listener, PDR_sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_UI);
        PDR_sensorManager.registerListener(PDR_listener, PDR_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_UI);
    }

    private void Post_registSensor(){
        postManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        postManager.registerListener(postListener,postManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
        postManager.registerListener(postListener,postManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_UI);
        postManager.registerListener(postListener,postManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_UI);
    }

    private SensorEventListener createPostListener(){
        SensorEventListener thisListener=new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                switch (sensorEvent.sensor.getType()){
                    case Sensor.TYPE_ACCELEROMETER:
                        new_Post_acc=true;
                        post_Acc[0]=sensorEvent.values[0];
                        post_Acc[1]=sensorEvent.values[1];
                        post_Acc[2]=sensorEvent.values[2];
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        new_Post_magn=true;
                        post_Magn[0]=sensorEvent.values[0];
                        post_Magn[1]=sensorEvent.values[1];
                        post_Magn[2]=sensorEvent.values[2];
                        break;
                }
                if(new_Post_magn&&new_Post_acc){
                    float[] post_Orien=PDR.estimateOrientationByCustomer(post_Acc,post_Magn);
                    float dertaTheta=PostJudge.Judge(post_Orien,post_Acc);
                    float totalAcc=new Float(Math.sqrt(post_Acc[0]*post_Acc[0]+post_Acc[1]*post_Acc[1]+post_Acc[2]*post_Acc[2]));
                    if(PDR.getMillSecond()-PostRecordTime>=500){
                        if(totalAcc>=10.2){
                            compassAnimation(dertaTheta);
                            if(dertaTheta>=-45.0f&&dertaTheta<=45.0f){
                                postShow.setText("forward");
                            }else if(dertaTheta>45.0f&&dertaTheta<=160.0f){
                                postShow.setText("right");
                            }else if(dertaTheta>160.0f||dertaTheta<-160.0f){
                                postShow.setText("back");
                            }else if(dertaTheta>=-160.0f&&dertaTheta<-45.0f){
                                postShow.setText("left");
                            }
                            //postShow.setText(String.format("%.3f°",dertaTheta));
                        }else{
                            compassAnimation(0.0f);
                            postShow.setText("Static");
                        }
                        PostRecordTime=PDR.getMillSecond();
                    }
                    new_Post_acc=false;
                    new_Post_magn=false;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        return thisListener;
    }
    private void compassAnimation(float angle){
        ImageView compassImg=this.findViewById(R.id.compass);
        //ViewAnimator animator=this.findViewById(R.id.animate);
        float x=compassImg.getWidth();
        float y=compassImg.getHeight();
        Animation anim=new RotateAnimation(0.0f,angle,0.5f*x,0.5f*y);
        anim.setDuration(250);
        compassImg.startAnimation(anim);
        //compassImg.setAnimation(anim);
        //compassImg.setRotation();
    }
    //draw
    private SensorEventListener createPDRListener() {
        SensorEventListener thisListener = new SensorEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                switch (sensorEvent.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        new_PDR_acc = true;
                        PDR_acc_x.addItem(sensorEvent.values[0]);
                        PDR_acc_y.addItem(sensorEvent.values[1]);
                        PDR_acc_z.addItem(sensorEvent.values[2]);
                        /*
                         * 避免数据量过大
                         * */
                        if (PDR_acc_x.items.length > xLabelMax) {
                            PDR_acc_x.removeFirst();
                            PDR_acc_y.removeFirst();
                            PDR_acc_z.removeFirst();
                        }
                        custom_acc = new float[]{
                                (float) PDR_acc_x.items[PDR_acc_x.items.length - 1],
                                (float) PDR_acc_y.items[PDR_acc_y.items.length - 1],
                                (float) PDR_acc_z.items[PDR_acc_z.items.length - 1]
                        };
                        acceleration = new DataItem(custom_acc, PDR.getMillSecond());
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        new_PDR_magn = true;
                        PDR_magn_x.addItem(sensorEvent.values[0]);
                        PDR_magn_y.addItem(sensorEvent.values[1]);
                        PDR_magn_z.addItem(sensorEvent.values[2]);

                        custom_magn = new float[]{
                                (float) PDR_magn_x.items[PDR_magn_x.items.length - 1],
                                (float) PDR_magn_y.items[PDR_magn_y.items.length - 1],
                                (float) PDR_magn_z.items[PDR_magn_z.items.length - 1]
                        };
                        if (PDR_magn_x.items.length > xLabelMax) {
                            PDR_magn_x.removeFirst();
                            PDR_magn_y.removeFirst();
                            PDR_magn_z.removeFirst();
                        }
                        break;
                }
                if (new_PDR_acc && new_PDR_magn) {
                    float[] PDR_orien
                            = PDR.estimateOrientationByCustomer(custom_acc, custom_magn);
                    /*
                    yawItems.add(new KF.YawItem(PDR.getMillSecond(),PDR_orien[0]));
                    if(yawItems.size()==1){
                        MyMatrix initStateVar=new MyMatrix(2,2,new float[][]{
                                {0.05f*0.05f,0.0f},
                                {0.0f,0.05f*0.05f}
                        });
                        MyMatrix obvMat=new MyMatrix(1,2,new float[][]{
                                {1.0f,0.0f}
                        });
                        kf=new KF(0.0001f,0.01f,yawItems.get(0),initStateVar,obvMat);
                    }else if(yawItems.size()>1){
                        KF.YawItem thisyaw=new KF.YawItem(yawItems.get(yawItems.size()-1).l_time,
                                yawItems.get(yawItems.size()-1).yaw);
                        if(Math.abs(yawItems.get(yawItems.size()-2).yaw)>2.96&&
                                Math.abs(yawItems.get(yawItems.size()-1).yaw)>2.96){
                            if(yawItems.get(yawItems.size()-1).yaw<0){
                                float new_yaw=new Float(yawItems.get(yawItems.size()-1).yaw+2*Math.PI);
                                thisyaw=new KF.YawItem(yawItems.get(yawItems.size()-1).l_time,new_yaw);
                            }
                        }
                        float estimatYaw=kf.operator(thisyaw);
                        if(PDR_locations.size()>0){
                            GPSPoint nowPoint=PDR.estimatePosition(estimatYaw,acceleration,PDR_locations.get(PDR_locations.size()-1));
                            if(!PDR._isStatic){
                                upToNewLocation(nowPoint.lat, nowPoint.lon);
                                PDR_locations.add(nowPoint);
                            }
                        }
                    }*/
                    /*
                    yaw.add(PDR_orien[0]);
                    float average_yaw = PDR_orien[0];//newYaw
                    if (yaw.size() > 1) {
                        average_yaw = PDR.AverageYaw(yaw.get(yaw.size() - 2), yaw.get(yaw.size() - 1));
                    }
                    if (true) {
                        //更改位置
                        //float estimateYaw=kf.operator(yawItems.get(yawItems.size()-1));
                        if (PDR_locations.size() > 0) {
                            GPSPoint nowPoint = PDR.estimatePosition(average_yaw, acceleration, PDR_locations.get(PDR_locations.size() - 1));
                            if (!PDR._isStatic) {
                                upToNewLocation(nowPoint.lat, nowPoint.lon);
                                PDR_locations.add(nowPoint);
                            }
                        }
                        if (PDR_locations.size() > xLabelMax) {
                            PDR_locations.removeFirst();
                        }
                    }*/
                    yaw.add(PDR_orien[0]);
                    float estimateYaw = yaw.get(yaw.size() - 1);
                    if (yaw.size() == 1) {
                        trdAverYaw._lastYaw1 = yaw.get(0);
                        trdAverYaw._lastYaw2 = yaw.get(0);
                        trdAverYaw._lastYaw3 = yaw.get(0);
                        //PDR.avaerYaws.add(yaw.get(0));
                        estimateYaw = yaw.get(0);
                    } else {
                        estimateYaw = (float) (trdAverYaw.getAver(yaw.get(yaw.size() - 1), yaw.get(yaw.size() - 2)));
                        //estimateYaw=PDR.AverYaw2(yaw.get(yaw.size()-1));
                        //PDR.avaerYaws.add(estimateYaw);
                    }
                    if (PDR_locations.size() > 0) {
                        GPSPoint nowPoint = PDR.estimatePosition(estimateYaw, acceleration, PDR_locations.get(PDR_locations.size() - 1));
                        if (!PDR._isStatic) {
                            yawLogo=new Float(-(estimateYaw+estimateDeclination.estiDelenation())*180.0/Math.PI);
                            if (MyGnssStatus.isUseds.size() > 0) {
                                MyGnssStatus.IsUsed isUsed = MyGnssStatus.isUseds.get(MyGnssStatus.isUseds.size() - 1);
                                long currentTime = PDR.getMillSecond();
                                if (Math.abs(currentTime - isUsed.time) < 1000 && isUsed.count > 20) {
                                    if (ActivityCompat.checkSelfPermission(MainActivity.this,
                                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                            && ActivityCompat.checkSelfPermission(MainActivity.this,
                                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        // TODO: Consider calling
                                        //    ActivityCompat#requestPermissions
                                        ActivityCompat.requestPermissions(MainActivity.this,permission,1);
                                        //return;
                                    }
                                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    nowPoint=new GPSPoint(location.getLatitude(),location.getLongitude());
                                }
                            }
                            upToNewLocation(nowPoint.lat, nowPoint.lon);
                            PDR_locations.add(nowPoint);
                        }
                    }
                    new_PDR_magn = false;
                    new_PDR_acc = false;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        return thisListener;
    }

    private void sensorChange(SensorEvent sensorEvent) {
        if(lastTime==null){
            lastTime=PDR.getMillSecond();
        }else{
            if(Math.abs(PDR.getMillSecond()-lastTime)>=20){//Math.abs(PDR.getMillSecond()-lastTime)>=50
                lastTime=PDR.getMillSecond();
                DecimalFormat df = new DecimalFormat("+00.0000;-00.0000");
                switch (sensorEvent.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER: //加速度
                        check_acc = true;
                        acc_xList.addItem(sensorEvent.values[0]);
                        acc_yList.addItem(sensorEvent.values[1]);
                        acc_zList.addItem(sensorEvent.values[2]);

                        acc_x.setText(df.format(sensorEvent.values[0]));
                        acc_y.setText(df.format(sensorEvent.values[1]));
                        acc_z.setText(df.format(sensorEvent.values[2]));

                        acc_time.setText(PDR.getCurentTime());

                        custom_acc = new float[]{
                                (float) acc_xList.items[acc_xList.items.length - 1],
                                (float) acc_yList.items[acc_yList.items.length - 1],
                                (float) acc_zList.items[acc_zList.items.length - 1]
                        };

                        float[] myvalues = new float[]{
                                (float) acc_xList.items[acc_xList.items.length - 1],
                                (float) acc_yList.items[acc_yList.items.length - 1],
                                (float) acc_zList.items[acc_zList.items.length - 1]
                        };
                        total_acc.add(new DataItem(myvalues, PDR.getMillSecond()));//保存数据
                        if (acc_xList.items.length > xLabelMax) {
                            acc_xList.removeFirst();
                            acc_yList.removeFirst();
                            acc_zList.removeFirst();
                        }
                        DrawLines.drawChart(acc_chart, acc_xList, acc_yList, acc_zList, "加速度(x)", "加速度(y)", "加速度(z)");
                        break;
                    case Sensor.TYPE_GYROSCOPE://陀螺仪
                        gyr_xList.addItem(sensorEvent.values[0] * 180.0 / Math.PI);
                        gyr_yList.addItem(sensorEvent.values[1] * 180.0 / Math.PI);
                        gyr_zList.addItem(sensorEvent.values[2] * 180.0 / Math.PI);

                        gyr_x.setText(df.format(sensorEvent.values[0] * 180.0 / Math.PI));
                        gyr_y.setText(df.format(sensorEvent.values[1] * 180.0 / Math.PI));
                        gyr_z.setText(df.format(sensorEvent.values[2] * 180.0 / Math.PI));
                        gyr_time.setText(PDR.getCurentTime());

                        myvalues = new float[]{
                                (float) (gyr_xList.items[gyr_xList.items.length - 1] * Math.PI / 180.0),
                                (float) (gyr_yList.items[gyr_yList.items.length - 1] * Math.PI / 180.0),
                                (float) (gyr_zList.items[gyr_zList.items.length - 1] * Math.PI / 180.0)
                        };
                        total_gyr.add(new DataItem(myvalues, PDR.getMillSecond()));
                        if (gyr_xList.items.length > xLabelMax) {
                            gyr_xList.removeFirst();
                            gyr_yList.removeFirst();
                            gyr_zList.removeFirst();
                        }
                        DrawLines.drawChart(gyr_chart, gyr_xList, gyr_yList, gyr_zList, "陀螺仪(x)", "陀螺仪(y)", "陀螺仪(z)");
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD://磁场强度
                        check_magn = true;
                        magn_xList.addItem(sensorEvent.values[0]);
                        magn_yList.addItem(sensorEvent.values[1]);
                        magn_zList.addItem(sensorEvent.values[2]);

                        custom_magn = new float[]{
                                (float) magn_xList.items[magn_xList.items.length - 1],
                                (float) magn_yList.items[magn_yList.items.length - 1],
                                (float) magn_zList.items[magn_zList.items.length - 1]
                        };
                        magn_x.setText(df.format(sensorEvent.values[0]));
                        magn_y.setText(df.format(sensorEvent.values[1]));
                        magn_z.setText(df.format(sensorEvent.values[2]));
                        magn_time.setText(PDR.getCurentTime());

                        myvalues = new float[]{
                                (float) magn_xList.items[magn_xList.items.length - 1],
                                (float) magn_yList.items[magn_yList.items.length - 1],
                                (float) magn_zList.items[magn_zList.items.length - 1]
                        };
                        total_magn.add(new DataItem(myvalues, PDR.getMillSecond()));
                        if (magn_xList.items.length > xLabelMax) {
                            magn_xList.removeFirst();
                            magn_yList.removeFirst();
                            magn_zList.removeFirst();
                        }
                        DrawLines.drawChart(magn_chart, magn_xList, magn_yList, magn_zList, "磁场强度(x)", "磁场强度(y)", "磁场强度(z)");
                        break;
                }
                if (check_acc && check_magn) {
                    float[] orien_system = PDR.estimateOrientationBySystem(acc_xList, acc_yList, acc_zList, magn_xList, magn_yList, magn_zList);

                    orgin_xList.addItem((double) orien_system[0] * 180.0 / Math.PI);
                    orgin_yList.addItem((double) orien_system[1] * 180.0 / Math.PI);
                    orgin_zList.addItem((double) orien_system[2] * 180.0 / Math.PI);

                    orien_x.setText(df.format(orien_system[0] * 180.0 / Math.PI));
                    orien_y.setText(df.format(orien_system[1] * 180.0 / Math.PI));
                    orien_z.setText(df.format(orien_system[2] * 180.0 / Math.PI));
                    orien_time.setText(PDR.getCurentTime());

                    total_orien.add(new DataItem(orien_system, PDR.getMillSecond()));
                    if (orgin_xList.items.length > xLabelMax) {
                        orgin_xList.removeFirst();
                        orgin_yList.removeFirst();
                        orgin_zList.removeFirst();
                    }
                    DrawLines.drawChart(orien_chart, orgin_xList, orgin_yList, orgin_zList, "航偏角", "俯仰角", "翻滚角");

                    float[] orien_custom = PDR.estimateOrientationByCustomer(custom_acc, custom_magn);

                    orien_xList1.addItem((double) orien_custom[0] * 180.0 / Math.PI);
                    orien_yList1.addItem((double) orien_custom[1] * 180.0 / Math.PI);
                    orien_zList1.addItem((double) orien_custom[2] * 180.0 / Math.PI);

                    orien_x1.setText(df.format(orien_custom[0] * 180.0 / Math.PI));
                    orien_y1.setText(df.format(orien_custom[1] * 180.0 / Math.PI));
                    orien_z1.setText(df.format(orien_custom[2] * 180.0 / Math.PI));
                    orien_time1.setText(PDR.getCurentTime());

                    total_orien1.add(new DataItem(orien_custom, PDR.getMillSecond()));
                    if (orien_xList1.items.length > xLabelMax) {
                        orien_xList1.removeFirst();
                        orien_yList1.removeFirst();
                        orien_zList1.removeFirst();
                    }
                    DrawLines.drawChart(orien_chart1, orien_xList1, orien_yList1, orien_zList1, "航偏角", "俯仰角", "翻滚角");

                    check_acc = false;
                    check_magn = false;
                }
            }
        }

    }

    private SensorEventListener getSensorListener(String sensorService) {
        SensorEventListener sensorEventListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                sensorChange(sensorEvent);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        return sensorEventListener;
    }
    //open page1
    private void page1Selected() throws IOException {
        if (startButton.getText() == "采集") {
            Toast.makeText(this, "数据仍在采集中", Toast.LENGTH_SHORT).show();
        }
        if (!drawLinesButton.isChecked()) {
            linesPoints = new ArrayList<LatLng>();
            baiduMap.removeOverLays(baiduLines);
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //服务对象监听
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            String[] permission = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            ActivityCompat.requestPermissions(this, permission, 1);
        }
        String permiss = locationManager.getBestProvider(createCriteria(), true);
        /*
        try {
            PDR_sensorManager.unregisterListener(PDR_listener);
        } catch (Exception e) {
        }
        try {
            if (radioGPS.isChecked()) {
                PDR_locations = new LinkedList<GPSPoint>();
                yaw=new LinkedList<Float>();
                PDR.avaerYaws=new LinkedList<Float>();
                isChangedLocation=false;
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    upToNewLocation(location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(this, "获取位置失败", Toast.LENGTH_SHORT).show();
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) 10, (float) 0.01, locationListener);
                //sensorManager.unregisterListener(listener);
            } else if (radioNet.isChecked()) {
                isChangedLocation=false;
                PDR_locations = new LinkedList<GPSPoint>();
                yaw=new LinkedList<Float>();
                PDR.avaerYaws=new LinkedList<Float>();
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    upToNewLocation(location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(this, "获取位置失败", Toast.LENGTH_SHORT).show();
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, (long) 10, (float) 0.01, locationListener);
                //sensorManager.unregisterListener(listener);
            } else if (radiolib.isChecked()) {
                isChangedLocation=false;
                PDR_locations = new LinkedList<GPSPoint>();
                yaw=new LinkedList<Float>();
                PDR.avaerYaws=new LinkedList<Float>();
                Location location = locationManager.getLastKnownLocation(permiss);
                if (location != null) {
                    upToNewLocation(location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(this, "获取位置失败", Toast.LENGTH_SHORT).show();
                }
                locationManager.requestLocationUpdates(permiss, (long) 10, (float) 0.01, locationListener);
                //sensorManager.unregisterListener(listener);
            } else if (radioGPSInternet.isChecked()) {
                isChangedLocation=false;
                PDR_locations = new LinkedList<GPSPoint>();
                yaw=new LinkedList<Float>();
                PDR.avaerYaws=new LinkedList<Float>();
                init_map();
                //sensorManager.unregisterListener(listener);
            } else if (radioGPSMy.isChecked()) {
                if (PDR_locations.size() > 0) {
                    PDR_registSensor();
                } else {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        PDR_locations.add(new GPSPoint(location.getLatitude(), location.getLongitude()));
                        upToNewLocation(location.getLatitude(), location.getLongitude());
                        locationManager.removeUpdates(locationListener);
                        PDR_registSensor();
                    } else {
                        Toast.makeText(this, "获取位置失败", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (radioInternetMy.isChecked()) {
                if (PDR_locations.size() > 0) {
                    PDR_registSensor();
                } else {
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        PDR_locations.add(new GPSPoint(location.getLatitude(), location.getLongitude()));
                        upToNewLocation(location.getLatitude(), location.getLongitude());
                        locationManager.removeUpdates(locationListener);
                        PDR_registSensor();
                    } else {
                        Toast.makeText(this, "获取位置失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            //Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
        }*/
        frame2.setVisibility(View.GONE);
        frame3.setVisibility(View.GONE);
        frame4.setVisibility(View.GONE);
        frame1.setVisibility(View.VISIBLE);

        upDataModel();
    }
    //open page2
    private void page2Selected() throws IOException {
        //float[] temp=new float[]{1.0f,2.0f};
        //long time=1;
        //SensorDataItem sensorDataItem=new SensorDataItem(Sensor.TYPE_ACCELEROMETER,temp,time);
        startButton.setBackground(myButton1);
        if (startButton.getText() == "采集" || startButton.getText() == "继续") {
            stopButton.setBackground(myButton1);
            cancelButton.setBackground(myButton1);
            saveButton.setBackground(myButton1);
        } else {
            stopButton.setBackground(myButton2);
            cancelButton.setBackground(myButton2);
            saveButton.setBackground(myButton2);
        }

        locationManager.removeUpdates(locationListener);
        frame1.setVisibility(View.GONE);
        frame3.setVisibility(View.GONE);
        frame4.setVisibility(View.GONE);
        frame2.setVisibility(View.VISIBLE);

        upDataModel();
    }
    //open page3
    private void page3Selected() throws IOException {
        if (startButton.getText() == "采集") {
            Toast.makeText(this, "数据仍在采集中", Toast.LENGTH_SHORT).show();
        }
        textHeigt.setText(PDR._model_param_height*100+"");
        textC.setText(PDR._model_param_c+"");
        frame1.setVisibility(View.GONE);
        frame2.setVisibility(View.GONE);
        frame4.setVisibility(View.GONE);
        frame3.setVisibility(View.VISIBLE);

        upDataModel();
    }
    //open page4
    private void page4Selected() throws IOException {
        //test the GPS status
        try{
            DrawLines.drawCombined(satelliteChart,new LinkedList<MyGnssStatus.Satellite>());
        }catch (Exception e){
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
        }
        frame1.setVisibility(View.GONE);
        frame2.setVisibility(View.GONE);
        frame3.setVisibility(View.GONE);
        frame4.setVisibility(View.VISIBLE);

        upDataModel();
    }
    //update the model data
    //include the height and c
    private void upDataModel() throws IOException {
        if(textHeigt.getText().length()>0&&textC.getText().length()>0){
            Float model_c=new Float(textC.getText().toString());
            Float model_height=new Float(textHeigt.getText().toString());
            model_height=model_height/100.0f;
            if(model_c!=PDR._model_param_c){
                PDR._model_param_c=model_c;
            }
            if(model_height!=PDR._model_param_height){
                PDR._model_param_height=model_height;

                File file=new File(getFilesDir(),".height.txt");
                if(!file.exists()){
                    if(!file.getParentFile().exists()){
                        file.getParentFile().mkdir();
                    }
                    file.createNewFile();
                }
                if(file.exists()){
                    FileOutputStream outputStream=new FileOutputStream(file);
                    outputStream.write(String.format("%f",PDR._model_param_height).getBytes());
                    outputStream.close();
                }
            }
        }
    }
    //开启收集数据服务
    public void startup(View view) {
        try {
            registSensor();
            stopButton.setBackground(myButton1);
            cancelButton.setBackground(myButton1);
            saveButton.setBackground(myButton1);
            startButton.setText("采集");
        } catch (Exception e) {
            Toast.makeText(this, "服务启动失败", Toast.LENGTH_SHORT).show();
        }

    }
    //中断收集数据
    public void stop(View view) {
        if (stopButton.getBackground() == myButton1) {
            sensorManager.unregisterListener(listener);
            startButton.setText("继续");
        }
    }
    //清除当前收集数据任务
    public void cancel(View view) {
        if (cancelButton.getBackground() == myButton1) {
            try {
                sensorManager.unregisterListener(listener);

                check_magn=false;
                check_acc=false;
                //清空矩阵
                total_acc = new LinkedList<DataItem>();
                total_gyr = new LinkedList<DataItem>();
                total_magn = new LinkedList<DataItem>();
                total_orien = new LinkedList<DataItem>();
                total_orien1 = new LinkedList<DataItem>();

                acc_xList = new MyList();
                acc_yList = new MyList();
                acc_zList = new MyList();
                gyr_xList = new MyList();
                gyr_yList = new MyList();
                gyr_zList = new MyList();
                magn_xList = new MyList();
                magn_yList = new MyList();
                magn_zList = new MyList();
                orgin_xList = new MyList();
                orgin_yList = new MyList();
                orgin_zList = new MyList();
                orien_xList1 = new MyList();
                orien_yList1 = new MyList();
                orien_zList1 = new MyList();

                acc_chart.clear();
                magn_chart.clear();
                gyr_chart.clear();
                orien_chart.clear();
                orien_chart1.clear();

                acc_time.setText("local time");
                gyr_time.setText("local time");
                magn_time.setText("local time");
                orien_time.setText("local time");
                orien_time1.setText("local time");

                acc_x.setText("acce-x");
                acc_y.setText("acce-y");
                acc_z.setText("acce-z");
                gyr_x.setText("gyro-x");
                gyr_y.setText("gyro-y");
                gyr_z.setText("gyro-z");
                magn_x.setText("magn-x");
                magn_y.setText("magn-y");
                magn_z.setText("magn-z");
                orien_x.setText("orien-x");
                orien_y.setText("orien-y");
                orien_z.setText("orien-z");
                orien_x1.setText("orien-x");
                orien_y1.setText("orien-y");
                orien_z1.setText("orien-z");

                startButton.setText("开始");
                stopButton.setBackground(myButton2);
                cancelButton.setBackground(myButton2);
                saveButton.setBackground(myButton2);

            } catch (Exception e) {

            }
        }
    }

    private static String acc_text;
    private static String gyr_text;
    private static String magn_text;
    private static String orien_text;
    private static String orien_text1;
    //保存收集到的数据
    public void save(View view) {
        if (startButton.getText() == "继续" && saveButton.getBackground() == myButton1) {
            acc_text = "Accelerate(m/s^2)\nMiilSeconds,x,y,z\n";
            gyr_text = "Gyroscope(rad/s)\nMiilSeconds,x,y,z\n";
            magn_text = "Magnetic Field(UT)\nMiilSeconds,x,y,z\n";
            orien_text = "Orientation(System)(rad)\nMiilSeconds,Yaw,Pitch,Row\n";
            orien_text1 = "Orientation(Custom)(rad)\nMiilSeconds,Yaw,Pitch,Row\n";
            //Toast.makeText(this,total_acc.size()+"",Toast.LENGTH_SHORT).show();
            for (int i = 0; i < total_acc.size(); i++) {
                acc_text += total_acc.get(i).toString();
            }
            for (int i = 0; i < total_gyr.size(); i++) {
                gyr_text += total_gyr.get(i).toString();
            }
            for (int i = 0; i < total_magn.size(); i++) {
                magn_text += total_magn.get(i).toString();
            }
            for (int i = 0; i < total_orien.size(); i++) {
                orien_text += total_orien.get(i).toString();
            }
            for (int i = 0; i < total_orien1.size(); i++) {
                orien_text1 += total_orien1.get(i).toString();
            }
            //导出文件
            savepath = "sdcard/documents";
            File checkPath = new File(savepath);
            try {
                if (!checkPath.exists()) {
                    checkPath.mkdir();
                }
            } catch (Exception e) {
                Toast.makeText(this, "创建documents文件夹失败", Toast.LENGTH_SHORT).show();
            }
            if (checkPath.exists()) {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setCancelable(false);
                alert.setTitle("请输入：输出文件夹的名称");
                alert.setMessage("保存目录为：手机存储/documents/");
                EditText alert_edit = new EditText(this);
                alert.setView(alert_edit);
                alert.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        savepath += "/";
                        savepath += alert_edit.getText();
                        File myPath = new File(savepath);
                        try {
                            if (!myPath.exists()) {
                                myPath.mkdir();
                            }
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, String.format("%s文件夹创建失败", savepath), Toast.LENGTH_SHORT).show();
                        }
                        if (myPath.exists()) {
                            String[] saveContents = new String[]{acc_text, gyr_text, magn_text, orien_text, orien_text1};
                            try {
                                Write.writeContext(savepath, saveContents);
                            } catch (Exception e) {
                            }
                        }
                    }
                });
                alert.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    }
                });
                alert.show();
            }
        }
    }
    //开启GNSS状态监听服务
    private void startGnssStaus() {
        //需要用GNSsStatus
        LocationManager testManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        MyGnssStatus.GnssListener(satelliteChart,time_Chart);//初始化Listener
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] permission = new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                };
                ActivityCompat.requestPermissions(this, permission, 1);
                return;
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//检验Android版本是否高于26
            testManager.registerGnssStatusCallback(MyGnssStatus.callback);
            testManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10l, 0.01f, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                }
            });
        }
    }
    //程序退出时间重写
    private static boolean decide;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent){
        decide=true;
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(startButton.getText()=="采集"){
                decide=false;
                //Toast.makeText(this,"数据仍在采集中，确定要退出",Toast.LENGTH_SHORT).show();
                AlertDialog.Builder alert=new AlertDialog.Builder(this);
                alert.setTitle("⚠️警告");
                alert.setMessage("数据仍在采集中，确定要退出？");
                alert.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        decide=true;
                        MainActivity.this.finish();
                    }
                });
                alert.setPositiveButton("取消",null);
                alert.setCancelable(false);
                alert.show();
            }
        }
        if(decide){
            super.onKeyDown(keyCode,keyEvent);
        }
        return decide;
    }

}