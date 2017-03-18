package com.lzb.musicbox.entity;

/**
 * Created by Administrator on 2016/1/23.
 * 榜单
 */
public class BillBoard {

    private int type;
    private String title;

    public BillBoard(){}

    public BillBoard(int type,String title){
        this.type = type;
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
