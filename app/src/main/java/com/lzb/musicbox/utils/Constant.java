package com.lzb.musicbox.utils;

import com.lzb.musicbox.R;

import java.io.File;

/**
 * Created by Administrator on 2016/1/18.
 */
public class Constant {
    public static final String BROADCAST_EXIT = "com.lzb.musicbox.EXIT";
    public static final String SP_NAME = "sp_musicbox";
    public static final String MUSIC_DIR = FileUtils.getSDCardPath("/mnt/sdcard") + File.separator + "musicbox";
    public static final String LRC_NAME = "lrc";
    public static final String SONG_NAME = "song";
    public static final String DB_NAME = "db_musicbox";
    public static final int DB_VERSION = 1;
    public static final String TABLE_MP3_LIST_CATE = "mb_mp3_list_cate";
    public static final String TABLE_MP3_INFO = "mb_mp3_info";
    public static final String CREATE_TABLE_LIST_CATE = "create table " +
            TABLE_MP3_LIST_CATE +
            "(" +
            "`id` integer primary key autoincrement," +
            "`title` text not null" +
            ")";
    public static final String CREATE_TABLE_LIST_MUSIC = "create table " +
            TABLE_MP3_INFO +
            "(" +
            "`id` integer primary key autoincrement," +
            "`originMediaId` integer not null," +
            "`title` text not null," +
            "`artist` text not null," +
            "`album` text not null," +
            "`albumId` integer not null," +
            "`duration` integer not null," +
            "`size` integer not null," +
            "`path` text not null," +
            "`index` integer not null," +
            "`cateId` integer not null" +
            ")";

    public static final String[] ARRAY_DEFAULT_SQL = new String[]{
            "insert into " + TABLE_MP3_LIST_CATE + " (title) values ('" + AppUtils.getString(R.string.list_prefer) + "')",
            "insert into " + TABLE_MP3_LIST_CATE + " (title) values ('" + AppUtils.getString(R.string.list_default) + "')",
            "insert into " + TABLE_MP3_LIST_CATE + " (title) values ('" + AppUtils.getString(R.string.list_latest) + "')",
    };
    public static final int LIST_LOCAL_ID = 0;  // 虚拟的ID，对本地歌曲的标识
    public static final int LIST_PREFER_ID = 1;
    public static final int LIST_DEFAULT_ID = 2;
    public static final int LIST_LATEST_ID = 3;

    // 搜索api {0}是查询关键字、{1}是当前页码、{2}是一页的条数
    public static final String BASE_URL_SEARCH = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=qianqian&version=2.1.0&method=baidu.ting.search.common&format=json&query={0}&page_no={1}&page_size={2}";
    // 榜单api {0}是榜单类型、{1}是偏移行数、{2}是一页的条数
    public static final String BASE_URL_BOARD = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=qianqian&version=2.1.0&method=baidu.ting.billboard.billList&format=json&type={0}&offset={1}&size={2}";
    // 下载api {0}是歌曲id
    public static final String BASE_URL_DOWNLOAD = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=qianqian&version=2.1.0&method=baidu.ting.song.getInfos&format=json&songid={0}&ts=1408284347323&e=JoN56kTXnnbEpd9MVczkYJCSx%2FE1mkLx%2BPMIkTcOEu4%3D&nw=2&ucf=1&res=1";

    public static final int BOARD_MUSIC_SIZE = 20;
    public static final int SEARCG_MUSIC_SIZE = 20;

    public static final int BOARD_TYPE_NEW = 1;              // 新歌榜
    public static final int BOARD_TYPE_HOT = 2;              // 热歌榜
    public static final int BOARD_TYPE_BILLBOARD = 8;        // Billboard
    public static final int BOARD_TYPE_HITO_CHINESE = 18;    // Hito中文榜

    /* 搜索api有用的字段
    pages：total（收到的中记录数）、[rn_num（当前页显示的条数）可选字段]
    song_list：song_id（歌曲id）、title（歌名）、author（歌手）、
    album_title（专辑）、[lrclink（歌词链接）可选字段]
     */
    /* 榜单api有用的字段
    song_list：song_id（歌曲id）、title（歌名）、author（歌手）、
    album_title（专辑）、[publishtime（发布时间，是字符串、2016-01-01格式）可选字段]
     */
    /* 下载api有用的字段
    songurl：file_size（文件大小、字节、需转换）、file_extension（文件扩展名、mp3、flac）、file_link（文件下载地址、有可能为空、可判断是否为空）
    songinfo：lrclink、[title、author，可选的字段]
     */

    public static int[] SKIN_IMAGE_ARRAY = new int[]{R.drawable.app_bg0,R.drawable.app_bg1,R.drawable.app_bg2,R.drawable.app_bg3,R.drawable.app_bg4,R.drawable.app_bg5};
    public static int[] SKIN_BLUR_IMAGE_ARRAY = new int[]{R.drawable.app_blur_bg0,R.drawable.app_blur_bg1,R.drawable.app_blur_bg2,R.drawable.app_blur_bg3,R.drawable.app_blur_bg4,R.drawable.app_blur_bg5};
    public static int[] SKIN_TARGET_IMAGE_ARRAY = SKIN_BLUR_IMAGE_ARRAY;

    public static int LANGUAGE_CHINESE = 0;     // 中文
    public static int LANGUAGE_ENGLISH = 1;     // 英文

    public static int ON = 0;        // 开启
    public static int OFF = 1;       // 关闭
}
