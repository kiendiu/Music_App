package com.example.musica;

import static com.example.musica.Adapter.PlaylistAdapter.playlistList;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.musica.Adapter.PlaylistAdapter;
import com.example.musica.Fragment.SubFragment.AddSongToPlaylistFragment;
import com.example.musica.Model.SongModel;
import com.example.musica.Object.MyExoplayer;
import com.example.musica.databinding.ActivityMainBinding;
import com.example.musica.databinding.ActivityMusicPlayerBinding;
import com.example.musica.databinding.MiniPlayerLayoutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;

public class MusicPlayerActivity extends AppCompatActivity {
    private SeekBar seekBar,vol_seekBar;
    private TextView currentTimeTextView;
    private TextView totalTimeTextView;
    ActivityMainBinding activityMainBinding;
    private boolean isSeekBarDragging = false;
    private final Handler handler = new Handler();
    private ImageView pausePlayButton;
    private ActivityMusicPlayerBinding binding;
    private static ExoPlayer exoPlayer;
    private View fullPlayer, miniPlayer;
    private ImageView miniImgSongs, miniPausePlay;
    private TextView miniNameSongs, miniArtistsSongs;
    private GestureDetector gestureDetector;
    private boolean isLiked = false;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exoPlayer = MyExoplayer.getExoPlayer();
        // Initialize binding before calling setContentView
        binding = ActivityMusicPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Initialize views
        fullPlayer = binding.fullPlayer;
        miniPlayer = binding.miniPlayer.getRoot();
        ImageView imgSongs = binding.imgSongs;
        pausePlayButton = binding.pausePlay;
        seekBar = binding.seekBar;
        currentTimeTextView = binding.currentTime;
        vol_seekBar = binding.volumeSeekbar;
        totalTimeTextView = binding.totalTime;
        ImageView backBtn = binding.backBtn;
        ImageView nextBtn = binding.next;
        ImageView previousBtn = binding.previous;
        RelativeLayout parentLayout = binding.parentLayout;
        // Mini player views
        MiniPlayerLayoutBinding miniPlayerBinding = MiniPlayerLayoutBinding.bind(miniPlayer);
        miniImgSongs = miniPlayerBinding.miniImgSongs;
        miniPausePlay = miniPlayerBinding.miniPausePlay;
        miniNameSongs = miniPlayerBinding.miniNameSongs;
        miniArtistsSongs = miniPlayerBinding.miniArtistsSongs;
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1.getY() - e2.getY() > 50) {
                    minimizePlayer();
                    return true;
                }
                return false;
            }
        });
        parentLayout.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Set touch listener for full player to detect swipe gestures
        fullPlayer.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Set click listener for pause/play button
        pausePlayButton.setOnClickListener(v -> togglePause());

        // Mini player click listener to expand full player
        miniPlayer.setOnClickListener(v -> expandFullPlayer());
        miniPausePlay.setOnClickListener(v -> togglePause());


    // Set click listener for next button
        nextBtn.setOnClickListener(v -> {
            playNextSong();
            pausePlayButton.setImageResource(R.drawable.baseline_pause_circle_outline_24);
            miniPausePlay.setImageResource(R.drawable.baseline_pause_circle_outline_24);
        });

        // Set click listener for previous button
        previousBtn.setOnClickListener(v -> {
            playPreviousSong();
            pausePlayButton.setImageResource(R.drawable.baseline_pause_circle_outline_24);
            miniPausePlay.setImageResource(R.drawable.baseline_pause_circle_outline_24);
        });
        PlaylistAdapter playlistAdapter = new PlaylistAdapter(playlistList, this);
        SongModel currentSong = MyExoplayer.getCurrentSong();
        String songId = getIntent().getStringExtra("songId");
        String imgSong = getIntent().getStringExtra("imgSong");

        Bundle bundle = new Bundle();
        bundle.putString("songId", songId);
        playlistAdapter.setSongId(songId);
        playlistAdapter.setCurrentSongId(songId);
        AddSongToPlaylistFragment addSongListFragment = new AddSongToPlaylistFragment();
        addSongListFragment.setArguments(bundle);

        // Log to check if songId has been retrieved
        Log.d("MusicPlayerActivity", "Song ID: " + songId);

        if (currentSong != null) {
            binding.nameSongs.setText(currentSong.getName());
            binding.artistsSongs.setText(currentSong.getArtists());
            Glide.with(binding.getRoot().getContext())
                    .load(currentSong.getImgUrl())
//                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.imgSongs);
        }

        if (currentSong != null) {
            miniNameSongs.setText(currentSong.getName());
            miniArtistsSongs.setText(currentSong.getArtists());
            Glide.with(this)
                    .load(currentSong.getImgUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(miniImgSongs);
        }
        binding.moreBtn.setOnClickListener(v -> {
            // Create a dialog and set the layout
            Dialog dialog = new Dialog(MusicPlayerActivity.this);
            dialog.setContentView(R.layout.more_option_dialog);
            // Set dialog properties
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().setDimAmount(0.8f);
            dialog.setCancelable(true);
            LinearLayout layoutAdd = dialog.findViewById(R.id.layoutAdd);
            layoutAdd.setOnClickListener(v1 -> {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(android.R.id.content, addSongListFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                dialog.dismiss();
            });

            // Show the dialog
            dialog.show();
        });

        pausePlayButton.setOnClickListener(v -> togglePause());

        // Set up SeekBar change listener
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        // Update SeekBar and time TextViews
        initVolumeSeekBar();

        updateSeekBar();
        // Set up handler to update SeekBar and time TextViews periodically
        handler.postDelayed(seekBarUpdater, 1000);

        if (MyExoplayer.getExoPlayer() != null) {
            MyExoplayer.getExoPlayer().addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_ENDED) {
                        playNextSong();
                    }
                }
            });
            MyExoplayer.getExoPlayer().addListener(new Player.Listener() {
                @Override
                public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                    Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                    updateUIWithCurrentSong();
                }
            });
        }
        binding.likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLikeStatus(getIntent().getStringExtra("songId"));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUIWithCurrentSong();
    }
    private void initVolumeSeekBar(){
        final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            binding.volumeSeekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            binding.volumeSeekbar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            binding.volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//    }
    private final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && exoPlayer != null) {
                long duration = exoPlayer.getDuration();
                long newPosition = (duration * progress) / 100;
                exoPlayer.seekTo(newPosition);
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekBarDragging = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isSeekBarDragging = false;
        }
    };

    private final Runnable seekBarUpdater = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
            handler.postDelayed(this, 1000);
        }
    };

    private void updateSeekBar() {
        if (exoPlayer != null && !isSeekBarDragging) {
            long currentPosition = exoPlayer.getCurrentPosition();
            long duration = exoPlayer.getDuration();

            if (duration > 0) {
                int progress = (int) ((currentPosition * 100) / duration);
                seekBar.setProgress(progress);
            }

            currentTimeTextView.setText(formatTime(currentPosition));
            totalTimeTextView.setText(formatTime(duration));
        }
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(long timeInMillis) {
        int totalSeconds = (int) (timeInMillis / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    private void togglePause() {
        ExoPlayer player = MyExoplayer.getExoPlayer();
        if (player != null) {
            if (MyExoplayer.isPlaying()) {
                player.pause();
                pausePlayButton.setImageResource(R.drawable.baseline_play_circle_24);
                miniPausePlay.setImageResource(R.drawable.baseline_play_circle_24);
            } else {
                player.play();
                pausePlayButton.setImageResource(R.drawable.baseline_pause_circle_outline_24);
                miniPausePlay.setImageResource(R.drawable.baseline_pause_circle_outline_24);
            }
            MyExoplayer.togglePlayPause();
        }
    }
    private void expandFullPlayer() {
        fullPlayer.setVisibility(View.VISIBLE);
        miniPlayer.setVisibility(View.GONE);
        updateUIWithCurrentSong();
    }

    private void minimizePlayer() {
        fullPlayer.setVisibility(View.GONE);
        miniPlayer.setVisibility(View.VISIBLE);
        updateUIWithCurrentSong();

        // Hiển thị mini player khi thu nhỏ
        View miniPlayerView = findViewById(R.id.miniPlayer);
        if (miniPlayerView != null) {
            miniPlayerView.setVisibility(View.VISIBLE);
        }
    }

    private void playNextSong() {
        MyExoplayer.playNextSong();
        updateUIWithCurrentSong();

    }

    private void playPreviousSong() {
        MyExoplayer.playPreviousSong();
        updateUIWithCurrentSong();

    }
    private void updateUIWithCurrentSong() {
        SongModel currentSong = MyExoplayer.getCurrentSong();
        if (currentSong != null && !isDestroyed() && !isFinishing()) {
            Log.d("MusicPlayerActivity", "Updating UI with current song: " + currentSong.getName());

            // Update full player views
            binding.nameSongs.setText(currentSong.getName());
            binding.artistsSongs.setText(currentSong.getArtists());
            Glide.with(this)
                    .load(currentSong.getImgUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.imgSongs);

            // Update mini player views
            miniNameSongs.setText(currentSong.getName());
            miniArtistsSongs.setText(currentSong.getArtists());
            Glide.with(this)
                    .load(currentSong.getImgUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(miniImgSongs);

            updateLikeIconState(getIntent().getStringExtra("songId"));

            Log.d("MusicPlayerActivity", "Mini Player updated with song: " + currentSong.getName());
        } else {
            Log.d("MusicPlayerActivity", "No current song found or Activity is destroyed");
        }
    }
    private void updateLikeIconState(String songId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String userId = user != null ? user.getUid() : null;
        if (userId == null) {
            Log.e("MusicPlayerActivity", "User not authenticated");
            return;
        }
        String playlistName = "Liked song"; // Đảm bảo là chính xác với tên của playlist trong Firestore
        CollectionReference playlistsRef = db.collection("playlists");
        // Query để lấy playlist "Liked song" của user hiện tại
        playlistsRef.whereEqualTo("userID", userId)
                .whereEqualTo("name", playlistName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot playlistDoc = queryDocumentSnapshots.getDocuments().get(0);
                        List<String> songIds = (List<String>) playlistDoc.get("songs");
                        if (songIds != null && songIds.contains(songId)) {
                            isLiked = true;
                            binding.likeIcon.setImageResource(R.drawable.heart_icon_fill);
                        } else {
                            isLiked = false;
                            binding.likeIcon.setImageResource(R.drawable.heart_icon);
                        }
                    } else {
                        isLiked = false;
                        binding.likeIcon.setImageResource(R.drawable.heart_icon);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MusicPlayerActivity", "Error getting liked songs playlist", e);
                    isLiked = false;
                    binding.likeIcon.setImageResource(R.drawable.heart_icon);
                });
    }
    private void toggleLikeStatus(String songId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String userId = user != null ? user.getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        String playlistName = "Liked song";
        CollectionReference playlistsRef = db.collection("playlists");
        // Query để lấy playlist "Liked song" của user hiện tại
        playlistsRef.whereEqualTo("userID", userId)
                .whereEqualTo("name", playlistName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot playlistDoc = task.getResult().getDocuments().get(0);
                        String playlistId = playlistDoc.getId();
                        DocumentReference playlistRef = playlistsRef.document(playlistId);

                        if (isLiked) {
                            // Remove song from playlist
                            playlistRef.update("songs", FieldValue.arrayRemove(songId))
                                    .addOnSuccessListener(aVoid -> {
                                        isLiked = false;
                                        binding.likeIcon.setImageResource(R.drawable.heart_icon);
                                        Toast.makeText(this, "Removed from Liked song", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("MusicPlayerActivity", "Error removing song from Liked song", e);
                                        Toast.makeText(this, "Failed to remove from Liked song", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // Add song to playlist
                            playlistRef.update("songs", FieldValue.arrayUnion(songId))
                                    .addOnSuccessListener(aVoid -> {
                                        isLiked = true;
                                        binding.likeIcon.setImageResource(R.drawable.heart_icon_fill);
                                        Toast.makeText(this, "Added to Liked song", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("MusicPlayerActivity", "Error adding song to Liked song", e);
                                        Toast.makeText(this, "Failed to add to Liked song", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.e("MusicPlayerActivity", "Error getting playlist", task.getException());
                    }
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(seekBarUpdater);
    }
}