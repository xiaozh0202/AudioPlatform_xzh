package com.example.lib_test;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.opencv.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

import org.bytedeco.javacpp.BytePointer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import uk.me.berndporr.iirj.Butterworth;


public class MyClass {
    public static double[] data;
    public static void main(String[] args) throws IOException {
        String filepath = "D:\\ALL_WORK\\AcouDigits\\code\\AudioPlatform\\lib_test\\src\\data_temp\\2020-07-12_10h-49m-00s.wav";
        WavToMatrix apptest = new WavToMatrix(filepath,"D:\\ALL_WORK\\AcouDigits\\code\\AudioPlatform\\lib_test\\src\\data_temp\\9998.txt");
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
