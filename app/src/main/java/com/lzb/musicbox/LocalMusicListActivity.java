package com.lzb.musicbox;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.andraskindler.quickscroll.QuickScroll;
import com.lzb.musicbox.adapter.MusicListAdapter;
import com.lzb.musicbox.entity.Mp3Info;
import com.lzb.musicbox.utils.AppUtils;
import com.lzb.musicbox.utils.Constant;
import com.lzb.musicbox.utils.LogUtils;
import com.lzb.musicbox.utils.MediaUtils;
import com.lzb.musicbox.utils.RedirectUtils;
import com.lzb.musicbox.utils.ToastUtils;
import com.lzb.musicbox.view.PlayControlView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class LocalMusicListActivity extends BaseActivity implements OnClickListener, OnItemClickListener {

    private ListView lvMusicList;
    private MusicListAdapter musicListAdapter;
    private List<Mp3Info> mp3InfoList;

    private ImageView ivActionBack;
    private ImageView ivShowMenu;
    private TextView tvShowLrc;

    private TextView tvRandomPlay;
    private TextView tvBatchAction;

    private PlayControlView playControlView;

    private boolean isItemClick = false;    // 是否点击列表项播放

    private boolean isBindList = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_local);
        initView();
        initData();
        initAdapter();
        initListener();
        initQuickScroll();
    }

    private void initView() {
        lvMusicList = (ListView) findViewById(R.id.lv_music_list);
        ivActionBack = (ImageView) findViewById(R.id.iv_action_bar_back);
        ivShowMenu = (ImageView) findViewById(R.id.iv_action_bar_menu);
        tvShowLrc = (TextView) findViewById(R.id.tv_action_bar_lyric);
        tvRandomPlay = (TextView) findViewById(R.id.tv_random_play);
        tvBatchAction = (TextView) findViewById(R.id.tv_batch_action);
        playControlView = (PlayControlView) findViewById(R.id.view_play_control);
    }

    private void initData() {
        mp3InfoList = MediaUtils.getMp3Infos(this);
        for (Mp3Info mp3Info : mp3InfoList) {
            mp3Info.setOriginMediaId(mp3Info.getId());        // 在收藏到喜欢列表需要用到，避免用id与数据库的id冲突
        }
//		mp3InfoList = new ArrayList<Mp3Info>();
    }

    private void initAdapter() {
        musicListAdapter = new MusicListAdapter(this, R.layout.item_music, mp3InfoList ,Constant.LIST_LOCAL_ID);
        lvMusicList.setAdapter(musicListAdapter);

    }

    private void initListener() {
        ivActionBack.setOnClickListener(this);
        ivShowMenu.setOnClickListener(this);
        tvShowLrc.setOnClickListener(this);
        lvMusicList.setOnItemClickListener(this);
        tvRandomPlay.setOnClickListener(this);
        tvBatchAction.setOnClickListener(this);
    }

    private void initQuickScroll(){
        QuickScroll quickscroll = (QuickScroll) findViewById(R.id.quick_scroll);
        quickscroll.init(QuickScroll.TYPE_INDICATOR_WITH_HANDLE, lvMusicList, musicListAdapter, QuickScroll.STYLE_HOLO);
        quickscroll.setHandlebarColor(AppUtils.getColor(R.color.scroll_block), AppUtils.getColor(R.color.scroll_block), AppUtils.getColor(R.color.scroll_block));
        quickscroll.setIndicatorColor(AppUtils.getColor(R.color.scroll_indicator_bg), AppUtils.getColor(R.color.scroll_indicator_tip), AppUtils.getColor(R.color.default_text_color));
        quickscroll.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        quickscroll.setTextPadding(10, 10, 10, 10);
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
            case R.id.iv_action_bar_back:
                this.finish();
                break;

            case R.id.iv_action_bar_menu:
                showSlidingMenu();
                break;

            case R.id.tv_action_bar_lyric:
                RedirectUtils.redirect(this, PlayDetailActivity.class, false);
                break;

            case R.id.tv_random_play:
                Random rand = new Random();
                int position = rand.nextInt(mp3InfoList.size());
                bindServiceAndPlay(position);
                break;

            case R.id.tv_batch_action:
                Bundle bundle = new Bundle();
                bundle.putInt("listCateId",Constant.LIST_LOCAL_ID);
                RedirectUtils.redirect(this, BatchActionListActivity.class,bundle, false);
                break;
            default:
                break;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        isItemClick = true;
        bindServiceAndPlay(position);
    }

    /**
     * 绑定服务并播放
     * @param position
     */
    private void bindServiceAndPlay(int position){
        if (!isBindList) {        // 是否在service已经绑定了列表
            musicPlayService.setMp3InfoList(mp3InfoList);
            musicPlayService.setCurrentPlayListId(Constant.LIST_LOCAL_ID);
            bindServiceForPlayControlView();
            isBindList = true;
        }
        musicPlayService.play(position);
    }

    @Override
    protected void change(int position) {
        if (isBindList || musicPlayService.getCurrentPlayListId() == Constant.LIST_LOCAL_ID) {        // 绑定了自己的列表才改变ui
            musicListAdapter.setSelectedPosition(position);
            musicListAdapter.notifyDataSetChanged();
            if (!isItemClick) {        // 如果不是点击列表项，即点击列表项不切换Selection
                lvMusicList.setSelection(position);
            }
        }
        playControlView.changeUI(position);
        isItemClick = false;
    }

    @Override
    protected void publish(long progress) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void bindServiceSuccess() {
//		mp3InfoList.clear();
//		mp3InfoList.addAll(musicPlayService.getMp3InfoList());
        bindServiceForPlayControlView();
        if (musicListAdapter != null){
            musicListAdapter.setMusicPlayService(musicPlayService);
        }
        updateListFromServer();
        updateListToServer();
    }

    /**
     * 为PlayControlView绑定服务
     */
    private void bindServiceForPlayControlView() {
        playControlView.setMusicPlayService(musicPlayService);
    }

    /**
     * 如果服务的列表发生改变，更新列表
     */
    private void updateListFromServer() {
        // 如果服务的列表发生改变，更新列表
        if (musicPlayService != null && musicPlayService.getListChange()){
            if (lvMusicList != null && musicListAdapter != null){
                mp3InfoList.clear();
                mp3InfoList.addAll(musicPlayService.getMp3InfoList());
                musicListAdapter.setSelectedPosition(musicPlayService.getCurrentPosition());
                musicListAdapter.notifyDataSetChanged();
                lvMusicList.setSelection(musicPlayService.getCurrentPosition());
            }
            musicPlayService.setListChange(false);
        }
    }

    /**
     * 如果当前的列表发生改变且服务正在播放该列表，更新列表
     */
    private void updateListToServer(){
        if (musicPlayService != null && musicPlayService.getCurrentPlayListId() == Constant.LIST_LOCAL_ID){
            if (mp3InfoList.size() != musicPlayService.getMp3InfoList().size()){
                long originMediaId = musicPlayService.getMp3InfoList().get(musicPlayService.getCurrentPosition()).getOriginMediaId();
                for (int i=0;i<mp3InfoList.size();i++){
                    if (mp3InfoList.get(i).getOriginMediaId() == originMediaId){
                        musicPlayService.setMp3InfoList(mp3InfoList);
                        musicPlayService.setCurrentPosition(i);
                        break;
                    }
                }
            }
        }
    }
}
