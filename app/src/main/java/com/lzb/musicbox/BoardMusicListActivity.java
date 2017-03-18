package com.lzb.musicbox;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lzb.musicbox.adapter.NetMusicListAdapter;
import com.lzb.musicbox.app.App;
import com.lzb.musicbox.entity.NetMusic;
import com.lzb.musicbox.fragment.DownloadDialogFragment;
import com.lzb.musicbox.utils.AppUtils;
import com.lzb.musicbox.utils.Constant;
import com.lzb.musicbox.utils.LogUtils;
import com.lzb.musicbox.utils.NetMusicUtils;
import com.lzb.musicbox.utils.RedirectUtils;
import com.lzb.musicbox.utils.ToastUtils;
import com.lzb.musicbox.view.PlayControlView;
import com.lzb.musicbox.view.PullUpListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/23.
 */
public class BoardMusicListActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private LinearLayout lyListBase;
    private TextView tvNoMusicTip;
    private PullUpListView lvMusicList;
    private NetMusicListAdapter netMusicListAdapter;
    private List<NetMusic> netMusicList = new ArrayList<NetMusic>();

    private ImageView ivActionBack;
    private ImageView ivShowMenu;
    private TextView tvShowLrc;
    private TextView tvTopListTitle;

    private PlayControlView playControlView;

    private int boardType = 0;
    private String boardTypeTitle = "";

    private int offset = 0;

    private final int LOAD_SUCCESS = 1;
    private final int LOAD_ERROR = 2;
    private final int LOAD_END = 3;

    private ProgressDialog progressDialog;
    private boolean isInit = true;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg){
            switch (msg.what){
                case LOAD_SUCCESS:
                    if (isInit){
                        closeProgressDialog();
                        isInit = false;
                    }
                    netMusicListAdapter.notifyDataSetChanged();
                    break;

                case LOAD_ERROR:
                    if (isInit){
                        closeProgressDialog();
                        isInit = false;
                    }
                    if (netMusicList.size() == 0) {
                        lyListBase.setVisibility(View.GONE);
                        tvNoMusicTip.setVisibility(View.VISIBLE);
                    }
                    break;

                case LOAD_END:
                    lvMusicList.searchEnd();
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_board);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            boardType = bundle.getInt("boardType");
            boardTypeTitle = bundle.getString("boardTypeTitle");
        }
        initView();
        initData();
        initAdapter();
        initListener();
        loadMusicList();
    }

    private void initView() {
        lyListBase = (LinearLayout) findViewById(R.id.ly_list_container);
        tvNoMusicTip = (TextView) findViewById(R.id.tv_no_music_tip);
        lvMusicList = (PullUpListView) findViewById(R.id.lv_music_list);
        ivActionBack = (ImageView) findViewById(R.id.iv_action_bar_back);
        ivShowMenu = (ImageView) findViewById(R.id.iv_action_bar_menu);
        tvShowLrc = (TextView) findViewById(R.id.tv_action_bar_lyric);
        tvTopListTitle = (TextView) findViewById(R.id.tv_top_list_title);
        playControlView = (PlayControlView) findViewById(R.id.view_play_control);

    }

    private void initData() {

        tvTopListTitle.setText(boardTypeTitle);
    }

    private void initAdapter() {
        netMusicListAdapter = new NetMusicListAdapter(this, R.layout.item_net_music, netMusicList);
        lvMusicList.setAdapter(netMusicListAdapter);
    }

    private void initListener() {
        ivActionBack.setOnClickListener(this);
        ivShowMenu.setOnClickListener(this);
        tvShowLrc.setOnClickListener(this);
        lvMusicList.setOnItemClickListener(this);
        lvMusicList.setOnItemLongClickListener(this);
        lvMusicList.setOnPullUpListener(new PullUpListView.OnPullUpListener() {
            @Override
            public void onTouchUp() {
                loadMusicList();
            }
        });
    }

    private void loadMusicList() {
        if (!AppUtils.isNetwork()){
            ToastUtils.longToast(AppUtils.getString(R.string.no_network));
            lvMusicList.loadEndDo();
            return;
        }
        if (isInit){
            showProgressDialog();
        }
        NetMusicUtils.getInstance().setOnNetMusicLoadListener(new NetMusicUtils.OnNetMusicLoadListener() {
            @Override
            public void onLoad(List<NetMusic> list) {
                lvMusicList.loadEndDo();
                if (list != null) {
                    netMusicList.addAll(list);
                    mHandler.sendEmptyMessage(LOAD_SUCCESS);
                } else if (netMusicList.size() != 0) {
                    mHandler.sendEmptyMessage(LOAD_END);
                } else {
                    mHandler.sendEmptyMessage(LOAD_ERROR);
                }
            }
        }).loadBoard(boardType, offset, Constant.BOARD_MUSIC_SIZE);
        offset += Constant.BOARD_MUSIC_SIZE;
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading ...");
        progressDialog.show();
    }

    private void closeProgressDialog(){
        progressDialog.dismiss();
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

            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!AppUtils.isNetwork()){
            ToastUtils.longToast(AppUtils.getString(R.string.no_network));
            return;
        }
        NetMusic netMusic = netMusicList.get(position);
        musicPlayService.play(netMusic);
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        NetMusic netMusic = netMusicList.get(i);
        DownloadDialogFragment downloadDialogFragment = new DownloadDialogFragment(netMusic);
        downloadDialogFragment.show(getSupportFragmentManager(), "download");
        return true;
    }

    @Override
    protected void change(int position) {
        playControlView.changeUI(position);
    }

    @Override
    protected void publish(long progress) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void bindServiceSuccess() {
        bindServiceForPlayControlView();
    }

    /**
     * 为PlayControlView绑定服务和列表
     */
    private void bindServiceForPlayControlView() {
        playControlView.setMusicPlayService(musicPlayService);
    }


}

