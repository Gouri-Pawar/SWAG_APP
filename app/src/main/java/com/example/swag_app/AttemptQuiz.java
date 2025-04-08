package com.example.swag_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttemptQuiz extends AppCompatActivity {

    private TextView questionNumberText, questionText;
    private RadioGroup optionsGroup;
    private RadioButton option1, option2, option3, option4;
    private Button prevButton, nextButton, submitButton;

    private List<Map<String, Object>> questionsList = new ArrayList<>();
    private List<String> userAnswers = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private String quizId;
    private String quizTitle = ""; // 🔹 Store the quiz title

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attempt_quiz);

        // View Initialization
        questionNumberText = findViewById(R.id.questionNumber);
        questionText = findViewById(R.id.questionText);
        optionsGroup = findViewById(R.id.optionsGroup);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        submitButton = findViewById(R.id.submitButton);

        quizId = getIntent().getStringExtra("quizId");

        if (quizId != null) {
            loadQuestionsFromFirestore(quizId);
        } else {
            Toast.makeText(this, "Quiz ID is missing", Toast.LENGTH_SHORT).show();
        }

        prevButton.setOnClickListener(v -> {
            saveAnswer();
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                displayQuestion();
            }
        });

        nextButton.setOnClickListener(v -> {
            saveAnswer();
            if (currentQuestionIndex < questionsList.size() - 1) {
                currentQuestionIndex++;
                displayQuestion();
            } else {
                Toast.makeText(this, "This was the last question!", Toast.LENGTH_SHORT).show();
            }
        });

        submitButton.setOnClickListener(v -> {
            saveAnswer();

            int totalQuestions = questionsList.size();
            int correctAnswers = calculateScore();
            int attemptedQuestions = calculateAttempted();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();

            // 🔽 Result data
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("userId", uid);
            resultData.put("email", user.getEmail());
            resultData.put("quizId", quizId);
            resultData.put("quizTitle", quizTitle); // 🔹 Add quiz title
            resultData.put("score", correctAnswers);
            resultData.put("totalQuestions", totalQuestions);
            resultData.put("attempted", attemptedQuestions);
            resultData.put("timestamp", System.currentTimeMillis());

            db.collection("quizAttempts")
                    .add(resultData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Result stored", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AttemptQuiz.this, ResultActivity.class);
                        intent.putExtra("score", correctAnswers);
                        intent.putExtra("totalQuestions", totalQuestions);
                        intent.putExtra("attempted", attemptedQuestions);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error storing result: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

    }

    private void loadQuestionsFromFirestore(String quizId) {
        FirebaseFirestore.getInstance().collection("quizzes")
                .document(quizId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        questionsList = (List<Map<String, Object>>) documentSnapshot.get("questions");
                        quizTitle = documentSnapshot.getString("title"); // 🔹 Fetch quiz title

                        if (questionsList != null && !questionsList.isEmpty()) {
                            for (int i = 0; i < questionsList.size(); i++) {
                                userAnswers.add(null);
                            }
                            displayQuestion();
                        } else {
                            Toast.makeText(this, "No questions found in this quiz.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading quiz: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void displayQuestion() {
        Map<String, Object> currentQuestion = questionsList.get(currentQuestionIndex);
        String question = (String) currentQuestion.get("question");
        List<String> options = (List<String>) currentQuestion.get("options");

        questionNumberText.setText("Question " + (currentQuestionIndex + 1));
        questionText.setText(question);

        if (options != null && options.size() == 4) {
            option1.setText(options.get(0));
            option2.setText(options.get(1));
            option3.setText(options.get(2));
            option4.setText(options.get(3));
        }

        optionsGroup.clearCheck();
        String previousAnswer = userAnswers.get(currentQuestionIndex);
        if (previousAnswer != null) {
            if (option1.getText().toString().equals(previousAnswer)) option1.setChecked(true);
            else if (option2.getText().toString().equals(previousAnswer)) option2.setChecked(true);
            else if (option3.getText().toString().equals(previousAnswer)) option3.setChecked(true);
            else if (option4.getText().toString().equals(previousAnswer)) option4.setChecked(true);
        }

        prevButton.setEnabled(currentQuestionIndex > 0);
        submitButton.setVisibility(currentQuestionIndex == questionsList.size() - 1 ? View.VISIBLE : View.GONE);
    }

    private void saveAnswer() {
        int selectedId = optionsGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedRadio = findViewById(selectedId);
            userAnswers.set(currentQuestionIndex, selectedRadio.getText().toString());
        }
    }

    private int calculateScore() {
        int score = 0;
        for (int i = 0; i < questionsList.size(); i++) {
            Map<String, Object> question = questionsList.get(i);
            String correctOptionLetter = (String) question.get("correctAnswer");
            List<String> options = (List<String>) question.get("options");
            String selectedAnswer = userAnswers.get(i);

            if (correctOptionLetter != null && selectedAnswer != null && options != null) {
                int correctIndex = letterToIndex(correctOptionLetter);
                if (correctIndex >= 0 && correctIndex < options.size()) {
                    if (options.get(correctIndex).trim().equalsIgnoreCase(selectedAnswer.trim())) {
                        score++;
                    }
                }
            }
        }
        return score;
    }

    private int calculateAttempted() {
        int count = 0;
        for (String answer : userAnswers) {
            if (answer != null) count++;
        }
        return count;
    }

    private int letterToIndex(String letter) {
        switch (letter.toUpperCase()) {
            case "A": return 0;
            case "B": return 1;
            case "C": return 2;
            case "D": return 3;
            default: return -1;
        }
    }
}
