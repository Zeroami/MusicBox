package com.lzb.musicbox;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lzb.musicbox.adapter.SkinImageAdapter;
import com.lzb.musicbox.app.App;
import com.lzb.musicbox.entity.SkinImage;
import com.lzb.musicbox.utils.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/2/1.
 */
public class ChangeSkinActivity extends Activity implements View.OnClickListener {

    private LinearLayout lyMainBg;

    private ImageView ivActionBack;

    private GridView gvSkinImages;
    private SkinImageAdapter skinImageAdapter;
    private List<SkinImage> skinImageList = new ArrayList<SkinImage>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_change_skin);
        initView();
        initData();
        initListener();
        initAdapter();
    }

    private void initView() {
        lyMainBg = (LinearLayout) findViewById(R.id.ly_main_bg);
        ivActionBack = (ImageView) findViewById(R.id.iv_action_bar_back);
        gvSkinImages = (GridView) findViewById(R.id.gv_skin_images);
    }

    private void initData() {
        int skinIndex = App.getSP().getInt("skinIndex",0);
        setMainBg(skinIndex);
        for (int i=0;i< Constant.SKIN_IMAGE_ARRAY.length;i++){
            SkinImage skinImage = new SkinImage();
            skinImage.setImageId(Constant.SKIN_IMAGE_ARRAY[i]);
            skinImage.setSelected((skinIndex == i) ? true : false);
            skinImageList.add(skinImage);
        }
    }

    private void initListener(){
        ivActionBack.setOnClickListener(this);
    }

    private void initAdapter(){
        skinImageAdapter = new SkinImageAdapter(this,R.layout.item_skin_image,skinImageList);
        gvSkinImages.setAdapter(skinImageAdapter);
    }

    public void setMainBg(int skinIndex){
        lyMainBg.setBackgroundResource(Constant.SKIN_TARGET_IMAGE_ARRAY[skinIndex]);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_action_bar_back:
                finish();
                break;
        }
    }
}
