package com.example.swag_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudentDashboard extends BaseActivityStudent {

    private DrawerLayout drawerLayout;
    private TextView welcomeText;
    private TextView txtQuizCount;
    private TextView txtAttemptedCount;
    private TextView txtAverageScore;
    private MaterialCardView cardAvailableQuizzes;
    private MaterialCardView cardAttemptedQuizzes;
    private MaterialCardView cardViewScores;
    private FloatingActionButton fabStartQuiz;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_student_dashboard);
        setToolbarTitle("Student Dashboard");
        setupNavigationDrawer();

        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        updateUserInfo();
        setupClickListeners();
        loadDashboardData();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.student_drawer_layout);
        welcomeText = findViewById(R.id.studentWelcomeText);
        txtQuizCount = findViewById(R.id.txtQuizCount);
        txtAttemptedCount = findViewById(R.id.txtAttemptedCount);
        txtAverageScore = findViewById(R.id.txtAverageScore);

        cardAvailableQuizzes = findViewById(R.id.cardAvailableQuizzes);
        cardAttemptedQuizzes = findViewById(R.id.cardAttemptedQuizzes);
        cardViewScores = findViewById(R.id.cardViewScores);
        fabStartQuiz = findViewById(R.id.fabStartQuiz);
    }

    private void updateUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String studentName = user.getEmail().split("@")[0];
            welcomeText.setText("Welcome, " + studentName + "!");
        } else {
            welcomeText.setText("Welcome, Student!");
        }
    }

    private void setupClickListeners() {
        cardAvailableQuizzes.setOnClickListener(v -> {
            startActivity(new Intent(this, AvailableQuizzesActivity.class));
            ActivityAnimationUtil.animateForward(this);
        });

        cardAttemptedQuizzes.setOnClickListener(v -> {
            startActivity(new Intent(this, AttemptedQuizzesActivity.class));
            ActivityAnimationUtil.animateForward(this);
        });

        cardViewScores.setOnClickListener(v -> {
            startActivity(new Intent(this, ViewScoresActivity.class));
            ActivityAnimationUtil.animateForward(this);
        });

        fabStartQuiz.setOnClickListener(v -> {
            startActivity(new Intent(this, AvailableQuizzesActivity.class));
            ActivityAnimationUtil.animateForward(this);
        });
    }

    private void loadDashboardData() {
        // Mock data
        txtQuizCount.setText("3 quizzes available");
        txtAttemptedCount.setText("2 quizzes completed");
        txtAverageScore.setText("Average score: 85%");
    }
}
