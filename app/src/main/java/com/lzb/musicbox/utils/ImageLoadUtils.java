package com.lzb.musicbox.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Administrator on 2016/1/26.
 */
public class ImageLoadUtils {

    public interface OnLoadImageListener{
        void onLoadImage(Bitmap bitmap);
    }

    private OnLoadImageListener onLoadImageListener;

    private static ImageLoadUtils imageLoadUtils;

    private final int LOAD_SUCCESS = 1;
    private final int LOAD_ERROR = 2;

    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case LOAD_SUCCESS:
                    if (onLoadImageListener != null){
                        onLoadImageListener.onLoadImage((Bitmap) msg.obj);
                    }
                    break;

                case LOAD_ERROR:
                    if (onLoadImageListener != null){
                        onLoadImageListener.onLoadImage(null);
                    }
                    break;
            }
        }
    };

    private ImageLoadUtils(){}

    public static ImageLoadUtils getInstance(){
        if (imageLoadUtils == null){
            synchronized (ImageLoadUtils.class){
                if (imageLoadUtils == null){
                    imageLoadUtils = new ImageLoadUtils();
                }
            }
        }
        return imageLoadUtils;
    }


    public ImageLoadUtils setOnLoadImageListener(OnLoadImageListener onLoadImageListener) {
        this.onLoadImageListener = onLoadImageListener;
        return this;
    }

    public void loadImage(final String urlStr){
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                HttpURLConnection conn = null;
                try {
                    url = new URL(urlStr);
                    conn = (HttpURLConnection) url.openConnection();
                    Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
                    Message msg = mHandler.obtainMessage(LOAD_SUCCESS,bitmap);
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(LOAD_ERROR);
                } finally {
                    if (conn != null){
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }

}
