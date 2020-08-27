package com.example.lib_test;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.opencv.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

import org.bytedeco.javacpp.BytePointer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import uk.me.berndporr.iirj.Butterworth;


public class MyClass {
    public static double[] data;
    public static void main(String[] args) throws IOException {
        int[] reslut = {9,8,7,6,5,4,3,2,1,2,3,4,5,6};
        int[] temp = new int[reslut.length];
        System.arraycopy(reslut,0,temp,0,reslut.length);
        Arrays.sort(temp);
//        System.out.println(temp[temp.length-1]);
//        System.out.println(temp[temp.length-2]);
        int max = temp[temp.length-2];
        int index  = Arrays.binarySearch(reslut,max);
        System.out.println(index+Arrays.toString(temp));
//        int[] max = {temp[0],temp[0],temp[0]};
//        int[] index = {0,0,0};
//        for(int num=1;num<temp.length;num++){
//            if (temp[num]>=max[0]){
//                max[2] = max[1];
//                index[2] =index[1];
//                max[1] = max[0];
//                index[1] =index[0];
//                max[0] = temp[num];
//                index[0] = num;
//            }
//            else if(temp[num]>=max[1]){
//                max[2] = max[1];
//                index[2] =index[1];
//                max[1] = temp[num];
//                index[1] = num;
//            }
//            else if(temp[num]>=max[2]){
//                max[2] = temp[num];
//                index[2] =num;
//            }
//        }
//        System.out.println(Arrays.toString(max));
//        System.out.println(Arrays.toString(index));
//        String pic_path = "D:\\ALL_WORK\\AcouDigits\\code\\AudioPlatform\\lib_test\\src\\data_temp\\rec2018-12-27_22h33m44.298s.png";
//        int val = -13646429;
//        val = Math.abs(val);
//        System.out.println(((val & 0x000000ff) )/255 );
//        System.out.println(((val & 0x0000ff00)>> 8)/255) ;
//        System.out.println(((val & 0x00ff0000)>> 16)/255) ;
//        System.out.println(((val )>> 24)) ;

//        WavToMatrix apptest = new WavToMatrix(filepath,"D:\\ALL_WORK\\AcouDigits\\code\\AudioPlatform\\lib_test\\src\\data_temp\\9998.txt");
//        String filepath = "G:\\文档\\Tencent Files\\1592216581\\FileRecv\\MobileFile\\2020-06-11_17h-47m-20s._Watch17.wav";
//        WavToMatrix apptest = new WavToMatrix(filepath,"G:\\all_android_projects\\AudioPlatform_IntegrateWithTencent\\lib_test\\src\\data_temp\\hsc1.txt");
//        WaveFileReader reader = new WaveFileReader(filepath);
//        if (reader.isSuccess()) {
//            data = reader.getData()[0]; //Get the first channel, data is not normalized
//        } else {
//            System.err.println(filepath + "not a wav");
//        }
//        long sr = reader.getSampleRate();


//        System.out.println(getMatElement(mat_new,4,3,0));
//        String filepath = "lib_test/src/data_temp/test_audio_3.wav";
//        String outputpath = "org.jpg";
//        String temp_path = "com.jpg";
//        App apptest = new App(filepath,outputpath,temp_path);
//        apptest.plot();

    }

}
