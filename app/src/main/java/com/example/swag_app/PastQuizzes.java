package com.example.swag_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class PastQuizzes extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseFirestore db;
    private ListView quizListView;
    private List<String> quizTitles = new ArrayList<>();
    private List<String> quizIds = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private FloatingActionButton addQuestionFab;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private TextView drawerUserName, drawerUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_quizzes);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Drawer + Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Drawer header
        View headerView = navigationView.getHeaderView(0);
        drawerUserName = headerView.findViewById(R.id.textViewStudentName);
        drawerUserEmail = headerView.findViewById(R.id.textViewStudentEmail);

        // Set user info
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String email = user.getEmail();

            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(querySnapshots -> {
                        if (!querySnapshots.isEmpty()) {
                            String name = email.split("@")[0];
                            drawerUserName.setText(name);
                            drawerUserEmail.setText(email);
                        }
                    })
                    .addOnFailureListener(e -> {
                        drawerUserName.setText("User");
                        drawerUserEmail.setText(user.getEmail());
                    });
        }

        quizListView = findViewById(R.id.pastQuizzesListView);
        addQuestionFab = findViewById(R.id.addQuestionFab);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quizTitles);
        quizListView.setAdapter(adapter);

        fetchQuizzes();

        quizListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(PastQuizzes.this, QuizQuestions.class);
            intent.putExtra("quizTitle", quizTitles.get(position));
            intent.putExtra("quizId", quizIds.get(position));
            startActivity(intent);
        });

        quizListView.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteConfirmation(quizIds.get(position), position);
            return true;
        });

        addQuestionFab.setOnClickListener(v -> {
            if (!quizIds.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PastQuizzes.this);
                builder.setTitle("Select a Quiz to Add Questions");

                String[] quizArray = quizTitles.toArray(new String[0]);
                builder.setItems(quizArray, (dialog, which) -> {
                    Intent intent = new Intent(PastQuizzes.this, AddQuestionsActivity.class);
                    intent.putExtra("quizId", quizIds.get(which));
                    intent.putExtra("quizTitle", quizTitles.get(which));
                    startActivity(intent);
                    ActivityAnimationUtil.animateForward(this);
                });

                builder.setNegativeButton("Cancel", null);
                builder.show();
            } else {
                Toast.makeText(PastQuizzes.this, "No quizzes available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchQuizzes() {
        db.collection("quizzes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(PastQuizzes.this, "Failed to load quizzes", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    quizIds.clear();
                    quizTitles.clear();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            quizIds.add(document.getId());
                            quizTitles.add(document.getString("title"));
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void showDeleteConfirmation(String quizId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Quiz")
                .setMessage("Are you sure you want to delete this quiz?")
                .setPositiveButton("Yes", (dialog, which) -> deleteQuiz(quizId, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteQuiz(String quizId, int position) {
        db.collection("quizzes").document(quizId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PastQuizzes.this, "Quiz deleted", Toast.LENGTH_SHORT).show();
                    quizTitles.remove(position);
                    quizIds.remove(position);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(PastQuizzes.this, "Failed to delete quiz", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
        } else if (id == R.id.nav_quizzes) {
            startActivity(new Intent(this, AvailableQuizzesActivity.class));
        } else if (id == R.id.nav_history) {
            startActivity(new Intent(this, PastQuizzes.class));
        } else if (id == R.id.nav_scores) {
            startActivity(new Intent(this, TrackProgressActivity.class));
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
