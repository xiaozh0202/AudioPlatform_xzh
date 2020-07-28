package com.example.lib_test;


import org.datavec.audio.Wave;
import org.datavec.audio.extension.Spectrogram;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import uk.me.berndporr.iirj.Butterworth;



public class waveDataProcess {
    public static int order = 6;
    public static int freq_low = 17200;
    public static int freq_high = 17800;
    public static double[] data;
    public static void main(String[] args)throws UnsupportedAudioFileException,
            LineUnavailableException, IOException {
        String filename = "G:\\all_android_projects\\AudioPlatform_IntegrateWithTencent\\lib_test\\src\\data_temp\\test_audio_3.wav";
        String txtfilename = "G:\\all_android_projects\\AudioPlatform_IntegrateWithTencent\\lib_test\\src\\data_temp\\audio_33.txt";
        WavToMatrix testclass = new WavToMatrix(filename,txtfilename);
    }
}
