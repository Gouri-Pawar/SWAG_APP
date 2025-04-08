package com.example.swag_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private RadioButton adminRadioButton, studentRadioButton;
    private Button loginButton;
    private TextView signUpTextView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        adminRadioButton = findViewById(R.id.adminRadioButton);
        studentRadioButton = findViewById(R.id.studentRadioButton);
        loginButton = findViewById(R.id.loginButton);
        signUpTextView = findViewById(R.id.signUpTextView);

        // Click listeners
        loginButton.setOnClickListener(v -> {
            animateLoginButton();
            loginUser();
        });

        signUpTextView.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please enter both email and password");
            return;
        }

        // Authenticate using FirebaseAuth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkUserRole(email);  // Check role after successful login
                    } else {
                        showToast("Login failed: " + task.getException().getMessage());
                    }
                });
    }

    private void checkUserRole(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String role = document.getString("role");
                            if ("admin".equals(role) && adminRadioButton.isChecked()) {
                                showToast("Logged in as Admin");
                                startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                                finish();
                            } else if ("student".equals(role) && studentRadioButton.isChecked()) {
                                showToast("Logged in as Student");
                                startActivity(new Intent(LoginActivity.this, StudentDashboard.class));
                                finish();
                            } else {
                                showToast("Role mismatch. Please check the correct role.");
                            }
                        }
                    } else {
                        showToast("User role not found. Please sign up.");
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void animateLoginButton() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(loginButton, "scaleX", 0.9f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(loginButton, "scaleY", 0.9f, 1f);
        scaleX.setDuration(200);
        scaleY.setDuration(200);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
    }
}
