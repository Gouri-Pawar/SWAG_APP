package com.example.swag_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudentDashboard extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Button logoutButton;
    private Button logoutButtonDrawer;
    private TextView welcomeText;
    private LinearLayout cardAvailableQuizzes;
    private LinearLayout cardAttemptedQuizzes;
    private LinearLayout cardViewScores;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        mAuth = FirebaseAuth.getInstance();

        drawerLayout = findViewById(R.id.student_drawer_layout);
        logoutButton = findViewById(R.id.studentLogoutButton);
        logoutButtonDrawer = findViewById(R.id.studentLogoutDrawer);
        welcomeText = findViewById(R.id.studentWelcomeText);
        cardAvailableQuizzes = findViewById(R.id.cardAvailableQuizzes);
        cardAttemptedQuizzes = findViewById(R.id.cardAttemptedQuizzes);
        cardViewScores = findViewById(R.id.cardViewScores);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();

            if (email != null && !email.isEmpty()) {
                // Extract username before '@'
                String adminName = email.split("@")[0];
                welcomeText.setText("Welcome " + adminName + " - Student");
            } else {
                welcomeText.setText("Welcome Student");
            }
        } else {
            welcomeText.setText("Welcome Student");
        }

        logoutButton.setOnClickListener(v -> logout());
        logoutButtonDrawer.setOnClickListener(v -> logout());

        // Handle card clicks
        cardAvailableQuizzes.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboard.this, AvailableQuizzesActivity.class);
            startActivity(intent);
            ActivityAnimationUtil.animateForward(this);
        });

        cardAttemptedQuizzes.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboard.this, AttemptedQuizzesActivity.class);
            startActivity(intent);
            ActivityAnimationUtil.animateForward(this);
        });

        cardViewScores.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboard.this, ViewScoresActivity.class);
            startActivity(intent);
            ActivityAnimationUtil.animateForward(this);
        });
    }

    private void logout() {
        // Clear user session or Firebase sign out logic
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(StudentDashboard.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        ActivityAnimationUtil.animateBackward(this);
        finish();
    }
}
