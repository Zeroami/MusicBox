package com.lzb.musicbox.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.lzb.musicbox.R;
import com.lzb.musicbox.utils.LogUtils;

/**
 * Created by Administrator on 2016/1/26.
 */
public class PullUpListView extends ListView implements AbsListView.OnScrollListener, View.OnTouchListener {

    private Context mContext;

    private LinearLayout lyFooterRefresh;
    private LinearLayout lyRefresh;
    private LinearLayout lyProgressTip;
    private LinearLayout lyEnd;

    private boolean isLastRow = false;
    private int listViewPaddingBottonm;

    private int downY;
    private int moveY;
    private int upY;

    private final int RESET_LIST_VIEW_PADDING = 3;
    private int totalItemCount;
    private boolean isSearchEnd = false;

    public interface OnPullUpListener{
        void onTouchUp();       // 上拉后手指抬起
    }

    public void setOnPullUpListener(OnPullUpListener onPullUpListener) {
        this.onPullUpListener = onPullUpListener;
    }

    private OnPullUpListener onPullUpListener;

    private boolean isOne = true;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg){
            switch (msg.what){
                case RESET_LIST_VIEW_PADDING:
                    setPadding(getPaddingLeft(), getPaddingTop(),getPaddingRight(),listViewPaddingBottonm);
                    lyRefresh.setVisibility(View.VISIBLE);
                    lyProgressTip.setVisibility(View.GONE);
                    break;
            }
        }
    };


    public PullUpListView(Context context) {
        this(context, null);
    }

    public PullUpListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullUpListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initFooterView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isOne){
            initData();
            initListener();
            isOne = false;
        }
    }


    private void initFooterView() {
        // 初始化footerView
        lyFooterRefresh = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.layout_listview_footer_refresh, null);
        lyRefresh = (LinearLayout) lyFooterRefresh.findViewById(R.id.ly_refresh);
        lyProgressTip = (LinearLayout) lyFooterRefresh.findViewById(R.id.ly_progress_tip);
        lyEnd = (LinearLayout) lyFooterRefresh.findViewById(R.id.ly_end);
        addFooterView(lyFooterRefresh);
        lyFooterRefresh.setVisibility(View.GONE);
        lyFooterRefresh.setClickable(true);     // 抢占点击事件
    }

    private void initData() {
        listViewPaddingBottonm = getPaddingBottom();
    }

    private void initListener() {
        setOnScrollListener(this);
        setOnTouchListener(this);
        lyRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onPullUpListener != null){
                    onPullUpListener.onTouchUp();
                }
                lyRefresh.setVisibility(View.GONE);
                lyProgressTip.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        //正在滚动时回调，回调2-3次，手指没抛则回调2次。scrollState = 2的这次不回调
        //回调顺序如下
        //第1次：scrollState = SCROLL_STATE_TOUCH_SCROLL(1) 正在滚动
        //第2次：scrollState = SCROLL_STATE_FLING(2) 手指做了抛的动作（手指离开屏幕前，用力滑了一下）
        //第3次：scrollState = SCROLL_STATE_IDLE(0) 停止滚动
        //当屏幕停止滚动时为0；当屏幕滚动且用户使用的触碰或手指还在屏幕上时为1；
        //由于用户的操作，屏幕产生惯性滑动时为2
        //当滚到最后一行且停止滚动时，执行加载
        //if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
        //加载元素
        //......
        //    isLastRow = false;
        //}
        if (isLastRow && scrollState == OnScrollListener.SCROLL_STATE_IDLE){

        }
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //滚动时一直回调，直到停止滚动时才停止回调。单击时回调一次。
        //firstVisibleItem：当前能看见的第一个列表项ID（从0开始）
        //visibleItemCount：当前能看见的列表项个数（小半个也算）
        //totalItemCount：列表项共数
        //判断是否滚到最后一行
        //if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
        //    isLastRow = true;
        //}
        isLastRow = false;
        if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 1 && firstVisibleItem != 0 && isLastRow == false){
            // 因为加入了footerView，totalItemCount默认为1
            // 当条件满足但firstVisibleItem == 0 表示数据一页已显示完，则不需要显示footerView
            if (lyFooterRefresh.getVisibility() == View.GONE){
                lyFooterRefresh.setVisibility(View.VISIBLE);
            }
            isLastRow = true;
            this.totalItemCount = totalItemCount;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (isLastRow && !isSearchEnd){
                    downY = (int) motionEvent.getY();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isLastRow && !isSearchEnd){
                    moveY = (int) motionEvent.getY();
                    if (moveY < downY){
                        // 向上
                        setSelection(totalItemCount);
                        setPadding(getPaddingLeft(),getPaddingTop(),getPaddingRight(),listViewPaddingBottonm + (downY-moveY));
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (isLastRow && !isSearchEnd){
                    isLastRow = false;
                    upY = (int) motionEvent.getY();
                    if (upY < downY){
                        // 向上
                        load();
                    }
                }

                break;

        }
        return false;
    }

    private void load() {
        if (onPullUpListener != null){
            onPullUpListener.onTouchUp();
        }
        lyRefresh.setVisibility(View.GONE);
        lyProgressTip.setVisibility(View.VISIBLE);
    }

    /**
     * 重置padding
     */
    private void resetListViewPadding() {
        mHandler.sendEmptyMessage(RESET_LIST_VIEW_PADDING);
    }

    /**
     * 分页加载完成
     */
    public void loadEndDo() {
        resetListViewPadding();
    }

    /**
     * 搜索初始化
     */
    public void searchInit(){
        lyRefresh.setVisibility(View.VISIBLE);
        lyProgressTip.setVisibility(View.GONE);
        lyEnd.setVisibility(View.GONE);
        isSearchEnd = false;
    }

    /**
     * 全部记录搜索完成
     */
    public void searchEnd(){
        lyRefresh.setVisibility(View.GONE);
        lyProgressTip.setVisibility(View.GONE);
        lyEnd.setVisibility(View.VISIBLE);
        isSearchEnd = true;
    }
}
