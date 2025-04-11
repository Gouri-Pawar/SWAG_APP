package com.example.swag_app;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.bind(model, position, reviewList.size());
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {

        TextView questionText, questionNumber;
        LinearLayout optionsContainer;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.questionText);
            questionNumber = itemView.findViewById(R.id.questionNumber);
            optionsContainer = itemView.findViewById(R.id.optionsContainer);
        }

        public void bind(ReviewModel model, int position, int totalQuestions) {
            int current = position + 1;
            questionText.setText(current + ". " + model.getQuestion());
            questionNumber.setText("Question " + current + " of " + totalQuestions);

            questionText.setTypeface(Typeface.DEFAULT_BOLD);
            optionsContainer.removeAllViews();

            String userAnswer = model.getUserAnswer();
            String correctAnswer = model.getCorrectAnswer();
            List<String> options = model.getOptions();

            for (int i = 0; i < options.size(); i++) {
                String option = options.get(i);
                char label = (char) ('a' + i); // a, b, c...

                TextView optionView = new TextView(itemView.getContext());
                optionView.setText(label + ") " + option);
                optionView.setTextSize(16);
                optionView.setPadding(8, 8, 8, 8);

                if (option.equals(userAnswer) && option.equals(correctAnswer)) {
                    optionView.setTextColor(Color.parseColor("#2E7D32")); // Green
                    optionView.setTypeface(null, Typeface.BOLD_ITALIC);
                } else if (option.equals(userAnswer)) {
                    optionView.setTextColor(Color.RED);
                    optionView.setTypeface(null, Typeface.BOLD);
                } else if (option.equals(correctAnswer)) {
                    optionView.setTextColor(Color.parseColor("#2E7D32")); // Green
                    optionView.setTypeface(null, Typeface.BOLD_ITALIC);
                } else {
                    optionView.setTextColor(Color.BLACK);
                    optionView.setTypeface(null, Typeface.NORMAL);
                }

                optionsContainer.addView(optionView);
            }
        }
    }
}
