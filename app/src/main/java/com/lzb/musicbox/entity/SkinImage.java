package com.lzb.musicbox.entity;

/**
 * Created by Administrator on 2016/2/1.
 */
public class SkinImage {

    private int imageId;
    private boolean selected;

    public SkinImage(){}

    public SkinImage(int imageId, boolean selected) {
        this.imageId = imageId;
        this.selected = selected;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
