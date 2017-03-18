package com.lzb.musicbox.entity;

/**
 * Created by Administrator on 2016/1/23.
 * 网络歌曲文件信息
 */
public class NetMusicFileInfo {

    private String name;        // 歌手-歌名
    private int fileSize;       // 文件大小，字节
    private String fileExt;     // 文件扩展名
    private String fileLink;    // 文件下载地址

    public String toString(){
        return "[name:" + name + ",fileSize:" + fileSize + ",fileExt:" + fileExt + ",fileLink:" + fileLink + "]";
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileLink() {
        return fileLink;
    }

    public void setFileLink(String fileLink) {
        this.fileLink = fileLink;
    }


}
