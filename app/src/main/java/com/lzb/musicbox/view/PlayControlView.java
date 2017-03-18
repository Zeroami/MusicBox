package com.lzb.musicbox.view;

import java.util.List;

import com.lzb.musicbox.PlayDetailActivity;
import com.lzb.musicbox.R;
import com.lzb.musicbox.R.layout;
import com.lzb.musicbox.adapter.PlayListAdapter;
import com.lzb.musicbox.entity.Mp3Info;
import com.lzb.musicbox.entity.NetMusic;
import com.lzb.musicbox.service.MusicPlayService;
import com.lzb.musicbox.utils.ImageLoadUtils;
import com.lzb.musicbox.utils.LogUtils;
import com.lzb.musicbox.utils.MediaUtils;
import com.lzb.musicbox.utils.RedirectUtils;
import com.lzb.musicbox.utils.ToastUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PlayControlView extends LinearLayout implements OnClickListener {

    private Context mContext;

    private RelativeLayout lyMusicInfo;

    private ImageView ivMusicAlbum;
    private TextView tvMusicTitle;
    private TextView tvMusicArtist;

    private ImageView ivPlay;
    private ImageView ivNext;
    private ImageView ivPlayList;

    private PopupWindow pwPlayList;
    private TextView tvPlayListTitle;
    private ListView lvPlayList;
    private List<Mp3Info> playList;
    private PlayListAdapter playListAdapter;

    private boolean isOne = true;

    private MusicPlayService musicPlayService;

    public void setMusicPlayService(MusicPlayService musicPlayService) {
        this.musicPlayService = musicPlayService;
    }

    public PlayControlView(Context context) {
        this(context, null);
    }

    public PlayControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isOne) {
            initView();
            initListener();
            isOne = false;
        }
    }


    private void initView() {
        lyMusicInfo = (RelativeLayout) findViewById(R.id.ly_music_info);
        ivMusicAlbum = (ImageView) findViewById(R.id.iv_bottom_music_album);
        tvMusicTitle = (TextView) findViewById(R.id.tv_bottom_music_title);
        tvMusicArtist = (TextView) findViewById(R.id.tv_bottom_music_artist);
        ivPlay = (ImageView) findViewById(R.id.iv_bottom_play);
        ivNext = (ImageView) findViewById(R.id.iv_bottom_next);
        ivPlayList = (ImageView) findViewById(R.id.iv_bottom_play_list);
    }

    private void initListener() {
        lyMusicInfo.setOnClickListener(this);
        ivPlay.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        ivPlayList.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ly_music_info:
                RedirectUtils.redirect(mContext, PlayDetailActivity.class, false);
                break;

            case R.id.iv_bottom_play:
                //ToastUtils.shortToast(mContext, "you click play");
                if (musicPlayService.isPlaying()) {
                    musicPlayService.pause();
                    ivPlay.setImageResource(R.drawable.icon_play);
                } else {                        //如果当前没有播放
                    if (musicPlayService.isInitStatus()) {        // 如果是初始状态，则从头播放
                        musicPlayService.play(musicPlayService.getCurrentPosition());
                    } else {                    // 如果是暂停，则继续播放
                        musicPlayService.start();
                    }
                    ivPlay.setImageResource(R.drawable.icon_pause);
                }
                break;

            case R.id.iv_bottom_next:
                musicPlayService.next();
                break;

            case R.id.iv_bottom_play_list:
                showPopupWindowPlayList();
                break;


            default:
                break;
        }
    }

    private void showPopupWindowPlayList() {
        LinearLayout view = (LinearLayout) LayoutInflater.from(mContext).inflate(layout.layout_popupwindow_play_list, null);
        pwPlayList = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        pwPlayList.setTouchable(true);
        pwPlayList.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        pwPlayList.setBackgroundDrawable(new BitmapDrawable());
        int popupHeight = view.getChildAt(0).getLayoutParams().height;
        /*int[] location = new int[2];
		view.getLocationOnScreen(location);*/
        pwPlayList.showAsDropDown(this, 0, -popupHeight - getHeight());
        tvPlayListTitle = (TextView) view.findViewById(R.id.tv_play_list_title);
        tvPlayListTitle.setText(musicPlayService.getCurrentPlayListTitle());
        initPlayListView(view);
    }

    private void initPlayListView(View view) {
        lvPlayList = (ListView) view.findViewById(R.id.lv_play_list);
        playList = musicPlayService.getPlayList();
        playListAdapter = new PlayListAdapter(mContext, layout.item_play_list, playList);
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

    public void changeUI(int position) {
        if (ivMusicAlbum == null) {
            initView();                // 如果还没获取控件，则初始化，防止未初始化调用报空指针
        }
        if (musicPlayService.isPlaying()) {    // 当播放时重新切换回来
            ivPlay.setImageResource(R.drawable.icon_pause);
        }
        if (musicPlayService.getPlayType() == musicPlayService.PLAY_TYPE_LOCAL) {
            if (musicPlayService.getMp3InfoList().size() == 0) return;
            Mp3Info mp3Info = musicPlayService.getMp3InfoList().get(position);
            ivMusicAlbum.setImageBitmap(MediaUtils.getArtwork(mContext, mp3Info.getId(), mp3Info.getAlbumId(), true, true));
            tvMusicTitle.setText(mp3Info.getTitle());
            tvMusicArtist.setText(mp3Info.getArtist());

            // 改变播放列表的状态
            if (pwPlayList != null && lvPlayList != null) {
                playListAdapter.setSelectedPosition(musicPlayService.getPlayListPosition());
                playListAdapter.notifyDataSetChanged();
            }
        } else {
            NetMusic netMusic = musicPlayService.getNetMusic();
            ivMusicAlbum.setImageBitmap(MediaUtils.getDefaultArtwork(mContext, true));
            tvMusicTitle.setText(netMusic.getTitle());
            tvMusicArtist.setText(netMusic.getArtist());
            if (!netMusic.getAlbumUrl().equals("")) {
                ImageLoadUtils.getInstance().setOnLoadImageListener(new ImageLoadUtils.OnLoadImageListener() {
                    @Override
                    public void onLoadImage(Bitmap bitmap) {
                        if (bitmap != null) {
                            ivMusicAlbum.setImageBitmap(bitmap);
                        }
                    }
                }).loadImage(netMusic.getAlbumUrl());
            }
        }
    }

}
