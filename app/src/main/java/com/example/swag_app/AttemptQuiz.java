package com.example.swag_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttemptQuiz extends BaseActivityStudent {

    private TextView questionNumberText, questionText;
    private RadioGroup optionsGroup;
    private RadioButton option1, option2, option3, option4;
    private Button prevButton, nextButton, submitButton;

    private List<Map<String, Object>> questionsList = new ArrayList<>();
    private List<String> userAnswers = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private String quizId;
    private String quizTitle = "";
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 1 * 60 * 1000; // 1 minute for testing
    private TextView timerTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_attempt_quiz);
        setToolbarTitle("Solve Quizzes");
        setupNavigationDrawer();

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
        timerTextView = findViewById(R.id.timerTextView);
        startTimer();

        quizId = getIntent().getStringExtra("quizId");


        if (quizId != null) {
            checkIfAlreadyAttempted();
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

            Map<String, Object> resultData = new HashMap<>();
            resultData.put("userId", uid);
            resultData.put("email", user.getEmail());
            resultData.put("quizId", quizId);
            resultData.put("quizTitle", quizTitle);
            resultData.put("score", correctAnswers);
            resultData.put("totalQuestions", totalQuestions);
            resultData.put("attempted", attemptedQuestions);
            resultData.put("timestamp", System.currentTimeMillis());
            resultData.put("userAnswers", new ArrayList<>(userAnswers)); // Added line


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

    private void checkIfAlreadyAttempted() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = user.getUid();

        db.collection("quizAttempts")
                .whereEqualTo("userId", uid)
                .whereEqualTo("quizId", quizId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        new AlertDialog.Builder(this)
                                .setTitle("Quiz Already Attempted")
                                .setMessage("You have already submitted this quiz. You cannot attempt it again.")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialog, which) -> {
                                    dialog.dismiss();
                                    finish();
                                })
                                .show();
                    } else {
                        loadQuestionsFromFirestore(quizId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to check attempts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadQuestionsFromFirestore(String quizId) {
        FirebaseFirestore.getInstance().collection("quizzes")
                .document(quizId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        questionsList = (List<Map<String, Object>>) documentSnapshot.get("questions");
                        quizTitle = documentSnapshot.getString("title");

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
    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                Toast.makeText(AttemptQuiz.this, "Time's up! Submitting quiz...", Toast.LENGTH_LONG).show();
                submitQuizAutomatically();
            }
        }.start();
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        timerTextView.setText(timeFormatted);
    }

    private void submitQuizAutomatically() {
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

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("userId", uid);
        resultData.put("email", user.getEmail());
        resultData.put("quizId", quizId);
        resultData.put("quizTitle", quizTitle);
        resultData.put("score", correctAnswers);
        resultData.put("totalQuestions", totalQuestions);
        resultData.put("attempted", attemptedQuestions);
        resultData.put("timestamp", System.currentTimeMillis());
        resultData.put("userAnswers", new ArrayList<>(userAnswers)); // Added line

        db.collection("quizAttempts")
                .add(resultData)
                .addOnSuccessListener(documentReference -> {
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
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

}
