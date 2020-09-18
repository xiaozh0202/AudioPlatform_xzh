package com.fruitbasket.audioplatform;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import uk.me.berndporr.iirj.Butterworth;


public class WaveData {
    public String path;
    public double[] data = null;
    private long SR = 0;
    public WaveData(String path) {
        this.path = path;
        this.init();
    }

    private  void init(){
        WaveFileReader reader = new WaveFileReader(path);
        this.SR = reader.getSampleRate();
        if (reader.isSuccess()) {
            data = reader.getData()[0]; //Get the first channel, data is not normalized
        }
        else {
            System.err.println(path + "not a wav");
        }
        for (int i =0;i<data.length;i++){
            data[i] /=32767.0d;
        }}

    public double[] getData(){
        return this.data;
    }

    public long getSR() {
        return this.SR;
    }
}
