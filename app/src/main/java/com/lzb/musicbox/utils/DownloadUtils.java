package com.lzb.musicbox.utils;

import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2016/1/27.
 */
public class DownloadUtils {

    private static DownloadUtils downloadUtils;

    private static int count = 0;

    public interface OnDownloadListener{
        void onSuccess(String savePath);
        void onFailure();
    }

    private DownloadUtils(){ }

    public static DownloadUtils getInstance(){
        if (downloadUtils == null){
            synchronized (DownloadUtils.class){
                if (downloadUtils == null){
                    downloadUtils = new DownloadUtils();
                }
            }
        }
        return downloadUtils;
    }

    public void download(final String urlStr, final String savePath ,final OnDownloadListener callback){
        checkSavePath(savePath);
        new Thread(new Runnable() {
           @Override
           public void run() {
               OkHttpClient okHttpClient = new OkHttpClient();
               Request request = null;
               OutputStream save = null;
               try {
                   request = new Request.Builder().url(urlStr).build();
                   File outFile = getFile(savePath);
                   Response response = okHttpClient.newCall(request).execute();
                   if (response.isSuccessful()){
                       save = new FileOutputStream(outFile);
                       save.write(response.body().bytes());
                       if (callback != null){
                           callback.onSuccess(outFile.getAbsolutePath());
                       }
                   }
               } catch (Exception e) {
                   e.printStackTrace();
                   if (callback != null){
                       callback.onFailure();
                   }
               } finally {
                   if (save != null){
                       try {
                           save.close();
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }
               }
           }
       }).start();
    }

    /**
     * 根据路径判断并创建文件
     * @param savePath
     * @return
     * @throws IOException
     */
    private synchronized File getFile(String savePath) throws IOException {

        File file = new File(savePath);
        if (!file.exists()){
            count = 0;
            file.createNewFile();
            return file;
        }else{
            count ++;
            String name = savePath.substring(savePath.lastIndexOf(File.separator),savePath.lastIndexOf("."));
            if (count == 1) {
                return getFile(savePath.replace(name, name + "(" + count + ")"));
            }else{
                return getFile(savePath.replace(name, name.substring(0,name.lastIndexOf("(")) + "(" + count + ")"));
            }
        }

    }

    private void checkSavePath(String savePath) {
        String dirStr = savePath.substring(0,savePath.lastIndexOf(File.separator));
        File dir = new File(dirStr);
        if (!dir.exists()){
            dir.mkdirs();
            return ;
        }
    }

}
