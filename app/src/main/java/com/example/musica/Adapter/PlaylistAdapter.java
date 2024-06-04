package com.example.musica.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musica.Model.PlaylistModel;
import com.example.musica.PlaylistDetailActivity;
import com.example.musica.R;
import com.example.musica.databinding.ItemPlaylistBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private boolean isLibraryFragment = true;
    public static List<PlaylistModel> playlistList;
    private String currentSongId;

    public void setIsLibraryFragment(boolean isLibraryFragment) {
        this.isLibraryFragment = isLibraryFragment;
    }
    public void setPlaylistList(List<PlaylistModel> playlistList) {
        PlaylistAdapter.playlistList = playlistList;
    }
    // Constructor
    public PlaylistAdapter(List<PlaylistModel> playlistList, Context context) {
        PlaylistAdapter.playlistList = playlistList;

    }
    public List<PlaylistModel> getPlaylistList() {
        return playlistList;
    }
    public void setSongId(String songId) {
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlaylistBinding binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PlaylistViewHolder(binding);
    }
    private void deletePlaylistFromFirestore(String playlistId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").document(playlistId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("PlaylistAdapter", "Playlist successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("PlaylistAdapter", "Error deleting playlist", e);
                    }
                });
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        PlaylistModel playlist = playlistList.get(position);
        holder.bind(playlist);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!playlist.getName().equals("Liked song")) {
                    // Hiển thị AlertDialog để xác nhận xóa playlist
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Delete Playlist")
                            .setMessage("Are you sure about dat?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Xóa playlist từ Firestore khi người dùng xác nhận
                                    deletePlaylistFromFirestore(playlist.getPlaylistId());  // Thêm dòng này
                                    // Xóa playlist khỏi danh sách và cập nhật giao diện
                                    playlistList.remove(holder.getAdapterPosition());
                                    notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                } else {
                    // Hiển thị thông báo rằng không thể xóa "Liked song"
                    Toast.makeText(v.getContext(), "Không thể xóa 'Liked song'", Toast.LENGTH_SHORT).show();
                }
                return true; // Trả về true để ngăn việc gọi sự kiện onClick sau khi long click
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }
    public String getSongId() {
        return currentSongId;
    }
    public void setCurrentSongId(String songId) {
        this.currentSongId = songId;
        notifyDataSetChanged();
    }
    // ViewHolder class
    public class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final ItemPlaylistBinding binding;

        public PlaylistViewHolder(@NonNull ItemPlaylistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Lấy playlist được chọn tại vị trí position
                    PlaylistModel selectedPlaylist = playlistList.get(position);
                    Log.d("PlaylistAdapter", "Clicked playlist name: " + selectedPlaylist.getName());

                    // Tạo Intent để chuyển sang PlaylistDetailActivity
                    Intent intent = new Intent(view.getContext(), PlaylistDetailActivity.class);
                    // Truyền thông tin playlist thông qua Intent
                    intent.putExtra("playlistName", selectedPlaylist.getName());
                    // Thực hiện chuyển sang PlaylistDetailActivity
                    view.getContext().startActivity(intent);
                }
            });
        }

        public void bind(PlaylistModel playlist) {
            Log.d("PlaylistAdapter", "Playlist name: " + playlist.getName());
            binding.namePlaylist.setText(playlist.getName());
            if (playlist.getName().equals("Liked song")) {
                Glide.with(itemView)
                        .load(R.drawable.like_playlist)
                        .into(binding.imgPlaylist);
            } else {
                Glide.with(itemView)
                        .load(playlist.getImgUrl())
                        .into(binding.imgPlaylist);
            }
            binding.namePlaylist.setText(playlist.getName());
            int songCount = playlist.getSongs().size();
            String songCountText = songCount + " song" + (songCount != 1 ? "s" : "");
            binding.songCount.setText(songCountText);

            if (isLibraryFragment) {
                // Ẩn checkbox
                binding.checkboxAddToPlaylist.setVisibility(View.GONE);
            } else {
                // Hiển thị checkbox
                binding.checkboxAddToPlaylist.setVisibility(View.VISIBLE);
            }

            if (playlist.getSongs() != null && playlist.getSongs().contains(currentSongId)) {
                // If the song is in the playlist, set the checkbox to checked
                binding.checkboxAddToPlaylist.setChecked(true);
                // Log checked state
                Log.d("PlaylistAdapter", "Checkbox checked for song ID: " + currentSongId);
            } else {
                // If the song is not in the playlist, set the checkbox to unchecked
                binding.checkboxAddToPlaylist.setChecked(false);
                // Log unchecked state
                Log.d("PlaylistAdapter", "Checkbox unchecked for song ID: " + currentSongId);
            }
            binding.checkboxAddToPlaylist.setOnCheckedChangeListener((buttonView, isChecked) -> {
                List<String> songs = playlist.getSongs();
                if (isChecked){
                    if (!songs.contains(currentSongId)){
                        songs.add(currentSongId);
                    }
                }
                else {
                    songs.remove(currentSongId);
                }
                playlist.setSongs(songs);
            });

        }

    }
}