package com.example.lib_test;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import uk.me.berndporr.iirj.Butterworth;

public class App {

    public static String filepath;
    public static String outputpath_org;
    public static String outputpath_com;
    public App (String file_path,String org_path,String com_path){
        filepath = file_path;
        outputpath_org = org_path;
        outputpath_com = com_path;
    }

    public static Color getColor(double power) {
        double H = power * 0.72; // Hue (note 0.4 = Green, see huge chart below)
        double S = 0.9; // Saturation
        double B = 0.7; // Brightness

        return Color.getHSBColor((float)H, (float)S, (float)B);
    }

    public  void plot() {
        // TODO Auto-generated method stub
        try {

            //get raw double array containing .WAV data
            readWAV2Array audioTest = new readWAV2Array(filepath);
            double[] rawData = audioTest.getByteArray();
//            filter function
            System.out.println(rawData.length);
            rawData = App.butter_bandpass_filter(rawData,18700,19300,44100,6);
            rawData = App.butter_stop_filter(rawData,18985,19015,44100,3);
            if (rawData.length>=70000){
                rawData = Arrays.copyOfRange(rawData, 12288,rawData.length-5121);
            }
            System.out.println(rawData.length);

//            filter function
            int length = rawData.length;
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
            double[][] plotData = new double[nX][nY];

            //apply FFT and find MAX and MIN amplitudes

            double maxAmp = Double.MIN_VALUE;
            double minAmp = Double.MAX_VALUE;

            double amp_square;

            double[] inputImag = new double[length];

            for (int i = 0; i < nX; i++){
                Arrays.fill(inputImag, 0.0);
                double[] WS_array = FFT.fft(Arrays.copyOfRange(rawData, i*windowStep, i*windowStep+WS), inputImag, true);
                for (int j = 0; j < nY; j++){
//                for (int j = 3474; j < 3587; j++){
                    amp_square = (WS_array[2*j]*WS_array[2*j]) + (WS_array[2*j+1]*WS_array[2*j+1]);
                    if (amp_square == 0.0){
                        plotData[i][j] = amp_square;
                    }
                    else{
                        plotData[i][j] = 10 * Math.log10(amp_square);
                    }

                    //find MAX and MIN amplitude
                    if(i==0&&j==0){
                        maxAmp = plotData[i][j];
                        minAmp = plotData[i][j];
                    }
                    else{
                        //find MAX and MIN amplitude
                        if (plotData[i][j] > maxAmp)
                            maxAmp = plotData[i][j];
                        else if (plotData[i][j] < minAmp)
                            minAmp = plotData[i][j];
                    }

                }
            }
//            //Normalization
//            double diff = maxAmp - minAmp;
//            for (int i = 0; i < nX; i++){
//                for (int j = 0; j < nY; j++){
//                    plotData[i][j] = (plotData[i][j]-minAmp)/diff;
//                }
//            }
//

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

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

    /**
     * 指定图形的长和宽
     *
     * @param iamgeSrc
     * @param imageDest
     * @param width
     * @param height
     * @throws IOException
     */
    public static void resizeImage(String iamgeSrc, String imageDest, int width, int height) {
        FileOutputStream outputStream = null;
        try {
            //读入文件
            File file = new File(iamgeSrc);
            // 构造Image对象
            BufferedImage src = javax.imageio.ImageIO.read(file);
            // 放大边长
            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            //绘制放大后的图片
            tag.getGraphics().drawImage(src, 0, 0, width, height, null);
            outputStream = new FileOutputStream(imageDest);
            ImageIO.write(tag, "jpg", outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
