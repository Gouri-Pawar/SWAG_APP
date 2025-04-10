package com.example.swag_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminDashboardActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView welcomeText, drawerUserName, drawerUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setToolbarTitle("Admin DashBoard");
        setupNavigationDrawer();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Header View in nav_header_student
        View headerView = navigationView.getHeaderView(0);
        drawerUserName = headerView.findViewById(R.id.textViewStudentName);
        drawerUserEmail = headerView.findViewById(R.id.textViewStudentEmail);

        welcomeText = findViewById(R.id.welcomeText);


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
                                drawerUserName.setText(name);
                                drawerUserEmail.setText(email);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch user info", Toast.LENGTH_SHORT).show();
                        String name = email.split("@")[0];
                        welcomeText.setText("Welcome " + name);
                        drawerUserName.setText(name);
                        drawerUserEmail.setText(email);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
        } else if (id==R.id.nav_quizzes) {
            startActivity(new Intent(this, AvailableQuizzesActivity.class));
        } else if (id==R.id.nav_history) {
            startActivity(new Intent(this, PastQuizzes.class));
        } else if (id==R.id.nav_scores) {
            startActivity(new Intent(this,TrackProgressActivity.class));
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }


        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
