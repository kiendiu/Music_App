package com.example.musica.LoginHandle;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.musica.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {

    private static final String TAG = "ResetPassword";

    private EditText editTextEmail;
    private TextView toLogin;
    private FirebaseAuth mAuth;
    private Button buttonResetPassword;
    private LottieAnimationView lottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        initViews();
        lottieAnimationView = findViewById(R.id.lottieAnimation);
        lottieAnimationView.setAnimation(R.raw.password); // Set your animation resource
        lottieAnimationView.playAnimation();
        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    showMessage("Enter your email!");
                } else if (!isValidEmail(email)) {
                    showMessage("Invalid email address");
                } else {
                    forgotPassword(email);
                }
            }
        });

        toLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonResetPassword = findViewById(R.id.button_reset_password);
        toLogin = findViewById(R.id.to_login);
        mAuth = FirebaseAuth.getInstance();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }

    private void showMessage(String message) {
        Toast.makeText(ResetPassword.this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

//    private void forgotPassword(final String email) {
//        buttonResetPassword.setEnabled(false);
//
//        mAuth.fetchSignInMethodsForEmail(email)
//                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
//                        buttonResetPassword.setEnabled(true);
//
//                        if (task.isSuccessful()) {
//                            SignInMethodQueryResult result = task.getResult();
//                            if (result != null && result.getSignInMethods() != null && !result.getSignInMethods().isEmpty()) {
//                                sendResetEmail(email);
//                            } else {
//                                showMessage("Email does not exist");
//                            }
//                        } else {
//                            Log.e(TAG, "Error checking email existence: ", task.getException());
//                            showMessage("Failed to check email existence");
//                        }
//                    }
//                });
//    }
//
//    private void sendResetEmail(String email) {
//        mAuth.sendPasswordResetEmail(email)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        showMessage("Password reset email sent successfully");
//                        navigateToLogin();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        showMessage("Error: " + e.getMessage());
//                        buttonResetPassword.setEnabled(true);
//                    }
//                });
//    }

    private void forgotPassword(String email) {
        buttonResetPassword.setEnabled(false); // Tạm thời vô hiệu hóa nút reset password

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showMessage("Password reset email sent successfully");
                        navigateToLogin(); // Điều hướng về màn hình đăng nhập sau khi gửi email thành công
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showMessage("Error: " + e.getMessage());
                        buttonResetPassword.setEnabled(true); // Kích hoạt lại nút reset password nếu có lỗi
                    }
                });
    }
}