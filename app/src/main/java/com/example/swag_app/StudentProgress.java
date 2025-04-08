package com.example.swag_app;
public class StudentProgress {
    public String email;
    public String quizId;
    public String quizTitle;
    public long score;
    public long totalQuestions;
    public long attempted;

    public StudentProgress() {
    }

    public StudentProgress(String email, String quizId, String quizTitle, Long score, Long totalQuestions, Long attempted) {
        this.email = email;
        this.quizId = quizId;
        this.quizTitle = quizTitle != null ? quizTitle : "Untitled";
        this.score = score != null ? score : 0;
        this.totalQuestions = totalQuestions != null ? totalQuestions : 0;
        this.attempted = attempted != null ? attempted : 0;
    }

    public String getEmail() {
        return email;
    }

    public String getQuizId() {
        return quizId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public long getScore() {
        return score;
    }

    public long getTotalQuestions() {
        return totalQuestions;
    }

    public long getAttempted() {
        return attempted;
    }
}
