package com.lzb.musicbox.fragment;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.lzb.musicbox.LocalMusicListActivity;
import com.lzb.musicbox.R;
import com.lzb.musicbox.UserMusicListActivity;
import com.lzb.musicbox.app.App;
import com.lzb.musicbox.db.DBUtilsBuilder;
import com.lzb.musicbox.entity.Mp3ListCate;
import com.lzb.musicbox.entity.Result;
import com.lzb.musicbox.utils.AppUtils;
import com.lzb.musicbox.utils.Constant;
import com.lzb.musicbox.utils.MediaUtils;
import com.lzb.musicbox.utils.RedirectUtils;
import com.lzb.musicbox.utils.ToastUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyFragment extends Fragment implements OnClickListener {

    private View layoutView;
    private Context mContext;
    private LayoutInflater inflater;

    private LinearLayout lyLocalList;
    private LinearLayout lyDownloadList;
    private LinearLayout lyBtnAddList;

    private LinearLayout lyListContainer;
    private List<LinearLayout> listItemList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //LogUtils.i("onCreateView");
        layoutView = inflater.inflate(R.layout.fragment_my, container, false);
        mContext = getActivity();
        initView();
        initData();
        initListener();

        return layoutView;
    }

    private void initView() {
        lyLocalList = (LinearLayout) layoutView.findViewById(R.id.ly_list_local);
//        lyDownloadList = (LinearLayout) layoutView.findViewById(R.id.ly_list_download);
        lyListContainer = (LinearLayout) layoutView.findViewById(R.id.ly_list_container);
        lyBtnAddList = (LinearLayout) layoutView.findViewById(R.id.ly_list_add);
    }

    private void initData() {
        inflater = LayoutInflater.from(mContext);
    }

    private void initListener() {
        lyLocalList.setOnClickListener(this);
//        lyDownloadList.setOnClickListener(this);
        lyBtnAddList.setOnClickListener(this);
    }

    /**
     * 根据数据库动态添加列表项并添加事件监听
     */
    private void initListItem() {
        lyListContainer.removeAllViews();
        DBUtilsBuilder.DBUtils dbUtilsForCate = DBUtilsBuilder.getInstance(Mp3ListCate.class);
        DBUtilsBuilder.DBUtils dbUtilsForResult = DBUtilsBuilder.getInstance(Result.class);
        // 检查当前语言并改变数据库中列表名称
        if (App.getSP().getBoolean("isLanguageChange",false)){
            // 如果语言已改变，更新列表名称
            Mp3ListCate mp3ListCate = new Mp3ListCate();
            mp3ListCate.setTitle(AppUtils.getString(R.string.list_prefer));
            dbUtilsForCate.table(Constant.TABLE_MP3_LIST_CATE).column(new String[]{"title"}).where("id=?",new String[]{String.valueOf(Constant.LIST_PREFER_ID)}).update(mp3ListCate);
            mp3ListCate.setTitle(AppUtils.getString(R.string.list_default));
            dbUtilsForCate.table(Constant.TABLE_MP3_LIST_CATE).column(new String[]{"title"}).where("id=?",new String[]{String.valueOf(Constant.LIST_DEFAULT_ID)}).update(mp3ListCate);
            mp3ListCate.setTitle(AppUtils.getString(R.string.list_latest));
            dbUtilsForCate.table(Constant.TABLE_MP3_LIST_CATE).column(new String[]{"title"}).where("id=?",new String[]{String.valueOf(Constant.LIST_LATEST_ID)}).update(mp3ListCate);
        }
        List<Mp3ListCate> mp3ListCateList = dbUtilsForCate.table(Constant.TABLE_MP3_LIST_CATE).select();
        if (mp3ListCateList != null || mp3ListCateList.size() == 0) {
            listItemList = new ArrayList<LinearLayout>();
            for (final Mp3ListCate mp3ListCate : mp3ListCateList) {
                // 获取listItem的模板
                LinearLayout lyListItem = (LinearLayout) inflater.inflate(R.layout.layout_list_item, null);
                // 改变UI
                ImageView ivListIcon = (ImageView) lyListItem.findViewById(R.id.iv_list_icon);
                TextView tvListTitle = (TextView) lyListItem.findViewById(R.id.tv_list_title);
                TextView tvListSize = (TextView) lyListItem.findViewById(R.id.tv_list_size);
                if (mp3ListCate.getId() == Constant.LIST_PREFER_ID) {
                    ivListIcon.setImageResource(R.drawable.icon_list_prefer);
                } else if (mp3ListCate.getId() == Constant.LIST_DEFAULT_ID) {
                    ivListIcon.setImageResource(R.drawable.icon_list_default);
                } else if (mp3ListCate.getId() == Constant.LIST_LATEST_ID) {
                    ivListIcon.setImageResource(R.drawable.icon_list_latest);
                } else {
                    ivListIcon.setImageResource(R.drawable.icon_stop);
                    // 为自定义列表添加重命名和删除操作
                    lyListItem.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            showListOperateBtn(mp3ListCate);
                            return false;
                        }
                    });
                }
                tvListTitle.setText(mp3ListCate.getTitle());
                Result result = (Result) dbUtilsForResult.table(Constant.TABLE_MP3_INFO).column(new String[]{"count(1) as count"}).where("cateId=?", new String[]{String.valueOf(mp3ListCate.getId())}).find();
                tvListSize.setText(MessageFormat.format(AppUtils.getString(R.string.music_count), result.getCount()));
                if (mp3ListCate.getId() == Constant.LIST_LATEST_ID && result.getCount() > 100) {
                    tvListSize.setText(MessageFormat.format(AppUtils.getString(R.string.music_count), 100));
                }
                // 设置监听器
                lyListItem.setTag(mp3ListCate.getId());
                //lyListItem.setClickable(true);
                lyListItem.setOnClickListener(this);
                lyListContainer.addView(lyListItem);
            }
        }
    }

    /**
     * 显示列表操作对话框
     */
    private void showListOperateBtn(final Mp3ListCate mp3ListCate) {
        View view = inflater.inflate(R.layout.alert_list_operate, null);
        Button btnRename = (Button) view.findViewById(R.id.btn_list_operate_rename);
        Button btnDelete = (Button) view.findViewById(R.id.btn_list_operate_delete);
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(AppUtils.getString(R.string.alert_list_operate_title));
        builder.setView(view);
        final Dialog dialog = builder.show();
        btnRename.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 关闭原来的对话框，显示重命名对话框
                dialog.dismiss();
                showListRenameDialog(mp3ListCate);
            }
        });
        btnDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 关闭原来的对话框，从数据库删除该列表，并重新初始化列表UI
                dialog.dismiss();
                DBUtilsBuilder.DBUtils dbUtils = DBUtilsBuilder.getInstance(Mp3ListCate.class);
                dbUtils.table(Constant.TABLE_MP3_LIST_CATE).where("id=?", new String[]{String.valueOf(mp3ListCate.getId())}).delete();
                initListItem();
            }
        });
    }

    /**
     * 显示列表重命名对话框
     *
     * @param mp3ListCate
     */
    private void showListRenameDialog(final Mp3ListCate mp3ListCate) {
        View view = inflater.inflate(R.layout.alert_list_add, null);
        final EditText etNewListTitle = (EditText) view.findViewById(R.id.et_new_list_title);
        etNewListTitle.setText(mp3ListCate.getTitle());
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(AppUtils.getString(R.string.alert_list_rename_title));
        builder.setView(view);
        builder.setPositiveButton(AppUtils.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mp3ListCate.setTitle(etNewListTitle.getText().toString());
                DBUtilsBuilder.DBUtils dbUtils = DBUtilsBuilder.getInstance(Mp3ListCate.class);
                boolean isExist = dbUtils.table(Constant.TABLE_MP3_LIST_CATE).column(new String[]{"id"}).where("title=?", new String[]{mp3ListCate.getTitle()}).isExist();
                if (isExist) {
                    ToastUtils.longToast(AppUtils.getString(R.string.toast_error_list_exist));
                } else {
                    dbUtils.table(Constant.TABLE_MP3_LIST_CATE).column(new String[]{"title"}).where("id=?", new String[]{String.valueOf(mp3ListCate.getId())}).update(mp3ListCate);
                    initListItem();
                }
            }
        });
        builder.setNegativeButton(AppUtils.getString(R.string.cancel), null);
        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        changeLocalMusicCount(lyLocalList, MediaUtils.getMp3Infos(mContext).size());
        initListItem();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ly_list_local:
                RedirectUtils.redirect(mContext, LocalMusicListActivity.class, false);
                break;

