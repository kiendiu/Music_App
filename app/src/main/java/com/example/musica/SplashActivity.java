package com.example.musica;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.airbnb.lottie.LottieAnimationView;
import com.example.musica.LoginHandle.Login;
import com.example.musica.MainActivity;
import com.example.musica.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    private LottieAnimationView lottieAnimationView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        lottieAnimationView = findViewById(R.id.lottieAnimation);
        lottieAnimationView.setAnimation(R.raw.guitar); // Set your animation resource
        lottieAnimationView.playAnimation(); // Start the animation

        mAuth = FirebaseAuth.getInstance();

        // Delay for 3 seconds before navigating to the next activity
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        Intent intent;
                        if (currentUser != null) {
                            // User is signed in, navigate to MainActivity
                            intent = new Intent(SplashActivity.this, MainActivity.class);
                        } else {
                            // No user is signed in, navigate to Login activity
                            intent = new Intent(SplashActivity.this, Login.class);
                        }
                        startActivity(intent);
                        finish();
                    }
                },
                2000 // 2 seconds delay
        );
    }
}
