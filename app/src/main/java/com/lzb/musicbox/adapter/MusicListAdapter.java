package com.lzb.musicbox.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.andraskindler.quickscroll.Scrollable;
import com.lzb.musicbox.R;
import com.lzb.musicbox.db.DBUtilsBuilder;
import com.lzb.musicbox.entity.Mp3Info;
import com.lzb.musicbox.entity.Mp3ListCate;
import com.lzb.musicbox.service.MusicPlayService;
import com.lzb.musicbox.utils.AppUtils;
import com.lzb.musicbox.utils.Constant;
import com.lzb.musicbox.utils.LogUtils;
import com.lzb.musicbox.utils.MediaUtils;
import com.lzb.musicbox.utils.ToastUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MusicListAdapter extends ArrayAdapter<Mp3Info> implements OnClickListener, Scrollable {

    private Context mContext;
    private LayoutInflater inflater;
    private int mResourceId;
    private List<Mp3Info> mp3InfoList = new ArrayList<Mp3Info>();

    public void setMusicPlayService(MusicPlayService musicPlayService) {
        this.musicPlayService = musicPlayService;
    }

    private MusicPlayService musicPlayService;
    private int selectedPosition = -1;
    private List<Integer> operateViewPositionList = new ArrayList<Integer>();   // 已显示的操作位置列表

    private int type;   // 列表类型，即列表的id

    public MusicListAdapter(Context context, int textViewResourceId, List<Mp3Info> objects, int type) {
        super(context, textViewResourceId, objects);
        this.mContext = context;
        this.mResourceId = textViewResourceId;
        this.mp3InfoList = objects;
        this.type = type;
        inflater = LayoutInflater.from(mContext);
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public List<Integer> getOperateViewPositionList() {
        return operateViewPositionList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Mp3Info mp3Info = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = inflater.inflate(mResourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvMusicTitle = (TextView) view.findViewById(R.id.tv_item_music_title);
            viewHolder.tvMusicArtist = (TextView) view.findViewById(R.id.tv_item_music_artist);
            viewHolder.ivMusicAction = (ImageView) view.findViewById(R.id.iv_item_music_action);
            viewHolder.lyMusicOperateBar = (LinearLayout) view.findViewById(R.id.ly_music_operate_bar);
            viewHolder.lyAddTo = (LinearLayout) view.findViewById(R.id.ly_add_to);
            viewHolder.lyPrefer = (LinearLayout) view.findViewById(R.id.ly_prefer);
            viewHolder.lyShare = (LinearLayout) view.findViewById(R.id.ly_share);
            viewHolder.lySetBell = (LinearLayout) view.findViewById(R.id.ly_set_bell);
            viewHolder.lyDelete = (LinearLayout) view.findViewById(R.id.ly_delete);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.position = position;
        viewHolder.ivMusicAction.setTag(viewHolder);
        viewHolder.ivMusicAction.setOnClickListener(this);

        // 为音乐操作按钮设置事件监听
        viewHolder.lyAddTo.setTag(viewHolder);
        viewHolder.lyPrefer.setTag(viewHolder);
        viewHolder.lyShare.setTag(viewHolder);
        viewHolder.lySetBell.setTag(viewHolder);
        viewHolder.lyDelete.setTag(viewHolder);
        viewHolder.lyAddTo.setOnClickListener(this);
        viewHolder.lyPrefer.setOnClickListener(this);
        viewHolder.lyShare.setOnClickListener(this);
        viewHolder.lySetBell.setOnClickListener(this);
        viewHolder.lyDelete.setOnClickListener(this);

        viewHolder.tvMusicTitle.setText(mp3Info.getTitle());
        viewHolder.tvMusicArtist.setText(mp3Info.getArtist());
        if (position == selectedPosition) {
            viewHolder.tvMusicTitle.setTextColor(mContext.getResources().getColor(R.color.selected_text_color));
            viewHolder.tvMusicArtist.setTextColor(mContext.getResources().getColor(R.color.selected_text_color));
        } else {
            viewHolder.tvMusicTitle.setTextColor(mContext.getResources().getColor(R.color.default_text_color));
            viewHolder.tvMusicArtist.setTextColor(mContext.getResources().getColor(R.color.default_text_color));
        }

        if (operateViewPositionList.contains(position)) {       // 操作按钮显示状态
            viewHolder.ivMusicAction.setImageResource(R.drawable.icon_arrow_up);
            viewHolder.lyMusicOperateBar.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivMusicAction.setImageResource(R.drawable.icon_arrow_down);
            viewHolder.lyMusicOperateBar.setVisibility(View.GONE);
        }

        viewHolder.lyMusicOperateBar.setClickable(true);    // 设置操作条截取点击事件，onItemClick事件无效

        return view;
    }

    @Override
    public String getIndicatorForPosition(int childposition, int groupposition) {
        return mp3InfoList.get(childposition).getTitle();
    }

    @Override
    public int getScrollPosition(int childposition, int groupposition) {
        return childposition;
    }

    class ViewHolder {
        TextView tvMusicTitle;
        TextView tvMusicArtist;
        ImageView ivMusicAction;
        LinearLayout lyMusicOperateBar;
        LinearLayout lyAddTo;
        LinearLayout lyPrefer;
        LinearLayout lyShare;
        LinearLayout lySetBell;
        LinearLayout lyDelete;

        int position;       // 辅助性参数
    }

    @Override
    public void onClick(View v) {
        ViewHolder viewHolder = (ViewHolder) v.getTag();
        int position = viewHolder.position;
        //ToastUtils.shortToast(mContext, mp3InfoList.get(viewHolder.position).getTitle());
        switch (v.getId()) {
            case R.id.iv_item_music_action:
                changeOperateViewStatus(viewHolder, position);
                break;

            case R.id.ly_add_to:
                changeOperateViewStatus(viewHolder, position);// 隐藏操作按钮布局
                showListSelectDialog(position);
                break;

            case R.id.ly_prefer:
                changeOperateViewStatus(viewHolder, position);
                addMusicToList(Constant.LIST_PREFER_ID, position);
                break;

            case R.id.ly_share:
                changeOperateViewStatus(viewHolder, position);
                shareToOthers(position);
                break;

            case R.id.ly_set_bell:
                changeOperateViewStatus(viewHolder, position);
                showSetBellDialog(position);
                break;

            case R.id.ly_delete:
                changeOperateViewStatus(viewHolder, position);
                showDeleteDialog(position);
                break;
        }

        //notifyDataSetChanged();
    }


    private void changeOperateViewStatus(ViewHolder viewHolder, int position) {
        if (!operateViewPositionList.contains(position)) {    // 不存在就添加
            operateViewPositionList.add(position);
            viewHolder.ivMusicAction.setImageResource(R.drawable.icon_arrow_up);
            viewHolder.lyMusicOperateBar.setVisibility(View.VISIBLE);
        } else {        // 已存在则移除
            operateViewPositionList.remove((Integer) position);
            viewHolder.ivMusicAction.setImageResource(R.drawable.icon_arrow_down);
            viewHolder.lyMusicOperateBar.setVisibility(View.GONE);
        }
    }

    /**
     * 显示列表选择对话框
     */
    private void showListSelectDialog(final int position) {
        View view = inflater.inflate(R.layout.alert_list_select, null);
        TextView tvCreateList = (TextView) view.findViewById(R.id.tv_create_list);
        final RadioGroup rgList = (RadioGroup) view.findViewById(R.id.rg_list);
        DBUtilsBuilder.DBUtils dbUtils = DBUtilsBuilder.getInstance(Mp3ListCate.class);
        final List<Mp3ListCate> mp3ListCateList = dbUtils.table(Constant.TABLE_MP3_LIST_CATE).select();
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        for (Mp3ListCate mp3ListCate : mp3ListCateList) {
            if (mp3ListCate.getId() == Constant.LIST_PREFER_ID || mp3ListCate.getId() == Constant.LIST_LATEST_ID) {
                continue;
            }
            RadioButton rb = new RadioButton(mContext);
            rb.setText(mp3ListCate.getTitle());     // 显示
            rb.setTag(mp3ListCate.getId());         // 真实ID，用于数据库操作
            rgList.addView(rb, lp);
        }
        ((RadioButton) rgList.getChildAt(0)).setChecked(true);   // 默认选择第一个列表
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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
                        addMusicToList(listCateId, position);
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
                showListAddDialog(position);
            }
        });
    }


    /**
     * 显示新建列表对话框
     */
    private void showListAddDialog(final int position) {
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
                    int listCateId = dbUtils.table(Constant.TABLE_MP3_LIST_CATE).insert(mp3ListCate);
                    addMusicToList(listCateId, position);
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
     * @param position
     */
    private void addMusicToList(int listCateId, int position) {
        // 插入前需检验列表是否已存在该歌曲！存在则不插入
        Mp3Info mp3Info = mp3InfoList.get(position);
        mp3Info.setCateId(listCateId);
        DBUtilsBuilder.DBUtils dbUtils = DBUtilsBuilder.getInstance(Mp3Info.class);
        boolean isExist = dbUtils.table(Constant.TABLE_MP3_INFO).column(new String[]{"id"}).where("cateId=? and originMediaId=?", new String[]{String.valueOf(listCateId), String.valueOf(mp3Info.getOriginMediaId())}).isExist();
        if (isExist) {
            ToastUtils.longToast(AppUtils.getString(R.string.toast_add_to_list_exist));
        } else {
            dbUtils.table(Constant.TABLE_MP3_INFO).insert(mp3Info);
            ToastUtils.longToast(AppUtils.getString(R.string.toast_add_to_list_success));
        }
    }

    /**
     * 分享操作
     *
     * @param position
     */
    private void shareToOthers(int position) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mp3InfoList.get(position).getPath()));
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();//实例化MediaMetadataRetriever对象mmr
        mmr.setDataSource(mp3InfoList.get(position).getPath());//设置mmr对象的数据源为绝对路径
        String mimetypeString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);//获取音乐mime类型
        intent.setType(mimetypeString);
        mContext.startActivity(Intent.createChooser(intent, AppUtils.getString(R.string.send_to)));
    }

    /**
     * 显示设置铃声的对话框
     *
     * @param position
     */
    private void showSetBellDialog(final int position) {
        String[] items = new String[]{AppUtils.getString(R.string.type_ringtone), AppUtils.getString(R.string.type_alarm), AppUtils.getString(R.string.type_notification)};
        final int[] values = new int[]{RingtoneManager.TYPE_RINGTONE, RingtoneManager.TYPE_ALARM, RingtoneManager.TYPE_NOTIFICATION};
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(AppUtils.getString(R.string.choose_ringtone_type));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setBell(values[i], position);
            }
        });
        builder.show();
    }

    /**
     * 设置铃声
     *
     * @param ringtoneType
     * @param position
     */
    private void setBell(int ringtoneType, int position) {
        boolean isRingtone = false, isNotification = false, isAlarm = false, isMusic = false;
        ContentValues values = new ContentValues();
        switch (ringtoneType) {
            case RingtoneManager.TYPE_ALARM://闹铃
                isAlarm = true;
                values.put(MediaStore.Audio.Media.IS_ALARM, true);
                break;
            case RingtoneManager.TYPE_NOTIFICATION://通知
                isNotification = true;
                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                break;
            case RingtoneManager.TYPE_RINGTONE://来电
                isRingtone = true;
                values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            default:
                break;
        }
        Mp3Info mp3Info = mp3InfoList.get(position);


        // content://media/external/audio/media (MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        // content://media/internal/audio/media (系统媒体库 = MediaStore.Audio.Media.INTERNAL_CONTENT_URI)
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(mp3InfoList.get(position).getPath());

        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA + "=?", new String[]{mp3Info.getPath()}, null);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            mContext.getContentResolver().update(uri, values, MediaStore.Audio.Media._ID + "=?", new String[]{String.valueOf(id)});
            uri = ContentUris.withAppendedId(uri, id);
        } else {
            // content://media/internal/audio/media/id (插入系统媒体库的uri)
            values.put(MediaStore.MediaColumns.DATA, mp3Info.getPath());
            values.put(MediaStore.MediaColumns.TITLE, mp3Info.getTitle());
            values.put(MediaStore.MediaColumns.SIZE, mp3Info.getSize());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
            values.put(MediaStore.Audio.Media.ARTIST, mp3Info.getArtist());
            values.put(MediaStore.Audio.Media.DURATION, mp3Info.getDuration());
            values.put(MediaStore.Audio.Media.IS_RINGTONE, isRingtone);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, isNotification);
            values.put(MediaStore.Audio.Media.IS_ALARM, isAlarm);
            uri = mContext.getContentResolver().insert(uri, values);
        }
        RingtoneManager.setActualDefaultRingtoneUri(mContext, ringtoneType, uri);
    }

    /**
     * 显示删除对话框
     * @param position
     */
    private void showDeleteDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(AppUtils.getString(R.string.friend_tip));
        if (type == Constant.LIST_LOCAL_ID) {
            builder.setMessage(AppUtils.getString(R.string.comfirm_delete));
        } else {
            builder.setMessage(AppUtils.getString(R.string.comfirm_remove));
        }
        builder.setPositiveButton(AppUtils.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Mp3Info mp3Info = mp3InfoList.get(position);
                // 更新当前列表
                mp3InfoList.remove(position);
                notifyDataSetChanged();
                if (musicPlayService != null) {
                    // 判断是否删除的是服务正在播放的列表
                    if (type == musicPlayService.getCurrentPlayListId()) {
                        // 删除的时候为当前播放的歌曲
                        if (position == musicPlayService.getCurrentPosition()) {
                            musicPlayService.pause();
                            // 如果当前列表还有歌曲，播放下一首，否则默认播放本地歌曲
                            if (mp3InfoList.size() != 0) {
                                musicPlayService.setMp3InfoList(mp3InfoList);
                                if (position >= mp3InfoList.size()) {
                                    musicPlayService.play(0);
                                } else {
                                    musicPlayService.play(position);
                                }
                            } else {
                                musicPlayService.setMp3InfoList(MediaUtils.getMp3Infos(mContext));
                                musicPlayService.setCurrentPlayListId(Constant.LIST_LOCAL_ID);
                                musicPlayService.play(0);
                            }
                        }else if(position < musicPlayService.getCurrentPosition()){
                            musicPlayService.setMp3InfoList(mp3InfoList);
                            musicPlayService.setCurrentPosition(musicPlayService.getCurrentPosition() - 1);
                        }else{
                            musicPlayService.setMp3InfoList(mp3InfoList);
                        }
                    }
                }
                // 只有在本地列表删除才会删除本地歌曲文件
                if (type == Constant.LIST_LOCAL_ID) {
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
                    mContext.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media._ID + "=?", new String[]{String.valueOf(mp3Info.getOriginMediaId())});
                }
                // 从数据库删除
                String selection = null;
                String[] selectionArgs = null;
                if (type == Constant.LIST_LOCAL_ID){
                    selection = "originMediaId=?";
                    selectionArgs =  new String[]{String.valueOf(mp3Info.getOriginMediaId())};
                }else{
                    selection = "cateId=? and originMediaId=?";
                    selectionArgs = new String[]{String.valueOf(type),String.valueOf(mp3Info.getOriginMediaId())};
                }
                DBUtilsBuilder.getInstance(Mp3Info.class).table(Constant.TABLE_MP3_INFO).where(selection,selectionArgs).delete();
            }
        });
        builder.setNegativeButton(AppUtils.getString(R.string.cancel), null);
        builder.show();
    }
}
