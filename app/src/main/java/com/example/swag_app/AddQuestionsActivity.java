package com.example.swag_app;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AddQuestionsActivity extends BaseActivity {

    private EditText questionEditText, optionAEditText, optionBEditText, optionCEditText, optionDEditText;
    private Spinner correctAnswerSpinner;
    private Button submitButton;

    private FirebaseFirestore db;
    private String quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_add_questions);
        // Firebase and Intent data
        db = FirebaseFirestore.getInstance();
        quizId = getIntent().getStringExtra("quizId");
        setToolbarTitle("Add Questions");
        setupNavigationDrawer();
        // Initialize views
        questionEditText = findViewById(R.id.questionInput);
        optionAEditText = findViewById(R.id.optionA);
        optionBEditText = findViewById(R.id.optionB);
        optionCEditText = findViewById(R.id.optionC);
        optionDEditText = findViewById(R.id.optionD);
        correctAnswerSpinner = findViewById(R.id.correctAnswerSpinner);
        submitButton = findViewById(R.id.addQuestionButton);

        // Set Spinner options
        String[] answerChoices = {"A", "B", "C", "D"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, answerChoices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        correctAnswerSpinner.setAdapter(adapter);

        // Button click listener
        submitButton.setOnClickListener(v -> {
            String question = questionEditText.getText().toString().trim();
            String optionA = optionAEditText.getText().toString().trim();
            String optionB = optionBEditText.getText().toString().trim();
            String optionC = optionCEditText.getText().toString().trim();
            String optionD = optionDEditText.getText().toString().trim();
            String correctAnswer = correctAnswerSpinner.getSelectedItem().toString().trim().toUpperCase();

            if (question.isEmpty() || optionA.isEmpty() || optionB.isEmpty()
                    || optionC.isEmpty() || optionD.isEmpty() || correctAnswer.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Prepare data to store
            Map<String, Object> questionData = new HashMap<>();
            questionData.put("question", question);
            questionData.put("options", Arrays.asList(optionA, optionB, optionC, optionD));
            questionData.put("correctAnswer", correctAnswer);

            // Upload to Firestore
            db.collection("quizzes").document(quizId)
                    .update("questions", FieldValue.arrayUnion(questionData))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Question added!", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to previous screen
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error adding question", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
