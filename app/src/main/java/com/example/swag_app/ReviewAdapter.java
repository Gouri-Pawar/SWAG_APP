package com.example.swag_app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swag_app.ReviewModel;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<ReviewModel> reviewList;

    public ReviewAdapter(List<ReviewModel> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewModel model = reviewList.get(position);
        holder.bind(model, position);
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {

        TextView questionText;
        LinearLayout optionsContainer;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.questionText);
            optionsContainer = itemView.findViewById(R.id.optionsContainer);
        }

        public void bind(ReviewModel model, int position) {
            questionText.setText((position + 1) + ". " + model.getQuestion());
            questionText.setTypeface(Typeface.DEFAULT_BOLD);

            optionsContainer.removeAllViews();

            String userAnswer = model.getUserAnswer();
            String correctAnswer = model.getCorrectAnswer();

            for (String option : model.getOptions()) {
                TextView optionView = new TextView(itemView.getContext());
                optionView.setText("- " + option);
                optionView.setTextSize(16);
                optionView.setPadding(8, 8, 8, 8);

                if (option.equals(userAnswer) && option.equals(correctAnswer)) {
                    // User selected correct answer
                    optionView.setTextColor(Color.parseColor("#2E7D32")); // Green
                    optionView.setTypeface(null, Typeface.BOLD_ITALIC);
                } else if (option.equals(userAnswer) && !option.equals(correctAnswer)) {
                    // User selected wrong answer
                    optionView.setTextColor(Color.RED);
                    optionView.setTypeface(null, Typeface.BOLD);
                } else if (option.equals(correctAnswer)) {
                    // Correct answer (not selected by user)
                    optionView.setTextColor(Color.parseColor("#2E7D32")); // Green
                    optionView.setTypeface(null, Typeface.BOLD_ITALIC);
                } else {
                    // Other options
                    optionView.setTextColor(Color.BLACK);
                }

                optionsContainer.addView(optionView);
            }
        }


    }
}
