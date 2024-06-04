package com.example.musica.Model;

public class SongModel {

    private String id;
    private String imgUrl;
    private String artists;
    private String songUrl;
    private String name;

    // Required empty constructor for Firestore deserialization
    public SongModel() {
        // Default constructor required for Firestore
    }

    public SongModel(String id, String imgUrl, String artists, String songUrl, String name) {
        this.id = id;
        this.imgUrl = imgUrl;
        this.artists = artists;
        this.songUrl = songUrl;
        this.name = name;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getArtists() {
        return artists;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
