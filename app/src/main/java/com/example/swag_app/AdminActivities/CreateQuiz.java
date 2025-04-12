package com.example.swag_app.AdminActivities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import com.example.swag_app.BaseActivity;
import com.example.swag_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class CreateQuiz extends BaseActivity {

    private EditText quizTitle, questionInput, optionA, optionB, optionC, optionD;
    private Spinner correctAnswerSpinner;
    private Button addQuestionButton, submitQuizButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Map<String, Object>> questionList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(com.example.swag_app.R.layout.activity_create_quiz);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setToolbarTitle("Create Quiz");
        setupNavigationDrawer();
        // UI components
        quizTitle = findViewById(com.example.swag_app.R.id.quizTitle);
        questionInput = findViewById(com.example.swag_app.R.id.questionInput);
        optionA = findViewById(com.example.swag_app.R.id.optionA);
        optionB = findViewById(com.example.swag_app.R.id.optionB);
        optionC = findViewById(com.example.swag_app.R.id.optionC);
        optionD = findViewById(com.example.swag_app.R.id.optionD);
        correctAnswerSpinner = findViewById(com.example.swag_app.R.id.correctAnswerSpinner);
        addQuestionButton = findViewById(com.example.swag_app.R.id.addQuestionButton);
        submitQuizButton = findViewById(R.id.submitQuizButton);

        // Spinner options
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"A", "B", "C", "D"});
        correctAnswerSpinner.setAdapter(adapter);

        // Add Question
        addQuestionButton.setOnClickListener(v -> addQuestion());

        // Submit Quiz
        submitQuizButton.setOnClickListener(v -> submitQuiz());
    }

    private void addQuestion() {
        String question = questionInput.getText().toString().trim();
        String a = optionA.getText().toString().trim();
        String b = optionB.getText().toString().trim();
        String c = optionC.getText().toString().trim();
        String d = optionD.getText().toString().trim();
        String correctAnswer = correctAnswerSpinner.getSelectedItem().toString();

        if (question.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> questionData = new HashMap<>();
        questionData.put("question", question);
        questionData.put("options", Arrays.asList(a, b, c, d));
        questionData.put("correctAnswer", correctAnswer);

        questionList.add(questionData);
        clearFields();
        Toast.makeText(this, "Question added!", Toast.LENGTH_SHORT).show();
    }

    private void clearFields() {
        questionInput.setText("");
        optionA.setText("");
        optionB.setText("");
        optionC.setText("");
        optionD.setText("");
        correctAnswerSpinner.setSelection(0);
    }

    private void submitQuiz() {
        String title = quizTitle.getText().toString().trim();

        if (title.isEmpty() || questionList.isEmpty()) {
            Toast.makeText(this, "Please enter a quiz title and add at least one question!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> quizData = new HashMap<>();
        quizData.put("title", title);
        quizData.put("questions", questionList);
        quizData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("quizzes").add(quizData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CreateQuiz.this, "Quiz Submitted", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(CreateQuiz.this, AdminDashboardActivity.class);
                        intent.putExtra("quizTitle", title);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to submit quiz.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
