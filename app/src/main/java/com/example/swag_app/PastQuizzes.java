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

public class PastQuizzes extends BaseActivity{

    private FirebaseFirestore db;
    private ListView quizListView;
    private List<String> quizTitles = new ArrayList<>();
    private List<String> quizIds = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private FloatingActionButton addQuestionFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_past_quizzes);
        setToolbarTitle("Previous Quizzes");
        setupNavigationDrawer();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

}
