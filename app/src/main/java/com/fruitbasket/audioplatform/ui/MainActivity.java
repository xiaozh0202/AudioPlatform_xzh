package com.fruitbasket.audioplatform.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.chaquo.python.android.AndroidPlatform;
import com.fruitbasket.audioplatform.AppCondition;
import com.fruitbasket.audioplatform.AudioService;
import com.fruitbasket.audioplatform.Constents;
import com.fruitbasket.audioplatform.R;
import com.fruitbasket.audioplatform.WaveProducer;
import com.fruitbasket.audioplatform.play.Player;
import com.fruitbasket.audioplatform.play.WavePlayer;
import com.fruitbasket.audioplatform.play2.GlobalConfig;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;

import com.chaquo.python.Python;


final public class MainActivity extends Activity implements View.OnClickListener{
    private static final String TAG=".MainActivity";

    private ToggleButton waveProducerTB;
    private SeekBar waveRateSB;
    private ToggleButton recorderTB;
    private TextView inputOption;
    private Button deleteOne;
    private Button deleteAll;
    private Button blankspace;
    private Button comma;
    private Button end;
    private EditText write_box;
    private HorizontalScrollView horizontalScrollView;
    private LinearLayout container;
    private String[] showOption = new String[5];
    private ArrayList<String> data;

    private Interpreter tflite = null;
    private boolean load_result = false;

    private List<String> resultLabel = new ArrayList<>();
    private double[][] save_result;
    private int save_times=0;
    //add 10.15
    public boolean ispredicting;
    PredictHandler1 predictHandler1=new PredictHandler1();
    PredictHandler2 predictHandler2=new PredictHandler2();
    // PredictHandler3 predictHandler3=new PredictHandler3();
    //add 10.15
    private int channelOut= Player.CHANNEL_OUT_BOTH;
    private int channelIn= AudioFormat.CHANNEL_IN_MONO;
    //    private int channelIn = AudioFormat.CHANNEL_IN_STEREO;
    private int waveRate;//声波的频率

    public  int iBeginHz = 19000;
    public  int iStepHz=0;
    public  int ifreqNum = 1;
    public  int iSimpleHz = 44100;
    private int current_state;
    private int model_index = 0;
    private int[] ddims = {1, 3, 56, 56};
    private static final String[] PADDLE_MODEL = {
            "model_v79"
    };
    private String[] texttype = {"digits","letter"};

    private Python py;
    private PyObject obj1;
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
        //1121
        readCacheLabelFromLocalFile();
        load_model(PADDLE_MODEL[0]);
        initializeViews();
        //1121
        long start1 = System.currentTimeMillis();
        initPython();
        this.obj1=this.py.getModule("function");
        long end1 = System.currentTimeMillis();
        long time = end1 - start1;
        Log.d(TAG,"time used :"+ time);
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
        this.py=Python.getInstance();
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

