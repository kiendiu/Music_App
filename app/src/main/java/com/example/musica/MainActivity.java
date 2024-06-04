package com.example.musica;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.musica.Fragment.BottomMenuFragment.HomeFragment;
import com.example.musica.Fragment.BottomMenuFragment.LibraryFragment;
import com.example.musica.Fragment.BottomMenuFragment.SearchFragment;
import com.example.musica.Fragment.BottomMenuFragment.UserFragment;
import com.example.musica.Fragment.SubFragment.AddSongToPlaylistFragment;
import com.example.musica.Model.PlaylistModel;
import com.example.musica.Model.SongModel;
import com.example.musica.Object.MyExoplayer;
import com.example.musica.databinding.ActivityMainBinding;
import com.example.musica.databinding.ActivityMusicPlayerBinding;
import com.example.musica.databinding.MiniPlayerLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String playlistName;
    ActivityMainBinding binding;
    ActivityMusicPlayerBinding musicPlayerBinding;
    private boolean isPlaying = false;
    private View  miniPlayer;
    private ImageView miniImgSongs, miniPausePlay;
    private TextView miniNameSongs, miniArtistsSongs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d("MainActivity", "User ID: " + userId);
            // Gọi fragment LibraryFragment và truyền userId vào đó
            LibraryFragment libraryFragment = new LibraryFragment();
            AddSongToPlaylistFragment addSongToPlaylistFragment = new AddSongToPlaylistFragment();

            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);

            libraryFragment.setArguments(bundle);
            addSongToPlaylistFragment.setArguments(bundle);
            replaceFragment(libraryFragment);
            replaceFragment(addSongToPlaylistFragment);

        } else {
            Log.d("MainActivity", "User not logged in");
        }

        // Kiểm tra intent để xác định xem cần mở HomeFragment hay không
        if (getIntent().getBooleanExtra("goToHomeFragment", false)) {
            replaceFragment(new HomeFragment());
        } else {
            replaceFragment(new HomeFragment());  // Load HomeFragment mặc định
        }

        binding.bottomNavigationView.setBackground(null);
