package com.lzb.musicbox.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.lzb.musicbox.app.App;

import java.util.Locale;

/**
 * Created by Administrator on 2016/1/20.
 */
public class AppUtils {

    /**
     * 获取字符串
     * @param resId
     * @return
     */
    public static String getString(int resId){
        return App.getContext().getResources().getString(resId);
    }

    /**
     * 获取颜色
     * @param resId
     * @return
     */
    public static int getColor(int resId){
        return App.getContext().getResources().getColor(resId);
    }

    /**
     * 隐藏输入法
     * @param view
     */
    public static void hideInputMethod(View view){
        InputMethodManager imm = (InputMethodManager) App.getContext().getSystemService(App.getContext().INPUT_METHOD_SERVICE);
        if (imm.isActive()){
            imm.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 扫描系统歌曲文件
     */
    public static void scanning(){
        App.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        LogUtils.i("scan : file://"  + Environment.getExternalStorageDirectory());
    }

    /**
     * 单个文件的扫描，速度快
     * @param path
     */
    public static void scanning(String path){
        App.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
        LogUtils.i("scan : file://" + path);
    }

    public static boolean isNetwork(){
        ConnectivityManager connectivityManager = (ConnectivityManager) App.getContext().getSystemService(App.getContext().CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null){
            return networkInfo.isAvailable() && networkInfo.isConnected();
        }
        return false;
    }

    /**
     * 改变语言设置
     * @param locale
     */
    public static void changeLanguageSetting(Locale locale){
        Resources resources = App.getContext().getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        resources.updateConfiguration(configuration,resources.getDisplayMetrics());
    }
}
