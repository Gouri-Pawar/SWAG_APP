package com.example.swag_app;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class QuizDetailsActivity extends AppCompatActivity {

    private TextView tvQuizTitle;
    private TextView tvAttemptedCount;
    private TextView tvCorrectCount;
    private TextView tvIncorrectCount;
    private TextView tvCorrectPercentage;
    private TextView tvIncorrectPercentage;
    private ProgressBar progressCorrect;
    private ProgressBar progressIncorrect;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_details);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        tvQuizTitle = findViewById(R.id.tvQuizTitle);
        tvAttemptedCount = findViewById(R.id.tvAttemptedCount);
        tvCorrectCount = findViewById(R.id.tvCorrectCount);
        tvIncorrectCount = findViewById(R.id.tvIncorrectCount);
        tvCorrectPercentage = findViewById(R.id.tvCorrectPercentage);
        tvIncorrectPercentage = findViewById(R.id.tvIncorrectPercentage);
        progressCorrect = findViewById(R.id.progressCorrect);
        progressIncorrect = findViewById(R.id.progressIncorrect);

        // Get quiz name from intent
        String quizName = getIntent().getStringExtra("QUIZ_NAME");
        tvQuizTitle.setText(quizName);

        fetchQuizDetailsFromFirebase(quizName);
    }

    private void fetchQuizDetailsFromFirebase(String quizTitle) {
        String userId = auth.getCurrentUser().getUid();

        db.collection("quizAttempts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("quizTitle", quizTitle)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);

                        Long attemptedLong = doc.getLong("attempted");
                        Long scoreLong = doc.getLong("score");
                        Long totalQuestionsLong = doc.getLong("totalQuestions");

                        if (attemptedLong == null || scoreLong == null || totalQuestionsLong == null) {
                            Toast.makeText(this, "Some data is missing in Firestore", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int attempted = attemptedLong.intValue();
                        int correct = scoreLong.intValue(); // Assuming "score" is number of correct answers
                        int totalQuestions = totalQuestionsLong.intValue();
                        int incorrect = totalQuestions - correct;

                        float correctPercentage = attempted > 0 ? (float) correct / totalQuestions * 100 : 0;
                        float incorrectPercentage = attempted > 0 ? (float) incorrect / totalQuestions * 100 : 0;

                        tvAttemptedCount.setText(String.valueOf(attempted));
                        tvCorrectCount.setText(String.valueOf(correct));
                        tvIncorrectCount.setText(String.valueOf(incorrect));
                        tvCorrectPercentage.setText(String.format("%.1f%%", correctPercentage));
                        tvIncorrectPercentage.setText(String.format("%.1f%%", incorrectPercentage));

                        progressCorrect.setProgress(Math.round(correctPercentage));
                        progressIncorrect.setProgress(Math.round(incorrectPercentage));
                    } else {
                        Toast.makeText(this, "No data found for this quiz", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load quiz data", Toast.LENGTH_SHORT).show();
                });
    }
}
