package com.example.musica.Fragment.BottomMenuFragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.example.musica.Adapter.SongListAdapter;
import com.example.musica.Model.CategoryModel;
import com.example.musica.R;
import com.example.musica.databinding.FragmentSongListBinding;

import java.util.List;

public class SongListFragment extends Fragment {

    private static final String ARG_PARAM_CATEGORY = "category";
    private FragmentSongListBinding binding; // View Binding reference
    private CategoryModel selectedCategory;

    private List<String> songIdList; // List to store song IDs
    private SongListAdapter adapter; // Reference to the SongListAdapter
    public SongListFragment() {
    }

    public static SongListFragment newInstance(CategoryModel category) {
        SongListFragment fragment = new SongListFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedCategory = getArguments().getParcelable(ARG_PARAM_CATEGORY);
            if (selectedCategory != null) {
                Log.d("SongListFragment", "Received Category: " + selectedCategory.getName());
                Log.d("SongListFragment", "Received Category: " + selectedCategory.getImgUrl());

            } else {
                Log.w("SongListFragment", "No CategoryModel object received!");
            }
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout using View Binding
        binding = FragmentSongListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Access views using binding
        ImageView imgCategory = binding.imgCategories;
        TextView txtCategoryName = binding.nameCategories;
        RecyclerView recyclerView = binding.songListRecyclerView;
        if (selectedCategory != null) {
            txtCategoryName.setText(selectedCategory.getName());

            List<String> songs = selectedCategory.getSongs();
            if (songs != null) {
                Log.d("SongListFragment", "Received songs: " + songs);
                setupSongListRecycler(recyclerView);
            } else {
                Log.w("SongListFragment", "No songs data received!");
            }

            // Check for image URL and use Glide (assuming proper Glide initialization)
            if (selectedCategory.getImgUrl() != null) {
                Glide.with(requireContext())
                        .load(selectedCategory.getImgUrl())
                        .placeholder(R.drawable.saxophone_svgrepo_com) // Placeholder image while loading
                        .into(imgCategory);
                setupSongListRecycler(recyclerView);
            } else {
                // If the image URL is null, set a default placeholder image
                imgCategory.setImageResource(R.drawable.baseline_home_24);
            }
        } else {
            Log.w("SongListFragment", "No CategoryModel object received!");
        }
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

    }
    private void setupSongListRecycler(RecyclerView recyclerView) {
        // Check if song data is available in the category object
        if (selectedCategory != null && selectedCategory.getSongs() != null) {
            // Create SongListAdapter instance with the list of songs from the category
            adapter = new SongListAdapter(selectedCategory.getSongs());

            // Set Layout Manager for the RecyclerView (e.g., LinearLayoutManager)
            binding.songListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

            // Set the adapter for the RecyclerView
            binding.songListRecyclerView.setAdapter(adapter);
        } else {
            Log.w("SongListFragment", "No song data found in the selected category!");
        }
    }
}
