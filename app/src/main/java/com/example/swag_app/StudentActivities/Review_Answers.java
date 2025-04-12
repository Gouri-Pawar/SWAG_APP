package com.example.swag_app.StudentActivities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swag_app.BaseActivityStudent;
import com.example.swag_app.R;
import com.example.swag_app.adapter.ReviewAdapter;
import com.example.swag_app.model.ReviewModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Review_Answers extends BaseActivityStudent {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private String quizId;
    private ArrayList<String> userAnswers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_review_answers);
        setToolbarTitle("Review Answers");
        setupNavigationDrawer();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        // Get intent extras
        quizId = getIntent().getStringExtra("quizId");
        userAnswers = (ArrayList<String>) getIntent().getSerializableExtra("userAnswers");

        if (quizId != null && userAnswers != null) {
            fetchQuestions();
        } else {
            Toast.makeText(this, "Missing data to review answers.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchQuestions() {
        db.collection("quizzes")
                .document(quizId)
                .get()
                .addOnSuccessListener(doc -> {
                    List<Map<String, Object>> questions = (List<Map<String, Object>>) doc.get("questions");

                    if (questions != null && !questions.isEmpty()) {
                        List<ReviewModel> reviewList = new ArrayList<>();

                        int minSize = Math.min(questions.size(), userAnswers.size()); // Avoid out of bounds
                        for (int i = 0; i < minSize; i++) {
                            Map<String, Object> q = questions.get(i);

                            String questionText = (String) q.get("question");
                            List<String> options = (List<String>) q.get("options");
                            String correctAnswer = (String) q.get("correctAnswer");
                            String userAnswer = userAnswers.get(i);

                            reviewList.add(new ReviewModel(questionText, options, userAnswer, correctAnswer));
                        }

                        recyclerView.setAdapter(new ReviewAdapter(reviewList));
                    } else {
                        Toast.makeText(this, "No questions found for this quiz.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching questions: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
