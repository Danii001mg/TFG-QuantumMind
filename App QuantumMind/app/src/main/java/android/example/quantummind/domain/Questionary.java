package android.example.quantummind.domain;

import java.util.List;
public class Questionary {
    private String lessonId;
    private List<Question> questions;

    public Questionary() { }

    public Questionary(String lessonId, List<Question> questions) {
        this.lessonId = lessonId;
        this.questions = questions;
    }

    public String getLessonId() {
        return lessonId;
    }
    public List<Question> getQuestions() {
        return questions;
    }
}
