package com.lzb.musicbox.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.lzb.musicbox.R;
import com.lzb.musicbox.app.App;
import com.lzb.musicbox.db.DBUtilsBuilder;
import com.lzb.musicbox.entity.Mp3Info;
import com.lzb.musicbox.entity.Mp3ListCate;
import com.lzb.musicbox.entity.NetMusic;
import com.lzb.musicbox.utils.AppUtils;
import com.lzb.musicbox.utils.Constant;
import com.lzb.musicbox.utils.LogUtils;
import com.lzb.musicbox.utils.MediaUtils;
import com.lzb.musicbox.utils.NetMusicUtils;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

public class MusicPlayService extends Service implements OnCompletionListener {

    private MediaPlayer mediaPlayer;
    private List<Mp3Info> mp3InfoList;
    private List<Mp3Info> mp3InfoRandomList;
    private int currentPosition;        // 当前歌曲在列表的位置
    private int currentRandomPosition;    // 当前随机列表的下标
    private int currentPlayListId;        // 当前播放列表的ID

    private int publishTime = 1000;
    private Timer timer;

    public final int MODE_ORDER = 1;    // 顺序播放
    public final int MODE_RANDOM = 2;    // 随机播放
    public final int MODE_LOOP = 3;        // 单曲循环
    private int playMode;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private boolean isInitStatus;        // 是否为刚打开界面的初始状态

    public final int PLAY_TYPE_LOCAL = 1;   // 播放本地歌曲
    public final int PLAY_TYPE_NET = 2;     // 播放在线歌曲
    private int playType = PLAY_TYPE_LOCAL;


    private boolean isListChange = false;   // 是否列表改变

    private NetMusic netMusic;

    public interface OnMusicPlayListener {
        void onChange(int position);        // 曲目发生改变时调用

        void onPublish(long progress);        // 播放时实时调用
    }

    private OnMusicPlayListener mListener;

