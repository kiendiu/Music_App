package com.example.musica.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.musica.Model.SongModel;
import com.example.musica.MusicPlayerActivity;
import com.example.musica.Object.MyExoplayer;
import com.example.musica.databinding.SongsItemRowBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.MyViewHolder> {

    private List<String> songIdList;
    private final List<SongModel> playlist; // Add a playlist member variable

    public SongListAdapter(List<String> songIdList) {
        if (songIdList == null) {
            this.songIdList = new ArrayList<>();
        } else {
            this.songIdList = songIdList;
        }
        this.playlist = new ArrayList<>(); // Initialize the playlist
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final SongsItemRowBinding binding;

        public MyViewHolder(SongsItemRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindData(String songId, List<SongModel> playlist) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("songs")
                    .document(songId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                SongModel song = documentSnapshot.toObject(SongModel.class);
                                if (song != null) {
                                    // Add the song to the playlist
                                    playlist.add(song);

                                    // Update UI elements based on retrieved song data
                                    binding.nameSongs.setText(song.getName());
                                    binding.artistsSongs.setText(song.getArtists());
                                    Glide.with(binding.getRoot().getContext())
                                            .load(song.getImgUrl())
                                            .apply(RequestOptions.centerCropTransform()
                                                    .transform(new RoundedCorners(16)))
                                            .into(binding.imgSongs);
                                    binding.getRoot().setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Context context = v.getContext();
                                            MyExoplayer.startPlaying(context, song, playlist); // Pass the playlist

                                            // Tạo Intent và thêm songId vào Intent
                                            Intent intent = new Intent(context, MusicPlayerActivity.class);
                                            intent.putExtra("songId", songId); // Thêm songId vào Intent
                                            context.startActivity(intent);
                                        }
                                    });
                                }
                            } else {
                                // Handle the case where the document doesn't exist
                                Log.d("SongListAdapter", "Document does not exist: " + songId);
                                // You can choose to show a message or do nothing
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle the case where getting song data fails
                            Log.e("SongListAdapter", "Failed to get song data: " + e.getMessage());
                            // You can choose to show a message or do nothing
                        }
                    });
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SongsItemRowBinding binding = SongsItemRowBinding.inflate(inflater, parent, false);
        return new MyViewHolder(binding);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Check if the songIdList is not empty before accessing its elements
        if (!songIdList.isEmpty() && position >= 0 && position < songIdList.size()) {
            holder.bindData(songIdList.get(position), playlist); // Pass the playlist
        }
    }
    public void setSongIdList(List<String> songIdList) {
        this.songIdList = songIdList;
        notifyDataSetChanged(); // Cập nhật RecyclerView sau khi thiết lập danh sách mới
    }
    @Override
    public int getItemCount() {
        return songIdList.size();
    }
}