package android.example.quantummind.domain.entities;

import java.util.List;

public class Question {
    private String id;
    private String lessonId;
    private String questionText;
    private List<String> answerOptions;
    private int correctAnswerIndex;
    private String nextQuestionId;
    private String image;

    public Question() {
    }

    public Question(String id, String lessonId, String questionText, List<String> answerOptions, int correctAnswerIndex, String nextQuestionId, String image) {
        this.id = id;
        this.lessonId = lessonId;
        this.questionText = questionText;
        this.answerOptions = answerOptions;
        this.correctAnswerIndex = correctAnswerIndex;
        this.nextQuestionId = nextQuestionId;
        this.image = image;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getAnswerOptions() {
        return answerOptions;
    }

    public void setAnswerOptions(List<String> answerOptions) {
        this.answerOptions = answerOptions;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getNextQuestionId() { return nextQuestionId; }

    public void setNextQuestionId(String nextQuestionId) {
        this.nextQuestionId = nextQuestionId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
