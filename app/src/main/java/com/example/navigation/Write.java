package com.example.navigation;

import android.widget.Toast;

import java.io.*;
import java.util.LinkedList;

public class Write {
    public static void writeContext(String path,String[] context) throws IOException {
        String[] filename=new String[]{
                "Acceleration",
                "Gyromagnetic",
                "Magnetic Filed",
                "Orientation(System)",
                "Orientation(Custom)"
        };
        File[] saveFile=new File[]{
                new File(path,filename[0]+".txt"),
                new File(path,filename[1]+".txt"),
                new File(path,filename[2]+".txt"),
                new File(path,filename[3]+".txt"),
                new File(path,filename[4]+".txt")
        };
        for(int i=0;i<saveFile.length;i++){
            if(!saveFile[i].exists()) {
                int count = 1;
                //saveFile[i].createNewFile();
                while (true){
                    try{
                        saveFile[i].createNewFile();
                    }catch(Exception e){
                    }
                    if(!saveFile[i].exists()){
                        saveFile[i]=new File(path, String.format("%s_%d.txt", filename[i], count));
                        count++;
                    }else{
                        break;
                    }
                    if(count>=100){
                        Toast.makeText(MainActivity.context,"保存失败，重新命名文件夹",Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
            if(saveFile[i].exists()){
                FileWriter fileWriter=new FileWriter(saveFile[i]);
                fileWriter.write(context[i]);
                fileWriter.close();
                Toast.makeText(MainActivity.context,String.format("%s文件保存成功",filename[i]),Toast.LENGTH_SHORT).show();
            }
        }
    }
}
