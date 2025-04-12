package com.example.swag_app.model;

public class QuizResultModel {
    private String quizTitle;
    private int score;
    private int totalQuestions;
    private int attempted;

    public QuizResultModel(String quizTitle, int score, int totalQuestions, int attempted) {
        this.quizTitle = quizTitle;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.attempted = attempted;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public int getAttempted() {
        return attempted;
    }

    public int getIncorrect() {
        return totalQuestions - score;
    }

    public float getCorrectRate() {
        return totalQuestions == 0 ? 0 : (score * 100f / totalQuestions);
    }

    public float getIncorrectRate() {
        return totalQuestions == 0 ? 0 : (getIncorrect() * 100f / totalQuestions);
    }

}
