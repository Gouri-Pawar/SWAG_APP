package com.example.swag_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private RadioGroup roleRadioGroup;
    private RadioButton adminRadioButton, studentRadioButton;
    private Button signUpButton;
    private ProgressBar progressBar;

    private String selectedRole = "Student"; // default
    private static final String ADMIN_CODE = "SGGS_SWAG_2025";
    private boolean isAdminCodeVerified = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        adminRadioButton = findViewById(R.id.adminRadioButton);
        studentRadioButton = findViewById(R.id.studentRadioButton);
        signUpButton = findViewById(R.id.signUpButton);
        progressBar = findViewById(R.id.progressBar);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Radio button clicks
        adminRadioButton.setOnClickListener(v -> {
            if (!isAdminCodeVerified) {
                showAdminCodeDialog();
            }
        });

        studentRadioButton.setOnClickListener(v -> {
            selectedRole = "Student";
            isAdminCodeVerified = false;
        });

        // SignUp button
        signUpButton.setOnClickListener(v -> attemptSignup());
    }

    private void showAdminCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Admin Code Required");

        final EditText input = new EditText(this);
        input.setHint("Enter Admin Code");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String enteredCode = input.getText().toString().trim();
            if (enteredCode.equals(ADMIN_CODE)) {
                selectedRole = "Admin";
                isAdminCodeVerified = true;
                Toast.makeText(this, "Admin verified!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Invalid Admin Code", Toast.LENGTH_SHORT).show();
                studentRadioButton.setChecked(true);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            studentRadioButton.setChecked(true);
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void attemptSignup() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (adminRadioButton.isChecked() && !isAdminCodeVerified) {
            Toast.makeText(this, "Admin code not verified", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                if (verifyTask.isSuccessful()) {
                                    showVerificationDialog(user, email);
                                } else {
                                    Toast.makeText(this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(this, "Signup Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showVerificationDialog(FirebaseUser user, String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email Verification");

        builder.setMessage("A verification email has been sent to " + email + ". Please verify and then click 'Verified'.");

        builder.setPositiveButton("Verified", (dialog, which) -> {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    saveUserToFirestore(user.getUid(), email, selectedRole);
                } else {
                    user.delete(); // delete unverified account
                    Toast.makeText(this, "Email not verified. Signup cancelled.", Toast.LENGTH_LONG).show();
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            user.delete();
            dialog.dismiss();
            Toast.makeText(this, "Signup cancelled.", Toast.LENGTH_SHORT).show();
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void saveUserToFirestore(String uid, String email, String role) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("role", role);

        db.collection("users").document(uid).set(userData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Signup complete and verified!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
