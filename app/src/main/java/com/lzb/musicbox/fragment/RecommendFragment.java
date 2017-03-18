package com.lzb.musicbox.fragment;

import com.lzb.musicbox.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RecommendFragment extends Fragment {

	private View layoutView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		layoutView = inflater.inflate(R.layout.fragment_recommend, container,false);
		return layoutView;
	}
}