//        binding.floatingbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openCreatePlaylistAlertDialog();
//            }
//        });

        setupBottomNavigation();

        SongModel currentSong = MyExoplayer.getCurrentSong();

        miniPlayer = binding.miniPlayer.getRoot();
        MiniPlayerLayoutBinding miniPlayerBinding = MiniPlayerLayoutBinding.bind(miniPlayer);
        miniImgSongs = miniPlayerBinding.miniImgSongs;
        miniPausePlay = miniPlayerBinding.miniPausePlay;
        miniNameSongs = miniPlayerBinding.miniNameSongs;
        miniArtistsSongs = miniPlayerBinding.miniArtistsSongs;
        miniPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MusicPlayerActivity.class);
                startActivity(intent);
            }
        });
        miniPausePlay.setOnClickListener(v -> togglePause());
        CollectionReference songsRef = FirebaseFirestore.getInstance().collection("songs");
        // Thực hiện truy vấn để lấy danh sách bài hát
        songsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<SongModel> playlistFromFirebase = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Lấy dữ liệu của mỗi bài hát từ Firestore và thêm vào danh sách bài hát
                    SongModel song = document.toObject(SongModel.class);
                    playlistFromFirebase.add(song);
                }
                // Sau khi lấy được danh sách bài hát từ Firebase, tiếp tục xử lý
                handlePlaylistFromFirebase(playlistFromFirebase);
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
        MyExoplayer.initializePlayer(this);
        MyExoplayer.getExoPlayer().addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                updateUIWithCurrentSong();
            }
        });
        if (currentSong != null) {
            miniNameSongs.setText(currentSong.getName());
            miniArtistsSongs.setText(currentSong.getArtists());
            Glide.with(this)
                    .load(currentSong.getImgUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(miniImgSongs);
        }
    }private void handlePlaylistFromFirebase(List<SongModel> playlistFromFirebase) {
        // Lấy bài hát đầu tiên từ danh sách làm bài hát mặc định (nếu có)
        SongModel defaultSong = null;
        if (!playlistFromFirebase.isEmpty()) {
            defaultSong = playlistFromFirebase.get(7); //Your Love Is King
        }
        MyExoplayer.initializePlayer(this);
        MyExoplayer.setSongList(playlistFromFirebase);
        if (defaultSong != null) {
            MyExoplayer.startPlaying(this, defaultSong, playlistFromFirebase);
            MyExoplayer.pause();
//            miniPausePlay.setImageResource(R.drawable.baseline_play_circle_24);
            updateUIWithCurrentSong();
        }
    }
    private void updateUIWithCurrentSong() {
        SongModel currentSong = MyExoplayer.getCurrentSong();
        if (currentSong != null && !isDestroyed() && !isFinishing()) {
            Log.d("MusicPlayerActivity", "Updating UI with current song: " + currentSong.getName());
            // Update mini player views
            miniNameSongs.setText(currentSong.getName());
            miniArtistsSongs.setText(currentSong.getArtists());
            Glide.with(this)
                    .load(currentSong.getImgUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(miniImgSongs);

            Log.d("MusicPlayerActivity", "Mini Player updated with song: " + currentSong.getName());
        } else {
            Log.d("MusicPlayerActivity", "No current song found or Activity is destroyed");
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);

        if (currentFragment instanceof HomeFragment) {
            // Handle back press in HomeFragment
            // Ví dụ: hiển thị một thông báo
            // Bạn có thể đặt thêm logic xử lý khác nếu muốn
        } else {
            // If there are more than one fragments in the back stack, pop the back stack
            if (fragmentManager.getBackStackEntryCount() > 1) {
                fragmentManager.popBackStack();
            } else {
                // If only one fragment is left (HomeFragment), then finish the activity
                finish();
            }
        }
    }
    private void togglePause() {
        ExoPlayer player = MyExoplayer.getExoPlayer();
        if (player != null) {
            if (MyExoplayer.isPlaying()) {
                player.pause();
                miniPausePlay.setImageResource(R.drawable.baseline_play_circle_24);
            } else {
                player.play();
                miniPausePlay.setImageResource(R.drawable.baseline_pause_circle_outline_24);
            }
            MyExoplayer.togglePlayPause();
        }
    }
    private void setupBottomNavigation() {
        binding.bottomNavigationView.setBackground(null);
//        binding.floatingbtn.setOnClickListener(v -> openCreatePlaylistAlertDialog());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId(); // Store the resource ID in a variable

            if (itemId == R.id.homepage) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.subscriptions) {
                replaceFragment(new SearchFragment());
            } else if (itemId == R.id.library) {
                replaceFragment(new LibraryFragment());
            } else if (itemId == R.id.user) {
                replaceFragment(new UserFragment());
            }
            return true;
        });
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);

            fragment.setArguments(bundle);

            fragmentTransaction.replace(R.id.frame_layout, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            Log.d("MainActivity", "No current user");
        }
    }

    private void openCreatePlaylistAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.create_playlist_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        TextInputEditText inputDialog = dialogView.findViewById(R.id.editTextDialog);
        CardView confirmButton = dialogView.findViewById(R.id.confirm_button);
        CardView cancelButton = dialogView.findViewById(R.id.cancel_button);
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getUid();
        playlistName = String.valueOf(inputDialog.getText());

        if (!playlistName.isEmpty()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference playlistsRef = db.collection("playlists");

            PlaylistModel playlist = new PlaylistModel(playlistName, userId,"https://firebasestorage.googleapis.com/v0/b/musicproject-53d9d.appspot.com/o/playlist.png?alt=media&token=bf8bce8e-926d-4a15-94cb-488135dcae41",new ArrayList<>());

            playlistsRef.add(playlist)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "Playlist added with ID: " + documentReference.getId());
                            dialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding playlist", e);
                        }
                    });
        } else {
            Toast.makeText(this, "Please enter a playlist name", Toast.LENGTH_SHORT).show();
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth = FirebaseAuth.getInstance();
                String userId = mAuth.getUid();
                playlistName = String.valueOf(inputDialog.getText());

                if (!playlistName.isEmpty()) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference playlistsRef = db.collection("playlists");

                    // Kiểm tra xem tên playlist đã tồn tại chưa
                    playlistsRef.whereEqualTo("name", playlistName)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().size() > 0) {
                                        } else {
                                            // Nếu tên playlist chưa tồn tại, thêm vào Firestore
                                            PlaylistModel playlist = new PlaylistModel(playlistName, userId, "https://firebasestorage.googleapis.com/v0/b/musicproject-53d9d.appspot.com/o/playlist.png?alt=media&token=bf8bce8e-926d-4a15-94cb-488135dcae41", new ArrayList<>());

                                            playlistsRef.add(playlist)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Log.d(TAG, "Playlist added with ID: " + documentReference.getId());
                                                            dialog.dismiss();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w(TAG, "Error adding playlist", e);
                                                        }
                                                    });
                                        }
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                }
            }
        });

    }
}