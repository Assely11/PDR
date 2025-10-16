package com.example.navigation;
/*
* 卡尔曼滤波，平滑姿态角
* */
public class KF {
    static class YawItem{
        public double time;
        public float yaw;
        public long l_time;
        public YawItem(long _time,float _yaw){
            l_time=_time;
            time=(double) ((_time - 1645883000000L) / 1000.0);
            yaw=_yaw;
        }
    }
    private float _sysError;
    private float _sysError2;
    private float _obvError;
    private float _obvError2;
    private MyMatrix _lastState;
    private MyMatrix _lastStateVar;
    private MyMatrix _obvMat;
    private double _lastTimeStamp;
    public boolean init=false;

    public KF(float sysError,//白噪声
              float observeError,//观测噪声
              YawItem initItem,
              MyMatrix initStateVar,
              MyMatrix observeMat){
       _sysError=sysError;
       _sysError2=sysError*sysError;
       _obvError=observeError;
       _obvError2=observeError*observeError;
       _lastState=new MyMatrix(2,1,new float[][]{{initItem.yaw},{0.0f}});
       _lastTimeStamp= initItem.time;
       _lastStateVar=initStateVar;
       _obvMat=observeMat;
       init=true;
    }

    protected MyMatrix[] prediction(float period){
        float period2=period*period;
        float period3=period2*period;

        float[][] data_stateMove=new float[][]{
                {1.0f,period},
                {0.0f,1.0f}
        };
        MyMatrix stateMoveMat=new MyMatrix(2,2,data_stateMove);//Phi(k)
        float[][] data_whiteNoize=new float[][]{
                {_sysError2*period3/3.0f,_sysError2*0.5f*period2},
                {_sysError2*0.5f*period2,_sysError2*period}
        };
        MyMatrix whiteNoize=new MyMatrix(2,2,data_whiteNoize);//Dw(k-1)

        MyMatrix predState=MyMatrix.Mutiply(stateMoveMat,_lastState);
        MyMatrix predStateVar=MyMatrix.Mutiply(stateMoveMat,
                MyMatrix.Mutiply(_lastStateVar,stateMoveMat.Transpose()));
        predStateVar=MyMatrix.Add(predStateVar,whiteNoize);

        return new MyMatrix[]{predState,predStateVar};
    }
    protected  void update(YawItem newItem,MyMatrix predState,MyMatrix predStateVar){
        float[][] data_obvVar=new float[][]{
                {_obvError2}
        };
        MyMatrix obvVar=new MyMatrix(1,1,data_obvVar);
        MyMatrix kalmanGain=MyMatrix.Mutiply(predStateVar,_obvMat.Transpose());
        MyMatrix data1=MyMatrix.Mutiply(_obvMat,
                MyMatrix.Mutiply(predStateVar,_obvMat.Transpose()));
        data1=MyMatrix.Add(data1,obvVar);
        kalmanGain=MyMatrix.Mutiply(kalmanGain,data1.Inverse());

        float error= newItem.yaw-MyMatrix.Mutiply(_obvMat,predState).data[0][0];

        _lastState=MyMatrix.Add(predState,MyMatrix.DotNumber(error,kalmanGain));
        _lastStateVar=MyMatrix.Mutiply(MyMatrix.Min(MyMatrix.Identity(2),
                        MyMatrix.Mutiply(kalmanGain,_obvMat)),
                predStateVar);
    }
    public float operator(YawItem newItem){
        float period= (float) (newItem.time-_lastTimeStamp);
        _lastTimeStamp= newItem.time;
        MyMatrix[] res_prediction=prediction(period);
        update(newItem,res_prediction[0],res_prediction[1]);
        return _lastState.data[0][0];
    }
}
