package com.example.swag_app.AdminActivities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.example.swag_app.BaseActivity;
import com.example.swag_app.R;
import com.example.swag_app.adapter.QuizQuestionAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuizQuestions extends BaseActivity {

    private ListView questionsListView;
    private QuizQuestionAdapter customAdapter;
    private ArrayList<Map<String, Object>> questionsDataList;
    private FirebaseFirestore db;
    private String quizTitle, quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_quiz_questions);
        setToolbarTitle("Available Quizzes");
        setupNavigationDrawer();
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
        questionsDataList = new ArrayList<>();
        customAdapter = new QuizQuestionAdapter(this, questionsDataList);
        questionsListView.setAdapter(customAdapter);

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
                            questionsDataList.clear();
                            questionsDataList.addAll(questionsArray);
                            customAdapter.notifyDataSetChanged();
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
        questionsDataList.remove(position);
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
    }
}
