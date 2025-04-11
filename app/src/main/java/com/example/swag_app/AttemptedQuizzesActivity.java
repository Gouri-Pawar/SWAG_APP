package com.example.swag_app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttemptedQuizzesActivity extends BaseActivityStudent {

    private RecyclerView recyclerView;
    private QuizAdapter adapter;
    private List<QuizModel> attemptedQuizzes = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<DocumentSnapshot> quizDocuments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_attempted_quizzes);

        recyclerView = findViewById(R.id.recyclerViewAttempted);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setToolbarTitle("Attempted Quizzes");
        setupNavigationDrawer();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new QuizAdapter(attemptedQuizzes, quiz -> {
            // Handle quiz tap: get quizAttempt document and launch ReviewAnswersActivity
            for (DocumentSnapshot doc : quizDocuments) {
                if (quiz.getId().equals(doc.getString("quizId"))) {
                    ArrayList<String> userAnswers = (ArrayList<String>) doc.get("userAnswers");
                    String quizId = doc.getString("quizId");
                    String quizTitle = doc.getString("quizTitle");

                    Intent intent = new Intent(AttemptedQuizzesActivity.this, Review_Answers.class);
                    intent.putExtra("quizId", quizId);
                    intent.putExtra("quizTitle", quizTitle);
                    intent.putExtra("userAnswers", userAnswers);  // must implement Serializable or use Parcelable
                    startActivity(intent);
                    break;
                }
            }
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
                    quizDocuments.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        String userId = doc.getString("userId");
                        if (currentUserId.equals(userId)) {
                            String quizId = doc.getString("quizId");
                            String quizTitle = doc.getString("quizTitle");
                            if (quizId != null && quizTitle != null) {
                                attemptedQuizzes.add(new QuizModel(quizId, quizTitle));
                                quizDocuments.add(doc);  // Store the whole document for later reference
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
