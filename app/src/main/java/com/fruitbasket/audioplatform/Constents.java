package com.fruitbasket.audioplatform;

import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

public class Constents {
    static public int LENCHUNKDESCRIPTOR = 4;
    static public int LENWAVEFLAG = 4;
    static public int LENFMTSUBCHUNK = 4;
    static public int LENDATASUBCHUNK = 4;
    public static String file_path = null;
    public static String user_path = null;
    public static int[] datalist = null;
    public static int dataLength = 0;
    public static long time_one = 0;
    public static long time_two = 0;
    public static String audio_name = null;
    //add 10.14
    public static Queue<String> pathqueue = new LinkedTransferQueue<>();
    //add 10.14
}