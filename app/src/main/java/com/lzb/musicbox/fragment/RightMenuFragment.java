package com.lzb.musicbox.fragment;

import com.lzb.musicbox.ChangeSkinActivity;
import com.lzb.musicbox.MainActivity;
import com.lzb.musicbox.R;
import com.lzb.musicbox.app.App;
import com.lzb.musicbox.receiver.ScannerReceiver;
import com.lzb.musicbox.service.MusicPlayService;
import com.lzb.musicbox.utils.ActivityUtils;
import com.lzb.musicbox.utils.AppUtils;
import com.lzb.musicbox.utils.Constant;
import com.lzb.musicbox.utils.LogUtils;
import com.lzb.musicbox.utils.RedirectUtils;
import com.lzb.musicbox.utils.ToastUtils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

public class RightMenuFragment extends Fragment implements OnClickListener {

    private Context mContext;
    private View layoutView;
    private TextView tvExit;

    private TextView tvScanMusic;
    private TextView tvChangeSkin;
    private RelativeLayout lySleepMode;
    private TextView tvSleepModeStatus;
    private RelativeLayout lyChangeLanguage;
    private TextView tvChangeLanguageStatus;
    private RelativeLayout lyBackgroundBlur;
    private TextView tvBackgroundBlurStatus;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private AlarmManager alarmManager;
    private PendingIntent pi = null;

