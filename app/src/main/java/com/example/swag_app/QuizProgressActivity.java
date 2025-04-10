package com.example.swag_app;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class QuizProgressActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ProgressAdapter adapter;
    private List<StudentProgress> progressList = new ArrayList<>();
    private FirebaseFirestore db;
    private String quizId, quizTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_quiz_progress);

        recyclerView = findViewById(R.id.recyclerViewQuizProgress);
        adapter = new ProgressAdapter(progressList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        setToolbarTitle("Track Progress");
        setupNavigationDrawer();
        Button exportBtn = findViewById(R.id.btnExportPdf);
        exportBtn.setOnClickListener(v -> generatePdf(progressList));

        db = FirebaseFirestore.getInstance();
        quizId = getIntent().getStringExtra("quizId");
        quizTitle = getIntent().getStringExtra("quizTitle");

        loadProgressForQuiz();
    }

    private void loadProgressForQuiz() {
        db.collection("quizAttempts")
                .whereEqualTo("quizId", quizId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    progressList.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        String email = doc.getString("email");
                        Long score = doc.getLong("score");
                        Long attempted = doc.getLong("attempted");
                        Long totalQuestions = doc.getLong("totalQuestions");

                        progressList.add(new StudentProgress(email, quizId, quizTitle, score, totalQuestions, attempted));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load attempts", Toast.LENGTH_SHORT).show());
    }

    private void generatePdf(List<StudentProgress> progressList) {
        PdfDocument document = new PdfDocument();
        Paint paint = new Paint();
        int pageWidth = 595, pageHeight = 842; // A4 size
        int y = 50;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(16f);
        paint.setFakeBoldText(true);
        canvas.drawText("Quiz: " + quizTitle, 40, y, paint);
        y += 30;

        paint.setFakeBoldText(false);
        paint.setTextSize(14f);

        for (StudentProgress sp : progressList) {
            if (y > pageHeight - 100) {
                document.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }

            canvas.drawText("User: " + sp.email, 40, y, paint); y += 20;
            canvas.drawText("Score: " + sp.score + "/" + sp.totalQuestions, 40, y, paint); y += 20;
            canvas.drawText("Attempted: " + sp.attempted, 40, y, paint); y += 30;
        }

        document.finishPage(page);

        try {
            File pdfDir = new File(getExternalFilesDir(null), "exports");
            if (!pdfDir.exists()) pdfDir.mkdirs();

            String fileName = "Progress_" + quizTitle.replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + ".pdf";
            File pdfFile = new File(pdfDir, fileName);

            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                document.writeTo(fos);
            }

            document.close();

            Toast.makeText(this, "PDF saved to: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);

            // Open Share Intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share PDF using"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show();
        }
    }

}
