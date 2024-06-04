package com.example.musica.Fragment.BottomMenuFragment;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.musica.Adapter.PlaylistAdapter;
import com.example.musica.Model.PlaylistModel;
import com.example.musica.R;
import com.example.musica.databinding.FragmentLibraryBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {
    private FirebaseAuth mAuth;
    private boolean isLibraryFragment = true;
    private String playlistName;

    private FragmentLibraryBinding binding;
    private List<PlaylistModel> playlistList;
    private PlaylistAdapter playlistAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);


        return binding.getRoot();

    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SearchView searchView = (SearchView) view.findViewById(R.id.searchBtn);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String userId = bundle.getString("userId");
            playlistList = new ArrayList<>();
            isLibraryFragment = true;
            playlistAdapter = new PlaylistAdapter(playlistList, requireContext());
            binding.musicRecyclerViewLibrary.setAdapter(playlistAdapter);

            // Set LayoutManager cho RecyclerView
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            binding.musicRecyclerViewLibrary.setLayoutManager(layoutManager);

            // Set isLibraryFragment cho adapter
            playlistAdapter.setIsLibraryFragment(isLibraryFragment);
            loadPlaylistData(userId);
        } else {
            Log.d("LibraryFragment", "Bundle is null");
        }
        binding.addPlaylistBtn.setOnClickListener(v -> openCreatePlaylistAlertDialog());
        loadUserImage();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search when user submits query
                performSearch(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                // Perform search as user types
                performSearch(newText);
                return true;
            }
        });
    }
    private void loadUserImage() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance("https://musicproject-53d9d-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users_image").child(userId).child("profilePicture");

            userRef.get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    String imageUrl = dataSnapshot.getValue(String.class);
                    if (imageUrl != null) {
                        // Check if the fragment's view is not null before accessing its children
                        if (getView() != null) {
                            ImageView imageView = getView().findViewById(R.id.userAvatar);
                            if (imageView != null) {
                                new UserFragment.DownloadImageTask(imageView).execute(imageUrl);
                            } else {
                                showToast("ImageView not found in the fragment's view");
                            }
                        } else {
                            Log.d("LibraryFragment", "getView() returned null");
                        }
                    }
                }
            }).addOnFailureListener(e -> showToast("Failed to load image: " + e.getMessage()));
        }
    }
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void loadPlaylistData(String userId) {
        Log.d("LibraryFragment", "Loading playlist data for user: " + userId);
        CollectionReference playlistsRef = FirebaseFirestore.getInstance().collection("playlists");

        playlistsRef.whereEqualTo("userID", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        playlistList.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            String playlistId = doc.getId();
                            String name = doc.getString("name");
                            String userID = doc.getString("userID");
                            String imgUrl = doc.getString("imgUrl");
                            List<String> songs = (List<String>) doc.get("songs");
//                            Log.d("LibraryFragment", "Playlist ID: " + playlistId);
//                            Log.d("LibraryFragment", "Document data: " + doc.getData());

                            if (userId.equals(userID)) {
                                PlaylistModel playlist = new PlaylistModel(name, userID, imgUrl, songs);
                                playlist.setPlaylistId(playlistId);
                                playlistList.add(playlist);
                            }
                        }
                        playlistAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Failed to load playlists: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void openCreatePlaylistAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.create_playlist_dialog, null);
        builder.setView(dialogView);

        TextInputEditText inputDialog = dialogView.findViewById(R.id.editTextDialog);
        CardView confirmButton = dialogView.findViewById(R.id.confirm_button);
        CardView cancelButton = dialogView.findViewById(R.id.cancel_button);
        AlertDialog alertDialog = builder.create();
        cancelButton.setOnClickListener(v -> alertDialog.dismiss());

        confirmButton.setOnClickListener(view -> {
            mAuth = FirebaseAuth.getInstance();
            String userId = mAuth.getUid();
            playlistName = String.valueOf(inputDialog.getText());

//            Log.d(TAG, "Playlist name: " + playlistName);

            if (!playlistName.isEmpty()) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference playlistsRef = db.collection("playlists");

                // Query to check if the playlist name already exists
                playlistsRef.whereEqualTo("name", playlistName).whereEqualTo("userID", userId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Number of playlists with same name: " + task.getResult().size());
                                if (task.getResult().isEmpty()) {
                                    PlaylistModel playlist = new PlaylistModel(playlistName, userId, "https://firebasestorage.googleapis.com/v0/b/musicproject-53d9d.appspot.com/o/playlist.png?alt=media&token=bf8bce8e-926d-4a15-94cb-488135dcae41", new ArrayList<>());

                                    playlistsRef.add(playlist)
                                            .addOnSuccessListener(documentReference -> {
                                                Log.d(TAG, "Playlist added with ID: " + documentReference.getId());

                                                alertDialog.dismiss();
                                                loadPlaylistData(userId);
                                                Toast.makeText(requireContext(), "Playlist created successfully", Toast.LENGTH_SHORT).show();

                                            })
                                            .addOnFailureListener(e -> Log.w(TAG, "Error adding playlist", e));
                                } else {
                                    Toast.makeText(requireContext(), "Playlist name already exists", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(requireContext(), "Failed to check playlist name: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(requireContext(), "Please enter a playlist name", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.show();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void performSearch(String query) {
        List<PlaylistModel> filteredList = new ArrayList<>();
        for (PlaylistModel playlist : playlistList) {
            if (playlist.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(playlist);
            }
        }
        playlistAdapter.setPlaylistList(filteredList);
        playlistAdapter.notifyDataSetChanged();
    }
}

