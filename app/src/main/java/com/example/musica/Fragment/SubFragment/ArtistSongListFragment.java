package com.example.musica.Fragment.SubFragment;

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
import com.example.musica.Model.ArtistsModel;
import com.example.musica.R;
import com.example.musica.databinding.FragmentSongListBinding;

import java.util.List;

public class ArtistSongListFragment extends Fragment {

    private static final String ARG_PARAM_ARTIST = "artist";
    private FragmentSongListBinding binding; // Tham chiếu View Binding
    private ArtistsModel selectedArtist;

    private SongListAdapter adapter; // Tham chiếu đến SongListAdapter
    public ArtistSongListFragment() {
    }

    public static ArtistSongListFragment newInstance(ArtistsModel artist) {
        ArtistSongListFragment fragment = new ArtistSongListFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM_ARTIST, artist);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedArtist = getArguments().getParcelable(ARG_PARAM_ARTIST);
            if (selectedArtist != null) {
                Log.d("ArtistSongListFragment", "Received Artist: " + selectedArtist.getName());
                Log.d("ArtistSongListFragment", "Received Artist: " + selectedArtist.getImgUrl());

            } else {
                Log.w("ArtistSongListFragment", "No ArtistsModel object received!");
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

        // Truy cập views sử dụng binding
        ImageView imgArtist = binding.imgCategories;
        TextView txtArtistName = binding.nameCategories;
        RecyclerView recyclerView = binding.songListRecyclerView;
        if (selectedArtist != null) {
            txtArtistName.setText(selectedArtist.getName());

            List<String> songs = selectedArtist.getSongs();
            if (songs != null) {
                Log.d("ArtistSongListFragment", "Received songs: " + songs);
                setupSongListRecycler(recyclerView);
            } else {
                Log.w("ArtistSongListFragment", "No songs data received!");
            }

            // Kiểm tra URL hình ảnh và sử dụng Glide (giả sử khởi tạo Glide đúng cách)
            if (selectedArtist.getImgUrl() != null) {
                Glide.with(requireContext())
                        .load(selectedArtist.getImgUrl())
                        .placeholder(R.drawable.saxophone_svgrepo_com) // Hình ảnh tạm thời trong quá trình tải
                        .into(imgArtist);
                setupSongListRecycler(recyclerView);
            } else {
                // Nếu URL hình ảnh là null, đặt một hình ảnh mặc định
                imgArtist.setImageResource(R.drawable.baseline_home_24);
            }
        } else {
            Log.w("ArtistSongListFragment", "No ArtistsModel object received!");
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
        // Kiểm tra xem dữ liệu bài hát có sẵn trong đối tượng nghệ sĩ không
        if (selectedArtist != null && selectedArtist.getSongs() != null) {
            // Tạo một thể hiện của SongListAdapter với danh sách bài hát từ nghệ sĩ
            adapter = new SongListAdapter(selectedArtist.getSongs());

            // Đặt Layout Manager cho RecyclerView (ví dụ: LinearLayoutManager)
            binding.songListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

            // Đặt adapter cho RecyclerView
            binding.songListRecyclerView.setAdapter(adapter);
        } else {
            Log.w("ArtistSongListFragment", "No song data found for the selected artist!");
        }
    }
}