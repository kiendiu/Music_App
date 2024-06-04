package com.example.musica.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ArtistsModel implements Parcelable {
    private String name;
    private String imgUrl;
    private List<String> songs; // Thêm trường songs với kiểu dữ liệu List<String>

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(imgUrl);
        // Viết các thuộc tính khác vào Parcel nếu cần
    }

    public static final Creator<ArtistsModel> CREATOR = new Creator<ArtistsModel>() {
        @Override
        public ArtistsModel createFromParcel(Parcel source) {
            ArtistsModel artist = new ArtistsModel();
            artist.name = source.readString();
            artist.imgUrl = source.readString();
            // Đọc các thuộc tính khác từ Parcel nếu cần
            return artist;
        }

        @Override
        public ArtistsModel[] newArray(int size) {
            return new ArtistsModel[size];
        }
    };

    public ArtistsModel() {
    }

    public ArtistsModel(String name, String imgUrl, List<String> songs) {
        this.name = name;
        this.imgUrl = imgUrl;
        this.songs = songs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}