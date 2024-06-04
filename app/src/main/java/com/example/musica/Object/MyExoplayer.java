package com.example.musica.Object;

import android.content.Context;
import android.view.View;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import com.example.musica.Model.SongModel;
import java.util.ArrayList;
import java.util.List;

public class MyExoplayer {
    private static boolean isPlaying = false;
    private static List<SongModel> songList = new ArrayList<>();
    private static int currentSongIndex = 0;
    private static ExoPlayer exoPlayer;

    public static void startPlaying(Context context, SongModel song, List<SongModel> playlist) {
        if (exoPlayer == null) {
            initializePlayer(context);
        }
        if (playlist != null) {
            setSongList(playlist);
            currentSongIndex = songList.indexOf(song); // Set current song index
            playSong(song);
        }
    }

    public static void initializePlayer(Context context) {
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(context).build();
        }
    }

    public static void playNextSong() {
        if (!songList.isEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % songList.size();
            playSong(songList.get(currentSongIndex));
        }
    }
    public static void togglePlayPause() {
        isPlaying = !isPlaying;
    }

    public static boolean isPlaying() {
        return isPlaying;
    }
    public static void playPreviousSong() {
        if (!songList.isEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + songList.size()) % songList.size();
            playSong(songList.get(currentSongIndex));
        }
    }
    public static void pause() {
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    public static void playSong(SongModel song) {
        if (exoPlayer != null && song != null) {
            exoPlayer.stop();
            MediaItem mediaItem = MediaItem.fromUri(song.getSongUrl());
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.play();
        }
    }

    public static SongModel getCurrentSong() {
        if (songList.isEmpty()) {
            return null;
        }
        return songList.get(currentSongIndex);
    }

    public static void release() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    public static void setSongList(List<SongModel> songs) {
        songList.clear();
        if (songs != null) {
            songList.addAll(songs);
        }
    }

    public static ExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    public static void handleLogout(Context context) {
        release();
    }
}
