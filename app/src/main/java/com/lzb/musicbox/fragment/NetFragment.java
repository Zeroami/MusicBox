package com.lzb.musicbox.fragment;

import com.lzb.musicbox.BoardMusicListActivity;
import com.lzb.musicbox.R;
import com.lzb.musicbox.adapter.BoardListAdapter;
import com.lzb.musicbox.entity.BillBoard;
import com.lzb.musicbox.utils.AppUtils;
import com.lzb.musicbox.utils.Constant;
import com.lzb.musicbox.utils.RedirectUtils;
import com.lzb.musicbox.utils.ToastUtils;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class NetFragment extends Fragment implements AdapterView.OnItemClickListener{

	private View layoutView;
	private Context mContext;

	private ListView lvBoardList;
	private BoardListAdapter boardListAdapter;
	protected List<BillBoard> billBoardList = new ArrayList<BillBoard>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getActivity();
		layoutView = inflater.inflate(R.layout.fragment_net, container,false);
		initView();
		initData();
		initAdapter();
		initListener();
		return layoutView;
	}

	private void initView(){
		lvBoardList = (ListView) layoutView.findViewById(R.id.lv_board_list);
	}

	private void initData(){
		billBoardList.clear();		// 防止多次调用
		billBoardList.add(new BillBoard(Constant.BOARD_TYPE_NEW,AppUtils.getString(R.string.board_type_new_title)));
		billBoardList.add(new BillBoard(Constant.BOARD_TYPE_HOT,AppUtils.getString(R.string.board_type_hot_title)));
		billBoardList.add(new BillBoard(Constant.BOARD_TYPE_BILLBOARD,AppUtils.getString(R.string.board_type_billboard_title)));
		billBoardList.add(new BillBoard(Constant.BOARD_TYPE_HITO_CHINESE,AppUtils.getString(R.string.board_type_hito_chinese_title)));
	}

	private void initAdapter(){
		boardListAdapter = new BoardListAdapter(mContext,R.layout.item_board,billBoardList);
		lvBoardList.setAdapter(boardListAdapter);
	}

	private void initListener(){
		lvBoardList.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		BillBoard billBoard = billBoardList.get(i);
		Bundle bundle = new Bundle();
		bundle.putInt("boardType",billBoard.getType());
		bundle.putString("boardTypeTitle", billBoard.getTitle());
		RedirectUtils.redirect(mContext, BoardMusicListActivity.class,bundle,false);
	}
}
