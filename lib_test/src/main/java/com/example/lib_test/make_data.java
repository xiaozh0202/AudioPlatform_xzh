package com.example.lib_test;

import java.io.File;
import java.io.IOException;

public class make_data {
    public static void main(String[] args) throws IOException {
        String outputpath_org = "D:\\ALL_WORK\\AcouDigits\\data\\txt_data\\withOutResize\\XZH";
        int all_index = 0;
        String[] filepath = {
                "D:\\ALL_WORK\\AcouDigits\\data\\wav_data\\XZH",
//                "D:\\ALL_WORK\\AcouDigits\\data\\wav_data\\HYT",
//                "D:\\ALL_WORK\\AcouDigits\\data\\wav_data\\WD",
//                "D:\\ALL_WORK\\AcouDigits\\data\\wav_data\\YQ",
        };
        for(String temppath:filepath){
            File file = new File(temppath);
            File[] f1=file.listFiles();
            String label = null;
            int index;
            for(File ff1:f1) {
                label = ff1.getName();
                File org_dir=new File(outputpath_org + "\\"+label);
                if(!org_dir.exists()){
                    org_dir.mkdir();
                }
                index = org_dir.listFiles().length+1;
                File file2 = new File(ff1.getParent() + "\\" + ff1.getName());
                File[] f2 = file2.listFiles();
                for(File ff2:f2){
                    String file_path = ff2.getParent()+"\\"+ff2.getName();
                    String org_path = outputpath_org + "\\"+label + "\\"+index  + ".txt";
                    WavToMatrix apptest = new WavToMatrix(file_path,org_path);
                    index++;
                    all_index++;
                    System.out.println(ff2.getName() + " is done!lable is "+label + "!index is "+all_index);
                }
            }
            System.out.println(temppath +" is done!");
        }
//        File file = new File("G:\\文档\\Tencent Files\\1592216581\\FileRecv\\AcouDigits\\Data\\Martin实验数据\\所有实验数据\\acoustic_data\\赵猛_戴晴朗\\DQL\\DQL");
//        File[] f1=file.listFiles();
//        String label = null;
//        int index;
//        for(File ff1:f1) {
//            label = ff1.getName();
//            File org_dir=new File(outputpath_org + "\\"+label);
//            if(!org_dir.exists()){
//                org_dir.mkdir();
//            }
//            index = org_dir.listFiles().length+1;
//            File file2 = new File(ff1.getParent() + "\\" + ff1.getName());
//            File[] f2 = file2.listFiles();
//            for(File ff2:f2){
//                String file_path = ff2.getParent()+"\\"+ff2.getName();
//                String org_path = outputpath_org + "\\"+label + "\\"+index  + ".txt";
//                WavToMatrix apptest = new WavToMatrix(file_path,org_path);
//                index++;
//                System.out.println(ff2.getName() + " is done!lable is "+label + "!index is "+index);
//            }
//        }
    }
}
