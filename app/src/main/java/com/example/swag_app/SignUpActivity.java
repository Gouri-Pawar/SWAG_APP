package com.example.swag_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private TextView loginText;
    private RadioButton adminRadioButton, studentRadioButton;
    private Button signUpButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        adminRadioButton = findViewById(R.id.adminRadioButton);
        studentRadioButton = findViewById(R.id.studentRadioButton);
        signUpButton = findViewById(R.id.signUpButton);
        progressBar = findViewById(R.id.progressBar);
        loginText = findViewById(R.id.loginLinkText);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signUpButton.setOnClickListener(v -> registerUser());

        loginText.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String role = adminRadioButton.isChecked() ? "admin" : studentRadioButton.isChecked() ? "student" : "";

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role.isEmpty()) {
            showToast("Please fill all fields and select a role");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    saveUserToFirestore(email, password, role);
                })
                .addOnFailureListener(e -> {
                    showToast("Sign up failed: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void saveUserToFirestore(String email, String password, String role) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("password", password); // 🔒 Optional: Hash this before storing in production
        user.put("role", role);

        db.collection("users").add(user)
                .addOnSuccessListener(documentReference -> {
                    showToast("Sign up successful!");
                    progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to save user: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