    private ScannerReceiver scanReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        layoutView = inflater.inflate(R.layout.fragment_right_menu, container, false);
        initView();
        initData();
        initListener();
        initReceiver();
        return layoutView;
    }

    private void initReceiver() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        scanReceiver = new ScannerReceiver();
        mContext.registerReceiver(scanReceiver, intentFilter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContext.unregisterReceiver(scanReceiver);
    }

    private void initView() {
        tvExit = (TextView) layoutView.findViewById(R.id.tv_exit);
        tvScanMusic = (TextView) layoutView.findViewById(R.id.tv_scan_music);
        tvChangeSkin = (TextView) layoutView.findViewById(R.id.tv_change_skin);
        lySleepMode = (RelativeLayout) layoutView.findViewById(R.id.ly_sleep_mode);
        tvSleepModeStatus = (TextView) layoutView.findViewById(R.id.tv_sleep_mode_status);
        lyChangeLanguage = (RelativeLayout) layoutView.findViewById(R.id.ly_change_language);
        tvChangeLanguageStatus = (TextView) layoutView.findViewById(R.id.tv_change_language_status);
        lyBackgroundBlur = (RelativeLayout) layoutView.findViewById(R.id.ly_background_blur);
        tvBackgroundBlurStatus = (TextView) layoutView.findViewById(R.id.tv_background_blur_status);
    }

    private void initData() {
        sp = App.getSP();
        editor = App.getSpEditor();
        alarmManager = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
        int sleepModeStatus = App.getSP().getInt("sleepModeStatus", Constant.OFF);
        if (sleepModeStatus == Constant.OFF){
            tvSleepModeStatus.setText(AppUtils.getString(R.string.off));
        }else{
            tvSleepModeStatus.setText(AppUtils.getString(R.string.on));
        }
        int languageStatus = sp.getInt("languageStatus", Constant.LANGUAGE_CHINESE);
        if (languageStatus == Constant.LANGUAGE_CHINESE){
            tvChangeLanguageStatus.setText(AppUtils.getString(R.string.language_chinese));
        }else if (languageStatus == Constant.LANGUAGE_ENGLISH){
            tvChangeLanguageStatus.setText(AppUtils.getString(R.string.language_english));
        }
        int backgroundBlurStatus = App.getSP().getInt("backgroundBlurStatus", Constant.ON);
        if (backgroundBlurStatus == Constant.ON){
            tvBackgroundBlurStatus.setText(AppUtils.getString(R.string.on));
        }else{
            tvBackgroundBlurStatus.setText(AppUtils.getString(R.string.off));
        }
    }


    private void initListener() {
        tvExit.setOnClickListener(this);
        tvScanMusic.setOnClickListener(this);
        tvChangeSkin.setOnClickListener(this);
        lySleepMode.setOnClickListener(this);
        lyChangeLanguage.setOnClickListener(this);
        lyBackgroundBlur.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_exit:
                mContext.sendBroadcast(new Intent(Constant.BROADCAST_EXIT));
                break;

            case R.id.tv_scan_music:
                if (!App.getSP().getBoolean("isScanning",false)) {
                    App.getSpEditor().putBoolean("isScanning", true).commit();
                    AppUtils.scanning();
                }
                break;

            case R.id.tv_change_skin:
                RedirectUtils.redirect(mContext, ChangeSkinActivity.class,false);
                break;

            case R.id.ly_sleep_mode:
                showSleepModeDialog();
                break;

            case R.id.ly_change_language:
                showChangeLanguageDialog();
                break;

            case R.id.ly_background_blur:
                showBackgroundBlurDialog();
                break;
            default:
                break;
        }
    }

    /**
     * 睡眠模式对话框
     */
    private void showSleepModeDialog() {
        final int[] times = new int[]{10, 20, 30, 40, 50, 60};
        String[] items = new String[times.length + 2];
        String minutes = AppUtils.getString(R.string.minutes);
        for (int i = 0; i < times.length; i++) {
            items[i] = times[i] + minutes;
        }
        items[times.length] = AppUtils.getString(R.string.sleep_time_set);
        items[times.length + 1] = AppUtils.getString(R.string.close_sleep_mode);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i >= 0 && i < times.length) {
                    setAlarmTime(times[i]);
                } else if (i == times.length) {
                    showSleepTimeSetDialog();
                } else if (i == times.length + 1) {
                    cancelAlarmTime();
                }
            }
        });
        builder.show();

    }


    /**
     * 显示睡眠时间设置对话框
     */
    private void showSleepTimeSetDialog() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.alert_sleep_time_set, null);
        final EditText etSleepTime = (EditText) view.findViewById(R.id.et_sleep_time);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(AppUtils.getString(R.string.sleep_time_set));
        builder.setView(view);
        builder.setPositiveButton(AppUtils.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    float time = Float.parseFloat(etSleepTime.getText().toString());
                    if (time != 0.0) {
                        setAlarmTime(time);
                    }
                } catch (Exception e) {
                    ToastUtils.longToast(AppUtils.getString(R.string.input_invalid));
                }
            }
        });
        builder.setNegativeButton(AppUtils.getString(R.string.cancel), null);
        builder.show();
    }

    /**
     * 设置定时任务
     *
     * @param time
     */
    private void setAlarmTime(float time) {
        if (pi != null) {
            alarmManager.cancel(pi);
        }
        pi = PendingIntent.getBroadcast(mContext, 0, new Intent(Constant.BROADCAST_EXIT), 0);
        long triggerAtTime = System.currentTimeMillis() + (long) (time * 60 * 1000);
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtTime, pi);

        editor.putInt("sleepModeStatus", Constant.ON);
        tvSleepModeStatus.setText(AppUtils.getString(R.string.on));
    }

    /**
     * 取消定时任务
     */
    private void cancelAlarmTime() {
        if (pi != null) {
            alarmManager.cancel(pi);
        }
        editor.putInt("sleepModeStatus", Constant.OFF);
        tvSleepModeStatus.setText(AppUtils.getString(R.string.off));
    }


    /**
     * 显示语言切换对话框
     */
    private void showChangeLanguageDialog(){
        String[] items = new String[]{AppUtils.getString(R.string.language_chinese),AppUtils.getString(R.string.language_english)};
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == App.getSP().getInt("languageStatus", Constant.LANGUAGE_CHINESE)) {
                    return;
                }
                if (i == 0) {
                    AppUtils.changeLanguageSetting(Locale.SIMPLIFIED_CHINESE);
                } else if (i == 1) {
                    AppUtils.changeLanguageSetting(Locale.US);
                }
                App.getSpEditor().putInt("languageStatus", i).putBoolean("isLanguageChange", true).commit();
                ActivityUtils.finisiAllActivity();
                RedirectUtils.redirect(mContext, MainActivity.class, false);
            }
        });
        builder.show();
    }


    /**
     * 显示背景模糊化对话框
     */
    private void showBackgroundBlurDialog(){
        String[] items = new String[]{AppUtils.getString(R.string.on),AppUtils.getString(R.string.off)};
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == sp.getInt("backgroundBlurStatus", Constant.ON)) {
                    return;
                }
                if (i == 0) {
                    Constant.SKIN_TARGET_IMAGE_ARRAY = Constant.SKIN_BLUR_IMAGE_ARRAY;
                } else if (i == 1) {
                    Constant.SKIN_TARGET_IMAGE_ARRAY = Constant.SKIN_IMAGE_ARRAY;
                }
                editor.putInt("backgroundBlurStatus", i).commit();
                ActivityUtils.finisiAllActivity();
                RedirectUtils.redirect(mContext, MainActivity.class, false);
            }
        });
        builder.show();
    }
}
