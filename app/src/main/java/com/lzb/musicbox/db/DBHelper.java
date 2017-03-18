package com.lzb.musicbox.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lzb.musicbox.utils.Constant;
import com.lzb.musicbox.utils.LogUtils;

/**
 * Created by Administrator on 2016/1/19.
 */
public class DBHelper extends SQLiteOpenHelper{


    public DBHelper(Context context){
        this(context, Constant.DB_NAME,null,Constant.DB_VERSION);
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtils.i("DBHelper onCreate");
        db.execSQL(Constant.CREATE_TABLE_LIST_CATE);
        db.execSQL(Constant.CREATE_TABLE_LIST_MUSIC);
        for (String sql : Constant.ARRAY_DEFAULT_SQL){
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
