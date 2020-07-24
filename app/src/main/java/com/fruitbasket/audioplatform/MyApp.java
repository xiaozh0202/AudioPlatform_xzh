package com.fruitbasket.audioplatform;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.fruitbasket.audioplatform.play2.GlobalConfig;

import uk.me.berndporr.iirj.Butterworth;

/**
 * Created by FruitBasket on 2017/5/26.
 */

final public class MyApp extends Application {




    private static final String TAG=".MyApp";

    private static Context context;

    static{
        System.loadLibrary("jni");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.i(TAG,"onCreate()");
        context=getApplicationContext();

        GlobalConfig.fAbsolutepath.mkdirs();//创建文件夹
        GlobalConfig.stWaveFileUtil.initIQFile();//创建IQTXT文件
        //GlobalConfig.stPhaseProxy.init();//处理相位数据
    }

    @Override
    public void onTerminate(){
        super.onTerminate();
        Log.i(TAG,"onTerminate()");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        Log.i(TAG,"onConfigurationChanged()");
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        Log.i(TAG,"onLowMemory()");
    }

    @Override
    public void onTrimMemory(int level){
        super.onTrimMemory(level);
        Log.i(TAG,"onTrimMemory()");
    }

    public static Context getContext(){
        return context;
    }
}
