package com.lzb.musicbox.entity;

import java.util.List;

/**
 * Created by Administrator on 2016/1/23.
 * 网络歌曲信息
 */
public class NetMusic {

    private int songId;         // 歌曲id
    private String title;       // 歌名
    private String artist;      // 歌手
    private String album;       // 专辑
    private String albumUrl;    // 专辑图片
    private String lrcLink;     // 歌词下载地址

    private List<NetMusicFileInfo> netMusicFileInfoList;        // 文件信息列表

    public String toString() {
        return "[songId:" + "title:" + title + ",artist:" + artist + ",album:" + album + ",fileList:" + netMusicFileInfoList + "]";
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbumUrl() {
        return albumUrl;
    }

    public void setAlbumUrl(String albumUrl) {
        this.albumUrl = albumUrl;
    }

    public String getLrcLink() {
        return lrcLink;
    }

    public void setLrcLink(String lrcLink) {
        this.lrcLink = lrcLink;
    }

    public List<NetMusicFileInfo> getNetMusicFileInfoList() {
        return netMusicFileInfoList;
    }

    public void setNetMusicFileInfoList(List<NetMusicFileInfo> netMusicFileInfoList) {
        this.netMusicFileInfoList = netMusicFileInfoList;
    }
}
