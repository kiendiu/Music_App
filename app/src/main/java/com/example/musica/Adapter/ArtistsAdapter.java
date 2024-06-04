package com.example.musica.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.musica.Fragment.SubFragment.ArtistSongListFragment;
import com.example.musica.Model.ArtistsModel;
import com.example.musica.R;

import java.util.List;

public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ArtistsViewHolder> {

    private Context context;
    private List<ArtistsModel> artistsList;

    public ArtistsAdapter(Context context, List<ArtistsModel> artistsList) {
        this.context = context;
        this.artistsList = artistsList;
    }

    @NonNull
    @Override
    public ArtistsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_artists, parent, false);
        return new ArtistsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistsViewHolder holder, int position) {
        ArtistsModel artist = artistsList.get(position);
        RequestOptions requestOptions = new RequestOptions().circleCrop();

        // Load the artist image using Glide with circle crop
        Glide.with(holder.itemView.getContext())
                .load(artist.getImgUrl())
                .apply(requestOptions)
                .into(holder.imgArtists);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the FragmentManager from the Activity (assuming context is an Activity)
                FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();

                // Create a new ArtistSongListFragment instance and pass the selected artist
                ArtistsModel selectedArtist = artist; // Assign the current artist
                ArtistSongListFragment artistSongListFragment = ArtistSongListFragment.newInstance(selectedArtist);

                // Begin a fragment transaction
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                // Replace the current fragment with ArtistSongListFragment
                transaction.replace(R.id.frame_layout, artistSongListFragment); // Replace with your container ID
                transaction.addToBackStack(null); // Add to back stack (optional)
                transaction.commit();
            }
        });

        // Set the artist name to the TextView
        holder.nameArtists.setText(artist.getName());

        // Load the artist image using Glide into the ImageView
        if (artist.getImgUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(artist.getImgUrl())
                    .apply(requestOptions)
                    .into(holder.imgArtists);
        } else {
            // If the image URL is null, set a default placeholder image
            holder.imgArtists.setImageResource(R.drawable.baseline_home_24);
        }
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }

    static class ArtistsViewHolder extends RecyclerView.ViewHolder {

        ImageView imgArtists;
        TextView nameArtists;

        public ArtistsViewHolder(View itemView) {
            super(itemView);
            imgArtists = itemView.findViewById(R.id.imgArtists);
            nameArtists = itemView.findViewById(R.id.nameTextView);
        }
    }
}