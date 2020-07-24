package com.fruitbasket.audioplatform.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.fruitbasket.audioplatform.AppCondition;
import com.fruitbasket.audioplatform.AudioService;
import com.fruitbasket.audioplatform.Constents;
import com.fruitbasket.audioplatform.R;
import com.fruitbasket.audioplatform.WaveData;
import com.fruitbasket.audioplatform.WaveProducer;
import com.fruitbasket.audioplatform.play.Player;
import com.fruitbasket.audioplatform.play.WavePlayer;
import com.fruitbasket.audioplatform.play2.GlobalConfig;
import com.fruitbasket.audioplatform.record.PredictWav;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.chaquo.python.android.AndroidPlatform;
import com.chaquo.python.Python;


final public class MainActivity extends Activity {
    private static final String TAG=".MainActivity";

    private ToggleButton waveProducerTB;
    private SeekBar waveRateSB;
    private ToggleButton recorderTB;



    private Interpreter tflite = null;
    private boolean load_result = false;
    private TextView result_text;
    private List<String> resultLabel = new ArrayList<>();
    private TextView batterLevel;
    private BroadcastReceiver batteryLevelRcvr;
    private IntentFilter batteryLevelFilter;


    private int channelOut= Player.CHANNEL_OUT_BOTH;
    //private int channelIn= AudioFormat.CHANNEL_IN_MONO;
    private int channelIn = AudioFormat.CHANNEL_IN_STEREO;
    private int waveRate;//声波的频率

    public  int iBeginHz = 19000;
    public  int iStepHz=0;
    public  int ifreqNum = 1;
    public  int iSimpleHz = 44100;

    private Intent intent;
    private AudioService audioService;
    private ServiceConnection serviceConnection=new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG,"ServiceConnection.onServiceConnection()");
            audioService =((AudioService.AudioServiceBinder)binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG,"ServiceConnection.onServiceDisConnection()");
            audioService =null;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate()");
        setContentView(R.layout.activity_main);
        result_text = (TextView) findViewById(R.id.result_text);
//        batterLevel = (TextView)findViewById(R.id.batteryLevel);
        readCacheLabelFromLocalFile();
        load_model("MNmodel_v33");
//        monitorBatteryState();
        initializeViews();
        initPython();
        intent=new Intent(this,AudioService.class);
        if(audioService ==null) {
            Log.i(TAG,"begin to bind service");
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

    }

    // 初始化Python环境
    void initPython(){
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }

    void callpythoncode(){
        Python py = Python.getInstance();
        PyObject obj1 = py.getModule("test_code").callAttr("add",2,3);
        Integer sum = obj1.toJava(Integer.class);
        Log.i(TAG,"python's add = " + sum.toString());
    }


    @Override
    protected void onStart(){
        super.onStart();
        Log.i(TAG,"onStart()");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i(TAG,"onResume()");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.i(TAG,"onPause()");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.i(TAG,"onStop()");
    }

    @Override
    protected void onDestroy(){
        Log.i(TAG,"onDestroy()");
        //在断开声音服务前，释放播放器资源
        if(audioService!=null){
            audioService.releasePlayer();
        }
        unbindService(serviceConnection);
        stopService(intent);//must stop the Service
        super.onDestroy();
    }

    private void initializeViews(){


        ToggleCheckedChangeListener tcListener=new ToggleCheckedChangeListener();
//        channelOut= WavePlayer.CHANNEL_OUT_RIGHT;
        channelOut= WavePlayer.CHANNEL_OUT_LEFT;

        waveProducerTB =(ToggleButton)findViewById(R.id.wave_player_tb);
        waveProducerTB.setOnCheckedChangeListener(tcListener);

//        waveRateTV =(TextView)findViewById(R.id.waverate_tv);

        waveRateSB =(SeekBar)findViewById(R.id.waverate_sb);
        waveRateSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                waveRate =progress*1000;
//                waveRateTV.setText(getResources().getString(R.string.frequency,progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        waveRate = waveRateSB.getProgress()*1000;
//        waveRateTV.setText(getResources().getString(R.string.frequency,waveRateSB.getProgress()));

        channelIn=AudioFormat.CHANNEL_IN_MONO;
//        channelIn = AudioFormat.CHANNEL_IN_STEREO;

        recorderTB=(ToggleButton)findViewById(R.id.recorder_tb);
        recorderTB.setOnCheckedChangeListener(tcListener);
    }

    private class ToggleCheckedChangeListener implements CompoundButton.OnCheckedChangeListener{
        private static final String TAG="...TCCListener";

        @Override
        public void onCheckedChanged(CompoundButton button,boolean isChecked){
            switch (button.getId()) {
                case R.id.wave_player_tb:
                    if (isChecked) {
                        startPlayWav();
                    } else {
                        stopPlay();
                    }
                    break;
                case R.id.recorder_tb:
                    if (isChecked) {
                        startRecordWav();
                    } else {
                        stopRecord();
                    }
                    break;
                default:
                    Log.w(TAG,"onClick(): id error");
            }
        }

        private void startPlayWav(){
            Log.i(TAG,"starPlayWav()");
            if(audioService !=null){

                GlobalConfig.START_FREQ=iBeginHz;
                GlobalConfig.FREQ_INTERVAL=iStepHz;
                GlobalConfig.NUM_FREQ=ifreqNum;
                GlobalConfig.stPhaseProxy.init();//处理相位数据

                audioService.startPlayWav(channelOut, waveRate, WaveProducer.COS, iSimpleHz, iBeginHz, iStepHz, ifreqNum);
            }
            else{
                Log.w(TAG,"audioService==null");
            }
        }

        private void stopPlay(){
            Log.i(TAG,"stopPlay()");
            if(audioService !=null){
                audioService.stopPlay();
            }
            else{
                Log.w(TAG,"audioService==null");
            }
        }

        private void startRecordWav(){
            Log.i(TAG,"startRecordWav()");
            if(audioService!=null){
                audioService.startRecordWav(
                        channelIn,
                        AppCondition.DEFAULE_SIMPLE_RATE,
                        AudioFormat.ENCODING_PCM_16BIT
                );
            }
            else{
                Log.w(TAG,"audioService==null");
            }

        }

        private void stopRecord(){
            Log.i(TAG,"stopRecord()");
            if(audioService!=null){
                audioService.stopRecord();
//            added code6.11
                Log.i(TAG,"path is :" + Constents.file_path);
                PredictWav(Constents.file_path);
//            added code6.11
            }
            else{
                Log.w(TAG,"audioService==null");
            }

        }

        private void startTest(){
            Log.i(TAG,"startTest()");
            if(audioService!=null){
                audioService.startRecordTest();
            }
            else{
                Log.w(TAG,"audioService==null");
            }
        }

        private void stopTest(){
            Log.i(TAG,"stopTest()");
            if(audioService!=null){
                audioService.stopRecord();
            }
            else{
                Log.w(TAG,"audioService==null");
            }
        }
    }


