package com.example.swag_app;

import java.io.Serializable;
import java.util.List;

public class ReviewModel implements Serializable {
    private String question;
    private List<String> options;
    private String userAnswer;
    private String correctAnswer;

    public ReviewModel(String question, List<String> options, String userAnswer, String correctAnswer) {
        this.question = question;
        this.options = options;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
