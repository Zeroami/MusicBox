package com.lzb.musicbox.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.lzb.musicbox.entity.NetMusic;
import com.lzb.musicbox.entity.NetMusicFileInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/23.
 */
public class NetMusicUtils {

    private static NetMusicUtils netMusicUtils;

    private NetMusicUtils() {
    }

    public static NetMusicUtils getInstance() {
        if (netMusicUtils == null) {
            synchronized (NetMusicUtils.class) {
                if (netMusicUtils == null) {
                    netMusicUtils = new NetMusicUtils();
                }
            }
        }
        return netMusicUtils;
    }

    public interface OnNetMusicLoadListener {
        void onLoad(List<NetMusic> netMusicList);
    }

    public interface OnNetMusicSearchListener {
        void onSearch(List<NetMusic> netMusicList, int total);
    }

    private OnNetMusicLoadListener onNetMusicLoadListener;
    private OnNetMusicSearchListener onNetMusicSearchListener;

    public NetMusicUtils setOnNetMusicLoadListener(OnNetMusicLoadListener onNetMusicLoadListener) {
        this.onNetMusicLoadListener = onNetMusicLoadListener;
        return this;
    }

    public NetMusicUtils setOnNetMusicSearchListener(OnNetMusicSearchListener onNetMusicSearchListener) {
        this.onNetMusicSearchListener = onNetMusicSearchListener;
        return this;
    }

    public void loadBoard(int type, int offset, int size) {
        String url = MessageFormat.format(Constant.BASE_URL_BOARD, type, offset, size);
        new LoadAsyncTask().execute(url, "load");
    }

    public void search(String key, int page, int pageSize) {
        String url = null;
        try {
            key = URLEncoder.encode(key, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        url = MessageFormat.format(Constant.BASE_URL_SEARCH, key, page, pageSize);
        new LoadAsyncTask().execute(url, "search");
    }



    private class LoadAsyncTask extends AsyncTask<String, Void, Integer> {

        private String action;      // 操作

        @Override
        protected Integer doInBackground(String... strings) {
            String urlStr = strings[0];
            action = strings[1];
            String response = HttpUtils.requestByGet(urlStr);
            List<NetMusic> list = null;
            if (response != null) {
                try {
                    int total = 0;
                    if (action.equals("search")) {
                        total = new JSONObject(response).getJSONObject("pages").getInt("total");
                    }
                    list = parseToNetMusicList(response);
                    if (action.equals("load") && onNetMusicLoadListener != null) {
                        onNetMusicLoadListener.onLoad(list);
                    }
                    if (action.equals("search") && onNetMusicSearchListener != null) {
                        onNetMusicSearchListener.onSearch(list, total);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (action.equals("load") && onNetMusicLoadListener != null) {
                        onNetMusicLoadListener.onLoad(null);
                    }
                    if (action.equals("search") && onNetMusicSearchListener != null) {
                        onNetMusicSearchListener.onSearch(null, 0);
                    }
                }
            } else {
                if (action.equals("load") && onNetMusicLoadListener != null) {
                    onNetMusicLoadListener.onLoad(null);
                }
                if (action.equals("search") && onNetMusicSearchListener != null) {
                    onNetMusicSearchListener.onSearch(null, 0);
                }
            }
            return 1;
        }

        /**
         * 解析json到音乐列表
         * @param response
         * @return
         * @throws JSONException
         */
        private List<NetMusic> parseToNetMusicList(String response) throws JSONException {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray jsonSongList = jsonResponse.getJSONArray("song_list");
            List<NetMusic> list = new ArrayList<NetMusic>();
            for (int i = 0; i < jsonSongList.length(); i++) {
                JSONObject jsonSong = (JSONObject) jsonSongList.get(i);
                NetMusic netMusic = new NetMusic();
                netMusic.setSongId(jsonSong.getInt("song_id"));
                netMusic.setTitle(replaceEMTag(jsonSong.getString("title")));
                netMusic.setArtist(replaceEMTag(jsonSong.getString("author")));
                netMusic.setAlbum(replaceEMTag(jsonSong.getString("album_title")));
                getFileInfo(netMusic);
                list.add(netMusic);
            }
            return list;
        }

        /**
         * 获取音乐文件信息列表
         * @param netMusic
         * @throws JSONException
         */
        private void getFileInfo(NetMusic netMusic) throws JSONException {
            String urlStr = MessageFormat.format(Constant.BASE_URL_DOWNLOAD, String.valueOf(netMusic.getSongId()));
            String response = HttpUtils.requestByGet(urlStr);
            JSONObject jsonResponse = new JSONObject(response);
            JSONObject jsonSongInfo = jsonResponse.getJSONObject("songinfo");
            netMusic.setLrcLink(jsonSongInfo.getString("lrclink"));
            netMusic.setAlbumUrl(jsonSongInfo.getString("pic_small"));
            JSONArray jsonSongUrls = jsonResponse.getJSONObject("songurl").getJSONArray("url");
            List<NetMusicFileInfo> netMusicFileInfoList = new ArrayList<NetMusicFileInfo>();
            for (int i=0;i<jsonSongUrls.length();i++){
                JSONObject jsonSongUrl = jsonSongUrls.getJSONObject(i);
                if (!jsonSongUrl.getString("file_link").equals("")) {
                    NetMusicFileInfo fileInfo = new NetMusicFileInfo();
                    fileInfo.setName(netMusic.getArtist() + "-" + netMusic.getTitle());
                    fileInfo.setFileSize(jsonSongUrl.getInt("file_size"));
                    fileInfo.setFileExt(jsonSongUrl.getString("file_extension"));
                    fileInfo.setFileLink(jsonSongUrl.getString("file_link"));
                    netMusicFileInfoList.add(fileInfo);
                }
            }
            netMusic.setNetMusicFileInfoList(netMusicFileInfoList);
        }

        private String replaceEMTag(String str){
            return str.replace("<em>","").replace("</em>","");
        }
    }
}