//            case R.id.ly_list_download:
//
//                break;

            case R.id.ly_list_add:
                showListAddDialog();
                break;

            default:
                int tag = (Integer) v.getTag();
                Bundle bundle = new Bundle();
                bundle.putInt("listCateId", tag);
                RedirectUtils.redirect(mContext, UserMusicListActivity.class, bundle, false);
                break;
        }
    }

    /**
     * 显示新建列表对话框
     */
    private void showListAddDialog() {
        View view = inflater.inflate(R.layout.alert_list_add, null);
        final EditText etNewListTitle = (EditText) view.findViewById(R.id.et_new_list_title);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(AppUtils.getString(R.string.alert_list_add_title));
        builder.setView(view);
        builder.setPositiveButton(AppUtils.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Mp3ListCate mp3ListCate = new Mp3ListCate();
                mp3ListCate.setTitle(etNewListTitle.getText().toString());
                DBUtilsBuilder.DBUtils dbUtils = DBUtilsBuilder.getInstance(Mp3ListCate.class);
                boolean isExist = dbUtils.table(Constant.TABLE_MP3_LIST_CATE).column(new String[]{"id"}).where("title=?", new String[]{mp3ListCate.getTitle()}).isExist();
                if (isExist) {
                    ToastUtils.longToast(AppUtils.getString(R.string.toast_error_list_exist));
                } else {
                    dbUtils.table(Constant.TABLE_MP3_LIST_CATE).insert(mp3ListCate);
                    initListItem();
                }
            }
        });
        builder.setNegativeButton(AppUtils.getString(R.string.cancel), null);
        builder.show();
    }


    private void changeLocalMusicCount(LinearLayout lyContainer, int count) {
        TextView tvCount = (TextView) ((LinearLayout) lyContainer.getChildAt(1)).getChildAt(1);
        tvCount.setText(MessageFormat.format(AppUtils.getString(R.string.music_count), count));
    }
}
