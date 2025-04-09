package com.example.swag_app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuizResultAdapter extends RecyclerView.Adapter<QuizResultAdapter.QuizViewHolder> {
    private List<QuizResultModel> quizResults;
    private Context context;

    public QuizResultAdapter(List<QuizResultModel> quizResults) {
        this.quizResults = quizResults;
    }

    public void setData(List<QuizResultModel> newResults) {
        this.quizResults.clear();
        this.quizResults.addAll(newResults);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_quiz, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        QuizResultModel result = quizResults.get(position);
        holder.tvQuizName.setText(result.getQuizTitle());
        holder.tvQuizStats.setText(String.format("Correct: %.1f%% | Incorrect: %.1f%%",
                result.getCorrectRate(),
                result.getIncorrectRate()));

        holder.progressCorrect.setProgress(Math.round(result.getCorrectRate()));
        holder.progressIncorrect.setProgress(Math.round(result.getIncorrectRate()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, QuizDetailsActivity.class);
            intent.putExtra("QUIZ_NAME", result.getQuizTitle());
            intent.putExtra("ATTEMPTED", result.getAttempted());
            intent.putExtra("CORRECT", result.getScore());
            intent.putExtra("INCORRECT", result.getIncorrect());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return quizResults.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuizName, tvQuizStats;
        ProgressBar progressCorrect, progressIncorrect;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuizName = itemView.findViewById(R.id.tvQuizName);
            tvQuizStats = itemView.findViewById(R.id.tvQuizStats);
            progressCorrect = itemView.findViewById(R.id.progressCorrect);
            progressIncorrect = itemView.findViewById(R.id.progressIncorrect);
        }
    }
}