//    added code6.10
    /**
     * Memory-map the model file in Assets.
     */
    private MappedByteBuffer loadModelFile(String model) throws IOException {
        AssetFileDescriptor fileDescriptor = getApplicationContext().getAssets().openFd(model + ".tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // load infer model
    private void load_model(String model) {
        try {
            tflite = new Interpreter(loadModelFile(model).asReadOnlyBuffer());
            Toast.makeText(MainActivity.this, model + " model load success", Toast.LENGTH_SHORT).show();
            Log.d(TAG, model + " model load success");
//            tflite.setNumThreads(4);
            load_result = true;
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, model + " model load fail", Toast.LENGTH_SHORT).show();
            Log.d(TAG, model + " model load fail");
            load_result = false;
            e.printStackTrace();
        }
    }

    public static ByteBuffer getScaledMatrix(double[][] fft_data) {
        ByteBuffer imgData = ByteBuffer.allocateDirect(113*113*4);
        imgData.order(ByteOrder.nativeOrder());
        for (int i = 0; i < 113; ++i) {
            for (int j = 0; j < 113; ++j) {
                final double val = fft_data[i][j];
                imgData.putFloat((float) val);
            }
        }
        return imgData;
    }

    // get max probability label
    // predict image
    private int[] get_max_result(float[] result) {
        float probability = result[0];
        float sepro = result[0];
        int[] r = {0,0};
        for (int i = 0; i < result.length; i++) {
            if (probability < result[i]) {
                r[1] = r[0];
                sepro = probability;
                probability = result[i];
                r[0] = i;
            }
            else if(sepro<result[i]){
                r[1] = i;
                sepro = result[i];
            }
        }
        return r;
    }

    private void readCacheLabelFromLocalFile() {
        try {
            AssetManager assetManager = getApplicationContext().getAssets();
            BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open("labels.txt")));
            String readLine = null;
            while ((readLine = reader.readLine()) != null) {
                resultLabel.add(readLine);
            }
            reader.close();
        } catch (Exception e) {
            Log.e("labelCache", "error " + e);
        }
    }

//    private void monitorBatteryState(){
//        batteryLevelRcvr = new BroadcastReceiver(){
//
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                // TODO Auto-generated method stub
//                StringBuilder sb = new StringBuilder();
//                double rawlevel = intent.getIntExtra("level", -1);
//                int scale = intent.getIntExtra("scale", -1);
////                int level = -1;
////                if(rawlevel >= 0 && scale > 0){
////                    level = (rawlevel*100)/scale;
////                }
//                sb.append("电池电量: ");
////                sb.append(level + "%\n" + rawlevel+"/"+scale);
//                sb.append(rawlevel+"/"+scale);
//
//                batterLevel.setText(sb.toString());
//            }
//
//        };
//        batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//        registerReceiver(batteryLevelRcvr, batteryLevelFilter);
//    }

    private void PredictWav(String path){
        File f = new File(path);
        while (true){
            if(f.exists()){
                Toast.makeText(MainActivity.this, path + " make success", Toast.LENGTH_SHORT).show();
//                WaveData reader = new WaveData("/storage/emulated/0/AcouDigits/2020-06-14_16h-07m-31s.wav");
                WaveData reader = new WaveData(path);
                double[][] tempdata = reader.getData();
                ByteBuffer inputData =getScaledMatrix(tempdata);
                try {
                    float[][] labelProbArray = new float[1][10];
                    long start = System.currentTimeMillis();
                    // get predict result
                    tflite.run(inputData, labelProbArray);
                    long end = System.currentTimeMillis();
                    long time = end - start;
                    float[] results = new float[labelProbArray[0].length];
                    System.arraycopy(labelProbArray[0], 0, results, 0, labelProbArray[0].length);
                    // show predict result and time
                    int[] r = get_max_result(results);
                    String show_text = "You might write：\n" + resultLabel.get(r[0]) +"\t\t\t\t\t\t\tProbability:\t\t"+ results[r[0]]*100+"%\n\nPredict Used:"+time + "ms"+ "\n\nMake wav file Used:"+Constents.makewavfiletime + "ms";
                    result_text.setText(show_text);
                    callpythoncode();//添加行
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                monitorBatteryState();
                break;
            }
        }
    }

//    added code6.10
}
