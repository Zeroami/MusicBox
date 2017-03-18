package com.lzb.musicbox.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.lzb.musicbox.ChangeSkinActivity;
import com.lzb.musicbox.R;
import com.lzb.musicbox.app.App;
import com.lzb.musicbox.entity.SkinImage;

import java.util.List;

/**
 * Created by Administrator on 2016/2/1.
 */
public class SkinImageAdapter extends ArrayAdapter<SkinImage> implements View.OnClickListener {

    private Context mContext;
    private LayoutInflater inflater;
    private int mResourceId;
    private List<SkinImage> skinImageList;

    private int selectedPosition;


    public SkinImageAdapter(Context context, int textViewResourceId, List<SkinImage> objects) {
        super(context, textViewResourceId, objects);
        mContext = context;
        mResourceId = textViewResourceId;
        skinImageList = objects;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SkinImage skinImage = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null){
            view = inflater.inflate(mResourceId,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.ivSkin = (ImageView) view.findViewById(R.id.iv_skin);
            viewHolder.ivSelected = (ImageView) view.findViewById(R.id.iv_selected);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.position = position;

        viewHolder.ivSkin.setTag(viewHolder);
        viewHolder.ivSkin.setOnClickListener(this);

        if (skinImage.getSelected() == true){
            viewHolder.ivSelected.setVisibility(View.VISIBLE);
            selectedPosition = position;
        }else{
            viewHolder.ivSelected.setVisibility(View.GONE);
        }
        viewHolder.ivSkin.setImageResource(skinImage.getImageId());
        return view;
    }

    @Override
    public void onClick(View view) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (selectedPosition == viewHolder.position){
            return;
        }else{
            skinImageList.get(selectedPosition).setSelected(false);
            skinImageList.get(viewHolder.position).setSelected(true);
            ((ChangeSkinActivity)mContext).setMainBg(viewHolder.position);
            App.getSpEditor().putInt("skinIndex",viewHolder.position).commit();
            notifyDataSetChanged();
        }
    }

    class ViewHolder{
        ImageView ivSkin;
        ImageView ivSelected;

        int position;
    }
}
