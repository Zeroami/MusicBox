package com.lzb.musicbox.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.lzb.musicbox.utils.Constant;

/**
 * Created by Administrator on 2016/1/23.
 */
public class App extends Application {

    private static Context context;
    private static SharedPreferences sp;
    private static SharedPreferences.Editor spEditor;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        sp = getSharedPreferences(Constant.SP_NAME, MODE_PRIVATE);
        spEditor = sp.edit();
    }

    public static Context getContext(){
        return context;
    }

    public static SharedPreferences getSP(){
        return sp;
    }

    public static SharedPreferences.Editor getSpEditor(){
        return spEditor;
    }
}
