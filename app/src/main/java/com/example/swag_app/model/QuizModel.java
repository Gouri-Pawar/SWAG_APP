package com.example.swag_app.model;

public class QuizModel {
        private String id;
        private String title;

        public QuizModel(String id, String title) {
            this.id = id;
            this.title = title;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }
}
