package com.example.swag_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class QuizQuestionAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String, Object>> questions;
    private LayoutInflater inflater;

    public QuizQuestionAdapter(Context context, List<Map<String, Object>> questions) {
        this.context = context;
        this.questions = questions;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return questions.size();
    }

    @Override
    public Object getItem(int position) {
        return questions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView questionText, optA, optB, optC, optD, correctAnswer;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_question, parent, false);
            h = new ViewHolder();
            h.questionText = convertView.findViewById(R.id.questionTextView);
            h.optA = convertView.findViewById(R.id.optionATextView);
            h.optB = convertView.findViewById(R.id.optionBTextView);
            h.optC = convertView.findViewById(R.id.optionCTextView);
            h.optD = convertView.findViewById(R.id.optionDTextView);
            h.correctAnswer = convertView.findViewById(R.id.correctAnswerTextView);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        Map<String, Object> q = questions.get(position);

        String questionText = "Q" + (position + 1) + ": " + (String) q.get("question");
        List<String> options = (List<String>) q.get("options");
        String correct = (String) q.get("correctAnswer");

        h.questionText.setText(questionText);
        if (options != null && options.size() == 4) {
            h.optA.setText("A: " + options.get(0));
            h.optB.setText("B: " + options.get(1));
            h.optC.setText("C: " + options.get(2));
            h.optD.setText("D: " + options.get(3));
        }
        h.correctAnswer.setText("Correct: " + correct);

        return convertView;
    }
}