        horizontalScrollView = (HorizontalScrollView) findViewById(R.id.word_basket);
        container = (LinearLayout) findViewById(R.id.ItemContainer);
        write_box = (EditText) findViewById(R.id.write_box);
        blankspace = (Button) findViewById(R.id.blankspace_button);
        comma = (Button) findViewById(R.id.comma_button);
        end = (Button) findViewById(R.id.end_button);
        deleteAll = (Button) findViewById(R.id.deletAll_button);
        deleteOne = (Button) findViewById(R.id.backspace_button);
        blankspace.setOnClickListener(this);
        comma.setOnClickListener(this);
        end.setOnClickListener(this);
        deleteOne.setOnClickListener(this);
        deleteAll.setOnClickListener(this);
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
        //code change 2020.9.27
    }
    //将字符串数组与集合绑定起来
    private void bindData()
    {

        Collections.addAll(data, showOption);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.blankspace_button:
                addText(" ");
                break;
            case R.id.comma_button:
                addText("，");
                break;
            case R.id.end_button:
                addText("。");
                break;
            case R.id.backspace_button:
                deleteText();
                break;
            case  R.id.deletAll_button:
                write_box.setText("");
                break;
        }
    }
    private  void deleteText(){
        Log.d(TAG, "deleteText: ");
        String S =write_box.getText().toString() ;
        if(S.equals("")||S==null)
            return;
        write_box.setText(S.substring(0,S.length()-1));
    }
    private void addText(String s){
        Log.d(TAG, "addText: " + s);
        String S =write_box.getText().toString() + s ;
        write_box.setText(S);
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
                        //1121
                        Constents.user_path = "Acoudigits";
                        //1121
                        startRecordWav();
                    } else {
                        try {
                            stopRecord();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
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
            // add 10.14
            GetPredictPath();
            //add 10.14
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

        private void stopRecord() throws FileNotFoundException {
            Log.i(TAG,"stopRecord()");
            //add 10.14
            ispredicting=false;
            //add 10.14
            if(audioService!=null){
                audioService.stopRecord();
//            added code6.11
                Log.i(TAG,"path is :" + Constents.file_path);

            }
            else{
                Log.w(TAG,"audioService==null");
            }

        }


        //add 10.14
        public void GetPredictPath()
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ispredicting=true;
                    while(ispredicting)
                    {
                        String current_path=Constents.pathqueue.poll();
                        if(current_path!=null)
                        {
                            System.out.println("mainactivity11111:"+current_path);
                            if(current_state==0) {
                                double[] A=PredictWav(current_path);
                                setLanguageResult(A);
                            }
                            else{
                                PredictWav(current_path);
                                String showOption= "";
                                Message message2=new Message();
                                message2.what=200;
                                message2.obj=showOption;
                                predictHandler2.sendMessage(message2);
                            }


                        }

                    }
                }
            }).start();
        }
        //add 10.14

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


    public static ByteBuffer getScaledMatrix(Bitmap bitmap, int[] ddims) {
        ByteBuffer imgData = ByteBuffer.allocateDirect(ddims[0] * ddims[1] * ddims[2] * ddims[3] * 4);
        imgData.order(ByteOrder.nativeOrder());
        // get image pixel
        int[] pixels = new int[ddims[2] * ddims[3]];
        Bitmap bm = Bitmap.createScaledBitmap(bitmap, ddims[2], ddims[3], false);
        bm.getPixels(pixels, 0, bm.getWidth(), 0, 0, ddims[2], ddims[3]);
        int pixel = 0;
//        Log.i(TAG,"first pixel is :" +pixels[0]);
        for (int i = 0; i < ddims[2]; ++i) {
            for (int j = 0; j < ddims[3]; ++j) {
                final int val = pixels[pixel++];
                imgData.putFloat(((val & 0x000000ff) )/255f);
                imgData.putFloat(((val & 0x0000ff00)>>8 )/255f);
                imgData.putFloat(((val & 0x00ff0000) >>16)/255f);
//                Log.i(TAG,"rgb:" +((val & 0x000000ff) )/255f+ " ,"+ ((val & 0x0000ff00)>>8 )/255f + " ," + ((val & 0x00ff0000) >>16)/255f);
            }
        }
//
        if (bm.isRecycled()) {
            bm.recycle();
        }
        return imgData;
    }
    //
