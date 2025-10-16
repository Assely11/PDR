package com.example.navigation;

public class MyMatrix {
    public int m;//rows
    public int n;//colums
    public float[][]data;
    public MyMatrix(int _m,//rows
                    int _n//colums
    ){
        this.m=_m;
        this.n=_n;
        this.data=new float[_m][_n];
    }
    public MyMatrix(){}
    public MyMatrix(int _m,int _n,float[][] _data){
        this.m=_m;
        this.n=_n;
        this.data=new float[_m][_n];
        for(int i=0;i<m;i++){
            for(int j=0;j<n;j++){
                this.data[i][j]=_data[i][j];
            }
        }
    }
    public MyMatrix Reminder(int _i,int _j){//求余子式
        MyMatrix res=new MyMatrix(m-1, n-1);
        for(int i=0;i<_i;i++){
            for(int j=0;j<_j;j++){
                res.data[i][j]=this.data[i][j];
            }
            for(int j=_j;j<n-1;j++){
                res.data[i][j]=this.data[i][j+1];
            }
        }
        for(int i=_i;i<m-1;i++){
            for(int j=0;j<_j;j++){
                res.data[i][j]=this.data[i+1][j];
            }
            for(int j=_j;j<n-1;j++){
                res.data[i][j]=this.data[i+1][j+1];
            }
        }
        return res;
    }
    public Float Det(){//行列式
        Float total=0.0f;
        if(this.m==this.n){
            if(this.m==2){
                total=this.data[0][0]*this.data[1][1]-this.data[0][1]*this.data[1][0];
            }else if(this.m==1){
                total=this.data[0][0];
            }else{
                for(int j=0;j<this.n;j++){
                    float a1=(float)(Math.pow(-1, j+2)*this.data[0][j]);
                    MyMatrix a2=this.Reminder(0, j);
                    total+=a1*a2.Det();
                }
            }
        }else{
            total=null;
        }
        return total;
    }
    public MyMatrix Inverse(){//逆矩阵
        MyMatrix res=new MyMatrix(this.m, this.n);
        Float Data=this.Det();
        if(Data!=0&&(this.m==this.n)){
            for(int i=0;i<this.m;i++){
                for(int j=0;j<this.n;j++){
                    float data1=(float)(Math.pow(-1, i+j+2));
                    float data2=this.Reminder(i, j).Det();
                    res.data[j][i]=data1*data2/Data;
                }
            }
        }else{
            res=null;
        }
        return res;
    }
    public MyMatrix Transpose(){//转置
        MyMatrix res=new MyMatrix(this.n, this.m);
        for(int i=0;i<this.m;i++){
            for(int j=0;j<this.n;j++){
                res.data[j][i]=this.data[i][j];
            }
        }
        return res;
    }
    public static MyMatrix Identity(int _m){//单位矩阵
        MyMatrix res=new MyMatrix(_m,_m);
        for(int i=0;i<_m;i++){
            for(int j=0;j<_m;j++){
                if(i==j){
                    res.data[i][j]=1.0f;
                }else{
                    res.data[i][j]=0.0f;
                }
            }
        }
        return res;
    }
    public static MyMatrix Add(MyMatrix matrix1,MyMatrix matrix2){//+
        MyMatrix res=new MyMatrix(matrix1.m, matrix1.n);
        for(int i=0;i<res.m;i++){
            for(int j=0;j<res.n;j++){
                res.data[i][j]=matrix1.data[i][j]+matrix2.data[i][j];
            }
        }
        return res;
    }
    public static MyMatrix Min(MyMatrix matrix1,MyMatrix matrix2){//-
        MyMatrix res=new MyMatrix(matrix1.m,matrix1.n);
        for(int i=0;i<res.m;i++){
            for(int j=0;j<res.n;j++){
                res.data[i][j]=matrix1.data[i][j]-matrix2.data[i][j];
            }
        }
        return res;
    }
    public static MyMatrix DotNumber(float Number,MyMatrix matrix){//x float
        MyMatrix res=new MyMatrix(matrix.m, matrix.n);
        for(int i=0;i<res.m;i++){
            for(int j=0;j<res.n;j++){
                res.data[i][j]=Number*matrix.data[i][j];
            }
        }
        return res;
    }
    public static MyMatrix DiveNumber(float Number,MyMatrix matrix){// ÷ float
        MyMatrix res=new MyMatrix(matrix.m, matrix.n);
        for(int i=0;i<res.m;i++){
            for(int j=0;j<res.n;j++){
                res.data[i][j]=matrix.data[i][j]/Number;
            }
        }
        return res;
    }
    public static MyMatrix AddNumber(float Number,MyMatrix matrix){
        MyMatrix res=new MyMatrix(matrix.m, matrix.n);
        for(int i=0;i<res.m;i++){
            for(int j=0;j<res.n;j++){
                res.data[i][j]=Number+matrix.data[i][j];
            }
        }
        return res;
    }
    public static MyMatrix MinNumber(float Number,MyMatrix matrix){
        MyMatrix res=new MyMatrix(matrix.m, matrix.n);
        for(int i=0;i<res.m;i++){
            for(int j=0;j<res.n;j++){
                res.data[i][j]=matrix.data[i][j]-Number;
            }
        }
        return res;
    }
    public static MyMatrix Mutiply(MyMatrix matrix1,MyMatrix matrix2){//矩阵相乘
        MyMatrix res=new MyMatrix(matrix1.m, matrix2.n);
        for(int i=0;i<matrix1.m;i++){
            for(int j=0;j<matrix2.n;j++){
                float sum=0.0f;
                for(int k=0;k<matrix1.n;k++){
                    sum+=matrix1.data[i][k]*matrix2.data[k][j];
                }
                res.data[i][j]=sum;
            }
        }
        return res;
    }
    public String toString(){
        String res="";
        for(int i=0;i<this.m;i++){
            for(int j=0;j<this.n;j++){
                res+=String.format("%f,", this.data[i][j]);
            }
            res+="\n";
        }
        return res;
    }

}
