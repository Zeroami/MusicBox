package com.lzb.musicbox.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.lzb.musicbox.MainActivity;
import com.lzb.musicbox.R;
import com.lzb.musicbox.entity.NetMusic;
import com.lzb.musicbox.entity.NetMusicFileInfo;
import com.lzb.musicbox.utils.AppUtils;
import com.lzb.musicbox.utils.Constant;
import com.lzb.musicbox.utils.DownloadUtils;
import com.lzb.musicbox.utils.LogUtils;
import com.lzb.musicbox.utils.ToastUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.text.MessageFormat;

/**
 * Created by Administrator on 2016/1/27.
 */
public class DownloadDialogFragment extends DialogFragment {

    private Context mContext;
    private String[] items = {AppUtils.getString(R.string.try_listen), AppUtils.getString(R.string.download), AppUtils.getString(R.string.cancel)};
    private NetMusic netMusic;

    private final int DOWNLOAD_SUCCESS = 1;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_SUCCESS:
                    ToastUtils.longToast(AppUtils.getString(R.string.download_success));
                    break;
            }
        }
    };

    public DownloadDialogFragment() {
    }

    @SuppressLint("ValidFragment")
    public DownloadDialogFragment(NetMusic netMusic) {
        this.netMusic = netMusic;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        // 试听
                        if (!AppUtils.isNetwork()){
                            ToastUtils.longToast(AppUtils.getString(R.string.no_network));
                            return;
                        }
                        ((MainActivity) mContext).getMusicPlayService().play(netMusic);
                        break;

                    case 1:
                        // 下载
                        if (!AppUtils.isNetwork()){
                            ToastUtils.longToast(AppUtils.getString(R.string.no_network));
                            return;
                        }
                        if (netMusic.getNetMusicFileInfoList().size() == 0) {
                            ToastUtils.longToast("该歌曲不支持下载");
                        } else {
                            showDownloadSelectDialog();
                        }
                        break;

                    case 3:
                        // 取消
                        dismiss();
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        return dialog;
    }

    /**
     * 显示歌曲下载列表
     */
    private void showDownloadSelectDialog() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.alert_download_select, null);
        final RadioGroup rgMusicInfoList = (RadioGroup) view.findViewById(R.id.rg_music_info_list);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        String musicInfoStr = "{0}.{1}（{2}）";
        for (int i = 0; i < netMusic.getNetMusicFileInfoList().size(); i++) {
            RadioButton rbMusicInfo = new RadioButton(mContext);
            if (i == 0) {
                rbMusicInfo.setChecked(true);
            }
            NetMusicFileInfo fileInfo = netMusic.getNetMusicFileInfoList().get(i);
            rbMusicInfo.setText(MessageFormat.format(musicInfoStr, fileInfo.getName(), fileInfo.getFileExt(), formatSize(fileInfo.getFileSize())));
            rbMusicInfo.setTag(i);
            rgMusicInfoList.addView(rbMusicInfo, lp);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(AppUtils.getString(R.string.alert_download_list_title));
        builder.setView(view);
        builder.setPositiveButton(AppUtils.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int ii) {
                for (int i = 0; i < rgMusicInfoList.getChildCount(); i++) {
                    RadioButton rb = (RadioButton) rgMusicInfoList.getChildAt(i);
                    if (rb.isChecked()) {
                        int tag = (Integer) rb.getTag();
                        final NetMusicFileInfo fileInfo = netMusic.getNetMusicFileInfoList().get(i);
                        final String songSavePath = Constant.MUSIC_DIR + File.separator + Constant.SONG_NAME + File.separator + fileInfo.getName() + "." + fileInfo.getFileExt();
                        String lrcSavePath = Constant.MUSIC_DIR + File.separator + Constant.LRC_NAME + File.separator + fileInfo.getName() + ".lrc";
                        DownloadUtils.getInstance().download(fileInfo.getFileLink(), songSavePath, new DownloadUtils.OnDownloadListener() {
                            @Override
                            public void onSuccess(String savePath) {
                                mHandler.sendEmptyMessage(DOWNLOAD_SUCCESS);
                                AppUtils.scanning(savePath);
                            }

                            @Override
                            public void onFailure() {
                            }
                        });
                        if (!netMusic.getLrcLink().equals("")) {
                            DownloadUtils.getInstance().download(netMusic.getLrcLink(), lrcSavePath, null);
                        }
                        break;
                    }

                }
            }
        });
        builder.setNegativeButton(AppUtils.getString(R.string.cancel), null);
        builder.show();

    }

    /**
     * 格式化大小单位
     *
     * @param fileSize
     * @return
     */
    private String formatSize(float fileSize) {
        String[] ext = new String[]{"B", "K", "M"};
        int flag = 0;
        while (fileSize / 1024 >= 1.0f) {
            fileSize = fileSize / 1024;
            flag++;
        }
        String size = new DecimalFormat("####.00").format(fileSize);
        return size + ext[flag];
    }
}