//    // compress picture
    public static Bitmap getScaleBitmap(String filePath) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opt);

        int bmpWidth = opt.outWidth;
        int bmpHeight = opt.outHeight;
        int maxSize = 500;
        // compress picture with inSampleSize
        opt.inSampleSize = 1;
        while (true) {
            if (bmpWidth / opt.inSampleSize < maxSize || bmpHeight / opt.inSampleSize < maxSize) {
                break;
            }
            opt.inSampleSize += 2;
        }
        opt.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, opt);
    }


    // get max probability label
    // predict image
    private int[] get_max_result(double[] result) {
        double [] temp = new double[result.length];
        System.arraycopy(result, 0, temp, 0, result.length);
        Arrays.sort(temp);
        int[] top_five = new int[5];
        for(int i=0;i<5;i++){
            double max = temp[result.length-i-1];
            for(int j=0;j<result.length;j++){
                if (result[j]==max){
                    top_five[i] = j;
                }
            }
        }
        return top_five;
    }


    //change 11.8
    private void readCacheLabelFromLocalFile() {

        save_times=0;
        save_result=new double[15][];
        model_index = 0;
        current_state=0;
        try {
            resultLabel.clear();
            AssetManager assetManager = getApplicationContext().getAssets();
            String labelFileName="26labels.txt";
            if(current_state==1)
                labelFileName= "30labels.txt";
            BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelFileName)));
            String readLine = null;
            while ((readLine = reader.readLine()) != null) {
                resultLabel.add(readLine);
            }
            reader.close();
        } catch (Exception e) {
            Log.e("labelCache", "error " + e);
        }
    }

    //add 10.15
    public class PredictHandler1 extends Handler{
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 100) {
                String res = (String) msg.obj;
                Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class PredictHandler2 extends Handler{
        @Override
        public void handleMessage(Message msg) {
            //add 11.8
            if(msg.what==200){
                data = new ArrayList<>();
                bindData();
                bindHZSWData();
            }
        }
    }
    //将集合中的数据绑定到HorizontalScrollView上
    private void bindHZSWData()
    {	//为布局中textview设置好相关属性
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(20, 10, 20, 10);
        container.removeAllViews();

        for (int index = 0; index< data.size();index++)
        {
            TextView textView = new TextView(this);
            textView.setText(data.get(index));
            textView.setTextSize(16);
            //textView.setTextColor(Color.WHITE);
            textView.setLayoutParams(layoutParams);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectWord(view);
                }
            });
            container.addView(textView);
            container.invalidate();
        }
    }
    private void selectWord(View view){
        String S =write_box.getText().toString() +( (TextView) view).getText().toString();
        write_box.setText(S);

    }

    private double[] PredictWav(String path){
        File f = new File(path);
        int currentsum=10;
        if(current_state==0)
            currentsum=26;
        double[] new_resluts=null;
        while (true){
            if(f.exists()){

                new_resluts= new double[currentsum];
                //change 10.15
                String str=path + " make success";
                Message message1=new Message();
                message1.what=100;
                message1.obj=str;
                predictHandler1.sendMessage(message1);
                //change 10.15
                long start1 = System.currentTimeMillis();
                String pic_path = path.substring(0,path.length()-4)+ ".png";
                this.obj1.callAttr("wav2picture",new Kwarg("wav_path", path),new Kwarg("pic_path", pic_path));
                long end1 = System.currentTimeMillis();
                long time = end1 - start1;
                Log.i(TAG,"python plot fft picture time used :" +time);
                Bitmap bmp = getScaleBitmap(pic_path);
                ByteBuffer inputData = getScaledMatrix(bmp, ddims);
                try {
                    float[][] labelProbArray;
                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+current_state);
                    if(current_state==0)
                        labelProbArray= new float[1][78];
                    else
                        labelProbArray=new float[1][30];
                    long start = System.currentTimeMillis();
                    // get predict result
                    // multiple input
//                    Object[] input = {inputData,inputData,inputData};
//                    Map<Integer, Object> outputs = new HashMap();
//                    outputs.put(0, labelProbArray);
//                    tflite.runForMultipleInputsOutputs(input, outputs);
                    // single input
                    tflite.run(inputData, labelProbArray);
                    long end = System.currentTimeMillis();
                    time = end - start;

                    float[] resluts = new float[labelProbArray[0].length];
                    System.arraycopy(labelProbArray[0], 0, resluts, 0, labelProbArray[0].length);
                    //                  add code 8.14
                    for(int i=0;i<currentsum;i++){
                        new_resluts[i] = resluts[i*3] + resluts[i*3+1] + resluts[i*3+2];
                    }
//                   add code 8.14
                    // show predict result and time
                    int[] r = get_max_result(new_resluts);
                    String show_text="";
                    show_text = "You might write：\n" + resultLabel.get(r[0]) + "\t\t\t\t\t\t\tProbability:\t\t" + new_resluts[r[0]] * 100 + "%\n" + resultLabel.get(r[1]) +
                            "\t\t\t\t\t\t\tProbability:\t\t" + new_resluts[r[1]] * 100 + "%\n" + resultLabel.get(r[2]) + "\t\t\t\t\t\t\tProbability:\t\t" + new_resluts[r[2]] * 100 +
                            "%\n" + resultLabel.get(r[3]) + "\t\t\t\t\t\t\tProbability:\t\t" + new_resluts[r[3]] * 100 + "%\n" + resultLabel.get(r[4]) + "\t\t\t\t\t\t\tProbability:\t\t" + new_resluts[r[4]] * 100 +
                            "%\nPredict Used:" + time + "ms" + "\n\nMake wav file Used:" + Constents.makewavfiletime + "ms";
                    if(new_resluts==null)
                        Log.d(TAG, "PredictWav: null");

//                    展示频谱图
                    //change 10.15
                    Bitmap bitmap = BitmapFactory.decodeFile(pic_path);
//                    picture1.setImageBitmap(bitmap);
                    //change 10.15

                    return new_resluts;

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return new_resluts;

    }
    //add 11.8
    public void setLanguageResult(double[] A){
        char[][] output_result=null;
        if(current_state==0){
            save_result[save_times++]=A;
            output_result=lg_model.testCode(save_result,save_times);
            //1121
            int length = 0;
            for(int i=0;i<5;i++){
                if(!String.valueOf(output_result[i]).equals(""))
                    showOption[length++] = String.valueOf(output_result[i]);
            }
            //1121
            Message message2=new Message();
            message2.what=200;
            message2.obj=showOption;
            predictHandler2.sendMessage(message2);
        }


    }

//    added code6.10
}
