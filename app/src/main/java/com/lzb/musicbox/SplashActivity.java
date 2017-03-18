package com.lzb.musicbox;


import com.lzb.musicbox.app.App;
import com.lzb.musicbox.service.MusicPlayService;
import com.lzb.musicbox.utils.Constant;
import com.lzb.musicbox.utils.RedirectUtils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.Window;

public class SplashActivity extends Activity {

    private static final int REQUEST_READ_CONTACTS = 1;
    private Handler mHandler = new Handler();
    private Runnable r;
    private int delayTime = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        // 首先检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 检查用户是否拒绝了这个权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                // 给出一个提示，告诉用户为什么需要这个权限

            } else {
                // 用户没有拒绝，直接申请权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ_CONTACTS);
            }
        } else {
            //已经拥有授权
            start();
        }


    }

/*	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_UP){
			mHandler.removeCallbacks(r);
			RedirectUtils.redirect(this, MainActivity.class, true);
		}
		return true;
	}*/

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    start();
                } else {
                    // 权限拒绝了。
                    finish();
                }
                return;
            }
        }
    }

    private void start() {
        Intent intent = new Intent(this, MusicPlayService.class);
        startService(intent);
        boolean isFromExit = App.getSP().getBoolean("isExit", true);
        if (!isFromExit) {
            delayTime = 0;
        }
        r = new Runnable() {

            @Override
            public void run() {
                RedirectUtils.redirect(SplashActivity.this, MainActivity.class, true);
            }
        };
        mHandler.postDelayed(r, delayTime);
    }

}
