package com.lzb.musicbox.utils;

import android.widget.Toast;

import com.lzb.musicbox.app.App;

public class ToastUtils {

    public static boolean isShow = true;

    public static void toast(String text, int duration) {
        if (isShow) {
            Toast.makeText(App.getContext(), text, duration).show();
        }
    }

    public static void shortToast(String text) {
        toast(text, Toast.LENGTH_SHORT);
    }

    public static void longToast(String text) {
        toast(text, Toast.LENGTH_LONG);
    }
}
