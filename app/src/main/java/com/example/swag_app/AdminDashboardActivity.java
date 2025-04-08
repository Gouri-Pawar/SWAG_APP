package com.example.swag_app;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView welcomeText;
    private LinearLayout cardPastQuizzes, cardCreateQuiz, cardTrackProgress;
    private Button logoutButton, logoutButtonDrawer;
    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private String quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        welcomeText = findViewById(R.id.welcomeText);
        cardPastQuizzes = findViewById(R.id.cardPastQuizzes);
        cardCreateQuiz = findViewById(R.id.cardCreateQuiz);
        cardTrackProgress = findViewById(R.id.cardTrackProgress);
        logoutButton = findViewById(R.id.logoutButton);
        logoutButtonDrawer = findViewById(R.id.logoutButtonDrawer);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);

        // Fetch Admin's name (from Firebase)
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();

            if (email != null && !email.isEmpty()) {
                // Extract username before '@'
                String adminName = email.split("@")[0];
                welcomeText.setText("Welcome " + adminName + " - Admin");
            } else {
                welcomeText.setText("Welcome Admin");
            }
        } else {
            welcomeText.setText("Welcome Admin");
        }

        // Navigation actions
        cardPastQuizzes.setOnClickListener(v -> openPastQuizzes());
        cardCreateQuiz.setOnClickListener(v -> openCreateQuiz());
        cardTrackProgress.setOnClickListener(v -> openTrackProgress());
        logoutButton.setOnClickListener(v -> logout());
        logoutButtonDrawer.setOnClickListener(v -> logout());
    }

    private void openPastQuizzes() {
        Intent intent=new Intent(this, PastQuizzes.class);
        intent.putExtra("quizId", quizId);
        startActivity(intent);
        ActivityAnimationUtil.animateForward(this);
    }

    private void openCreateQuiz() {
        startActivity(new Intent(this, CreateQuiz.class));
        ActivityAnimationUtil.animateForward(this);
    }

    private void openTrackProgress() {
        Intent intent = new Intent(AdminDashboardActivity.this, TrackProgressActivity.class);
        intent.putExtra("quizId", quizId);
        startActivity(intent);
        ActivityAnimationUtil.animateForward(this);
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        ActivityAnimationUtil.animateBackward(this);
        finish();
    }
}
