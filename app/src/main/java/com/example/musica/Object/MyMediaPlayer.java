package com.example.musica.Object;

import android.media.MediaPlayer;

import com.example.musica.Model.SongModel;

public class MyMediaPlayer {
    private static MediaPlayer instance;
    private static int currentIndex = -1;
    private static SongModel currentSong; // Thêm thuộc tính để lưu trữ thông tin về bài hát hiện tại

    public static MediaPlayer getInstance(){
        if(instance == null){
            instance = new MediaPlayer();
        }
        return instance;
    }

    public static void start() {
        if (instance != null && !instance.isPlaying()) {
            instance.start();
        }
    }

    public static void pause() {
        if (instance != null && instance.isPlaying()) {
            instance.pause();
        }
    }

    // Thêm phương thức để cập nhật thông tin về bài hát hiện tại
    public static void setCurrentSong(SongModel song) {
        currentSong = song;
    }

    // Thêm phương thức để lấy thông tin về bài hát hiện tại
    public static SongModel getCurrentSong() {
        return currentSong;
    }
}
