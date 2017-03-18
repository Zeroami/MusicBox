package com.lzb.musicbox.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lzb.musicbox.R;
import com.lzb.musicbox.entity.NetMusic;
import com.lzb.musicbox.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class NetMusicListAdapter extends ArrayAdapter<NetMusic>{

    private Context mContext;
    private LayoutInflater inflater;
    private int mResourceId;
    private List<NetMusic> netMusicList = new ArrayList<NetMusic>();

    private String type;

    public void setType(String type){
        this.type = type;
    }

    public NetMusicListAdapter(Context context, int textViewResourceId, List<NetMusic> objects) {
        super(context, textViewResourceId, objects);
        this.mContext = context;
        this.mResourceId = textViewResourceId;
        this.netMusicList = objects;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NetMusic netMusic = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = inflater.inflate(mResourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvMusicNum = (TextView) view.findViewById(R.id.tv_item_music_num);
            viewHolder.tvMusicTitle = (TextView) view.findViewById(R.id.tv_item_music_title);
            viewHolder.tvMusicArtist = (TextView) view.findViewById(R.id.tv_item_music_artist);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.tvMusicNum.setText(String.valueOf(position + 1));
        if (position < 3){
            viewHolder.tvMusicNum.setTextColor(AppUtils.getColor(R.color.top3_text_color));
        }else{
            viewHolder.tvMusicNum.setTextColor(AppUtils.getColor(R.color.default_text_color));
        }
        viewHolder.tvMusicTitle.setText(netMusic.getTitle());
        viewHolder.tvMusicArtist.setText(netMusic.getArtist());

        if (type != null && type.equals("search")){
            viewHolder.tvMusicNum.setVisibility(View.GONE);
        }

        return view;
    }

    class ViewHolder {
        TextView tvMusicNum;
        TextView tvMusicTitle;
        TextView tvMusicArtist;
    }
}