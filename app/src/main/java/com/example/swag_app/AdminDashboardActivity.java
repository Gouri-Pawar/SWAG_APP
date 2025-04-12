package com.example.swag_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminDashboardActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView welcomeText;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setToolbarTitle("Admin DashBoard");
        setupNavigationDrawer();
        welcomeText = findViewById(R.id.welcomeText);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Set up refresh listener
        swipeRefreshLayout.setOnRefreshListener(this::loadUserData);

        // Load user data when the activity starts
        loadUserData();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String email = user.getEmail();

            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(querySnapshots -> {
                        if (!querySnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshots) {
                                String name = email.split("@")[0];
                                String role = document.getString("role");

                                welcomeText.setText("Welcome " + name + " - " + role);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch user info", Toast.LENGTH_SHORT).show();
                        String name = email.split("@")[0];
                        welcomeText.setText("Welcome " + name);
                    });
        }
        // Dashboard cards
        MaterialCardView cardCreateQuiz = findViewById(R.id.cardCreateQuiz);
        MaterialCardView cardPastQuizzes = findViewById(R.id.cardPastQuizzes);
        MaterialCardView cardTrackProgress = findViewById(R.id.cardTrackProgress);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, CreateQuiz.class);
            startActivity(intent);
        });


        cardCreateQuiz.setOnClickListener(v -> startActivity(new Intent(this, CreateQuiz.class)));
        cardPastQuizzes.setOnClickListener(v -> startActivity(new Intent(this, PastQuizzes.class)));
        cardTrackProgress.setOnClickListener(v -> startActivity(new Intent(this, TrackProgressActivity.class)));
    }
    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String email = user.getEmail();

            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(querySnapshots -> {
                        if (!querySnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshots) {
                                String name = email.split("@")[0];
                                String role = document.getString("role");

                                welcomeText.setText("Welcome " + name + " - " + role);
                            }
                        } else {
                            welcomeText.setText("Welcome " + email.split("@")[0]);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch user info", Toast.LENGTH_SHORT).show();
                        String name = email.split("@")[0];
                        welcomeText.setText("Welcome " + name);
                    })
                    .addOnCompleteListener(task -> {
                        // Stop the refresh animation when the data is loaded
                        swipeRefreshLayout.setRefreshing(false);
                    });
        } else {
            swipeRefreshLayout.setRefreshing(false); // Stop refresh if no user is found
        }
    }
}
