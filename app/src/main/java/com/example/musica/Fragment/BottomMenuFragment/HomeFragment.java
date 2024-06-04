package com.example.musica.Fragment.BottomMenuFragment;

import static android.widget.Toast.LENGTH_SHORT;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musica.Adapter.ArtistsAdapter;
import com.example.musica.Adapter.CategoriesAdapter;
import com.example.musica.Model.ArtistsModel;
import com.example.musica.Model.CategoryModel;
import com.example.musica.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
public class HomeFragment extends Fragment {

    private List<ArtistsModel> artistsList;
    private RecyclerView recyclerView;
    private ArtistsAdapter adapter;
    private List<CategoryModel> categoriesList;
    private RecyclerView categoriesRecyclerView;
    private CategoriesAdapter categoriesAdapter;
    private RecyclerView artistsRecyclerView;
    private ArtistsAdapter artistsAdapter;


    private Handler artistsHandler;
    private Runnable artistsRunnable;
    private Handler categoriesHandler;
    private Runnable categoriesRunnable;
    private static final int AUTO_SCROLL_INTERVAL = 6000;

    private ImageButton btnArtistsPrev, btnArtistsNext;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle back button press

            }
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.artists_recycler_view);
        categoriesRecyclerView = view.findViewById(R.id.categories_recycler_view);
        artistsRecyclerView = view.findViewById(R.id.artists_recycler_view);
        categoriesRecyclerView = view.findViewById(R.id.categories_recycler_view);
        btnArtistsPrev = view.findViewById(R.id.btn_artists_prev);
        btnArtistsNext = view.findViewById(R.id.btn_artists_next);

        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prepareArtistsData();
        setupCategoriesRecyclerView();
        prepareCategoriesData();
        setupArtistsRecyclerView();

        setupAutoScroll();
        setupButtonListeners();
    }


    private void setupArtistsRecyclerView() {
        artistsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        artistsList = new ArrayList<>();
        artistsAdapter = new ArtistsAdapter(getContext(), artistsList);
        artistsRecyclerView.setAdapter(artistsAdapter);
    }
    @SuppressLint("NotifyDataSetChanged")
    private void prepareArtistsData() {
        CollectionReference artistsRef = FirebaseFirestore.getInstance().collection("artists");
        artistsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                artistsList.clear();
                for (DocumentSnapshot doc : task.getResult()) {
                    String name = doc.getString("name");
                    String imageUrl = doc.getString("imgUrl");
                    List<String> songs = (List<String>) doc.get("songs");
                    Log.d("HomeFragment", "Number of songs: " + (songs != null ? songs.size() : 0));
                    ArtistsModel artist = new ArtistsModel(name, imageUrl, songs);
                    artistsList.add(artist);
                }
                Log.d("HomeFragment", "Number of artists retrieved: " + artistsList.size());
                Log.d("HomeFragment", "Artists data:");
                for (ArtistsModel artist : artistsList) {
                    Log.d("HomeFragment", "- Name: " + artist.getName());
                    Log.d("HomeFragment", "  Image URL: " + artist.getImgUrl());
                }
                artistsAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Failed to load artists: " + task.getException().getMessage(), LENGTH_SHORT).show();
            }
        });
    }
    private void setupCategoriesRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2); // 2 items per row
        categoriesRecyclerView.setLayoutManager(gridLayoutManager);
        categoriesList = new ArrayList<>();
        categoriesAdapter = new CategoriesAdapter(getContext(), categoriesList);
        categoriesRecyclerView.setAdapter(categoriesAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void prepareCategoriesData() {
        CollectionReference categoriesRef = FirebaseFirestore.getInstance().collection("categories");
        categoriesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                categoriesList.clear();  // Clear the list before adding new data
                for (DocumentSnapshot doc : task.getResult()) {
                    if (doc.contains("name") && doc.contains("imgUrl")) {
                        String name = doc.getString("name");
                        String imageUrl = doc.getString("imgUrl");
                        List<String> songs = (List<String>) doc.get("songs"); // Get list of songs
//                        Log.d("HomeFragment", "Number of songs: " + (songs != null ? songs.size() : 0)); // Log the size of songs list
                        CategoryModel category = new CategoryModel(name, imageUrl, songs);
                        categoriesList.add(category);
                    } else {
                        Log.w("HomeFragment", "Skipping category with missing field(s): " + doc.getId());
                    }
                }
                categoriesAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Failed to load categories: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupAutoScroll() {
        artistsHandler = new Handler();
        artistsRunnable = new Runnable() {
            int currentIndex = 0;
            @Override
            public void run() {
                if (artistsAdapter.getItemCount() == 0) return;

                if (currentIndex == artistsAdapter.getItemCount()) {
                    currentIndex = 0;
                }
                artistsRecyclerView.smoothScrollToPosition(currentIndex++);
                artistsHandler.postDelayed(this, AUTO_SCROLL_INTERVAL);
            }
        };
        categoriesHandler = new Handler();
        categoriesRunnable = new Runnable() {
            int currentIndex = 0;
            @Override
            public void run() {
                if (categoriesAdapter.getItemCount() == 0) return;

                if (currentIndex == categoriesAdapter.getItemCount()) {
                    currentIndex = 0;
                }
                categoriesRecyclerView.smoothScrollToPosition(currentIndex++);
                categoriesHandler.postDelayed(this, AUTO_SCROLL_INTERVAL);
            }
        };
    }
    private void setupButtonListeners() {
        btnArtistsPrev.setOnClickListener(v -> {
            int currentPosition = ((LinearLayoutManager) artistsRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            if (currentPosition > 0) {
                artistsRecyclerView.smoothScrollToPosition(currentPosition - 1);
            }
        });

        btnArtistsNext.setOnClickListener(v -> {
            int currentPosition = ((LinearLayoutManager) artistsRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            if (currentPosition < artistsAdapter.getItemCount() - 1) {
                artistsRecyclerView.smoothScrollToPosition(currentPosition + 1);
            }
        });

    }
    @Override
    public void onResume() {
        super.onResume();
        artistsHandler.postDelayed(artistsRunnable, AUTO_SCROLL_INTERVAL);
        categoriesHandler.postDelayed(categoriesRunnable, AUTO_SCROLL_INTERVAL);
    }
    @Override
    public void onPause() {
        super.onPause();
        artistsHandler.removeCallbacks(artistsRunnable);
        categoriesHandler.removeCallbacks(categoriesRunnable);
    }
}
