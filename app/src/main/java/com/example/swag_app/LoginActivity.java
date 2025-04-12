package com.example.swag_app;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.BounceInterpolator;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swag_app.AdminActivities.AdminDashboardActivity;
import com.example.swag_app.StudentActivities.StudentDashboard;
import com.example.swag_app.util.ActivityAnimationUtil;
import com.google.firebase.auth.*;
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
        String selectedRole = adminRadioButton.isChecked() ? "Admin" : studentRadioButton.isChecked() ? "Student" : "";

        if (email.isEmpty() || password.isEmpty() || selectedRole.isEmpty()) {
            showToast("Please fill all fields and select a role");
            return;
        }

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String storedRole = document.getString("role");

                        if (!selectedRole.equals(storedRole)) {
                            showToast("Role mismatch. Please select the correct role.");
                            return;
                        }

                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null && user.isEmailVerified()) {
                                            navigateToDashboard(storedRole);
                                        } else {
                                            mAuth.signOut(); // logout unverified user
                                            showToast("Please verify your email before logging in.");
                                        }
                                    } else {
                                        showToast("Login failed: " + task.getException().getMessage());
                                    }
                                });

                    } else {
                        showToast("User not found. Please sign up.");
                    }
                })
                .addOnFailureListener(e -> showToast("Error checking role: " + e.getMessage()));
    }

    private void navigateToDashboard(String role) {
        if ("Admin".equals(role)) {
            showToast("Logged in as Admin");
            startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
            ActivityAnimationUtil.animateForward(this);
        } else {
            showToast("Logged in as Student");
            startActivity(new Intent(LoginActivity.this, StudentDashboard.class));
            ActivityAnimationUtil.animateForward(this);
        }
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void animateLoginButton() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(loginButton, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(loginButton, "scaleY", 0.8f, 1f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);

        // Apply bounce effect
        scaleX.setInterpolator(new BounceInterpolator());
        scaleY.setInterpolator(new BounceInterpolator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
    }

}
