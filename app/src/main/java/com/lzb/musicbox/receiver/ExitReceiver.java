package com.lzb.musicbox.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lzb.musicbox.service.MusicPlayService;
import com.lzb.musicbox.utils.ActivityUtils;

/**
 * Created by Administrator on 2016/1/31.
 */
public class ExitReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context,MusicPlayService.class);
        context.stopService(serviceIntent);
        ActivityUtils.finisiAllActivity();
    }
}
