package com.example.musica.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;


public class CategoryModel implements Parcelable {
    private String name;
    private String imgUrl;
    private List<String> songs; // Thêm trường songs với kiểu dữ liệu List<String>
    @Override
    public int describeContents() {
        return 0;
    } @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(imgUrl);
        // Write other attributes to Parcel if needed
    }public static final Creator<CategoryModel> CREATOR = new Creator<CategoryModel>() {
        @Override
        public CategoryModel createFromParcel(Parcel source) {
            CategoryModel category = new CategoryModel();
            category.name = source.readString();
            category.imgUrl = source.readString();
            // Read other attributes from Parcel if needed
            return category;
        }

        @Override
        public CategoryModel[] newArray(int size) {
            return new CategoryModel[size];
        }
    };
    public CategoryModel() {
    }

    public CategoryModel(String name, String imgUrl, List<String> songs) {
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
