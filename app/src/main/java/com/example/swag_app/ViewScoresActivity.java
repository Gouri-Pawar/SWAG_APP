package com.example.swag_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewScoresActivity extends BaseActivityStudent {

    private TextView tvAttemptedValue, tvCorrectValue, tvIncorrectValue;
    private ProgressBar progressAttempted, progressCorrect, progressIncorrect;
    private RecyclerView rvQuizList;

    private QuizResultAdapter adapter;
    private List<QuizResultModel> quizResults = new ArrayList<>();

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_view_scores);
        setToolbarTitle("Your Score");
        setupNavigationDrawer();
        tvAttemptedValue = findViewById(R.id.tvAttemptedValue);
        tvCorrectValue = findViewById(R.id.tvCorrectValue);
        tvIncorrectValue = findViewById(R.id.tvIncorrectValue);
        progressAttempted = findViewById(R.id.progressAttempted);
        progressCorrect = findViewById(R.id.progressCorrect);
        progressIncorrect = findViewById(R.id.progressIncorrect);
        rvQuizList = findViewById(R.id.rvQuizList);

        adapter = new QuizResultAdapter(quizResults);
        rvQuizList.setLayoutManager(new LinearLayoutManager(this));
        rvQuizList.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fetchQuizResultsFromFirebase();
    }

    private void fetchQuizResultsFromFirebase() {
        db.collection("quizAttempts")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalAttempted = 0, totalCorrect = 0, totalQuestions = 0;

                    quizResults.clear();

                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        Map<String, Object> data = doc.getData();

                        String quizTitle = (String) data.get("quizTitle");
                        Long scoreLong = (Long) data.get("score");
                        Long totalQuestionsLong = (Long) data.get("totalQuestions");
                        Long attemptedLong = (Long) data.get("attempted");

                        if (quizTitle == null || scoreLong == null || totalQuestionsLong == null || attemptedLong == null) {
                            continue;
                        }

                        int score = scoreLong.intValue();
                        int totalQ = totalQuestionsLong.intValue();
                        int attempted = attemptedLong.intValue();

                        totalCorrect += score;
                        totalAttempted += attempted;
                        totalQuestions += totalQ;

                        quizResults.add(new QuizResultModel(quizTitle, score, totalQ, attempted));
                    }

                    updateOverallPerformance(totalAttempted, totalCorrect, totalQuestions);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("ViewScoresActivity", "Error fetching quiz attempts", e));
    }

    private void updateOverallPerformance(int attempted, int correct, int totalQuestions) {
        int incorrect = totalQuestions - correct;

        tvAttemptedValue.setText(String.valueOf(attempted));
        tvCorrectValue.setText(String.valueOf(correct));
        tvIncorrectValue.setText(String.valueOf(incorrect));

        if (totalQuestions == 0) totalQuestions = 1; // avoid division by zero

        progressAttempted.setProgress((attempted * 100) / totalQuestions);
        progressCorrect.setProgress((correct * 100) / totalQuestions);
        progressIncorrect.setProgress((incorrect * 100) / totalQuestions);
    }
}
