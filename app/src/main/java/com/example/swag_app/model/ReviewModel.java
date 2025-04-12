package com.example.swag_app.model;

import java.io.Serializable;
import java.util.List;

public class ReviewModel implements Serializable {
    private String question;
    private List<String> options;
    private String userAnswer;
    private String correctAnswer;
    private int userAnswerIndex;
    private int correctAnswerIndex;

    public ReviewModel(String question, List<String> options, String userAnswer, String correctAnswer,
                       int userAnswerIndex, int correctAnswerIndex) {
        this.question = question;
        this.options = options;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.userAnswerIndex = userAnswerIndex;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public ReviewModel(String questionText, List<String> options, String userAnswer, String correctAnswer) {
        this.question = questionText;
        this.options = options;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.userAnswerIndex = options.indexOf(userAnswer);
        this.correctAnswerIndex = options.indexOf(correctAnswer);
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

    public int getUserAnswerIndex() {
        return userAnswerIndex;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public void setUserAnswerIndex(int userAnswerIndex) {
        this.userAnswerIndex = userAnswerIndex;
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }
}
