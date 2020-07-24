package com.example.lib_test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import uk.me.berndporr.iirj.Butterworth;

public class WavToMatrix {
    public static String filepath;
    public static String txtpath;
    public static double[] data;
    public WavToMatrix (String file_path,String org_path) throws IOException {
        filepath = file_path;
        txtpath = org_path;
        matrixfile();
    }

    public void matrixfile() throws IOException {
        WaveFileReader reader = new WaveFileReader(filepath);
        if (reader.isSuccess()) {
            data = reader.getData()[0]; //Get the first channel, data is not normalized
        } else {
            System.err.println(filepath + "not a wav");
        }
        for (int i =0;i<data.length;i++){
            data[i] /=32767.0d;
        }
        data = butter_bandpass_filter(data,18700,19300,44100,6);
        data = butter_stop_filter(data,18985,19015,44100,3);
        if (data.length>=70000){
            data = Arrays.copyOfRange(data, 12289,data.length-5120);
        }
        int length = data.length;
        //initialize parameters for FFT
        int WS = 8192; //WS = window size
//            int OF = 8;    //OF = overlap factor
        int windowStep = 1024;

        //initialize plotData array
        int nX = (length-WS)/windowStep;
        int nY = WS/2+1;
        if (nX<0){
            return;
        }
        double[][] plotData = new double[nX][113];



        double amp_square;

        double[] inputImag = new double[length];

        for (int i = 0; i < nX; i++){
            Arrays.fill(inputImag, 0.0);
            double[] WS_array = FFT.fft(Arrays.copyOfRange(data, i*windowStep, i*windowStep+WS), inputImag, true);
            for (int j = 0; j < nY; j++){
                amp_square = (WS_array[2*j]*WS_array[2*j]) + (WS_array[2*j+1]*WS_array[2*j+1]);
                if(j>=3474&&j<3587){
                    if (amp_square == 0.0){
                        plotData[i][j-3474] = amp_square;
                    }
                    else{
                        plotData[i][j-3474] = 10 * Math.log10(amp_square);
                    }
                }

            }
        }

//        System.out.println(plotData.length+" "+plotData[0].length+" "+plotData[0][0]);
//        File file = new File(txtpath);
//        FileWriter out = new FileWriter(file);
//        for (int i = 0; i < nX; i++){
//            for (int j = 0; j < 113; j++) {
//                out.write(plotData[i][j] + "\t");
//            }
//        }
//        out.close();


        double[][] plotData_new;
        plotData_new = resize(plotData);
//        System.out.println(plotData_new.length+" "+plotData_new[0].length+" "+plotData_new[0][0]);
        File file = new File(txtpath);
        FileWriter out = new FileWriter(file);
        for (int i = 0; i < 113; i++){
            for (int j = 0; j < 113; j++) {
                out.write(plotData_new[i][j] + "\t");
            }
        }
//        System.out.print(plotData_new.length+" "+plotData_new[0].length+" "+plotData_new[0][0]+" ");
        out.close();
    }

    /**
     *
     * @param data   data need to filter
     * @param lowcut    low cutoff frequency
     * @param highcut   high cutoff frequency
     * @param sampleRate sampleRate
     * @param order  butter filter's order
     * @return   data after filter
     */
    public static double[] butter_bandpass_filter(double[] data, int lowcut, int highcut, int sampleRate, int order) {
        Butterworth butterworth = new Butterworth();
        double widthFrequency = highcut - lowcut;
        double centerFrequency = (highcut + lowcut) / 2;
        butterworth.bandPass(order, sampleRate, centerFrequency, widthFrequency);
        double[] list = new double[data.length];
        int in = 0;
        for (double v : data) {
            double f = butterworth.filter(v);
            list[in] = f;
            in++;
        }
        return list;
    }

    public static double[] butter_stop_filter(double[] data, int lowcut, int highcut, int sampleRate, int order) {
        Butterworth butterworth = new Butterworth();
        double widthFrequency = highcut - lowcut;
        double centerFrequency = (highcut + lowcut) / 2;
        butterworth.bandStop(order, sampleRate, centerFrequency, widthFrequency);
        double[] list = new double[data.length];
        int in = 0;
        for (double v : data) {
            double f = butterworth.filter(v);
            list[in] = f;
            in++;
        }
        return list;
    }

    public static double[][] resize(double[][] org_arr){
        double[][] new_arr = new double[113][113];
        int time_lenth = org_arr.length;
        if(time_lenth<113){
            int one_dis = (113/(Math.abs(time_lenth-113)+1));
            for(int i =0;i<113;i++){
                for (int j = 0; j < 113; j++) {
                    new_arr[i][j] = org_arr[i - i / one_dis][j];
                }
            }
        }
        else if (time_lenth>113){
            int one_dis = (time_lenth/(Math.abs(time_lenth-113)));
            for(int i =0,k=0;i<113;i++,k++){
                if(k%one_dis==0&&k/one_dis>0&&k/one_dis<=Math.abs(time_lenth-113)){
                    k++;
                }
                for (int j = 0; j < 113; j++){
                    new_arr[i][j] = org_arr[k][j];
                }
            }
        }
        else {
            return org_arr;
        }

        return new_arr;
    }

}
