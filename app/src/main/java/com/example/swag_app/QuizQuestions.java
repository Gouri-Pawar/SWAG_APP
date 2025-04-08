package com.example.swag_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.*;

import java.util.*;

public class QuizQuestions extends AppCompatActivity {

    private ListView questionsListView;
    private ArrayAdapter<String> questionsAdapter;
    private ArrayList<String> questionsDisplayList; // For showing Q & A
    private ArrayList<Map<String, Object>> questionsDataList; // Full question data
    private FirebaseFirestore db;
    private String quizTitle, quizId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_questions);

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        quizTitle = intent != null ? intent.getStringExtra("quizTitle") : null;
        quizId = intent != null ? intent.getStringExtra("quizId") : null;

        if (quizTitle == null || quizTitle.isEmpty() || quizId == null) {
            Toast.makeText(this, "Error loading quiz", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle(quizTitle);

        questionsListView = findViewById(R.id.questionsListView);
        questionsDisplayList = new ArrayList<>();
        questionsDataList = new ArrayList<>();
        questionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, questionsDisplayList);
        questionsListView.setAdapter(questionsAdapter);

        fetchQuestions(quizId);

        questionsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteConfirmation(position);
            return true;
        });
    }

    private void fetchQuestions(String quizId) {
        db.collection("quizzes").document(quizId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        List<Map<String, Object>> questionsArray = (List<Map<String, Object>>) document.get("questions");
                        if (questionsArray != null && !questionsArray.isEmpty()) {
                            questionsDisplayList.clear();
                            questionsDataList.clear();

                            for (Map<String, Object> questionObj : questionsArray) {
                                questionsDataList.add(questionObj); // Store full object

                                String questionText = (String) questionObj.get("question");
                                List<String> options = (List<String>) questionObj.get("options");
                                String correct = (String) questionObj.get("correctAnswer");

                                StringBuilder display = new StringBuilder();
                                display.append("Q: ").append(questionText).append("\n");

                                if (options != null && options.size() == 4) {
                                    display.append("A: ").append(options.get(0)).append("\n");
                                    display.append("B: ").append(options.get(1)).append("\n");
                                    display.append("C: ").append(options.get(2)).append("\n");
                                    display.append("D: ").append(options.get(3)).append("\n");
                                }

                                display.append("Correct: ").append(correct);
                                questionsDisplayList.add(display.toString());
                            }

                            questionsAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No questions found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Quiz does not exist", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("QuizQuestions", "Error loading quiz", e);
                    Toast.makeText(this, "Error loading questions", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteConfirmation(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Question")
                .setMessage("Are you sure you want to delete this question?")
                .setPositiveButton("Yes", (dialog, which) -> deleteQuestion(position))
                .setNegativeButton("No", null)
                .show();
    }


    private void deleteQuestion(int position) {

        questionsDataList.remove(position); // Remove from memory
        db.collection("quizzes").document(quizId)
                .update("questions", questionsDataList)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Question deleted", Toast.LENGTH_SHORT).show();
                    fetchQuestions(quizId); // Refresh UI
                })
                .addOnFailureListener(e -> {
                    Log.e("QuizQuestions", "Failed to delete question", e);
                    Toast.makeText(this, "Failed to delete question", Toast.LENGTH_SHORT).show();
                });
        questionsAdapter.notifyDataSetChanged();
    }
}
