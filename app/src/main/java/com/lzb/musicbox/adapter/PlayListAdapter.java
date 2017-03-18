package com.lzb.musicbox.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lzb.musicbox.R;
import com.lzb.musicbox.entity.Mp3Info;

import java.util.List;

/**
 * Created by Administrator on 2016/1/18.
 */
public class PlayListAdapter extends ArrayAdapter<Mp3Info> {

    private Context mContext;
    private int mResource;
    private List<Mp3Info> mp3InfoList;

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    private int selectedPosition;

    public PlayListAdapter(Context context, int textViewResourceId, List<Mp3Info> objects) {
        super(context, textViewResourceId, objects);
        mContext = context;
        mResource = textViewResourceId;
        mp3InfoList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Mp3Info mp3Info = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(mContext).inflate(mResource,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.tvPlayListItemText = (TextView) view.findViewById(R.id.tv_play_list_item_text);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.tvPlayListItemText.setText(mp3Info.getTitle() + " - " + mp3Info.getArtist());
        if(position == selectedPosition){
            viewHolder.tvPlayListItemText.setTextColor(mContext.getResources().getColor(R.color.selected_text_color));
        }else{
            viewHolder.tvPlayListItemText.setTextColor(mContext.getResources().getColor(R.color.default_text_color));
        }

        return view;
    }

    class ViewHolder{
        TextView tvPlayListItemText;
    }
}
