package com.lzb.musicbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.lzb.musicbox.adapter.BatchActionListAdapter;
import com.lzb.musicbox.db.DBUtilsBuilder;
import com.lzb.musicbox.entity.Mp3Info;
import com.lzb.musicbox.entity.Mp3ListCate;
import com.lzb.musicbox.utils.AppUtils;
import com.lzb.musicbox.utils.Constant;
import com.lzb.musicbox.utils.LogUtils;
import com.lzb.musicbox.utils.MediaUtils;
import com.lzb.musicbox.utils.ToastUtils;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BatchActionListActivity extends BaseActivity implements OnClickListener, CompoundButton.OnCheckedChangeListener {

    private LayoutInflater inflater;
    private ListView lvMusicList;
    private BatchActionListAdapter batchActionListAdapter;
    private List<Mp3Info> mp3InfoList;

    private ImageView ivActionBack;
    private TextView tvTopListTitle;
    private CheckBox cbAllSelect;

    private TextView tvSelectTip;

    private TextView tvAddTo;
    private TextView tvDelete;

    private int listCateId = 0;

    private static int TYPE_INSERT = 1;
    private static int TYPE_DELETE = 2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_batch_action);
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
        lvMusicList = (ListView) findViewById(R.id.lv_music_list);
        ivActionBack = (ImageView) findViewById(R.id.iv_action_bar_back);
        tvTopListTitle = (TextView) findViewById(R.id.tv_top_list_title);
        cbAllSelect = (CheckBox) findViewById(R.id.cb_all_select);
        tvSelectTip = (TextView) findViewById(R.id.tv_select_tip);
        tvAddTo = (TextView) findViewById(R.id.tv_add_to);
        tvDelete = (TextView) findViewById(R.id.tv_delete);
    }

    private void initData() {
        inflater = LayoutInflater.from(this);
        if (listCateId == Constant.LIST_LOCAL_ID) {
            mp3InfoList = MediaUtils.getMp3Infos(this);
            for (Mp3Info mp3Info : mp3InfoList) {
                mp3Info.setOriginMediaId(mp3Info.getId());        // 在收藏到喜欢列表需要用到，避免用id与数据库的id冲突
            }
            tvTopListTitle.setText(AppUtils.getString(R.string.list_local));
        } else {
            String limit = null;
            String order = null;
            if (listCateId == Constant.LIST_LATEST_ID) {
                limit = "0,100";
                order = "id desc";
            }
            mp3InfoList =  DBUtilsBuilder.getInstance(Mp3Info.class).table(Constant.TABLE_MP3_INFO).where("cateId=?", new String[]{String.valueOf(listCateId)}).limit(limit).order(order).select();
            Mp3ListCate mp3ListCate = (Mp3ListCate) DBUtilsBuilder.getInstance(Mp3ListCate.class).table(Constant.TABLE_MP3_LIST_CATE).column(new String[]{"title"}).where("id=?", new String[]{String.valueOf(listCateId)}).find();
            tvTopListTitle.setText(mp3ListCate.getTitle());
        }
        updateSelectTip(0);
    }

    private void initAdapter() {
        batchActionListAdapter = new BatchActionListAdapter(this, R.layout.item_music_batch_action, mp3InfoList);
        lvMusicList.setAdapter(batchActionListAdapter);
    }

    private void initListener() {
        ivActionBack.setOnClickListener(this);
        cbAllSelect.setOnCheckedChangeListener(this);
        tvAddTo.setOnClickListener(this);
        tvDelete.setOnClickListener(this);
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

            case R.id.tv_add_to:
                if (batchActionListAdapter.getSelectedPositionList().size() == 0) {
                    ToastUtils.longToast(AppUtils.getString(R.string.no_music_selected));
                    return;
                }
                showListSelectDialog(batchActionListAdapter.getSelectedPositionList());
                break;

            case R.id.tv_delete:
                if (batchActionListAdapter.getSelectedPositionList().size() == 0) {
                    ToastUtils.longToast(AppUtils.getString(R.string.no_music_selected));
                    return;
                }
                showDeleteDialog(batchActionListAdapter.getSelectedPositionList());
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        List<Integer> list = new ArrayList<Integer>();
        if (b == true) {
            for (int i = 0; i < mp3InfoList.size(); i++) {
                list.add(i);
            }
        }
        batchActionListAdapter.setSelectedPositionList(list);
        batchActionListAdapter.notifyDataSetChanged();
    }

    /**
     * 更新选中提示
     *
     * @param selectedCount
     */
    public void updateSelectTip(int selectedCount) {
        tvSelectTip.setText(MessageFormat.format(AppUtils.getString(R.string.list_select_tip), mp3InfoList.size(), selectedCount));
    }

    /**
     * 显示列表选择对话框
     */
    private void showListSelectDialog(final List<Integer> selectedPositionList) {
        View view = inflater.inflate(R.layout.alert_list_select, null);
        TextView tvCreateList = (TextView) view.findViewById(R.id.tv_create_list);
        final RadioGroup rgList = (RadioGroup) view.findViewById(R.id.rg_list);
        DBUtilsBuilder.DBUtils dbUtils = DBUtilsBuilder.getInstance(Mp3ListCate.class);
        final List<Mp3ListCate> mp3ListCateList = dbUtils.table(Constant.TABLE_MP3_LIST_CATE).select();
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        for (Mp3ListCate mp3ListCate : mp3ListCateList) {
            if (mp3ListCate.getId() == Constant.LIST_PREFER_ID || mp3ListCate.getId() == Constant.LIST_LATEST_ID || mp3ListCate.getId() == listCateId) {
                continue;
            }
            RadioButton rb = new RadioButton(this);
            rb.setText(mp3ListCate.getTitle());     // 显示
            rb.setTag(mp3ListCate.getId());         // 真实ID，用于数据库操作
            rgList.addView(rb, lp);
        }
        ((RadioButton) rgList.getChildAt(0)).setChecked(true);   // 默认选择第一个列表
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(AppUtils.getString(R.string.alert_add_to_list_title));
        builder.setView(view);
        builder.setPositiveButton(AppUtils.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int ii) {
                // 获取选中的列表ID
                for (int i = 0; i < rgList.getChildCount(); i++) {
                    RadioButton rb = (RadioButton) rgList.getChildAt(i);
                    if (rb.isChecked()) {
                        int listCateId = (Integer) rb.getTag();
                        // 插入数据库，并退出循环
                        addMusicToList(listCateId, selectedPositionList);
                        break;
                    }
                }
            }
        });
        builder.setNegativeButton(AppUtils.getString(R.string.cancel), null);
        final Dialog dialog = builder.show();
        tvCreateList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showListAddDialog(selectedPositionList);
            }
        });
    }

    /**
     * 显示新建列表对话框
     */
    private void showListAddDialog(final List<Integer> selectedPositionList) {
        View view = inflater.inflate(R.layout.alert_list_add, null);
        final EditText etNewListTitle = (EditText) view.findViewById(R.id.et_new_list_title);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                    int listCateId = dbUtils.table(Constant.TABLE_MP3_LIST_CATE).insert(mp3ListCate);
                    addMusicToList(listCateId, selectedPositionList);
                }
            }
        });
        builder.setNegativeButton(AppUtils.getString(R.string.cancel), null);
        builder.show();
    }

    /**
     * 执行插入数据库操作
     *
     * @param listCateId
     * @param selectedPositionList
     */
    private void addMusicToList(int listCateId, List<Integer> selectedPositionList) {
        // 插入前需检验列表是否已存在该歌曲！存在则不插入
        DBUtilsBuilder.DBUtils dbUtils = DBUtilsBuilder.getInstance(Mp3Info.class);
        List<Mp3Info> existMp3InfoList = dbUtils.table(Constant.TABLE_MP3_INFO).column(new String[]{"originMediaId"}).where("cateId=?", new String[]{String.valueOf(listCateId)}).select();
        filterSelectedPositionList(selectedPositionList, existMp3InfoList);
        // 开始插入
        new DataActionAsyncTask(listCateId, selectedPositionList, TYPE_INSERT).execute();
    }

    /**
     * 过滤掉数据库已存在的选择歌曲
     *
     * @param selectedPositionList
     * @param existMp3InfoList
     */
    private void filterSelectedPositionList(List<Integer> selectedPositionList, List<Mp3Info> existMp3InfoList) {
        List<Integer> existOriginMediaIdList = new ArrayList<Integer>();
        List<Integer> existSelectedPositionList = new ArrayList<Integer>();
        for (Mp3Info mp3Info : existMp3InfoList) {
            existOriginMediaIdList.add((int) mp3Info.getOriginMediaId());
        }
        for (int selectedPosition : selectedPositionList) {
            if (existOriginMediaIdList.contains(Integer.valueOf((int) mp3InfoList.get(selectedPosition).getOriginMediaId()))) {
                existSelectedPositionList.add(selectedPosition);
            }
        }
        selectedPositionList.removeAll(existSelectedPositionList);
    }

    /**
     * 显示删除对话框
     *
     * @param selectedPositionList
     */
    private void showDeleteDialog(final List<Integer> selectedPositionList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(AppUtils.getString(R.string.friend_tip));
        if (listCateId == Constant.LIST_LOCAL_ID) {
            builder.setMessage(AppUtils.getString(R.string.comfirm_delete));
        } else {
            builder.setMessage(AppUtils.getString(R.string.comfirm_remove));
        }
        builder.setPositiveButton(AppUtils.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new DataActionAsyncTask(listCateId,selectedPositionList,TYPE_DELETE).execute();
            }
        });
        builder.setNegativeButton(AppUtils.getString(R.string.cancel), null);
        builder.show();
    }

    private class DataActionAsyncTask extends AsyncTask<Void, Integer, Void> {
        private int type;
        private int listCateId;
        private List<Integer> selectedPositionList;
        private int total;
        private int current = 0;
        private ProgressDialog progressDialog;

        public DataActionAsyncTask(int listCateId, List<Integer> selectedPositionList,int type) {
            this.listCateId = listCateId;
            this.selectedPositionList = selectedPositionList;
            total = selectedPositionList.size();
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(BatchActionListActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(total);
            if (type == TYPE_INSERT){
                progressDialog.setMessage(MessageFormat.format(AppUtils.getString(R.string.adding), 0, total));
            }else if (type == TYPE_DELETE){
                progressDialog.setMessage(MessageFormat.format(AppUtils.getString(R.string.deleting), 0, total));
            }
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... objects) {
            if (type == TYPE_INSERT){
                doAddAction();
            }else if (type == TYPE_DELETE){
                doDeleteAction();
            }
            return null;
        }

        private void doAddAction() {
            DBUtilsBuilder.DBUtils dbUtils = DBUtilsBuilder.getInstance(Mp3Info.class);
            for (int selectedPosition : selectedPositionList) {
                Mp3Info mp3Info = mp3InfoList.get(selectedPosition);
                mp3Info.setCateId(listCateId);
                dbUtils.table(Constant.TABLE_MP3_INFO).insert(mp3Info);
                current++;
                publishProgress(current);
            }
        }
        private void doDeleteAction() {
            boolean isContain = false;
            int beforePosition = 0;
            for (int position : selectedPositionList) {
                Mp3Info mp3Info = mp3InfoList.get(position);
                // 更新当前列表
                if (musicPlayService != null) {
                    // 判断是否删除的是服务正在播放的列表
                    if (listCateId == musicPlayService.getCurrentPlayListId()) {
                        // 如果删除的position包含服务正在播放的position
                        if (selectedPositionList.contains((Integer) musicPlayService.getCurrentPosition())) {
                            musicPlayService.pause();
                            isContain = true;
                        }
                        // 判断在当前播放歌曲前有多少首歌曲被删除
                        if (position < musicPlayService.getCurrentPosition()) {
                            beforePosition ++;
                        }
                    }
                }
                // 只有在本地列表删除才会删除本地歌曲文件
                if (listCateId == Constant.LIST_LOCAL_ID) {
                    // 从文件系统删除
                    File songFile = new File(mp3Info.getPath());
                    File lrcFile = new File(Constant.MUSIC_DIR + File.separator + Constant.LRC_NAME + File.separator + mp3Info.getArtist() + "-" + mp3Info.getTitle() + ".lrc");
                    if (songFile.exists()) {
                        songFile.delete();
                    }
                    if (lrcFile.exists()) {
                        lrcFile.delete();
                    }
                    // 从媒体库删除
                    BatchActionListActivity.this.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media._ID + "=?", new String[]{String.valueOf(mp3Info.getOriginMediaId())});
                }
                // 从数据库删除
                String selection = null;
                String[] selectionArgs = null;
                if (listCateId == Constant.LIST_LOCAL_ID){
                    selection = "originMediaId=?";
                    selectionArgs =  new String[]{String.valueOf(mp3Info.getOriginMediaId())};
                }else{
                    selection = "cateId=? and originMediaId=?";
                    selectionArgs = new String[]{String.valueOf(listCateId),String.valueOf(mp3Info.getOriginMediaId())};
                }
                DBUtilsBuilder.getInstance(Mp3Info.class).table(Constant.TABLE_MP3_INFO).where(selection, selectionArgs).delete();
            }

            List<Mp3Info> selectedList = new ArrayList<Mp3Info>();
            for (int position : selectedPositionList) {
                selectedList.add(mp3InfoList.get(position));
            }
            mp3InfoList.removeAll(selectedList);
            // 如果播放的是当前列表才需要进行处理
            if (listCateId == musicPlayService.getCurrentPlayListId()) {
                // 如果没有包含当前播放歌曲，则该列表不会为空，至少还剩下当前播放歌曲
                if (!isContain) {
                    musicPlayService.setMp3InfoList(mp3InfoList);
                    musicPlayService.setCurrentPosition(musicPlayService.getCurrentPosition() - beforePosition);
                } else {
                    // 如果当前列表还不为空
                    if (mp3InfoList.size() != 0) {
                        musicPlayService.setMp3InfoList(mp3InfoList);
                        musicPlayService.play(0);
                    } else {
                        // 如果为空，默认播放本地列表
                        musicPlayService.setMp3InfoList(MediaUtils.getMp3Infos(BatchActionListActivity.this));
                        musicPlayService.setCurrentPlayListId(Constant.LIST_LOCAL_ID);
                        musicPlayService.play(0);
                    }
                }
                musicPlayService.setListChange(true);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog != null) {
                progressDialog.cancel();
                progressDialog = null;
            }
            if (type == TYPE_INSERT) {
                Mp3ListCate mp3ListCate = DBUtilsBuilder.getInstance(Mp3ListCate.class).table(Constant.TABLE_MP3_LIST_CATE).where("id=?", new String[]{String.valueOf(this.listCateId)}).find();
                ToastUtils.longToast(MessageFormat.format(AppUtils.getString(R.string.toast_add_mutil_to_list_success), mp3ListCate.getTitle(), total));
            }else if (type == TYPE_DELETE){
                cbAllSelect.setChecked(false);
                updateSelectTip(0);
                batchActionListAdapter = new BatchActionListAdapter(BatchActionListActivity.this,R.layout.item_music_batch_action,mp3InfoList);
                lvMusicList.setAdapter(batchActionListAdapter);
                if (listCateId == Constant.LIST_LOCAL_ID){
                    ToastUtils.longToast(MessageFormat.format(AppUtils.getString(R.string.toast_delete_mutil_from_list_success),AppUtils.getString(R.string.list_local), total));
                }else{
                    Mp3ListCate mp3ListCate = DBUtilsBuilder.getInstance(Mp3ListCate.class).table(Constant.TABLE_MP3_LIST_CATE).where("id=?", new String[]{String.valueOf(this.listCateId)}).find();
                    ToastUtils.longToast(MessageFormat.format(AppUtils.getString(R.string.toast_delete_mutil_from_list_success), mp3ListCate.getTitle(), total));
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
            if (type == TYPE_INSERT){
                progressDialog.setMessage(MessageFormat.format(AppUtils.getString(R.string.adding), values[0], total));
            }else if (type == TYPE_DELETE){
                progressDialog.setMessage(MessageFormat.format(AppUtils.getString(R.string.deleting), values[0], total));
            }
        }

    }


    @Override
    protected void change(int position) {

    }

    @Override
    protected void publish(long progress) {

    }

    @Override
    protected void bindServiceSuccess() {

    }

}
