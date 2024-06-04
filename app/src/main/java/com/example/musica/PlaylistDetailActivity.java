package com.example.musica;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.musica.Adapter.SongListAdapter;
import com.example.musica.Fragment.SubFragment.AddSongListFragment;
import com.example.musica.databinding.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlaylistDetailActivity extends AppCompatActivity {

    private ActivityPlaylistDetailBinding binding;
    private List<String> songIdList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaylistDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseFirestore db = FirebaseFirestore.getInstance(); // Initialize Firestore instance

        songIdList = new ArrayList<>(); // Initialize songIdList
        binding.musicRecyclerViewPlaylist.setLayoutManager(new LinearLayoutManager(this));
        binding.musicRecyclerViewPlaylist.setAdapter(new SongListAdapter(songIdList));

        Intent intent = getIntent();
        if (intent != null) {
            Log.d(TAG, "Intent received");
            String playlistName = intent.getStringExtra("playlistName");
            if (playlistName != null) {
                binding.playlistName.setText(playlistName); // Update playlist name using data binding
            } else {
                Log.d(TAG, "Playlist name not found in Intent");
            }

            String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            db.collection("playlists")
                    .whereEqualTo("userID", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            songIdList.clear();  // Clear existing playlists
                            if (task.getResult().isEmpty()) {
                                Toast.makeText(PlaylistDetailActivity.this, "You don't have any playlists yet.", Toast.LENGTH_SHORT).show();
                            } else {
                                for (DocumentSnapshot doc : task.getResult()) {
                                    // Check if document name matches playlist name from Intent (optional)
                                    if (Objects.equals(doc.getString("name"), playlistName)) {
                                        List<String> retrievedSongIdList = (List<String>) doc.get("songs");
                                        if (retrievedSongIdList != null) {
                                            songIdList.addAll(retrievedSongIdList);
                                        } else {
                                            Log.d(TAG, "Songs not found in playlist document");
                                        }
                                        break;
                                    }
                                }
                            }
                            Objects.requireNonNull(binding.musicRecyclerViewPlaylist.getAdapter()).notifyDataSetChanged(); // Update RecyclerView
                        } else {
                            Log.w(TAG, "Error getting playlists: ", task.getException());
                        }
                    });

        } else {
            Log.w(TAG, "Intent is null");
        }
        binding.addSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddSongListFragment();
            }
        });
        binding.backBtn.setOnClickListener(v -> finish());
    }
    private void showAddSongListFragment() {
        AddSongListFragment addSongListFragment = new AddSongListFragment(); // Create the fragment instance
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(android.R.id.content, addSongListFragment); // Add to activity's content area
        transaction.addToBackStack(null);
        transaction.commit();
    }
}

