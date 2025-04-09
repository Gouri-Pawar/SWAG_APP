package com.example.swag_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class TrackProgressActivity extends BaseActivity {

    private RecyclerView quizListView;
    private FirebaseFirestore db;
    private List<QuizModel> quizList = new ArrayList<>();
    private QuizAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_track_progress);

        quizListView = findViewById(R.id.quizzesRecyclerView);
        quizListView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new QuizAdapter(quizList, quiz -> {
            Intent intent = new Intent(TrackProgressActivity.this, QuizProgressActivity.class);
            intent.putExtra("quizId", quiz.getId());
            intent.putExtra("quizTitle", quiz.getTitle());
            startActivity(intent);
        });

        quizListView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchQuizzes();
        setToolbarTitle("Available Quizzes");
        setupNavigationDrawer();
    }

    private void fetchQuizzes() {
        db.collection("quizzes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load quizzes", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    quizList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String title = doc.getString("title");
                        if (title != null) {
                            quizList.add(new QuizModel(id, title));
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

}
