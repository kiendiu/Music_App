package com.example.musica.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaylistModel {
    private String playlistId;
    private String name;
    private String userID;
    private String imgUrl;
    private List<String> songs;
    private Map<String, Boolean> songCheckedMap;

    public PlaylistModel(String name, String userID, String imgUrl, List<String> songs) {
        this.name = name;
        this.userID = userID;
        this.imgUrl = imgUrl;
        this.songs = songs;

        this.songCheckedMap = new HashMap<>();
        initializeSongCheckedMap();
    }

    private void initializeSongCheckedMap() {
        // Khởi tạo map với trạng thái mặc định của tất cả các bài hát là false (chưa chọn)
        for (String songId : songs) {
            songCheckedMap.put(songId, false);
        }
    }

    public Map<String, Boolean> getSongCheckedMap() {
        return songCheckedMap;
    }

    public void setSongCheckedMap(Map<String, Boolean> songCheckedMap) {
        this.songCheckedMap = songCheckedMap;
    }
    // Cập nhật trạng thái của checkbox cho một bài hát trong playlist
    public void setSongChecked(String songId, boolean isChecked) {
        if (songCheckedMap.containsKey(songId)) {
            songCheckedMap.put(songId, isChecked);
        }
    }

    // Kiểm tra trạng thái của checkbox cho một bài hát trong playlist
    public boolean isSongChecked(String songId) {
        return songCheckedMap.getOrDefault(songId, false);
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public List<String> getSongs() {
        return songs;
    }

    public void setSongs(List<String> songs) {
        this.songs = songs;
    }


    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

}
