package com.lzb.musicbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.lzb.musicbox.adapter.PlayListAdapter;
import com.lzb.musicbox.db.DBUtilsBuilder;
import com.lzb.musicbox.entity.Mp3Info;
import com.lzb.musicbox.entity.NetMusic;
import com.lzb.musicbox.utils.AppUtils;
import com.lzb.musicbox.utils.Constant;
import com.lzb.musicbox.utils.DownloadUtils;
import com.lzb.musicbox.utils.FileUtils;
import com.lzb.musicbox.utils.HttpUtils;
import com.lzb.musicbox.utils.LogUtils;
import com.lzb.musicbox.utils.MediaUtils;
import com.lzb.musicbox.utils.NetMusicUtils;
import com.lzb.musicbox.utils.ToastUtils;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import douzi.android.view.DefaultLrcBuilder;
import douzi.android.view.ILrcBuilder;
import douzi.android.view.ILrcView;
import douzi.android.view.LrcRow;
import douzi.android.view.LrcView;

public class PlayDetailActivity extends BaseActivity implements OnClickListener, OnSeekBarChangeListener {

    private ImageView ivBack;
    private ImageView ivPrefer;
    private TextView tvMusicTitle;
    private ImageView ivPlayMode;
    private ImageView ivPlayList;
    private ImageView ivPrev;
    private ImageView ivPlay;
    private ImageView ivNext;

    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private SeekBar sbProgress;

    private List<Mp3Info> mp3InfoList;

    private PopupWindow pwPlayList;
    private ListView lvPlayList;
    private List<Mp3Info> playList;
    private PlayListAdapter playListAdapter;

    private TextView tvPlayListTitle;

    private LrcView lrcView;

    private final int CHANGE_CURRENT_TIME = 1;      // 改变时间和进度
    private final int LOAD_LRC_SUCCESS = 2;         // 加载在线歌词

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHANGE_CURRENT_TIME:
                    tvCurrentTime.setText((String) msg.obj);
                    sbProgress.setProgress(msg.arg1);
                    lrcView.seekLrcToTime(msg.arg1);
                    break;

                case LOAD_LRC_SUCCESS:
                    createLrc((String) msg.obj);
                    break;
            }
        }
    };
    private boolean isLoad = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_detail);
        initView();
        initListener();
    }

