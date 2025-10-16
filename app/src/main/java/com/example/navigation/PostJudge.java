package com.example.navigation;

public class PostJudge {
    public static float Judge(float[] oriegn,float[] acc){
        MyMatrix xyz=new MyMatrix(3,1,new float[][]{{-acc[1]},{-acc[0]},{-acc[2]}});
        float[][] dataTrans3=new float[][]{
                {1,0,0},
                {0,new Float(Math.cos(oriegn[2])),new Float(-Math.sin(oriegn[2]))},
                {0,new Float(Math.sin(oriegn[2])),new Float(Math.cos(oriegn[2]))}
        };
        MyMatrix MatrixTrans3=new MyMatrix(3,3,dataTrans3);

        float[][] dataTrans2=new float[][]{
                {new Float(Math.cos(oriegn[1])),0,new Float(-Math.sin(oriegn[1]))},
                {0,1,0},
                {new Float(Math.sin(oriegn[1])),0,new Float(Math.cos(oriegn[1]))}
        };
        MyMatrix MatrixTrans2=new MyMatrix(3,3,dataTrans2);

        float[][] dataTrans1=new float[][]{
                {new Float(Math.cos(oriegn[0])),new Float(-Math.sin(oriegn[0])),0},
                {new Float(Math.sin(oriegn[0])),new Float(Math.cos(oriegn[0])),0},
                {0,0,1}
        };
        MyMatrix MatrixTrans1=new MyMatrix(3,3,dataTrans1);

        MyMatrix neh=MyMatrix.Mutiply(MatrixTrans3,MatrixTrans2);
        neh=MyMatrix.Mutiply(neh,MatrixTrans1);
        neh=MyMatrix.Mutiply(neh,xyz);
        //求加速度在地理坐标系下与北方向的夹角
        float accTheta=new Float(Math.atan2(neh.data[1][0],neh.data[0][0]));

        //将该夹角与航偏角比较，判断运动姿态
        float dertaTheta=accTheta-oriegn[0];
        if(dertaTheta<-Math.PI){
            dertaTheta=dertaTheta+new Float(2*Math.PI);
        }
        if(dertaTheta>Math.PI){
            dertaTheta=dertaTheta+new Float(-2*Math.PI);
        }
        dertaTheta=new Float(dertaTheta*180.0/Math.PI);
        return dertaTheta;//输出数值，实验判断
    }
}