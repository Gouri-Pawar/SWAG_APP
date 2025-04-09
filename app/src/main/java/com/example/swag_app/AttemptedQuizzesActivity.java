package com.example.swag_app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class AttemptedQuizzesActivity extends BaseActivityStudent {

    private RecyclerView recyclerView;
    private QuizAdapter adapter;
    private List<QuizModel> attemptedQuizzes = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_attempted_quizzes);

        recyclerView = findViewById(R.id.recyclerViewAttempted);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setToolbarTitle("Available Quizzes");
        setupNavigationDrawer();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new QuizAdapter(attemptedQuizzes, quiz -> {
            // TODO: handle click to show quiz result/progress
        });

        recyclerView.setAdapter(adapter);

        fetchAttemptedQuizzes();
    }

    private void fetchAttemptedQuizzes() {
        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("quizAttempts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    attemptedQuizzes.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        String userId = doc.getString("userId");
                        if (currentUserId.equals(userId)) {
                            String quizId = doc.getString("quizId");
                            String quizTitle = doc.getString("quizTitle");
                            if (quizId != null && quizTitle != null) {
                                attemptedQuizzes.add(new QuizModel(quizId, quizTitle));
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load attempted quizzes", Toast.LENGTH_SHORT).show();
                });
    }
}
