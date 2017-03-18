package com.lzb.musicbox.adapter;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.andraskindler.quickscroll.Scrollable;
import com.lzb.musicbox.BatchActionListActivity;
import com.lzb.musicbox.R;
import com.lzb.musicbox.db.DBUtilsBuilder;
import com.lzb.musicbox.entity.Mp3Info;
import com.lzb.musicbox.entity.Mp3ListCate;
import com.lzb.musicbox.service.MusicPlayService;
import com.lzb.musicbox.utils.AppUtils;
import com.lzb.musicbox.utils.Constant;
import com.lzb.musicbox.utils.LogUtils;
import com.lzb.musicbox.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BatchActionListAdapter extends ArrayAdapter<Mp3Info> implements CompoundButton.OnCheckedChangeListener {

    private Context mContext;
    private LayoutInflater inflater;
    private int mResourceId;
    private List<Mp3Info> mp3InfoList = new ArrayList<Mp3Info>();

    public void setSelectedPositionList(List<Integer> selectedPositionList) {
        this.selectedPositionList = selectedPositionList;
    }

    public List<Integer> getSelectedPositionList() {
        return selectedPositionList;
    }

    private List<Integer> selectedPositionList = new ArrayList<Integer>();

    public BatchActionListAdapter(Context context, int textViewResourceId, List<Mp3Info> objects) {
        super(context, textViewResourceId, objects);
        this.mContext = context;
        this.mResourceId = textViewResourceId;
        this.mp3InfoList = objects;
        inflater = LayoutInflater.from(mContext);
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
            viewHolder.cbMusic = (CheckBox) view.findViewById(R.id.cb_item_music);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.cbMusic.setTag(position);
        viewHolder.cbMusic.setOnCheckedChangeListener(this);

        viewHolder.tvMusicTitle.setText(mp3Info.getTitle());
        viewHolder.tvMusicArtist.setText(mp3Info.getArtist());
        if (selectedPositionList.contains(position)){
            viewHolder.cbMusic.setChecked(true);
        }else{
            viewHolder.cbMusic.setChecked(false);
        }

        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Integer position = (Integer) compoundButton.getTag();
        if (selectedPositionList.contains(position)){
            if (!compoundButton.isChecked()){
                selectedPositionList.remove(position);
            }
        }else{
            if (compoundButton.isChecked()){
                selectedPositionList.add(position);
            }
        }
        ((BatchActionListActivity)mContext).updateSelectTip(selectedPositionList.size());
    }


    class ViewHolder {
        TextView tvMusicTitle;
        TextView tvMusicArtist;
        CheckBox cbMusic;
    }
}
