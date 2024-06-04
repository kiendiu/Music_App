package com.example.musica.Fragment.BottomMenuFragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musica.Adapter.SongListAdapter;
import com.example.musica.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class SearchFragment extends Fragment {

    private RecyclerView searchRecyclerView;
    private EditText searchView;
    private ImageView searchIcon;
    private SongListAdapter songListAdapter;
    private List<String> songIdList;
    private FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        songIdList = new ArrayList<>();
        songListAdapter = new SongListAdapter(songIdList);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchRecyclerView = view.findViewById(R.id.searchRecyclerView);
        searchView = view.findViewById(R.id.searchView);

        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchRecyclerView.setAdapter(songListAdapter);
        loadUserImage();
        loadAllSongs();

        // Update search results as the user types
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (!TextUtils.isEmpty(keyword)) {
                    searchSongs(keyword);
                } else {
                    loadAllSongs(); // Load all songs if search view is empty
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed after text changes
            }
        });

        // Dismiss the keyboard when clicking outside the search view
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View focusedView = getActivity().getCurrentFocus();
                if (focusedView != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                }
            }
            return false;
        });

        return view;
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
                            Log.d("SearchFragment", "getView() returned null");

                        }
                    }
                }
            }).addOnFailureListener(e -> showToast("Failed to load image: " + e.getMessage()));
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void loadAllSongs() {
        db.collection("songs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        songIdList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            songIdList.add(document.getId());
                        }
                        songListAdapter.notifyDataSetChanged();  // Update the adapter
                    } else {
                        Toast.makeText(getContext(), "Failed to load songs", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void searchSongs(String keyword) {
        db.collection("songs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        songIdList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String songName = document.getString("name");
                            String songArtists = document.getString("artists");
                            // Perform search within song name and artists
                            if (songName != null && songName.toLowerCase().contains(keyword.toLowerCase())
                                    || songArtists != null && songArtists.toLowerCase().contains(keyword.toLowerCase())) {
                                songIdList.add(document.getId());
                            }
                        }
                        if (songIdList.isEmpty()) {
                            Toast.makeText(getContext(), "No matching songs found", Toast.LENGTH_SHORT).show();
                        }
                        songListAdapter.notifyDataSetChanged();  // Update the adapter
                    } else {
                        Toast.makeText(getContext(), "Failed to search songs", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}