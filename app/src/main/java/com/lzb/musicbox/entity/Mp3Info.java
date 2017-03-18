package com.lzb.musicbox.entity;

public class Mp3Info {

	private long id;				// 在媒体库的ID
	private String title;			// 文件名，即歌名
	private String artist;			// 艺术家，即歌手名
	private String album;			// 专辑名
	private long albumId;			// 专辑ID
	private long duration;			// 时长
	private long size;				// 文件大小
	private String path;			// 文件路径

	// 自定义
	private long originMediaId;		// 源媒体库的ID，在自定义数据表中使用该字段保存在媒体库的ID
	private int index;				// 在随机列表中保存歌曲在真实列表中的索引
	private int cateId;				// 列表分类ID

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getOriginMediaId() {
		return originMediaId;
	}
	public void setOriginMediaId(long originMediaId) {
		this.originMediaId = originMediaId;
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
	public long getAlbumId() {
		return albumId;
	}
	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getCateId() {
		return cateId;
	}
	public void setCateId(int cateId) {
		this.cateId = cateId;
	}
}