/*	@Override
    protected boolean isAttachSlidingMenu() {
		return false;						//不关联侧滑菜单
	}*/

    private void initView() {
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivPrefer = (ImageView) findViewById(R.id.iv_prefer);
        tvMusicTitle = (TextView) findViewById(R.id.tv_music_title);
        ivPlayMode = (ImageView) findViewById(R.id.iv_play_mode);
        ivPlayList = (ImageView) findViewById(R.id.iv_play_list);
        ivPrev = (ImageView) findViewById(R.id.iv_prev);
        ivPlay = (ImageView) findViewById(R.id.iv_play);
        ivNext = (ImageView) findViewById(R.id.iv_next);
        tvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        tvTotalTime = (TextView) findViewById(R.id.tv_total_time);
        sbProgress = (SeekBar) findViewById(R.id.sb_progress);
        lrcView = (LrcView) findViewById(R.id.view_lrc);
    }

    private void initListener() {
        ivBack.setOnClickListener(this);
        ivPrefer.setOnClickListener(this);
        ivPlayMode.setOnClickListener(this);
        ivPlayList.setOnClickListener(this);
        ivPrev.setOnClickListener(this);
        ivPlay.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        sbProgress.setOnSeekBarChangeListener(this);
        lrcView.setListener(new ILrcView.LrcViewListener() {
            @Override
            public void onLrcSeeked(int newPosition, LrcRow row) {
                if (musicPlayService != null) {
                    if (musicPlayService.isInitStatus()) {
                        musicPlayService.play(musicPlayService.getCurrentPosition());
                    }
                    musicPlayService.seekTo((int) row.time);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService();
        setMainBg(findViewById(R.id.ly_main_bg));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;

            case R.id.iv_play:
                if (musicPlayService.isPlaying()) {
                    musicPlayService.pause();
                    ivPlay.setImageResource(R.drawable.icon_play);
                } else {
                    if (musicPlayService.isInitStatus()) {
                        musicPlayService.play(musicPlayService.getCurrentPosition());
                    } else {
                        musicPlayService.start();
                    }
                    ivPlay.setImageResource(R.drawable.icon_pause);
                }
                break;

            case R.id.iv_prev:
                musicPlayService.prev();
                break;

            case R.id.iv_next:
                musicPlayService.next();
                break;

            case R.id.iv_play_mode:
                if (musicPlayService.getPlayMode() == musicPlayService.MODE_ORDER) {
                    musicPlayService.setPlayMode(musicPlayService.MODE_RANDOM);
                    ivPlayMode.setImageResource(R.drawable.icon_mode_random);
                    ToastUtils.longToast(AppUtils.getString(R.string.mode_play_random));
                } else if (musicPlayService.getPlayMode() == musicPlayService.MODE_RANDOM) {
                    musicPlayService.setPlayMode(musicPlayService.MODE_LOOP);
                    ivPlayMode.setImageResource(R.drawable.icon_mode_loop);
                    ToastUtils.longToast(AppUtils.getString(R.string.mode_play_loop));
                } else if (musicPlayService.getPlayMode() == musicPlayService.MODE_LOOP) {
                    musicPlayService.setPlayMode(musicPlayService.MODE_ORDER);
                    ivPlayMode.setImageResource(R.drawable.icon_mode_order);
                    ToastUtils.longToast(AppUtils.getString(R.string.mode_play_order));
                }
                break;

            case R.id.iv_play_list:
                showPopupWindowPlayList();
                break;

            case R.id.iv_prefer:
                if (musicPlayService.getPlayType() == musicPlayService.PLAY_TYPE_LOCAL) {
                    Mp3Info mp3Info = mp3InfoList.get(musicPlayService.getCurrentPosition());
                    DBUtilsBuilder.DBUtils dbUtils = DBUtilsBuilder.getInstance(Mp3Info.class);
                    boolean isExist = dbUtils.table(Constant.TABLE_MP3_INFO).column(new String[]{"id"}).where("cateId=? and originMediaId=?", new String[]{String.valueOf(Constant.LIST_PREFER_ID), String.valueOf(mp3Info.getOriginMediaId())}).isExist();
                    if (!isExist) {
                        // 还没添加入喜欢列表，则添加
                        mp3Info.setCateId(Constant.LIST_PREFER_ID);
                        dbUtils.table(Constant.TABLE_MP3_INFO).insert(mp3Info);
                        ToastUtils.longToast(AppUtils.getString(R.string.toast_add_to_prefer));
                        ivPrefer.setImageResource(R.drawable.icon_prefer_selected);
                    } else {
                        // 已添加，则取消添加
                        dbUtils.table(Constant.TABLE_MP3_INFO).where("cateId=? and originMediaId=?", new String[]{String.valueOf(Constant.LIST_PREFER_ID), String.valueOf(mp3Info.getOriginMediaId())}).delete();
                        ToastUtils.longToast(AppUtils.getString(R.string.toast_delete_from_prefer));
                        ivPrefer.setImageResource(R.drawable.icon_prefer);
                    }
                } else {
                    ToastUtils.longToast(AppUtils.getString(R.string.please_download));
                }
                break;


            default:
                break;
        }

    }

    /**
     * 显示播放列表弹窗
     */
    private void showPopupWindowPlayList() {
        LinearLayout view = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.layout_popupwindow_play_list, null);
        pwPlayList = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        // 使其点击弹窗外部可关闭弹窗
        pwPlayList.setTouchable(true);
        pwPlayList.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;   // 不处理onTouch时间
            }
        });
        pwPlayList.setBackgroundDrawable(new BitmapDrawable()); // 支持按back退出弹窗，必须有背景
        // ----------
        int popupHeight = view.getChildAt(0).getLayoutParams().height;
        View bottomBar = findViewById(R.id.ly_bottom_bar);
        pwPlayList.showAsDropDown(bottomBar, 0, -popupHeight - bottomBar.getLayoutParams().height);
        tvPlayListTitle = (TextView) view.findViewById(R.id.tv_play_list_title);
        tvPlayListTitle.setText(musicPlayService.getCurrentPlayListTitle());
        initPlayListView(view);
    }

    /**
     * 初始化播放列表
     * @param view
     */
    private void initPlayListView(View view) {
        lvPlayList = (ListView) view.findViewById(R.id.lv_play_list);
        playList = musicPlayService.getPlayList();
        playListAdapter = new PlayListAdapter(this, R.layout.item_play_list, playList);
        lvPlayList.setAdapter(playListAdapter);
        playListAdapter.setSelectedPosition(musicPlayService.getPlayListPosition());
        lvPlayList.setSelection(musicPlayService.getPlayListPosition());
        lvPlayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                musicPlayService.play(playList.get(i).getIndex());
            }
        });
    }

    @Override
    protected void change(int position) {
        changeUI(position);
    }


    @Override
    protected void publish(long progress) {
        Message msg = new Message();
        msg.what = CHANGE_CURRENT_TIME;
        msg.obj = MediaUtils.formatTime(progress);
        msg.arg1 = (int) progress;
        mHandler.sendMessage(msg);

    }

    @Override
    protected void bindServiceSuccess() {
        mp3InfoList = musicPlayService.getMp3InfoList();
        changePlayModeIcon();
    }

    /**
     * 根据service判断当前的播放模式并更新ui
     */
    private void changePlayModeIcon() {
        if (musicPlayService.getPlayMode() == musicPlayService.MODE_ORDER) {
            ivPlayMode.setImageResource(R.drawable.icon_mode_order);
        } else if (musicPlayService.getPlayMode() == musicPlayService.MODE_RANDOM) {
            ivPlayMode.setImageResource(R.drawable.icon_mode_random);
        } else if (musicPlayService.getPlayMode() == musicPlayService.MODE_LOOP) {
            ivPlayMode.setImageResource(R.drawable.icon_mode_loop);
        }
    }

    private void changeUI(int position) {
        if (musicPlayService.isPlaying()) {
            ivPlay.setImageResource(R.drawable.icon_pause);
        }
        tvTotalTime.setText(MediaUtils.formatTime(musicPlayService.getDuration()));
        sbProgress.setMax((int) musicPlayService.getDuration());
        lrcView.setLrc(null);
        isLoad = false;
        if (musicPlayService.getPlayType() == musicPlayService.PLAY_TYPE_LOCAL) {
            if (mp3InfoList.size() == 0) return;
            final Mp3Info mp3Info = mp3InfoList.get(position);
            tvMusicTitle.setText(mp3Info.getTitle() + " - " + mp3Info.getArtist());
            final String lrcFilePath = Constant.MUSIC_DIR + File.separator + Constant.LRC_NAME + File.separator + mp3Info.getArtist() + "-" + mp3Info.getTitle() + ".lrc";
            File lrcFile = new File(lrcFilePath);
            lrcView.setLoadingTipText(AppUtils.getString(R.string.load_lrc));
            if (lrcFile.exists()){
                loadLrc(lrcFilePath);
                isLoad = true;
            }else{
                // 不存在则下载
               if (AppUtils.isNetwork()){
                   NetMusicUtils.getInstance().setOnNetMusicSearchListener(new NetMusicUtils.OnNetMusicSearchListener() {
                       @Override
                       public void onSearch(List<NetMusic> netMusicList, int total) {
                           if (netMusicList != null && netMusicList.size() != 0){
                               int index = 0;
                               for (int i=0;i<netMusicList.size();i++){
                                   NetMusic netMusic = netMusicList.get(i);
                                   // 尽可能匹配歌手名
                                   if (netMusic.getTitle().equals(mp3Info.getTitle()) && netMusic.getArtist().indexOf(mp3Info.getArtist()) != -1){
                                       if (!netMusic.getLrcLink().equals("")){
                                           index = i;
                                           break;
                                       }
                                   }
                               }
                               NetMusic netMusic = netMusicList.get(index);
                               if (!netMusic.getTitle().equals(mp3Info.getTitle())){    // 快速切歌，下载的歌词跟当前不对应
                                   return ;
                               }

                               DownloadUtils.getInstance().download(netMusic.getLrcLink(), lrcFilePath, new DownloadUtils.OnDownloadListener() {
                                   @Override
                                   public void onSuccess(final String savePath) {
                                       if (!isLoad){ // 如果还没有加载歌词
                                           runOnUiThread(new Runnable() {
                                               @Override
                                               public void run() {
                                                   loadLrc(savePath);
                                               }
                                           });
                                       }
                                   }

                                   @Override
                                   public void onFailure() {

                                   }
                               });
                           }
                       }
                   }).search(mp3Info.getTitle(),1,5);
               }
            }

            // 改变播放列表的状态（在播放列表点击的时候改变UI）
            if (pwPlayList != null && lvPlayList != null) {
                playListAdapter.setSelectedPosition(musicPlayService.getPlayListPosition());
                playListAdapter.notifyDataSetChanged();
            }

            // 判断该歌曲是否已添加进喜欢列表
            DBUtilsBuilder.DBUtils dbUtils = DBUtilsBuilder.getInstance(Mp3Info.class);
            Mp3Info mp3InfoFromDB = (Mp3Info) dbUtils.table(Constant.TABLE_MP3_INFO).where("cateId=? and originMediaId=?", new String[]{String.valueOf(Constant.LIST_PREFER_ID), String.valueOf(mp3Info.getOriginMediaId())}).find();
            if (mp3InfoFromDB != null) {
                ivPrefer.setImageResource(R.drawable.icon_prefer_selected);
            } else {
                ivPrefer.setImageResource(R.drawable.icon_prefer);
            }
        } else {
            final NetMusic netMusic = musicPlayService.getNetMusic();
            tvMusicTitle.setText(netMusic.getTitle() + " - " + netMusic.getArtist());
            lrcView.setLoadingTipText(AppUtils.getString(R.string.load_lrc));
            if (!netMusic.getLrcLink().equals("")) {
                // 请求网络歌词
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String response = HttpUtils.requestByGet(netMusic.getLrcLink());
                        mHandler.sendMessage(mHandler.obtainMessage(LOAD_LRC_SUCCESS, response));
                    }
                }).start();
            }
        }
    }

    /**
     * 加载歌词
     *
     * @param filePath
     */
    private void loadLrc(String filePath) {
        String lrcStr = getLrcString(filePath);
        createLrc(lrcStr);
    }

    private void createLrc(String lrcStr){
        ILrcBuilder lrcBuilder = new DefaultLrcBuilder();
        List<LrcRow> rows = lrcBuilder.getLrcRows(lrcStr);
        lrcView.setLrc(rows);
    }

    /**
     * 从歌词文件获取歌词内容
     *
     * @param filePath
     * @return
     */
    private String getLrcString(String filePath) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuffer result = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
//                if (line.equals("")) {
//                    continue;
//                }
                result.append(line).append("\r\n");
            }
            return result.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // 进度条改变
        if (fromUser) {
            musicPlayService.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // 开始拖动
        if (musicPlayService.isInitStatus()) {
            musicPlayService.play(musicPlayService.getCurrentPosition());
        }
        musicPlayService.pause();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // 结束拖动
        musicPlayService.start();
    }

}
