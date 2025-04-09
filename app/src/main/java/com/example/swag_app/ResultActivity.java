package com.example.swag_app;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends BaseActivityStudent {

    private TextView tvTotalQuestions, tvAttempted, tvCorrect, tvScorePercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_result);
        setToolbarTitle("Available Quizzes");
        setupNavigationDrawer();
        tvTotalQuestions = findViewById(R.id.tvTotalQuestions);
        tvAttempted = findViewById(R.id.tvAttempted);
        tvCorrect = findViewById(R.id.tvCorrect);
        tvScorePercent = findViewById(R.id.tvScorePercent);

        int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);
        int attempted = getIntent().getIntExtra("attempted", 0);
        int correct = getIntent().getIntExtra("score", 0);

        float percentage = (float) correct / totalQuestions * 100;

        tvTotalQuestions.setText("Total Questions: " + totalQuestions);
        tvAttempted.setText("Attempted: " + attempted);
        tvCorrect.setText("Correct: " + correct);
        tvScorePercent.setText("Score: " + Math.round(percentage) + "%");
    }
}
