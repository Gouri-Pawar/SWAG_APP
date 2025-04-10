package com.example.swag_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AvailableQuizzesActivity extends BaseActivityStudent {

    private RecyclerView recyclerView;
    private QuizAdapter quizAdapter;
    private final List<QuizModel> quizList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_available_quizzes);

        setToolbarTitle("Available Quizzes");
        setupNavigationDrawer();

        recyclerView = findViewById(R.id.quizzesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        quizAdapter = new QuizAdapter(quizList, this::onQuizClick);
        recyclerView.setAdapter(quizAdapter);

        db = FirebaseFirestore.getInstance();
        fetchAvailableQuizzes();
    }

    private void fetchAvailableQuizzes() {
        db.collection("quizzes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    quizList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("title");
                        String quizId = doc.getId();
                        quizList.add(new QuizModel(quizId, title));
                    }
                    quizAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load quizzes", Toast.LENGTH_SHORT).show());
    }

    private void onQuizClick(QuizModel quiz) {
        Intent intent = new Intent(this, AttemptQuiz.class);
        intent.putExtra("quizId", quiz.getId());
        intent.putExtra("quizTitle", quiz.getTitle());
        startActivity(intent);
    }
}
