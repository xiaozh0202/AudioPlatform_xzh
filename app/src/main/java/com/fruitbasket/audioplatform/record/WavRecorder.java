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
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Created by FruitBasket on 2017/6/5.
 */

public class WavRecorder extends Recorder {
    private static final String TAG="..WavRecorder";
    private static double eachRecordTime = 2;
    private static int num = 0;
    private static double step = 0.5;
    private static double freezeTime = 0.5;
    private static double whileTime = 24*step;
    private static double queue_length = eachRecordTime/step;
    private static final int readResultSize = 3584;
    public static int timeOfEachRecord=6000;//每一段音频的时长，单位ms
    public static Byte[] recordData = new Byte[(int) (readResultSize*whileTime)];
    public static int sleepTime=500;//检测到有动作后的线程暂停时长，单位ms

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
                    File subFile=new File(AppCondition.getAppExternalDir()+File.separator + Constents.user_path);
                    boolean state=(subFile).mkdir();
                    Log.d(TAG,"create sub dir state=="+state);
                    File subFile2=new File(AppCondition.getAppExternalDir()+File.separator + Constents.user_path+"2");
                    boolean state2=(subFile2).mkdir();

                    audioName =getRecordedFileName();
                    Constents.file_path = AppCondition.getAppExternalDir()+File.separator + Constents.user_path+File.separator+audioName + ".pcm";
                    Log.i(TAG,"get audioname!!" );
                    File audioFile;
                    DataOutputStream output;

                    if (Environment.getExternalStorageState()// 如果外存存在
                            .equals(Environment.MEDIA_MOUNTED)){
                        Log.i(TAG,"make1: if the device has got a external storage");

                        audioFile=new File(Constents.file_path);
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
                    long starttime = System.currentTimeMillis();
                    long endtime = starttime;
                    int currentWhileTime = 0;
                    int k=0;
                    Byte[] temp;
                    Queue<Byte[]> audioQueue = new LinkedList<Byte[]>();
                    while (isRecording) {
                        currentWhileTime++;
                        int readResult = audioRecord.read(buffer, 0, bufferSize);
                        if (readResult == AudioRecord.ERROR_INVALID_OPERATION) {
                            Log.e(TAG, "readState==AudioRecord.ERROR_INVALID_OPERATION");
                            return;
                        } else if (readResult == AudioRecord.ERROR_BAD_VALUE) {
                            Log.e(TAG, "readState==AudioRecord.ERROR_BAD_VALUE");
                            return;
                        } else {
                            for (int i = 0; i < readResult; i++) {
                                recordData[k++] = buffer[i];
                            }
                        }

                        //0.5s时间到
                        if(currentWhileTime>=whileTime){
                            Log.d(TAG, "run: 0.25s时间到 " +audioQueue.size());
                            //把数组放队列里
                            Byte[] recordData2 = new Byte[(int) (readResultSize*whileTime)];
                            for(int i=0;i<readResultSize*whileTime;i++)
                                recordData2[i] = recordData[i];
                            audioQueue.add(recordData2);
                            currentWhileTime = 0;//重新开始循环
                            k = 0;              //重新开始记录
                        }
                        Byte[][] tempData = new Byte[20][(int) (readResultSize*whileTime)];
                        //2s的队列更新完毕
                        if(audioQueue.size()>=queue_length){
                            Log.d(TAG, "run: 2s的队列更新完毕 " + audioQueue.size());
                            int T = 0;
                            while(audioQueue.size()>0){
                                temp = audioQueue.poll();
                                tempData[T++] = temp;   //把队列中的4个数组合并成二维数组tempData

                            }
                            //  Log.d(TAG, "display: "+audioQueue.size()+" " +Constents.file_path);
                            //change 10.15
                            String audioName2 =getRecordedFileName()+"-"+getRecordedFileName2();//文件名加入时间方便测试看
                            //change 10.15
                            File audioFile2;
                            DataOutputStream output2;
                            String file_path = AppCondition.getAppExternalDir()+File.separator + Constents.user_path+"2"+File.separator+audioName2 + ".pcm";
                            if (Environment.getExternalStorageState()// 如果外存存在
                                    .equals(Environment.MEDIA_MOUNTED)){
                                Log.i(TAG,"make1: if the device has got a external storage");

                                audioFile2=new File(file_path);
                                output2= new DataOutputStream(
                                        new BufferedOutputStream(
                                                new FileOutputStream(audioFile2)
                                        )
                                );
                            }
                            else{//否则
                                Log.i(TAG,"mark2: the device has not got a external storage");
                                String string=audioName2;
                                output2= new DataOutputStream(
                                        new BufferedOutputStream(
                                                MyApp.getContext().openFileOutput(string, Context.MODE_PRIVATE)
                                        )
                                );
                                audioFile2=MyApp.getContext().getFileStreamPath(string);
                            }

                            //change 10.15
//                            display(tempData,T,output2);
//                            output2.flush();
//                            output2.close();
                            //change 10.15

                            //add 10.15
                            if (isActing(tempData,file_path,T,output2)){//检测到动作，生成文件
                                //TODO
                                Log.d(TAG, "run: 有动作");
                                try {
                                    Thread.sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            //add 10.15
                            else{               //没有动作

                                for(int i=1;i<T;i++){       //把后三段压入队列
                                    audioQueue.add(tempData[i]);
                                }

                                Log.d(TAG, "run: 没有动作" + audioQueue.size());
                            }
                            //add 10.15
                            output2.flush();
                            output2.close();
                            //add 10.15
                        }
                        endtime = System.currentTimeMillis();
                        //change 10.14
//                        if (endtime-starttime>=timeOfEachRecord)
//                            isRecording = false;
                        //change 10.14
                    }
                    Log.d(TAG, "run: 结束了" );
                    //结束以上循环后就停止播放并释放资源
                    audioRecord.stop();
                    output.flush();
                    output.close();
                    audioRecord.release();
                    Log.i(TAG, "successful create pcm file");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }).start();
        return true;
    }
    public String getRecordedFileName2(){
        num++;
        int temp = num;
        return String.valueOf(temp);
    }
    public void display(Byte[][] bytes,int l,DataOutputStream bw) throws IOException {
        //  File file=new File("D:\\a.txt");//创建文件对象
        for(int i=0;i<l;i++){
            for(int j=0;j<bytes[i].length;j++){
                bw.write(bytes[i][j]);
            }
        }
    }
    private int addInt(Byte[] tempdata){
        int res = 0;
        res = (tempdata[0] & 0x000000FF) + (((int) tempdata[1]) << 8);
        return res;
    }
    @Override
    public boolean stop() {
        isRecording = false;
        return true;
    }

    //add 10.15
    public boolean isActing(Byte[][] bytes,String path,int T,DataOutputStream output2){
        boolean isacting=true;
        System.out.println("wavrecord:"+path);
        if(isacting)
        {
            try {
                display(bytes,T,output2);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Constents.pathqueue.add(path);
        }
        return true;
    }
    //add 10.15
}