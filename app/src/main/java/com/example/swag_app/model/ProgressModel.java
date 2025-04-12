package com.example.swag_app.model;

public class ProgressModel {
    public String email, quizId;
    public int score, totalQuestions, attempted;
    public long timestamp;

    public ProgressModel(String email, String quizId, int score, int totalQuestions, int attempted, long timestamp) {
        this.email = email;
        this.quizId = quizId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.attempted = attempted;
        this.timestamp = timestamp;
    }
}
