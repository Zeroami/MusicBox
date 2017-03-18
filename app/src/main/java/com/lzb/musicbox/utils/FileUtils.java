package com.lzb.musicbox.utils;

import android.os.Environment;

/**
 * Created by Administrator on 2016/1/18.
 */
public class FileUtils {

    public static boolean isSDCardExist(){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return true;
        }else{
            return false;
        }
    }

    public static String getSDCardPath(){
        return getSDCardPath("");
    }

    public static String getSDCardPath(String defaultPath){
        if(isSDCardExist()){
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return defaultPath;
    }
}
