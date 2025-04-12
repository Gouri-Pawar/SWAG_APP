package com.example.swag_app.StudentActivities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.swag_app.BaseActivityStudent;
import com.example.swag_app.R;
import com.example.swag_app.util.ActivityAnimationUtil;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    @Override
    protected void onSwipeToRefresh() {
        txtQuizCount.setText("Refreshing...");
        txtAttemptedCount.setText("Refreshing..");
        txtAverageScore.setText("Refreshing..");

        loadDashboardData();
        stopRefreshing();
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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        // 1. Fetch total quizzes
        db.collection("quizzes").get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalQuizzes = querySnapshot.size();
                    txtQuizCount.setText(totalQuizzes + " quizzes available");
                })
                .addOnFailureListener(e -> txtQuizCount.setText("Quizzes not available"));

        // 2. Fetch attempts by current user from quizAttempts
        db.collection("quizAttempts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int attemptedCount = querySnapshot.size();
                    double totalPercentage = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Long score = doc.getLong("score");
                        Long totalQ = doc.getLong("totalQuestions");

                        if (score != null && totalQ != null && totalQ != 0) {
                            totalPercentage += (score * 100.0) / totalQ;
                        }
                    }

                    txtAttemptedCount.setText(attemptedCount + " quizzes completed");

                    if (attemptedCount > 0) {
                        double avg = totalPercentage / attemptedCount;
                        txtAverageScore.setText("Average score: " + Math.round(avg) + "%");
                    } else {
                        txtAverageScore.setText("Average score: N/A");
                    }
                })
                .addOnFailureListener(e -> {
                    txtAttemptedCount.setText("0 quizzes completed");
                    txtAverageScore.setText("Average score: N/A");
                });
    }

}
