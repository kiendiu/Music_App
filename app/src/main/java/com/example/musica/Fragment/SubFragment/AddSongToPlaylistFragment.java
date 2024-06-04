package com.example.musica.Fragment.SubFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.musica.Adapter.PlaylistAdapter;
import com.example.musica.Model.PlaylistModel;
import com.example.musica.databinding.FragmentAddSongToPlaylistBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddSongToPlaylistFragment extends Fragment {
    private List<PlaylistModel> playlistList;
    private boolean isLibraryFragment = true;
    private String currentSongId;
    private PlaylistAdapter playlistAdapter;
    int completedTasksCount;

    private FragmentAddSongToPlaylistBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddSongToPlaylistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void setCurrentSongId(String songId) {
        this.currentSongId = songId;
        playlistAdapter.setCurrentSongId(songId); // Cập nhật currentSongId trong adapter
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isLibraryFragment = false;
        binding.backBtn1.setOnClickListener(v -> handleBackButton());

        playlistList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        binding.musicRecyclerViewAddSong.setLayoutManager(layoutManager);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d("AddSongToPlaylistFragment", "User ID: " + userId);

            // Khởi tạo adapter trước khi sử dụng
            playlistAdapter = new PlaylistAdapter(playlistList, requireContext());
            // Set isLibraryFragment cho adapter
            playlistAdapter.setIsLibraryFragment(isLibraryFragment);
            binding.musicRecyclerViewAddSong.setAdapter(playlistAdapter);
            // Load playlist data using userId
            loadPlaylistData(userId);
        } else {
            Log.d("AddSongToPlaylistFragment", "User not logged in");
        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            // Lấy songId từ Bundle
            String songId = bundle.getString("songId");
            if (songId != null) {
                Log.d("AddSongToPlaylistFragment", "Song ID: " + songId);
                // Gọi setCurrentSongId để cập nhật currentSongId trong adapter
                setCurrentSongId(songId);
            } else {
                Log.d("AddSongToPlaylistFragment", "songId is null");
            }
        } else {
            Log.d("AddSongToPlaylistFragment", "Bundle is null");
        }
        binding.submitButton.setOnClickListener(v -> submitChanges());
    }

    private void handleBackButton() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            Log.d(TAG, "Back stack is empty. Navigating to previous fragment or homepage");
            fragmentManager.popBackStack("previous_fragment_tag", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void submitChanges() {
        for (PlaylistModel playlist : playlistList) {
            DocumentReference playlistRef = FirebaseFirestore.getInstance()
                    .collection("playlists")
                    .document(playlist.getPlaylistId());

            playlistRef.update("songs", playlist.getSongs())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            completedTasksCount++; // Tăng biến đếm khi công việc được hoàn thành

                            // Kiểm tra nếu đã hoàn thành tất cả các công việc
                            if (completedTasksCount == playlistList.size()) {
                                // Hiển thị thông báo chỉ một lần khi tất cả công việc đã hoàn thành
                                Toast.makeText(getContext(), "Changes submitted successfully", Toast.LENGTH_SHORT).show();
                                // Đặt lại biến đếm về 0 cho lần tiếp theo
                                completedTasksCount = 0;
                                getFragmentManager().popBackStack();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Failed to submit changes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void loadPlaylistData(String userId) {
        Log.d("AddSongToPlaylistFragment", "Loading playlist data for user: " + userId);
        CollectionReference playlistsRef = FirebaseFirestore.getInstance().collection("playlists");

        playlistsRef.whereEqualTo("userID", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        playlistList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String userID = document.getString("userID");
                            String imgUrl = document.getString("imgUrl");
                            List<String> songs = (List<String>) document.get("songs");
                            String playlistId = document.getId(); // Lấy playlistId từ Firestore
                            Log.d("AddSongToPlaylistFragment", "Playlist ID: " + playlistId);

                            Log.d("AddSongToPlaylistFragment", "Document data: " + document.getData());

                            if (userId.equals(userID)) {
                                PlaylistModel playlist = new PlaylistModel(name, userID, imgUrl, songs);
                                playlist.setPlaylistId(playlistId); // Đặt playlistId cho playlist
                                // Thêm playlist vào danh sách
                                playlistList.add(playlist);
                            }
                        }

                        // Cập nhật Adapter để hiển thị danh sách mới
                        playlistAdapter.notifyDataSetChanged();
                    } else {
                        // Hiển thị thông báo lỗi nếu không thành công
                        Toast.makeText(getContext(), "Failed to load playlists: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
