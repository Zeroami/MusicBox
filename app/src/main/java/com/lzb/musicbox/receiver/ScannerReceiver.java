package com.lzb.musicbox.receiver;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.lzb.musicbox.R;
import com.lzb.musicbox.app.App;
import com.lzb.musicbox.utils.AppUtils;
import com.lzb.musicbox.utils.LogUtils;
import com.lzb.musicbox.utils.MediaUtils;
import com.lzb.musicbox.utils.ToastUtils;

import java.text.MessageFormat;

/**
 * Created by Administrator on 2016/1/30.
 */
public class ScannerReceiver extends BroadcastReceiver {

    private Context mContext;
    private ProgressDialog progressDialog;
    private int startCount;
    private int finishCount;

    private final int STARTED = 1;
    private final int FINISHED = 2;

    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case STARTED:
                    scanStart();
                    break;

                case FINISHED:
                    scanFinish();
                    if (finishCount >= startCount){
                        ToastUtils.longToast(MessageFormat.format(AppUtils.getString(R.string.scan_increament),finishCount-startCount));
                    }else{
                        ToastUtils.longToast(MessageFormat.format(AppUtils.getString(R.string.scan_decreament),startCount-finishCount));
                    }
                    break;
            }
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.i("receiver");
        mContext = context;
        String action = intent.getAction();
        if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
            //当系统开始扫描sd卡时，为了用户体验，可以加上一个等待框
            mHandler.sendEmptyMessage(STARTED);
        }

        if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
            //当系统扫描完毕时，停止显示等待框，并重新查询ContentProvider
            mHandler.sendEmptyMessage(FINISHED);
        }
    }

    private void scanStart() {
        App.getSpEditor().putInt("startCount",MediaUtils.getMp3Infos(App.getContext()).size()).commit();
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(AppUtils.getString(R.string.scan_start_msg));
        //progressDialog.setCancelable(false);
        progressDialog.show();
        LogUtils.i("scanning start");
    }

    private void scanFinish() {
        startCount = App.getSP().getInt("startCount",0);
        App.getSpEditor().putBoolean("isScanning",false).commit();
        finishCount = MediaUtils.getMp3Infos(App.getContext()).size();
        if (progressDialog != null){
            progressDialog.dismiss();
        }
        LogUtils.i("scanning finish");
    }

}
