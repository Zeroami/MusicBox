package com.lzb.musicbox;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.lzb.musicbox.app.App;
import com.lzb.musicbox.fragment.RightMenuFragment;
import com.lzb.musicbox.service.MusicPlayService;
import com.lzb.musicbox.service.MusicPlayService.MyBinder;
import com.lzb.musicbox.service.MusicPlayService.OnMusicPlayListener;
import com.lzb.musicbox.utils.ActivityUtils;
import com.lzb.musicbox.utils.Constant;
import com.lzb.musicbox.utils.LogUtils;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;

public abstract class BaseActivity extends FragmentActivity {

    protected SlidingMenu slidingMenu = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        ActivityUtils.addActivity(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (isAttachSlidingMenu()) {
            initSlidingMenu();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityUtils.removeActivity(this);
    }

    /**
     * 子类可复写该方法决定是否与侧滑菜单关联
     *
     * @return
     */
    protected boolean isAttachSlidingMenu() {
        return true;
    }

    private void initSlidingMenu() {

        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.RIGHT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setBehindOffsetRes(R.dimen.sliding_menu_offset);
        slidingMenu.setFadeDegree(0.5f);
        slidingMenu.setBehindScrollScale(0);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        slidingMenu.setMenu(R.layout.layout_right_menu);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_right_menu, new RightMenuFragment()).commit();
    }

    protected void showSlidingMenu() {
        slidingMenu.showMenu(true);
    }

    protected MusicPlayService musicPlayService = null;

    private OnMusicPlayListener mListener = new OnMusicPlayListener() {

        @Override
        public void onPublish(long progress) {
            publish(progress);
        }

        @Override
        public void onChange(int position) {
            change(position);
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicPlayService = ((MyBinder) service).getService();
            bindServiceSuccess();
            if (mListener != null) {
                musicPlayService.setOnMusicPlayListener(mListener);
                mListener.onChange(musicPlayService.getCurrentPosition());    // 绑定成功初始化状态
            }
        }
    };

    protected abstract void change(int position);

    protected abstract void publish(long progress);

    protected abstract void bindServiceSuccess();        // 绑定成功回调，做初始化作用

    protected void bindService() {
        Intent intent = new Intent(this, MusicPlayService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    protected void unbindService() {
        unbindService(serviceConnection);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            showSlidingMenu();
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void setMainBg(View lyMainBg){
        int skinIndex = App.getSP().getInt("skinIndex",0);
        lyMainBg.setBackgroundResource(Constant.SKIN_TARGET_IMAGE_ARRAY[skinIndex]);
    }
}
