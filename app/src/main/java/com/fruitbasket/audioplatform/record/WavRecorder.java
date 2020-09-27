package com.fruitbasket.audioplatform.record;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.fruitbasket.audioplatform.AppCondition;
import com.fruitbasket.audioplatform.Constents;
import com.fruitbasket.audioplatform.MyApp;
import com.fruitbasket.audioplatform.WavHeader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;



/**
 * Created by FruitBasket on 2017/6/5.
 */

public class WavRecorder extends Recorder {
    private static final String TAG="..WavRecorder";

//    public static ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
//    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
//    public static int[] pcmdata = null;

    private boolean isRecording;
    public static String audioName;//录音文件的名字

    public String subDir;//用于存放录音文件的子目录

    public static String path = null;

    public WavRecorder(){
        super();
    }

    public WavRecorder(int channelIn, int sampleRate, int encoding){
        super(channelIn,sampleRate,encoding);
    }

    @Override
    public boolean start() {
        Log.i(TAG,"start()");
        //使用异步的方法录制音频
        new Thread(new Runnable() {
            @Override
            public void run() {

                int bufferSize = AudioRecord.getMinBufferSize(
                        sampleRate,
                        channelIn,
                        AudioFormat.ENCODING_PCM_16BIT);
                if (bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "recordingBufferSize==AudioRecord.ERROR_BAD_VALUE");
                    return;
                } else if (bufferSize == AudioRecord.ERROR) {
                    Log.e(TAG, "recordingBufferSize==AudioRecord.ERROR");
                    return;
                }
                byte[] buffer = new byte[bufferSize];

                try {
                    //创建子目录
                    File subFile=new File(AppCondition.getAppExternalDir()+File.separator);
                    boolean state=(subFile).mkdir();
                    Log.d(TAG,"create sub dir state=="+state);

                    audioName =getRecordedFileName();
                    Constents.file_path = AppCondition.getAppExternalDir()+File.separator+audioName + ".wav";
                    Log.i(TAG,"get audioname!!" );
                    File audioFile;
                    DataOutputStream output;

                    if (Environment.getExternalStorageState()// 如果外存存在
                            .equals(Environment.MEDIA_MOUNTED)){
                        Log.i(TAG,"make1: if the device has got a external storage");

                        audioFile=new File(AppCondition.getAppExternalDir()+File.separator+audioName);
                        output= new DataOutputStream(
                                new BufferedOutputStream(
                                        new FileOutputStream(audioFile)
                                )
                        );
                    }
                    else{//否则
                        Log.i(TAG,"mark2: the device has not got a external storage");
                        String string=audioName;
                        output= new DataOutputStream(
                                new BufferedOutputStream(
                                        MyApp.getContext().openFileOutput(string, Context.MODE_PRIVATE)
                                )
                        );
                        audioFile=MyApp.getContext().getFileStreamPath(string);
                    }

                    AudioRecord audioRecord = new AudioRecord(
                            MediaRecorder.AudioSource.MIC,
                            sampleRate,
                            channelIn,
                            encoding,
                            bufferSize);
                    audioRecord.startRecording();

                    isRecording = true;
                    while (isRecording) {

                        int readResult = audioRecord.read(buffer, 0, bufferSize);
                        if (readResult == AudioRecord.ERROR_INVALID_OPERATION) {
                            Log.e(TAG, "readState==AudioRecord.ERROR_INVALID_OPERATION");
                            return;
                        } else if (readResult == AudioRecord.ERROR_BAD_VALUE) {
                            Log.e(TAG, "readState==AudioRecord.ERROR_BAD_VALUE");
                            return;
                        } else {
                            for (int i = 0; i < readResult; i++) {
                                output.writeByte(buffer[i]);
//                                bos.write(buffer[i]);
                            }
                        }
                    }



                    //结束以上循环后就停止播放并释放资源
                    audioRecord.stop();
                    output.flush();
                    output.close();
                    audioRecord.release();
                    audioRecord = null;
                    long start = System.currentTimeMillis();
                    // get predict result

                    Log.i(TAG, "begin to make wav file");
                    //制作wav文件
                    ///这里先将原始音频保存起来，在改装成wav文件，这不是一个好做法
                    BufferedInputStream inputStream;
                    BufferedOutputStream outputStream;
                    int length;
                    if(Environment.getExternalStorageState()//如果外存存在
                            .equals(Environment.MEDIA_MOUNTED)){
                        Log.i(TAG,"the device has got a external storage");

                        FileInputStream fis = new FileInputStream(audioFile);
                        inputStream= new BufferedInputStream(fis);

                        outputStream = new BufferedOutputStream(
                                new FileOutputStream(AppCondition.getAppExternalDir()+File.separator+audioName + ".wav")
//                        new FileOutputStream(AppCondition.getAppExternalDir()+File.separator+subDir+File.separator+audioName + ".wav")
                        );
                        length= (int) fis.getChannel().size();
                    }
                    else{//否则
                        String string=audioName;
//                        String string=subDir+File.separator+audioName;
                        FileInputStream fis=MyApp.getContext().openFileInput(string);
                        inputStream= new BufferedInputStream(fis);

                        outputStream=new BufferedOutputStream(
                                MyApp.getContext().openFileOutput(string+".wav",Context.MODE_PRIVATE)
                        );
                        length=(int)fis.getChannel().size();

                    }

                    byte[] readBuffer = new byte[1024];

                    Log.i(TAG, "create a wav file header");
                    WavHeader wavHeader = new WavHeader();
                    wavHeader.setAdjustFileLength(length - 8);
                    wavHeader.setAudioDataLength(length - 44);
                    wavHeader.setBlockAlign(channelIn, encoding);
                    wavHeader.setByteRate(channelIn, sampleRate, encoding);
                    wavHeader.setChannelCount(channelIn);
                    wavHeader.setEncodingBit(encoding);
                    wavHeader.setSampleRate(sampleRate);
                    wavHeader.setWaveFormatPcm(WavHeader.WAV_FORMAT_PCM);

                    outputStream.write(wavHeader.getHeader());
//                    音频文件的转移，从pcm转成wav文件，可以考虑直接将pcm的数据保存进行使用
                    while (inputStream.read(readBuffer) != -1) {
                        outputStream.write(readBuffer);
                    }
                    inputStream.close();
                    outputStream.close();
                    audioFile.delete();//删除原始的pcm文件
                    Log.i(TAG, "successful create wav file");

                    long end = System.currentTimeMillis();
                    Constents.makewavfiletime = end - start;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }).start();
        return true;
    }

    @Override
    public boolean stop() {
        isRecording = false;
        return true;
    }
}