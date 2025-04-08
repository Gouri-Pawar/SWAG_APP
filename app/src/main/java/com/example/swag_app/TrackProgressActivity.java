package com.example.swag_app;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackProgressActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProgressAdapter adapter;
    List<StudentProgress> progressList = new ArrayList<>();
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_progress);

        recyclerView = findViewById(R.id.recyclerViewProgress);
        adapter = new ProgressAdapter(progressList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadProgressData();
    }

    private void loadProgressData() {
        db.collection("quizAttempts").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressList.clear();

                    List<StudentProgress> tempList = new ArrayList<>();
                    List<String> quizIdList = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String email = doc.getString("email");
                        String quizId = doc.getString("quizId");
                        Long score = doc.getLong("score");
                        Long totalQuestions = doc.getLong("totalQuestions");
                        Long attempted = doc.getLong("attempted");

                        if (email != null && quizId != null) {
                            quizIdList.add(quizId);
                            tempList.add(new StudentProgress(email, quizId, null, score, totalQuestions, attempted));
                        }
                    }

                    if (quizIdList.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    // Load all quiz titles at once
                    db.collection("quizzes").get()
                            .addOnSuccessListener(quizzesSnapshot -> {
                                Map<String, String> quizIdToTitle = new HashMap<>();
                                for (DocumentSnapshot doc : quizzesSnapshot) {
                                    quizIdToTitle.put(doc.getId(), doc.getString("title"));
                                }

                                for (StudentProgress progress : tempList) {
                                    String title = quizIdToTitle.getOrDefault(progress.quizId, "Untitled");
                                    progress.quizTitle = title;
                                    progressList.add(progress);
                                }

                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> Log.e("TrackProgress", "Error loading quizzes", e));

                })
                .addOnFailureListener(e -> Log.e("TrackProgress", "Error loading quiz attempts", e));
    }
}
