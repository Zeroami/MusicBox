package com.lzb.musicbox;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.lzb.musicbox.adapter.ContentFragmentPagerAdapter;
import com.lzb.musicbox.fragment.MyFragment;
import com.lzb.musicbox.fragment.NetFragment;
import com.lzb.musicbox.fragment.SearchFragment;
import com.lzb.musicbox.service.MusicPlayService;
import com.lzb.musicbox.utils.RedirectUtils;
import com.lzb.musicbox.view.PlayControlView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements OnClickListener{

	private ViewPager vpMainContent;
	private ContentFragmentPagerAdapter contentAdapter;
	private List<Fragment> fragmentList;

	private TextView tvNavMy;
	private TextView tvNavNet;
	private TextView tvNavSearch;
	private List<TextView> tabNavList;

	private MyFragment myFragment;
	private NetFragment netFragment;
	private SearchFragment searchFragment;

	private ImageView ivTabLine;
	private LayoutParams tabLineLayoutParams;
	private int mScreenWidth;

	private ImageView ivShowMenu;
	private TextView tvShowLrc;

	private PlayControlView playControlView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initFragment();
		initData();
		initListener();
		initViewPager();

	}

	private void initView(){
		vpMainContent = (ViewPager) findViewById(R.id.vp_main_content);
		ivShowMenu = (ImageView) findViewById(R.id.iv_action_bar_menu);
		tvShowLrc = (TextView) findViewById(R.id.tv_action_bar_lyric);
		ivTabLine = (ImageView) findViewById(R.id.iv_nav_tab_line);
		tvNavMy = (TextView) findViewById(R.id.tv_nav_my);
		tvNavNet = (TextView) findViewById(R.id.tv_nav_net);
		tvNavSearch = (TextView) findViewById(R.id.tv_nav_search);
		playControlView = (PlayControlView) findViewById(R.id.view_play_control);
	}

	private void initFragment(){
		fragmentList = new ArrayList<Fragment>();
		myFragment = new MyFragment();
		netFragment = new NetFragment();
		searchFragment = new SearchFragment();
		fragmentList.add(myFragment);
		fragmentList.add(netFragment);
		fragmentList.add(searchFragment);
	}

	private void initData(){
		initTabLine();
		tabNavList = new ArrayList<TextView>();
		tabNavList.add(tvNavMy);
		tabNavList.add(tvNavNet);
		tabNavList.add(tvNavSearch);
	}

	private void initTabLine(){
		WindowManager wm = getWindowManager();
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		mScreenWidth = outMetrics.widthPixels;
		tabLineLayoutParams = (LayoutParams) ivTabLine.getLayoutParams();
		tabLineLayoutParams.width = mScreenWidth/3;
		ivTabLine.setLayoutParams(tabLineLayoutParams);
	}

	private void initListener(){

		ivShowMenu.setOnClickListener(this);
		tvShowLrc.setOnClickListener(this);

		for(TextView tv : tabNavList){
			tv.setOnClickListener(this);
		}

		vpMainContent.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {

			}

			@Override
			public void onPageScrolled(int position, float offset, int offsetPixel) {
				tabLineLayoutParams.leftMargin = (int) (position * (mScreenWidth * 1.0f / 3) + offsetPixel * 1.0f / 3);
				ivTabLine.setLayoutParams(tabLineLayoutParams);
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}


	private void initViewPager(){
		contentAdapter = new ContentFragmentPagerAdapter(getSupportFragmentManager(), fragmentList);
		vpMainContent.setAdapter(contentAdapter);
		vpMainContent.setCurrentItem(0);
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

		for(int i=0;i<tabNavList.size();i++){
			if(v == tabNavList.get(i)){
				vpMainContent.setCurrentItem(i);
				return ;
			}
		}

		switch (v.getId()) {
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
	protected void change(int position) {
		playControlView.changeUI(position);
	}

	@Override
	protected void publish(long progress) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void bindServiceSuccess() {
		playControlView.setMusicPlayService(musicPlayService);
	}

	public MusicPlayService getMusicPlayService(){
		return musicPlayService;
	}
}
