package com.lzb.musicbox;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.andraskindler.quickscroll.QuickScroll;
import com.lzb.musicbox.adapter.MusicListAdapter;
import com.lzb.musicbox.db.DBUtilsBuilder;
import com.lzb.musicbox.entity.Mp3Info;
import com.lzb.musicbox.entity.Mp3ListCate;
import com.lzb.musicbox.utils.AppUtils;
import com.lzb.musicbox.utils.Constant;
import com.lzb.musicbox.utils.LogUtils;
import com.lzb.musicbox.utils.RedirectUtils;
import com.lzb.musicbox.utils.ToastUtils;
import com.lzb.musicbox.view.PlayControlView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2016/1/21.
 */
public class UserMusicListActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private LinearLayout lyListBase;
    private TextView tvNoMusicTip;
    private ListView lvMusicList;
    private MusicListAdapter musicListAdapter;
    private List<Mp3Info> mp3InfoList;

    private ImageView ivActionBack;
    private ImageView ivShowMenu;
    private TextView tvShowLrc;
    private TextView tvTopListTitle;

    private TextView tvRandomPlay;
    private TextView tvBatchAction;

    private PlayControlView playControlView;

    private boolean isItemClick = false;    // 是否点击列表项播放

    private int listCateId = -1;
    private String listCateTitle = "";
    private boolean isBindList = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            listCateId = bundle.getInt("listCateId");
        }
        initView();
        initData();
        initAdapter();
        initListener();
    }

    private void initView() {
        lyListBase = (LinearLayout) findViewById(R.id.ly_list_base);
        tvNoMusicTip = (TextView) findViewById(R.id.tv_no_music_tip);
        lvMusicList = (ListView) findViewById(R.id.lv_music_list);
        ivActionBack = (ImageView) findViewById(R.id.iv_action_bar_back);
        ivShowMenu = (ImageView) findViewById(R.id.iv_action_bar_menu);
        tvShowLrc = (TextView) findViewById(R.id.tv_action_bar_lyric);
        tvTopListTitle = (TextView) findViewById(R.id.tv_top_list_title);
        tvRandomPlay = (TextView) findViewById(R.id.tv_random_play);
        tvBatchAction = (TextView) findViewById(R.id.tv_batch_action);
        playControlView = (PlayControlView) findViewById(R.id.view_play_control);
        QuickScroll quickscroll = (QuickScroll) findViewById(R.id.quick_scroll);
        quickscroll.setVisibility(View.GONE);
    }

    private void initData() {
        mp3InfoList = new ArrayList<Mp3Info>();
        DBUtilsBuilder.DBUtils dbUtilsForCate = DBUtilsBuilder.getInstance(Mp3ListCate.class);
        Mp3ListCate mp3ListCate = (Mp3ListCate) dbUtilsForCate.table(Constant.TABLE_MP3_LIST_CATE).column(new String[]{"title"}).where("id=?", new String[]{String.valueOf(listCateId)}).find();
        listCateTitle = mp3ListCate.getTitle();
        tvTopListTitle.setText(listCateTitle);
    }

    private void initAdapter() {
        musicListAdapter = new MusicListAdapter(this, R.layout.item_music, mp3InfoList,listCateId);
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

    @Override
    protected void onResume() {
        super.onResume();
        bindService();
        // 更新列表
        updateList();
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
                bundle.putInt("listCateId",listCateId);
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
            LogUtils.i("UserList onItemClick");
            //List<Mp3Info> sMp3InfoList = (List<Mp3Info>) ((ArrayList<Mp3Info>) mp3InfoList).clone();
            //musicPlayService.setMp3InfoList(sMp3InfoList); // 不能直接传mp3InfoList，否则这边的列表改变，服务也跟着改变，服务的列表要相对独立
            musicPlayService.setMp3InfoList(mp3InfoList);
            musicPlayService.setCurrentPlayListId(listCateId);
            bindServiceForPlayControlView();
            isBindList = true;
        }
        musicPlayService.play(position);
    }

    @Override
    protected void change(int position) {
        if (isBindList || musicPlayService.getCurrentPlayListId() == listCateId) {        // 绑定了自己的列表才改变ui
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
        bindServiceForPlayControlView();
        if (musicListAdapter != null){
            musicListAdapter.setMusicPlayService(musicPlayService);
        }
        updateListFromServer();
        updateListToServer();
    }

    /**
     * 为PlayControlView绑定服务和列表
     */
    private void bindServiceForPlayControlView() {
        playControlView.setMusicPlayService(musicPlayService);
    }

    /**
     * 检验并更新列表
     */
    private void updateList() {
        DBUtilsBuilder.DBUtils dbUtilsForInfo = DBUtilsBuilder.getInstance(Mp3Info.class);
        String limit = null;
        String order = null;
        if (listCateId == Constant.LIST_LATEST_ID) {
            limit = "0,100";
            order = "id desc";
        }
        List<Mp3Info> newMp3InfoList = dbUtilsForInfo.table(Constant.TABLE_MP3_INFO).where("cateId=?", new String[]{String.valueOf(listCateId)}).limit(limit).order(order).select();
        if (newMp3InfoList.size() != mp3InfoList.size() || newMp3InfoList == null || newMp3InfoList.size() == 0) {  // 如果是第一次进入或者恢复时数据改变，则改变状态
            if (newMp3InfoList == null || newMp3InfoList.size() == 0) {
                lyListBase.setVisibility(View.GONE);
                tvNoMusicTip.setVisibility(View.VISIBLE);
                tvNoMusicTip.setText(listCateTitle + AppUtils.getString(R.string.no_music_tip));
            } else {
                lyListBase.setVisibility(View.VISIBLE);
                tvNoMusicTip.setVisibility(View.GONE);
                mp3InfoList.clear();
                mp3InfoList.addAll(newMp3InfoList);
                musicListAdapter.setSelectedPosition(-1);
                musicListAdapter.notifyDataSetChanged();
                isBindList = false;     // 每次恢复如果改变则设置重新绑定
            }
        }
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
