package com.example.swag_app;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.ViewHolder> {

    private List<StudentProgress> progressList;

    public ProgressAdapter(List<StudentProgress> progressList) {
        this.progressList = progressList;
    }

    @NonNull
    @Override
    public ProgressAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressAdapter.ViewHolder holder, int position) {
        StudentProgress progress = progressList.get(position);

        holder.emailText.setText("User: " + progress.email);
        holder.quizTitleText.setText("Quiz Title: " + progress.quizTitle);
        holder.scoreText.setText("Score: " + progress.score + "/" + progress.totalQuestions);
        holder.attemptedText.setText("Attempted: " + progress.attempted);
    }

    @Override
    public int getItemCount() {
        return progressList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView emailText, quizTitleText, scoreText, attemptedText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            emailText = itemView.findViewById(R.id.tvEmail);
            quizTitleText = itemView.findViewById(R.id.quizTitleText);
            scoreText = itemView.findViewById(R.id.tvScore);
            attemptedText = itemView.findViewById(R.id.tvAttempted);
        }
    }
}
