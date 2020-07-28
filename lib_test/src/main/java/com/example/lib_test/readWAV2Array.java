package com.example.lib_test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class readWAV2Array {

    private byte[] entireFileData;

    //SR = sampling rate
    public double getSR(){
        ByteBuffer wrapped = ByteBuffer.wrap(Arrays.copyOfRange(entireFileData, 24, 28)); // big-endian by default
        double SR = wrapped.order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        return SR;
    }

    public readWAV2Array(String filepath) throws IOException{
        Path path = Paths.get(filepath);
        this.entireFileData = Files.readAllBytes(path);
    }

    public double[] getByteArray (){
        byte[] data_raw = Arrays.copyOfRange(entireFileData, 44, entireFileData.length);
        int totalLength = data_raw.length;

        //declare double array for mono
        int new_length = totalLength / 2;
        double[] data_mono = new double[new_length];
        for (int i = 0; i < new_length; i++){
            data_mono[i] = ((int)(data_raw[2*i+1]) << 8) | (data_raw[2*i] & 0xff);
            data_mono[i] /= 32767.0d;
        }
        return data_mono;
    }

}
