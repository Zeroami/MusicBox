package com.lzb.musicbox.fragment;

import com.lzb.musicbox.MainActivity;
import com.lzb.musicbox.R;
import com.lzb.musicbox.adapter.NetMusicListAdapter;
import com.lzb.musicbox.entity.NetMusic;
import com.lzb.musicbox.utils.AppUtils;
import com.lzb.musicbox.utils.Constant;
import com.lzb.musicbox.utils.LogUtils;
import com.lzb.musicbox.utils.NetMusicUtils;
import com.lzb.musicbox.utils.ToastUtils;
import com.lzb.musicbox.view.PullUpListView;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements AdapterView.OnItemClickListener,View.OnClickListener, AdapterView.OnItemLongClickListener {

	private Context mContext;
	private View layoutView;

	private PullUpListView lvMusicList;
	private NetMusicListAdapter netMusicListAdapter;
	private List<NetMusic> netMusicList = new ArrayList<NetMusic>();

	private EditText etKey;
	private ImageView ivClear;
	private TextView tvSearch;
	private LinearLayout lyLoadProgress;

	private final int SEARCH_SUCCESS = 1;
	private final int SEARCH_ERROR = 2;
	private final int SEARCH_END = 3;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg){
			switch (msg.what){
				case SEARCH_SUCCESS:
					if (lvMusicList.getVisibility() == View.GONE){
						lyLoadProgress.setVisibility(View.GONE);
						lvMusicList.setVisibility(View.VISIBLE);
					}
					netMusicListAdapter.notifyDataSetChanged();
					break;

				case SEARCH_ERROR:
					if (lvMusicList.getVisibility() == View.GONE){
						lyLoadProgress.setVisibility(View.GONE);
						lvMusicList.setVisibility(View.VISIBLE);
					}
					ToastUtils.longToast("没有搜索到相关歌曲");
					break;

				case SEARCH_END:
					lvMusicList.searchEnd();
					break;
			}
		}
	};
	private int page = 1;
	private String lastKey;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getActivity();
		layoutView = inflater.inflate(R.layout.fragment_search, container,false);
		initView();
		initData();
		initAdapter();
		initListener();
		return layoutView;
	}

	private void initView(){
		lvMusicList = (PullUpListView) layoutView.findViewById(R.id.lv_music_list);
		etKey = (EditText) layoutView.findViewById(R.id.et_key);
		ivClear = (ImageView) layoutView.findViewById(R.id.iv_clear);
		tvSearch = (TextView) layoutView.findViewById(R.id.tv_search);
		lyLoadProgress = (LinearLayout) layoutView.findViewById(R.id.ly_load_progress);
	}

	private void initData(){

	}

	private void initAdapter(){
		netMusicListAdapter = new NetMusicListAdapter(mContext,R.layout.item_net_music,netMusicList);
		netMusicListAdapter.setType("search");
		lvMusicList.setAdapter(netMusicListAdapter);
	}

	private void initListener(){
		lvMusicList.setOnItemClickListener(this);
		lvMusicList.setOnItemLongClickListener(this);
		lvMusicList.setOnPullUpListener(new PullUpListView.OnPullUpListener() {
			@Override
			public void onTouchUp() {
				searchMusic(lastKey);
			}
		});
		etKey.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (etKey.getText().toString().length() != 0) {
					ivClear.setVisibility(View.VISIBLE);
				} else {
					ivClear.setVisibility(View.GONE);
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});

		ivClear.setOnClickListener(this);
		tvSearch.setOnClickListener(this);
	}

	public void searchMusic(String key){
		if (!AppUtils.isNetwork()){
			ToastUtils.longToast(AppUtils.getString(R.string.no_network));
			lvMusicList.loadEndDo();
			return;
		}
		if (page == 1){
			lyLoadProgress.setVisibility(View.VISIBLE);
			lvMusicList.setVisibility(View.GONE);
			lvMusicList.searchInit();
		}
		NetMusicUtils.getInstance().setOnNetMusicSearchListener(new NetMusicUtils.OnNetMusicSearchListener() {
			@Override
			public void onSearch(List<NetMusic> list, int total) {
				lvMusicList.loadEndDo();
				if (list != null && list.size() != 0) {
					netMusicList.addAll(list);
					mHandler.sendEmptyMessage(SEARCH_SUCCESS);
					if (total == netMusicList.size()) {
						mHandler.sendEmptyMessage(SEARCH_END);
					}
				}else{
					mHandler.sendEmptyMessage(SEARCH_ERROR);
				}

			}
		}).search(key, page, Constant.SEARCG_MUSIC_SIZE);
		page ++;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		if (!AppUtils.isNetwork()){
			ToastUtils.longToast(AppUtils.getString(R.string.no_network));
			return;
		}
		NetMusic netMusic = netMusicList.get(i);
		((MainActivity)mContext).getMusicPlayService().play(netMusic);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
		NetMusic netMusic = netMusicList.get(i);
		DownloadDialogFragment downloadDialogFragment = new DownloadDialogFragment(netMusic);
		downloadDialogFragment.show(getFragmentManager(),"download");
		return true;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.iv_clear:
				etKey.setText("");
				break;

			case R.id.tv_search:
				String key = etKey.getText().toString();
				if (TextUtils.isEmpty(key)){
					ToastUtils.longToast(AppUtils.getString(R.string.hint_search));
					return ;
				}
				if (!AppUtils.isNetwork()){
					ToastUtils.longToast(AppUtils.getString(R.string.no_network));
					return ;
				}
				page = 1;		// 新的搜索结果，重置当前页
				AppUtils.hideInputMethod(etKey);
				netMusicList.clear();
				searchMusic(key);
				lastKey = key;
				break;
		}
	}

}
