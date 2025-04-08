package com.example.swag_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AddQuestionsActivity extends AppCompatActivity {

    private EditText questionEditText, optionAEditText, optionBEditText, optionCEditText, optionDEditText, correctAnswerEditText;
    private Button submitButton;

    private FirebaseFirestore db;
    private String quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_questions);

        db = FirebaseFirestore.getInstance();
        quizId = getIntent().getStringExtra("quizId");

        questionEditText = findViewById(R.id.questionInput);
        optionAEditText = findViewById(R.id.optionA);
        optionBEditText = findViewById(R.id.optionB);
        optionCEditText = findViewById(R.id.optionC);
        optionDEditText = findViewById(R.id.optionD);
        correctAnswerEditText = findViewById(R.id.correctAnswer);
        submitButton = findViewById(R.id.addQuestionButton);

        submitButton.setOnClickListener(v -> {
            String question = questionEditText.getText().toString();
            String optionA = optionAEditText.getText().toString();
            String optionB = optionBEditText.getText().toString();
            String optionC = optionCEditText.getText().toString();
            String optionD = optionDEditText.getText().toString();
            String correctAnswer = correctAnswerEditText.getText().toString();

            if (question.isEmpty() || optionA.isEmpty() || optionB.isEmpty()
                    || optionC.isEmpty() || optionD.isEmpty() || correctAnswer.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Format the question as required
            Map<String, Object> questionData = new HashMap<>();
            questionData.put("question", question);
            questionData.put("options", Arrays.asList(optionA, optionB, optionC, optionD));
            questionData.put("correctAnswer", correctAnswer);

            db.collection("quizzes").document(quizId)
                    .update("questions", FieldValue.arrayUnion(questionData))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Question added!", Toast.LENGTH_SHORT).show();
                        finish(); // go back to quiz screen
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error adding question", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
