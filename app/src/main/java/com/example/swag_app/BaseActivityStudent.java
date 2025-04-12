package com.example.swag_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class BaseActivityStudent extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected FirebaseAuth mAuth;
    protected FirebaseFirestore db;
    protected Toolbar toolbar;
    protected TextView drawerUserName, drawerUserEmail;
    protected SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
        );

        swipeRefreshLayout.setOnRefreshListener(() -> {
            onSwipeToRefresh(); // Call method to handle refresh
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupNavigationDrawer();
    }

    protected void setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        drawerUserName = headerView.findViewById(R.id.textViewStudentName);
        drawerUserEmail = headerView.findViewById(R.id.textViewStudentEmail);

        fetchUserInfo(); // 🔁 same logic as AdminDashboardActivity
    }

    protected void setContentLayout(int layoutResID) {
        FrameLayout frameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(layoutResID, frameLayout, true);
    }

    protected void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void fetchUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String email = user.getEmail();

            drawerUserEmail.setText(email); // Always show email

            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(querySnapshots -> {
                        if (!querySnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshots) {
                                String name = email.split("@")[0];
                                drawerUserName.setText(name);
                            }
                        } else {
                            // fallback if no document found
                            drawerUserName.setText(email.split("@")[0]);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch user info", Toast.LENGTH_SHORT).show();
                        drawerUserName.setText(email.split("@")[0]);
                    });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            startActivity(new Intent(this, StudentDashboard.class));
        }else if (id == R.id.nav_history) {
            startActivity(new Intent(this, AvailableQuizzesActivity.class));
        } else if (id == R.id.nav_scores) {
            startActivity(new Intent(this, ViewScoresActivity.class));
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    protected void onSwipeToRefresh() {
        swipeRefreshLayout.setRefreshing(false); // default behavior
    }

    // Call this when refresh is complete from child
    protected void stopRefreshing() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

}