    public class MyBinder extends Binder {
        public MusicPlayService getService() {
            return MusicPlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i("service onCreate");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        sp = App.getSP();
        editor = App.getSpEditor();
        editor.putBoolean("isExit", false).commit();
        currentPosition = sp.getInt("currentPosition", 0);
        playMode = sp.getInt("playMode", MODE_ORDER);
        currentPlayListId = sp.getInt("currentPlayListId", Constant.LIST_LOCAL_ID);    // 0代表本地歌曲

        // 默认服务启动初始化的音乐列表
        if (currentPlayListId == 0) {
            mp3InfoList = MediaUtils.getMp3Infos(this);
            System.out.println(mp3InfoList);
            for (Mp3Info mp3Info : mp3InfoList) {
                mp3Info.setOriginMediaId(mp3Info.getId());        // 在收藏到喜欢列表需要用到，避免用id与数据库的id冲突
            }
        } else {
            mp3InfoList = DBUtilsBuilder.getInstance(Mp3Info.class).table(Constant.TABLE_MP3_INFO).where("cateId=?", new String[]{String.valueOf(currentPlayListId)}).select();
        }
        initRandomList(mp3InfoList);
        isInitStatus = true;
        timer = new Timer();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.i("service onDestroy");
        timer.cancel();
        timer = null;
        mediaPlayer.release();
        mediaPlayer = null;
        // 保存状态
        editor.putBoolean("isExit",true);
        editor.putInt("sleepModeStatus",Constant.OFF);
        editor.putInt("currentPosition", currentPosition);
        editor.putInt("playMode", playMode);
        editor.putInt("currentPlayListId", currentPlayListId);
        editor.commit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 播放本地歌曲
     * @param position
     */
    public void play(int position) {
        if (mp3InfoList.size() == 0){
            return ;
        }
        if (position < 0 || position >= mp3InfoList.size()) {    // 正确性检查
            position = 0;
        }
        playType = PLAY_TYPE_LOCAL;
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, Uri.parse(mp3InfoList.get(position).getPath()));
            // 支持播放在线音乐
            //mediaPlayer.setDataSource(this, Uri.parse("http://yinyueshiting.baidu.com/data2/music/123297858/123297858.mp3?xcode=19d27d20c5177397ed47f60d337fa3fd"));
            mediaPlayer.prepare();
            mediaPlayer.start();
            if (currentPlayListId != Constant.LIST_LATEST_ID) {
                addToLastetList(position);
            }
            currentPosition = position;
            if (mListener != null) {
                mListener.onChange(position);
                timer.cancel();            //重新开启定时器
                timer = null;
                timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        mListener.onPublish(mediaPlayer.getCurrentPosition());
                    }
                }, 0, publishTime);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isInitStatus = false;
    }

    /**
     * 播放网络歌曲
     * @param netMusic
     */
    public void play(NetMusic netMusic){
        if (netMusic.getNetMusicFileInfoList().size() == 0){
            return ;
        }
        this.netMusic = netMusic;
        playType = PLAY_TYPE_NET;
        try {
            mediaPlayer.reset();
            // 播放在线音乐
            mediaPlayer.setDataSource(this, Uri.parse(netMusic.getNetMusicFileInfoList().get(0).getFileLink()));
            mediaPlayer.prepare();
            mediaPlayer.start();
            if (mListener != null) {
                mListener.onChange(currentPosition);
                timer.cancel();            //重新开启定时器
                timer = null;
                timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        mListener.onPublish(mediaPlayer.getCurrentPosition());
                    }
                }, 0, publishTime);
            }

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isInitStatus = false;
    }

    public void next() {
        if (playType == PLAY_TYPE_LOCAL) {
            if (playMode == MODE_ORDER || playMode == MODE_LOOP) {
                currentPosition++;
                if (currentPosition == mp3InfoList.size()) {
                    currentPosition = 0;
                }
                play(currentPosition);
            } else if (playMode == MODE_RANDOM) {
                resetRandomPosition();
                currentRandomPosition++;
                if (currentRandomPosition == mp3InfoRandomList.size()) {
                    currentRandomPosition = 0;
                }
                play(mp3InfoRandomList.get(currentRandomPosition).getIndex());
            }
        }
    }

    public void prev() {
        if (playType == PLAY_TYPE_LOCAL){
            if (playMode == MODE_ORDER || playMode == MODE_LOOP) {
                currentPosition--;
                if (currentPosition == -1) {
                    currentPosition = mp3InfoList.size() - 1;
                }
                play(currentPosition);
            } else if (playMode == MODE_RANDOM) {
                resetRandomPosition();
                currentRandomPosition--;
                if (currentRandomPosition == -1) {
                    currentRandomPosition = mp3InfoRandomList.size() - 1;
                }
                play(mp3InfoRandomList.get(currentRandomPosition).getIndex());
            }
        }
    }

    public void start() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public boolean isInitStatus() {
        return isInitStatus;
    }

    public long getDuration() {
        if (playType == PLAY_TYPE_LOCAL) {
            if (mp3InfoList.size() == 0 ) return 0;
            return mp3InfoList.get(currentPosition).getDuration();
        }else{
            return mediaPlayer.getDuration();
        }
    }

    public void seekTo(int msec) {
        mediaPlayer.seekTo(msec);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (playType == PLAY_TYPE_LOCAL){
            if (playMode == MODE_LOOP){
                play(currentPosition);
            }else{
                next();
            }
        }else{
            if (mListener != null) {
                mListener.onChange(currentPosition);
            }
        }
    }

    /**
     * 初始化随机列表
     *
     * @param mp3InfoList
     */
    private void initRandomList(List<Mp3Info> mp3InfoList) {
        for (int i = 0; i < mp3InfoList.size(); i++) {
            mp3InfoList.get(i).setIndex(i);
        }
        mp3InfoRandomList = (List<Mp3Info>) ((ArrayList<Mp3Info>) mp3InfoList).clone();
        Collections.shuffle(mp3InfoRandomList);
    }

    /**
     * 添加到最近播放列表
     * @param position
     */
    private void addToLastetList(int position) {
        Mp3Info currentMp3Info = mp3InfoList.get(position);
        DBUtilsBuilder.DBUtils dbUtils = DBUtilsBuilder.getInstance(Mp3Info.class);
        Mp3Info mp3InfoFromDB = (Mp3Info) dbUtils.table(Constant.TABLE_MP3_INFO).where("cateId=? and originMediaId=?", new String[]{String.valueOf(Constant.LIST_LATEST_ID), String.valueOf(currentMp3Info.getOriginMediaId())}).find();
        if (mp3InfoFromDB != null) {
            dbUtils.table(Constant.TABLE_MP3_INFO).where("cateId=? and originMediaId=?", new String[]{String.valueOf(Constant.LIST_LATEST_ID), String.valueOf(currentMp3Info.getOriginMediaId())}).delete();
        }
        currentMp3Info.setCateId(Constant.LIST_LATEST_ID);
        dbUtils.table(Constant.TABLE_MP3_INFO).insert(currentMp3Info);
    }

    /**
     * 设置监听器
     *
     * @param onMusicPlayListener
     */
    public void setOnMusicPlayListener(OnMusicPlayListener onMusicPlayListener) {
        this.mListener = onMusicPlayListener;
    }

    /**
     * 设置歌曲列表
     *
     * @param mp3InfoList
     */
    public void setMp3InfoList(List<Mp3Info> mp3InfoList) {
        this.mp3InfoList = mp3InfoList;
        initRandomList(mp3InfoList);
    }

    /**
     * 返回歌曲列表
     *
     * @return
     */
    public List<Mp3Info> getMp3InfoList() {
        return mp3InfoList;
    }

    /**
     * 获取播放列表，当顺序播放时返回默认列表，当随机播放时返回随机列表
     *
     * @return
     */
    public List<Mp3Info> getPlayList() {
        if (playMode == MODE_RANDOM) {
            return mp3InfoRandomList;
        }
        return mp3InfoList;
    }

    /**
     * 返回播放列表的标题
     * @return
     */
    public String getCurrentPlayListTitle(){
        if (currentPlayListId == Constant.LIST_LOCAL_ID){
            return AppUtils.getString(R.string.list_local);
        }else{
            Mp3ListCate mp3ListCate = DBUtilsBuilder.getInstance(Mp3ListCate.class).table(Constant.TABLE_MP3_LIST_CATE).column(new String[]{"title"}).where("id=?",new String[]{String.valueOf(currentPlayListId)}).find();
            return mp3ListCate.getTitle();
        }
    }

    /**
     * 返回播放列表的位置
     *
     * @return
     */
    public int getPlayListPosition() {
        if (playMode == MODE_RANDOM) {
            resetRandomPosition();
            return currentRandomPosition;
        }
        return currentPosition;
    }

    /**
     * 返回播放列表当前位置
     *
     * @return
     */
    public int getCurrentPosition() {
        return currentPosition;
    }
    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
        if (mListener != null){
            mListener.onChange(currentPosition);
        }
    }

    public int getPlayType() {
        return playType;
    }

    public NetMusic getNetMusic() {
        return netMusic;
    }


    public int getPlayMode() {
        return playMode;
    }

    public void setPlayMode(int playMode) {
        this.playMode = playMode;
    }

    public int getCurrentPlayListId() {
        return currentPlayListId;
    }

    public void setCurrentPlayListId(int currentPlayListId) {
        this.currentPlayListId = currentPlayListId;
    }

    public boolean getListChange() {
        return isListChange;
    }

    public void setListChange(boolean isListChange) {
        this.isListChange = isListChange;
    }

    /**
     * 确定当前歌曲在随机列表中的位置
     */
    private void resetRandomPosition() {
        for (int i = 0; i < mp3InfoRandomList.size(); i++) {
            if (mp3InfoRandomList.get(i).equals(mp3InfoList.get(currentPosition))) {
                currentRandomPosition = i;
                return;
            }
        }
    }
}
