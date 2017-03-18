package com.lzb.musicbox.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lzb.musicbox.R;
import com.lzb.musicbox.entity.BillBoard;
import com.lzb.musicbox.entity.Mp3Info;

import java.util.List;

/**
 * Created by Administrator on 2016/1/18.
 */
public class BoardListAdapter extends ArrayAdapter<BillBoard> {

    private Context mContext;
    private int mResource;
    private List<BillBoard> billBoardList;

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    private int selectedPosition;

    public BoardListAdapter(Context context, int textViewResourceId, List<BillBoard> objects) {
        super(context, textViewResourceId, objects);
        mContext = context;
        mResource = textViewResourceId;
        billBoardList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BillBoard billBoard = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(mContext).inflate(mResource,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.tvBoardTitle = (TextView) view.findViewById(R.id.tv_board_title);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.tvBoardTitle.setText(billBoard.getTitle());
        return view;
    }

    class ViewHolder{
        TextView tvBoardTitle;
    }
}
